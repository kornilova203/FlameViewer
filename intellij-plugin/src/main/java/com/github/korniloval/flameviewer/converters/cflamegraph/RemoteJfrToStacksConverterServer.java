package com.github.korniloval.flameviewer.converters.cflamegraph;

import com.github.korniloval.flameviewer.converters.calltraces.StacksParser;

import java.io.File;
import java.util.Map;

/**
 * If jfr file is too big it will be parsed in separate process
 */
public class RemoteJfrToStacksConverterServer {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Specify path to file");
            return;
        }
        String fileName = args[0];
        if (fileName.charAt(0) == '"') {
            fileName = fileName.substring(1, fileName.length() - 1);
        }
        File file = new File(fileName);
        Map<String, Integer> stacks = new JfrToStacksConverter(file).convert();
        boolean res = StacksParser.writeTo(stacks, file);
        if (res) System.out.println("OK");
        else System.out.println("ERROR");
    }
}
