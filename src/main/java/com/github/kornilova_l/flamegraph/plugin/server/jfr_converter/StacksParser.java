package com.github.kornilova_l.flamegraph.plugin.server.jfr_converter;

import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class StacksParser {
    private static Pattern flamegraphLinePattern =
            Pattern.compile("([^ ]+ [^ ]+(([./])[^ ]+)+\\([^)]*\\)(;| \\d+))+");

    @Nullable
    public static Map<String, Integer> getStacks(File convertedFile) {
        try (BufferedReader reader = new BufferedReader(
                new FileReader(convertedFile)
        )) {
            Map<String, Integer> stacks = new HashMap<>();
            reader.lines()
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
                System.out.println(line);
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
}
