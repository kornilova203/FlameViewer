package com.github.kornilova_l.flamegraph.plugin.converters.jmc;

import com.github.kornilova_l.flight_parser.FlightParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

class FlightRecorderConverterNine extends Converter {
    private Map<String, Integer> stacks;

    FlightRecorderConverterNine(File file) {
        FlightParser flightParser = new FlightParser(file);
        stacks = flightParser.getStacksMap();
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
