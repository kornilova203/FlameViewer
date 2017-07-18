package com.github.kornilova_l.flamegraph.plugin;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class PluginFileManager {
    private static final boolean isWindows = System.getProperty("os.name").startsWith("Windows");
    private static final String PLUGIN_DIR_NAME = "flamegraph-profiler";
    private static final String LOG_DIR_NAME = "log";
    private static final String CONFIG_DIR_NAME = "configuration";
    private static final String STATIC_DIR_NAME = "static";
    private static final String REQUEST_PREFIX = "/flamegraph-profiler/";
    private static final String UPLOADED_FILES = "uploaded-files";
    @NotNull
    private final Path logDirPath;
    @NotNull
    private final Path configDirPath;
    @NotNull
    private final Path staticDirPath;
    @NotNull
    private final Path uploadedFilesPath;
    @NotNull
    private final Path convertedFilesPath;

    public PluginFileManager(@NotNull String systemDirPath) {
        Path systemDir = Paths.get(systemDirPath);
        Path pluginDir = Paths.get(systemDir.toString(), PLUGIN_DIR_NAME);
        createDirIfNotExist(pluginDir);
        logDirPath = Paths.get(pluginDir.toString(), LOG_DIR_NAME);
        createDirIfNotExist(logDirPath);
        configDirPath = Paths.get(pluginDir.toString(), CONFIG_DIR_NAME);
        createDirIfNotExist(configDirPath);
        staticDirPath = Paths.get(
                new File(
                        getClass().getResource("/" + STATIC_DIR_NAME).getPath()
                ).getAbsolutePath()
        );
        uploadedFilesPath = Paths.get(logDirPath.toString(), UPLOADED_FILES);
        createDirIfNotExist(uploadedFilesPath);
        convertedFilesPath = Paths.get(uploadedFilesPath.toString(), "converted");
        createDirIfNotExist(convertedFilesPath);
    }

    private static void createDirIfNotExist(@NotNull Path path) {
        File dir = new File(path.toString());
        if (!dir.exists()) {
            try {
                assert dir.mkdir();
            } catch (SecurityException se) {
                se.printStackTrace();
            }
        }
    }

    @Nullable
    private static File getLatestFile(File dir) {
        File[] files = dir.listFiles();
        if (files == null) {
            return null;
        }
        Optional<File> maxFile = Arrays.stream(files).max(Comparator.comparingLong(File::lastModified));
        return maxFile.orElse(null);
    }

    public File getConfigFile(String projectName) {
        Path path = Paths.get(configDirPath.toString(), projectName + ".config");
        return new File(path.toString());
    }

    public List<String> getFileNameList(@NotNull String projectName) {
        List<String> list = new LinkedList<>();
        File projectLogDir = new File(getLogDirPath(projectName));
        File[] files = projectLogDir.listFiles();
        if (files != null) {
            list = Arrays.stream(files)
                    .filter(file -> !file.isDirectory())
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
        Path filePath = Paths.get(uploadedFilesPath.toString(), fileName);
        File file = new File(filePath.toString());
        try (OutputStream outputStream = new FileOutputStream(file)) {
            byte[] bytes = new byte[content.readableBytes()];
            content.readBytes(bytes);
            outputStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getStaticFilePath(String staticFileUri) {
        Path path = Paths.get(
                staticDirPath.toString(),
                staticFileUri.substring(REQUEST_PREFIX.length(), staticFileUri.length())
        );
        return path.toString().replaceAll("%20", " ");
    }

    @NotNull
    public String getLogDirPath(@NotNull String projectName) {
        Path path = Paths.get(logDirPath.toString(), projectName);
        createDirIfNotExist(path);
        return path.toString();
    }

    @NotNull
    public String getPathToAgent() {
        String path = getClass().getResource("/javaagent.jar")
                .getPath()
                .replaceAll("%20", " ");
        if (isWindows) {
            path = path.substring(1, path.length()).replaceAll("/", "\\\\");
        }
        return path;
    }

    @Nullable
    public File getConfigFile(String projectName, String fileName) {
        String projectLogDirPath = getLogDirPath(projectName);
        Path path = Paths.get(projectLogDirPath, fileName);
        File file = new File(path.toString());
        if (file.exists()) {
            return file;
        }
        return null;
    }

    @Nullable
    public String getLatestFileName(@NotNull String projectName) {
        Path dirPath = Paths.get(logDirPath.toString(), projectName);
        File dir = new File(dirPath.toString());
        if (dir.exists() && dir.isDirectory()) {
            File latestFile = getLatestFile(dir);
            if (latestFile != null) {
                return latestFile.getName();
            }
        }
        return null;
    }

    @NotNull
    public List<String> getProjectList() {
        File logDir = new File(logDirPath.toString());
        if (logDir.exists() && logDir.isDirectory()) {
            File[] files = logDir.listFiles();
            if (files != null) {
                return Arrays.stream(files)
                        .filter(file ->
                                !Objects.equals(file.getName(), UPLOADED_FILES) &&
                                        file.isDirectory())
                        .map(File::getName)
                        .collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }

    @Nullable
    public File getConvertedFile(String name) {
        File dir = new File(convertedFilesPath.toString());
        assert dir.exists() && dir.isDirectory();
        File[] files = dir.listFiles();
        if (files == null) {
            return null;
        }
        name = getFileName(name);
        for (File file : files) {
            if (Objects.equals(getFileName(file.getName()), name)) {
                return file;
            }
        }
        return null;
    }

    static String getFileName(String name) {
        return name.substring(0, name.indexOf("."));
    }

    public File createdFileForConverted(File logFile) {
        String name = getFileName(logFile.getName());
        Path path = Paths.get(convertedFilesPath.toString(), name + ".converted");
        return new File(path.toString());
    }
}
