package com.github.korniloval.flameviewer.converters.cflamegraph;

import com.github.kornilova_l.flight_parser.FlightParser;
import com.github.korniloval.flameviewer.FlameIndicator;
import com.github.korniloval.flameviewer.converters.Converter;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class JfrToStacksConverter implements Converter<Map<String, Integer>> {
    public static final String EXTENSION = "jfr";
    private final File file;

    public JfrToStacksConverter(File file) {
        this.file = file;
    }

    @Override
    public Map<String, Integer> convert(@Nullable FlameIndicator indicator) {
        FlightParser flightParser = new FlightParser(file);
        if (indicator != null) indicator.checkCanceled();
        return removePackageInParameters(flightParser.getStacksMap());
    }

    /**
     * com.github.kornilova_l.algorithm_synthesis.grid2D.vertex_set_generator.LabelingFunction com.github.kornilova_l.algorithm_synthesis.grid2D.vertex_set_generator.VertexSetSolverKt.tryToFindSolution(java.util.Set, com.github.kornilova_l.algorithm_synthesis.grid2D.tiles.collections.DirectedGraph);
     * ->
     * LabelingFunction com.github.kornilova_l.algorithm_synthesis.grid2D.vertex_set_generator.VertexSetSolverKt.tryToFindSolution(Set, DirectedGraph);
     */
    private Map<String, Integer> removePackageInParameters(Map<String, Integer> stacks) {
        Map<String, Integer> newStacks = new HashMap<>();
        for (Map.Entry<String, Integer> entry : stacks.entrySet()) {
            String stack = entry.getKey();
            StringBuilder newStack = new StringBuilder();
            String[] calls = stack.split(";");
            for (int i = 0; i < calls.length; i++) {
                String call = calls[i];
                int spacePos = call.indexOf(' ');
                String returnValue = call.substring(0, spacePos);
                newStack.append(removePackage(returnValue));
                int openBracketPos = call.indexOf('(');
                newStack.append(call, spacePos, openBracketPos).append('('); // class and name of method
                String[] parameters = call.substring(openBracketPos + 1, call.length() - 1).split(" *, *");
                for (int j = 0; j < parameters.length; j++) {
                    newStack.append(removePackage(parameters[j]));
                    if (j != parameters.length - 1) {
                        newStack.append(", ");
                    }
                }
                newStack.append(')');
                if (i != calls.length - 1) {
                    newStack.append(";");
                }
            }
            newStacks.put(newStack.toString(), entry.getValue());
        }
        return newStacks;
    }

    private static String removePackage(String type) {
        int dot = type.lastIndexOf('.');
        if (dot != -1) {
            return type.substring(dot + 1);
        }
        return type;
    }
}
