package com.github.kornilova_l.flamegraph.plugin.server.trees.jfr_trees;

import com.github.kornilova_l.flamegraph.plugin.PluginFileManager;
import com.google.common.collect.Lists;
import oracle.jrockit.jfr.parser.ChunkParser;
import oracle.jrockit.jfr.parser.FLREvent;
import oracle.jrockit.jfr.parser.FLRStruct;
import oracle.jrockit.jfr.parser.Parser;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.kornilova_l.flamegraph.plugin.server.trees.DescriptionConverter.getBeautifulParameters;
import static com.github.kornilova_l.flamegraph.plugin.server.trees.DescriptionConverter.getBeautifulReturnValue;

/**
 * ParserFlightRecorderConverter takes .jfr file
 * Converts it to <a href="https://github.com/brendangregg/FlameGraph">FlameGraph</a> format
 * Saves to /stacks dir in profiler dir
 */
public class ParserFlightRecorderConverter {
    private final Map<String, Integer> stacks = new HashMap<>();
    private static Pattern classNamePattern = Pattern.compile("(?<=ClassLoader = null\\n {12}Name = ).*(?=\\n)");
    private static Pattern methodNamePattern = Pattern.compile("(?<=}\\n {9}Name = ).*(?=\\n)");
    private static Pattern signaturePattern = Pattern.compile("(?<=Signature = ).*(?=\\n)");


    public ParserFlightRecorderConverter(File unzippedFile) throws IllegalArgumentException {
        // TODO: beautify desc at the end
        System.out.println("start converting");
        buildStacks(unzippedFile);
    }

    @SuppressWarnings("deprecation")
    private void buildStacks(File file) {
        try {
            Parser parser = new Parser(file);
            for (ChunkParser chunkParser : parser) {
                for (FLREvent event : chunkParser) {
                    FLRStruct stackTrace = event.getStackTrace();
                    if (stackTrace != null) {
                        addStack(getStack(stackTrace));
                    }
                }
            }
            parser.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeTo(File file) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
            for (Map.Entry<String, Integer> entry : stacks.entrySet()) {
                bufferedWriter.write(String.format("%s %d%n", convertToNeededFormat(entry.getKey()), entry.getValue()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String convertToNeededFormat(String stack) {
        String[] methods = stack.split(" ");
        StringBuilder stringBuilder = new StringBuilder();
        for (String method : methods) {
            int bracket = method.lastIndexOf("(");
            String classAndMethod = method.substring(0, bracket);
            String fullDesc = method.substring(bracket, method.length());
            stringBuilder.append(getBeautifulReturnValue(fullDesc))
                    .append(" ")
                    .append(classAndMethod)
                    .append(getBeautifulParameters(fullDesc))
                    .append(";");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }

    private void addStack(String stack) {
        Integer count = stacks.get(stack);
        if (count == null) {
            count = 1;
        } else {
            count++;
        }
        stacks.put(stack, count);
    }

    @SuppressWarnings("deprecation")
    private static String getStack(FLRStruct savedStackTrace) {
        String fullString = savedStackTrace.toString();
        Matcher classNameMatcher = classNamePattern.matcher(fullString);
        Matcher methodNameMatcher = methodNamePattern.matcher(fullString);
        Matcher signatureMatcher = signaturePattern.matcher(fullString);
        List<String> calls = new ArrayList<>();
        while (classNameMatcher.find() && methodNameMatcher.find() && signatureMatcher.find()) {
            calls.add(classNameMatcher.group() +
                    "." +
                    methodNameMatcher.group() +
                    signatureMatcher.group());
        }
        return String.join(" ", Lists.reverse(calls));
    }

    @SuppressWarnings("deprecation")
    public static void main(String[] args) {
        new ParserFlightRecorderConverter(new File("/home/lk/Downloads/flightRecording.jfr"));
        try (InputStream inputStream = new FileInputStream(
                new File("/home/lk/Downloads/flight_recording_180121comintellijideaMain14552.jfr"
                ))) {
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            File unzippedFile = PluginFileManager.getInstance().unzip(bytes);
            new ParserFlightRecorderConverter(unzippedFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
