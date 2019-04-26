package com.github.korniloval.flameviewer.converters.calltraces;

import com.github.korniloval.flameviewer.converters.trees.TreesUtil;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class StacksParser {
    @Nullable
    public static Map<String, Integer> getStacks(File convertedFile) {
        try (BufferedReader reader = new BufferedReader(
                new FileReader(convertedFile)
        )) {
            Map<String, Integer> stacks = new HashMap<>();
            reader.lines()
                    .forEach(line -> {
                        if (line.isEmpty()) return;
                        int idx = line.lastIndexOf(" ");
                        if (idx == -1) return;
                        Integer width = TreesUtil.INSTANCE.parsePositiveInt(line, idx + 1, line.length());
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
