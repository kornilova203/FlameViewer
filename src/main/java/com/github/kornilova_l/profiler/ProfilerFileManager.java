package com.github.kornilova_l.profiler;

import com.intellij.openapi.application.PathManager;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ProfilerFileManager {

    private static String LOG_DIR_PATH = null;
    private static final String EVENTS_DIR_NAME = "events";
    private static File logDir;
    private static final int FILE_NAME_LENGTH = 4;
    private static final String PLUGIN_DIR_NAME = "/flamegraph-profiler";

    private static File getLogDir() {
        assert(LOG_DIR_PATH != null);
        createIfNotExist(LOG_DIR_PATH);
        return createIfNotExist(LOG_DIR_PATH + "/" + EVENTS_DIR_NAME);
    }

    public static void setPathToLogDir(String logDir) {
        LOG_DIR_PATH = logDir + PLUGIN_DIR_NAME;
        System.out.println("Path was set: " + LOG_DIR_PATH);
    }

    public static File createLogFile() {
        logDir = getLogDir();
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
        logDir = createIfNotExist(PathManager.getSystemPath() + PLUGIN_DIR_NAME + "/events");
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
        return new File( PathManager.getSystemPath() + PLUGIN_DIR_NAME + "/static");
    }

    public static List<String> getFileNameList() {
        List<String> list = new LinkedList<>();
        logDir = new File(PathManager.getSystemPath() + PLUGIN_DIR_NAME + "/events");
        if (logDir.exists()) {
            File[] files = logDir.listFiles();
            if (files != null) {
                list = Arrays.stream(files)
                        .sorted((f1, f2) -> {
                            if (f1.lastModified() == f2.lastModified()) {
                                return 0;
                            }
                            if (f1.lastModified() < f2.lastModified()) {
                                return -1;
                            }
                            return 1;
                        })
                        .map(File::getName)
                        .collect(Collectors.toList());
            }

        }
        return list;
    }
}
