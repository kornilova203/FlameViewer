package com.github.kornilova_l.flamegraph.plugin.server.converters.cflamegraph.jfr;

import com.github.kornilova_l.flamegraph.plugin.server.converters.cflamegraph.CFlamegraph;
import com.github.kornilova_l.flamegraph.plugin.server.converters.cflamegraph.CFlamegraphLine;
import com.github.kornilova_l.flamegraph.plugin.server.converters.cflamegraph.ProfilerToCFlamegraphConverter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static com.github.kornilova_l.flamegraph.plugin.server.converters.cflamegraph.UtilKt.getId;
import static com.github.kornilova_l.flamegraph.plugin.server.converters.cflamegraph.UtilKt.toArray;

class JfrToCFlamegraphConverter extends JfrToStacksConverter implements ProfilerToCFlamegraphConverter {
    private static final String ERROR_FRAME = "UNKNOWN";

    JfrToCFlamegraphConverter(File file) {
        super(file);
    }

    @NotNull
    @Override
    public CFlamegraph convert() {
        ArrayList<CFlamegraphLine> cFlamegraphLines = new ArrayList<>();
        HashMap<String, Integer> classNames = new HashMap<>();
        HashMap<String, Integer> methodNames = new HashMap<>();
        HashMap<String, Integer> descriptions = new HashMap<>();

        TreeMap<String, Integer> sortedStacks = new TreeMap<>(this.getStacks());

        // todo: it's better to split stacks beforehand.
        //  Currently split(";") is called n^2 times (in for loop and in getWidth method)
        //  Maybe something like: List<Stack> sortedStacks
        //  class Stack { String[] frames; int width }
        //  also it's good to filter here stacks with 0 width
        for (Map.Entry<String, Integer> stack : sortedStacks.entrySet()) {
            String[] frames = stack.getKey().split(";");
            boolean skippingDuplicates = true;
            for (int depth = 1; depth <= frames.length; depth++) {
                // I don't remember what was the problem about depth = 0
                // probably something related to serialization. So it should start with 1
                String frame = frames[depth - 1];
                String className = getClassName(frame);
                int classNameId = getId(classNames, className);
                String methodName = getMethodName(frame);
                int methodNameId = getId(methodNames, methodName);

                if (skippingDuplicates && lastNodeIsTheSame(cFlamegraphLines, classNameId, methodNameId, depth)) {
                    continue;
                }

                skippingDuplicates = false;
                int width = getWidth(sortedStacks, frame, depth);

                if (width == 0) {
                    continue;
                }

                cFlamegraphLines.add(new CFlamegraphLine(
                        classNameId,
                        methodNameId,
                        null,
                        width,
                        depth
                ));
            }
        }

        return new CFlamegraph(
                cFlamegraphLines,
                toArray(classNames),
                toArray(methodNames),
                toArray(descriptions)
        );
    }

    private boolean lastNodeIsTheSame(ArrayList<CFlamegraphLine> cFlamegraphLines, int classNameId, int methodNameId, int depth) {
        if (cFlamegraphLines.isEmpty()) return false;
        // todo: check description
        for (int i = cFlamegraphLines.size() - 1; i >= 0; i--) {
            CFlamegraphLine cFlamegraphLine = cFlamegraphLines.get(i);
            if (cFlamegraphLine.getDepth() == depth) {
                Integer clNameId = cFlamegraphLine.getClassNameId();
                return clNameId != null && clNameId == classNameId && cFlamegraphLine.getMethodNameId() == methodNameId;
            }
        }
        return false;
    }

    private int getWidth(Map<String, Integer> stacks, String frame, int depth) {
        // todo: bug here.
        //  We need to calc width across all neighbouring frames on the same depth
        //  (not across all frames on the same depth).
        //  This can be fixed after we solve above problem ^^ about splitting stacks beforehand.
        int width = 0;

        for (Map.Entry<String, Integer> stack : stacks.entrySet()) {
            String[] frames = stack.getKey().split(";");
            if (depth > frames.length) continue;
            String currentFrame = frames[depth - 1];
            if (currentFrame.equals(frame)) width += stack.getValue();
        }

        return width;
    }

    private String getClassName(String call) {
        // todo: reuse methods of StacksToTreeBuilder?
        int spacePos = call.indexOf(' ');
        int lastDotPos = call.lastIndexOf('.');
        if (spacePos == -1 || lastDotPos == -1) return ERROR_FRAME;
        return call.substring(spacePos + 1, lastDotPos);
    }

    private String getMethodName(String call) {
        String[] callParts = call.split("\\.");

        if(callParts.length > 2) {
            String methodWithParams = callParts[callParts.length - 1];
            return methodWithParams.substring(0, methodWithParams.indexOf("(")); // Last part, before the parenthesis
        }
        return ""; //Something went wrong
    }
}
