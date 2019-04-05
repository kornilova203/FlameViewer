package com.github.korniloval.flameviewer.converters.calltraces;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class StacksParser {
    private static final Pattern flamegraphLinePattern = Pattern.compile(".* \\d+");

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
                                    line.lastIndexOf(" ") + 1
                            ))));
            return stacks;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isFlamegraph(@NotNull byte[] bytes) {
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

    public static boolean writeTo(Map<String, Integer> stacks, File file) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
            for (Map.Entry<String, Integer> entry : stacks.entrySet()) {
                bufferedWriter.write(String.format("%s %d%n", entry.getKey(), entry.getValue()));
            }
            bufferedWriter.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
