package com.github.kornilova_l.flamegraph.plugin.converters.jmc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Converter {
    protected Map<String, Integer> stacks = new HashMap<>();

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
