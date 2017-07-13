package com.github.kornilova_l.profiler;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ProfilerFileManager {
    private static final String DELIMITER = System.getProperty("os.name").startsWith("Windows") ? "\\" : "/";
    private static final String PLUGIN_DIR_NAME = "/flamegraph-profiler";
    private static final String LOG_DIR_NAME = "events";
    private static final String CONFIG_DIR_NAME = "config";
    private static final String STATIC_DIR_NAME = "static";
    private final String PLUGIN_DIR_PATH;
    @NotNull
    private final File logDir;

    public ProfilerFileManager(@NotNull String systemDirPath) {
        PLUGIN_DIR_PATH = systemDirPath + PLUGIN_DIR_NAME;
        createDirIfNotExist(new File(PLUGIN_DIR_PATH));
        logDir = new File(PLUGIN_DIR_PATH + DELIMITER + LOG_DIR_NAME);
        createDirIfNotExist(logDir);
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
        return logDir.getAbsolutePath() + DELIMITER + fileName;
    }

    public File getConfigFile(String projectName) {
        File configDir = new File(PLUGIN_DIR_PATH + DELIMITER + CONFIG_DIR_NAME);
        createDirIfNotExist(configDir);
        return new File(configDir.getAbsolutePath() + DELIMITER + projectName + ".config");
    }

    @NotNull
    private File getStaticDir() {
        return new File(getClass().getResource(DELIMITER + STATIC_DIR_NAME).getPath());
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
        File file = new File(logDir.getAbsolutePath() + DELIMITER + fileName);
        try(OutputStream outputStream = new FileOutputStream(file)) {
            byte[] bytes = new byte[content.readableBytes()];
            content.readBytes(bytes);
            outputStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getStaticFilePath(String staticFileUri) {
        String staticFilePath = staticFileUri.replaceFirst(
                "/[^/]+/",
                getStaticDir().getAbsolutePath() + DELIMITER
        );
        return staticFilePath.replaceAll("/", DELIMITER);
    }
}
