package com.github.kornilova_l.flamegraph.plugin.server.converters.file.jmc;

import com.github.kornilova_l.flamegraph.plugin.server.converters.file.CFlamegraph;
import com.github.kornilova_l.flamegraph.plugin.server.converters.file.CFlamegraphLine;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class FlightRecorderCompressedConverter extends FlightRecorderConverter {
    private CFlamegraph cFlamegraph;

    public FlightRecorderCompressedConverter(File file) {
        super(file);

        parseAsCFlamegraph();
    }

    CFlamegraph getCFlamegraph() {
        return cFlamegraph;
    }

    private void parseAsCFlamegraph() {
        ArrayList<CFlamegraphLine> cFlamegraphLines = new ArrayList<>();
        HashMap<String, Integer> classNames = new HashMap<>();
        HashMap<String, Integer> methodNames = new HashMap<>();
        HashMap<String, Integer> descriptions = new HashMap<>();

        Map<String, Integer> stacks = this.getStacks();
        Set<String> stackFrames = stacks.keySet();

        for(String frame : stackFrames) {
            Integer width = stacks.get(frame);

            String[] calls = frame.split(";");
            Integer depth = calls.length;
            String topFrame = calls[calls.length - 1]; //TODO: Add a check on length before this access
            String className = getClassName(topFrame);
            String methodName = getMethodName(topFrame);

            CFlamegraphLine line = new CFlamegraphLine(
                    getId(classNames, className),
                    getId(methodNames, methodName),
                    null,
                    width,
                    depth);
            cFlamegraphLines.add(line);
        }

        this.cFlamegraph = new CFlamegraph(cFlamegraphLines, toArray(classNames), toArray(methodNames), toArray(descriptions));
    }

    private Integer getId(HashMap<String, Integer> map, String name) {
        Integer id = map.get(name);
        if (id == null) {
            Integer newId = map.size();
            map.put(name, newId);
            return newId;
        }
        return id;
    }

    private String[] toArray(HashMap<String, Integer> names)  {
        return names.keySet().toArray(new String[0]);
    }

    private String getClassName(String call) {
        String[] callParts = call.split("\\.");

        if(callParts.length > 1) {
            return callParts[callParts.length - 2]; // Not the last part (that's the method). Assuming the class is before.
        }
        return ""; //Something went wrong
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
