package com.github.kornilova_l.profiler;

import com.intellij.openapi.application.PathManager;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfilerFileManager {

    private static final String PROFILER_DIR_PATH = PathManager.getSystemPath() + "/.flamegraph-profiler";
    private static final String EVENTS_DIR_NAME = "events";
    private static final File logDir = getLogDir();
    private static final int FILE_NAME_LENGTH = 4;

    private static File getLogDir() {
        createIfNotExist(PROFILER_DIR_PATH);
        return createIfNotExist(PROFILER_DIR_PATH + "/" + EVENTS_DIR_NAME);
    }

    public static File createLogFile() {
        int max = getLargestFileNum();
        return new File(logDir.getAbsolutePath() + "/" + intToString(max + 1) + ".ser");
    }

    /**
     * 1 -> 0001
     * 235 -> 0235
     *
     * @param num number
     * @return string representation of number
     */
    private static String intToString(int num) {
        StringBuilder string = new StringBuilder(String.valueOf(num));
        int addZeros = FILE_NAME_LENGTH - string.length();
        for (int i = 0; i < addZeros; i++) {
            string.insert(0, "0");
        }
        return string.toString();
    }

    public static File getLatestFile() {
        File[] files = logDir.listFiles();
        if (files != null) {
            Optional<File> fileOptional = Arrays.stream(files)
                    .max(Comparator.comparing(File::getName));
            if (fileOptional.isPresent()) {
                return fileOptional.get();
            }
        }
        return null;
    }

    private static int getLargestFileNum() {
        File latestFile = getLatestFile();
        if (latestFile != null) {
            Matcher m = Pattern.compile("[0-9]+").matcher(latestFile.getName());
            if (m.find()) {
                return Integer.parseInt(m.group());
            }
        }
        return 0;
    }

    private static File createIfNotExist(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                dir.mkdir();
            } catch (SecurityException se) {
                se.printStackTrace();
            }
        }
        return dir;
    }

    public static File getStaticDir() {
        return createIfNotExist(PROFILER_DIR_PATH + "/" + "static");
    }
}
