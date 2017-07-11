package com.github.kornilova_l.profiler;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ProfilerFileManager {
    private static final String PLUGIN_DIR_NAME = "/flamegraph-profiler";
    private static final String LOG_DIR_NAME = "events";
    private static final String CONFIG_DIR_NAME = "config";
    private static final String STATIC_DIR_NAME = "static";
    private static final int FILE_NAME_LENGTH = 4;
    @NotNull
    private final File logDir;
    @NotNull
    private final File staticDir;
    @NotNull
    private final File configDir;

    public ProfilerFileManager(String systemDirPath) {
        String pluginDirPath = systemDirPath + PLUGIN_DIR_NAME;
        createDirIfNotExist(new File(pluginDirPath));
        logDir = new File(pluginDirPath + "/" + LOG_DIR_NAME);
        createDirIfNotExist(logDir);
        staticDir = new File(getClass().getResource("/static").getPath());
        configDir = new File(pluginDirPath + "/" + CONFIG_DIR_NAME);
        createDirIfNotExist(configDir);
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

    private static void createDirIfNotExist(@NotNull File dir) {
        if (!dir.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                dir.mkdir();
            } catch (SecurityException se) {
                se.printStackTrace();
            }
        }
    }

    public String getFilePath(String fileName) {
        return logDir.getAbsolutePath() + "/" + fileName;
    }

    public File getConfigFile(String projectName) {
        return new File(configDir.getAbsolutePath() + "/" + projectName + ".config");
    }

    public File createLogFile() {
        int max = getLargestFileNum();
        return new File(logDir.getAbsolutePath() + "/" + intToString(max + 1) + ".ser");
    }

    @Nullable
    public File getLatestFile() {
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

    private int getLargestFileNum() {
        File latestFile = getLatestFile();
        if (latestFile != null) {
            Matcher m = Pattern.compile("[0-9]+").matcher(latestFile.getName());
            if (m.find()) {
                return Integer.parseInt(m.group());
            }
        }
        return 0;
    }

    @NotNull
    public File getStaticDir() {
        return staticDir;
    }

    public List<String> getFileNameList() {
        List<String> list = new LinkedList<>();
        File[] files = logDir.listFiles();
        if (files != null) {
            list = Arrays.stream(files)
                    .sorted((f1, f2) -> {
                        if (f1.lastModified() == f2.lastModified()) {
                            return 0;
                        }
                        if (f1.lastModified() < f2.lastModified()) {
                            return 1;
                        }
                        return -1;
                    })
                    .map(File::getName)
                    .collect(Collectors.toList());
        }
        return list;
    }

    public void saveFile(ByteBuf content, String fileName) {
        File file = new File(logDir.getAbsolutePath() + "/" + fileName);
        try(OutputStream outputStream = new FileOutputStream(file)) {
            byte[] bytes = new byte[content.readableBytes()];
            content.readBytes(bytes);
            outputStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
