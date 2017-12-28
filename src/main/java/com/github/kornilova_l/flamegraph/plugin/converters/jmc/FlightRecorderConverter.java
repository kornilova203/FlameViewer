package com.github.kornilova_l.flamegraph.plugin.converters.jmc;

import com.github.kornilova_l.flight_parser.FlightParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class FlightRecorderConverter {
    private Map<String, Integer> stacks;

    FlightRecorderConverter(File file) {
        FlightParser flightParser = new FlightParser(file);
        Map<String, Integer> stacks = flightParser.getStacksMap();
        this.stacks = removePackageInParameters(stacks);
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
                newStack.append(call.substring(spacePos, openBracketPos)).append('('); // class and name of method
                String parameters[] = call.substring(openBracketPos + 1, call.length() - 1).split(" *, *");
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
            return type.substring(dot + 1, type.length());
        }
        return type;
    }

    void writeTo(File file) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
            for (Map.Entry<String, Integer> entry : stacks.entrySet()) {
                bufferedWriter.write(String.format("%s %d%n", entry.getKey(), entry.getValue()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
