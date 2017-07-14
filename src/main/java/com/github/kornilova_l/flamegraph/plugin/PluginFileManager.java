package com.github.kornilova_l.flamegraph.plugin;

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

public class PluginFileManager {
    private static final boolean isWindows = System.getProperty("os.name").startsWith("Windows");
    private static final String DELIMITER = isWindows ? "\\" : "/";
    private static final String PLUGIN_DIR_NAME = DELIMITER + "flamegraph-profiler";
    private static final String LOG_DIR_NAME = DELIMITER + "events";
    private static final String CONFIG_DIR_NAME = DELIMITER + "configuration";
    private static final String STATIC_DIR_NAME = "static";
    @NotNull
    private final File logDir;
    @NotNull
    private final File configDir;
    @NotNull
    private final File staticDir;

    public PluginFileManager(@NotNull String systemDirPath) {
        systemDirPath = getNormalSysPath(systemDirPath);
        String pluginDirPath = systemDirPath + PLUGIN_DIR_NAME;
        createDirIfNotExist(new File(pluginDirPath));
        logDir = new File(pluginDirPath + LOG_DIR_NAME);
        createDirIfNotExist(logDir);
        configDir = new File(pluginDirPath + CONFIG_DIR_NAME);
        createDirIfNotExist(configDir);
        staticDir = new File(getClass().getResource("/" + STATIC_DIR_NAME).getPath());
    }

    private String getNormalSysPath(@NotNull String systemDirPath) {
        if (isWindows) {
            return systemDirPath.replaceAll("/", "\\\\");
        }
        return systemDirPath;
    }

    private static void createDirIfNotExist(@NotNull File dir) {
        if (!dir.exists()) {
            try {
                assert dir.mkdir();
            } catch (SecurityException se) {
                se.printStackTrace();
            }
        }
    }

    public String getFilePath(@NotNull String fileName) {
        return logDir.getAbsolutePath() + DELIMITER + fileName;
    }

    public File getConfigFile(String projectName) {
        return new File(configDir.getAbsolutePath() + DELIMITER + projectName + ".config");
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
                (staticDir.getAbsolutePath() + DELIMITER).replaceAll("\\\\", "\\\\\\\\")
        );
        return staticFilePath.replaceAll("/", DELIMITER);
    }

    @NotNull
    public File getLogDir() {
        return logDir;
    }

    @NotNull
    public String getPathToAgent() {
        String path = getClass().getResource("/javaagent.jar").getPath();
        if (isWindows) {
            path = path.substring(1, path.length()).replaceAll("/", "\\\\");
        }
        return path;
    }
}
