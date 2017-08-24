package com.github.kornilova_l.flamegraph.plugin.server.trees.jfr_trees;

import com.github.kornilova_l.flamegraph.plugin.PluginFileManager;
import oracle.jrockit.jfr.parser.ChunkParser;
import oracle.jrockit.jfr.parser.FLREvent;
import oracle.jrockit.jfr.parser.FLRStruct;
import oracle.jrockit.jfr.parser.Parser;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.kornilova_l.flamegraph.plugin.server.trees.DescriptionConverter.getBeautifulParameters;
import static com.github.kornilova_l.flamegraph.plugin.server.trees.DescriptionConverter.getBeautifulReturnValue;

/**
 * FlightRecorderConverter takes .jfr file
 * Converts it to <a href="https://github.com/brendangregg/FlameGraph">FlameGraph</a> format
 * Saves to /stacks dir in profiler dir
 */
public class FlightRecorderConverter {
    private final Map<String, Integer> stacks = new HashMap<>();
    private static Pattern classNamePattern = Pattern.compile("(?<=ClassLoader = null\\n {12}Name = ).*(?=\\n)");
    private static Pattern methodNamePattern = Pattern.compile("(?<=}\\n {9}Name = ).*(?=\\n)");
    private static Pattern signaturePattern = Pattern.compile("(?<=Signature = ).*(?=\\n)");


    public FlightRecorderConverter(File unzippedFile) throws IllegalArgumentException {
        // TODO: beautify desc at the end
        System.out.println("start converting");
        buildStacks(unzippedFile);
    }

    @Nullable
    static Map<String, Integer> getStacks(File convertedFile) {
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
        StringBuilder stack = new StringBuilder();
        while (classNameMatcher.find() && methodNameMatcher.find() && signatureMatcher.find()) {
            stack.append(classNameMatcher.group())
                    .append(".")
                    .append(methodNameMatcher.group())
                    .append(signatureMatcher.group())
                    .append(" ");
        }
        stack.deleteCharAt(stack.length() - 1);
        return stack.toString();
    }

    @SuppressWarnings("deprecation")
    public static void main(String[] args) {
        new FlightRecorderConverter(new File("/home/lk/Downloads/flightRecording.jfr"));
        try (InputStream inputStream = new FileInputStream(
                new File("/home/lk/Downloads/flight_recording_180121comintellijideaMain14552.jfr"
                ))) {
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            File unzippedFile = PluginFileManager.getInstance().unzip(bytes);
            new FlightRecorderConverter(unzippedFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
