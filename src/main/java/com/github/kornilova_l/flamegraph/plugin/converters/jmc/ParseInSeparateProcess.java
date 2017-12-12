package com.github.kornilova_l.flamegraph.plugin.converters.jmc;

import java.io.File;

/**
 * If jfr file is too big it will be parsed in separate process
 */
public class ParseInSeparateProcess {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Specify path to file");
            return;
        }
        System.out.println(args[0]);
        String fileName = args[0];
        if (fileName.charAt(0) == '"') {
            fileName = fileName.substring(1, fileName.length() - 1);
        }
        File file = new File(fileName);
        System.out.println(file);
        try {
            new FlightRecorderConverterEight(file).writeTo(file);
        } catch (Exception e) { // if it is java 9
            new FlightRecorderConverterNine(file).writeTo(file);
        }
    }
}
