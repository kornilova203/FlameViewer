package com.github.kornilova_l.flamegraph.plugin.server.trees.flamegraph_format_trees;

import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class StacksParser {
    private static final Pattern flamegraphLinePattern = Pattern.compile(".* \\d+");
    private static final Pattern fullCallPattern = Pattern.compile("(?:[^ ()]+ [^ ()]+\\([^)]*\\);)*[^ ()]+ [^ ()]+\\([^)]*\\);?");

    @Nullable
    public static Map<String, Integer> getStacks(File convertedFile) {
        try (BufferedReader reader = new BufferedReader(
                new FileReader(convertedFile)
        )) {
            Map<String, Integer> stacks = new HashMap<>();
            reader.lines()
                    .filter(line -> !line.isEmpty())
                    .forEach(line -> stacks.put(
                            line.substring(0, line.lastIndexOf(" ")),
                            Integer.parseInt(line.substring(
                                    line.lastIndexOf(" ") + 1,
                                    line.length()
                            ))));
            return stacks;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isFlamegraph(byte[] bytes) {
        boolean hasValidLine = false;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(bytes)
        ))) {
            String line = reader.readLine();
            while (line != null && !Objects.equals(line, "")) {
                if (!flamegraphLinePattern.matcher(line).matches()) {
                    return false;
                } else {
                    hasValidLine = true;
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hasValidLine;
    }

    /**
     * @return true if stacktraces contain parameters and return value
     */
    static boolean doCallsContainParameters(Map<String, Integer> stacks) {
        for (String stack : stacks.keySet()) {
            if (!fullCallPattern.matcher(stack).matches()) {
                return false;
            }
        }
        return true;
    }
}
