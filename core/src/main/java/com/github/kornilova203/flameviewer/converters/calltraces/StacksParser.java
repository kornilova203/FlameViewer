package com.github.kornilova203.flameviewer.converters.calltraces;

import com.github.kornilova203.flameviewer.FlameIndicator;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static com.github.kornilova203.flameviewer.converters.trees.TreesUtilKt.parsePositiveInt;

public class StacksParser {
    @Nullable
    public static Map<String, Integer> getStacks(File convertedFile, @Nullable FlameIndicator indicator) {
        try (BufferedReader reader = new BufferedReader(
                new FileReader(convertedFile)
        )) {
            Map<String, Integer> stacks = new HashMap<>();
            reader.lines()
                    .forEach(line -> {
                        if (indicator != null) indicator.checkCanceled();
                        if (line.isEmpty()) return;
                        int idx = line.lastIndexOf(" ");
                        if (idx == -1) return;
                        Integer width = parsePositiveInt(line, idx + 1, line.length());
                        if (width != null) stacks.put(line.substring(0, idx), width);
                    });
            return stacks;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
