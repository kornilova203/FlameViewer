package com.github.kornilova_l.flamegraph.plugin;

import com.github.kornilova_l.flamegraph.plugin.converters.ProfilerToFlamegraphConverter;
import com.intellij.openapi.application.PathManager;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.github.kornilova_l.flamegraph.plugin.server.ProfilerHttpRequestHandler.getParameter;

public class PluginFileManager {
    private static final boolean isWindows = System.getProperty("os.name").startsWith("Windows");
    private static final String PLUGIN_DIR_NAME = "flamegraph-profiler";
    private static final String LOG_DIR_NAME = "log";
    private static final String CONFIG_DIR_NAME = "configuration";
    private static final String STATIC_DIR_NAME = "static";
    private static final String REQUEST_PREFIX = "/flamegraph-profiler/";
    private static final String UPLOADED_FILES = "uploaded-files";
    private static final String DELETED_FILES = "deleted";
    private static final String NOT_CONVERTED = "not-converted";
    private static final String SER_FILES = "ser";
    private static final String FLAMEGRAPH_FILES = "flamegraph";
    private static PluginFileManager pluginFileManager;
    @NotNull
    private final Path logDirPath;
    @NotNull
    private final Path configDirPath;
    @NotNull
    private final Path staticDirPath;
    @NotNull
    private final Path serFiles;
    @NotNull
    private final Path flamegraphFiles;

    public final FileSaver serFileSaver;

    public final FileSaver tempFileSaver; // save files before converting

    public final FlamegraphSaver flamegraphFileSaver;

    private PluginFileManager(@NotNull String systemDirPath) {
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
        Path uploadedFilesPath = Paths.get(logDirPath.toString(), UPLOADED_FILES);
        createDirIfNotExist(uploadedFilesPath);

        Path deletedFilesPath = Paths.get(logDirPath.toString(), DELETED_FILES);
        createDirIfNotExist(deletedFilesPath);

        Path notConvertedFiles = Paths.get(uploadedFilesPath.toString(), NOT_CONVERTED);
        createDirIfNotExist(notConvertedFiles);
        tempFileSaver = new FileSaver(notConvertedFiles);
        serFiles = Paths.get(uploadedFilesPath.toString(), SER_FILES);
        createDirIfNotExist(serFiles);
        serFileSaver = new FileSaver(serFiles);
        flamegraphFiles = Paths.get(uploadedFilesPath.toString(), FLAMEGRAPH_FILES);
        createDirIfNotExist(flamegraphFiles);
        clearDir(new File(notConvertedFiles.toString()));
        flamegraphFileSaver = new FlamegraphSaver(flamegraphFiles);
    }

    private void clearDir(File dir) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    public static PluginFileManager getInstance() {
        if (pluginFileManager == null) {
            pluginFileManager = new PluginFileManager(PathManager.getSystemPath());
        }
        return pluginFileManager;
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

    @Nullable
    public File getLogFile(QueryStringDecoder urlDecoder) {
        String projectName = getParameter(urlDecoder, "project");
        String fileName = getParameter(urlDecoder, "file");
        if (projectName == null || fileName == null) {
            return null;
        }
        return getLogFile(projectName, fileName);
    }

    public File getConfigurationFile(String projectName) {
        Path path = Paths.get(configDirPath.toString(), projectName + ".config");
        return new File(path.toString());
    }

    public File createLogFile(String projectName, String configurationName) {
        Path logDir = getLogDirPath(projectName);
        Path logFile = Paths.get(logDir.toString(),
                configurationName + "-" + new SimpleDateFormat("yyyy-MM-dd-HH_mm_ss").format(new Date()) + ".ser");
        return new File(logFile.toString());
    }

    @NotNull
    public List<FileNameAndDate> getFileNameList(@NotNull String projectName) {
        File projectLogDir = new File(getLogDirPath(projectName).toString());
        if (!Objects.equals(projectName, UPLOADED_FILES)) {
            return getFileNameList(projectLogDir);
        }
        List<FileNameAndDate> fileNames = new ArrayList<>();
        fileNames.addAll(getFileNameList(new File(serFiles.toString())));
        fileNames.addAll(getFileNameList(new File(flamegraphFiles.toString())));
        return fileNames;
    }

    @NotNull
    private List<FileNameAndDate> getFileNameList(File projectLogDir) {
        File[] files = projectLogDir.listFiles();
        if (files != null) {
            return Arrays.stream(files)
                    .filter(file -> !file.isDirectory())
                    .sorted((f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()))
                    .map(FileNameAndDate::new)
                    .collect(Collectors.toList());
        }
        return new LinkedList<>();
    }

    public String getStaticFilePath(String staticFileUri) {
        Path path = Paths.get(
                staticDirPath.toString(),
                staticFileUri.substring(REQUEST_PREFIX.length(), staticFileUri.length())
        );
        return path.toString().replaceAll("%20", " ");
    }

    @NotNull
    private Path getLogDirPath(@NotNull String projectName) {
        Path path = Paths.get(logDirPath.toString(), projectName);
        createDirIfNotExist(path);
        return path;
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
    private File getLogFile(String projectName, String fileName) {
        Path dirPath;
        if (!Objects.equals(projectName, UPLOADED_FILES)) {
            dirPath = getLogDirPath(projectName);
        } else {
            if (Objects.equals(ProfilerToFlamegraphConverter.Companion.getFileExtension(fileName), "ser")) {
                dirPath = serFiles;
            } else {
                dirPath = flamegraphFiles;
            }
        }

        File file = new File(Paths.get(dirPath.toString(), fileName).toString());
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
        removeEmptyProjects();
        File logDir = new File(logDirPath.toString());
        if (logDir.exists() && logDir.isDirectory()) {
            File[] files = logDir.listFiles();
            if (files != null) {
                return Arrays.stream(files)
                        .filter(file ->
                                !Objects.equals(file.getName(), UPLOADED_FILES) &&
                                        !Objects.equals(file.getName(), DELETED_FILES) &&
                                        file.isDirectory())
                        .map(File::getName)
                        .collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }

    private void removeEmptyProjects() {
        File logDir = new File(logDirPath.toString());
        if (logDir.exists() && logDir.isDirectory()) {
            File[] projects = logDir.listFiles();
            if (projects != null) {
                for (File project : projects) {
                    if (project.isDirectory()) {
                        if (Objects.equals(project.getName(), UPLOADED_FILES) ||
                                Objects.equals(project.getName(), DELETED_FILES)) {
                            continue;
                        }
                        File[] projectFiles = project.listFiles();
                        if (projectFiles == null || projectFiles.length == 0) {
                            //noinspection ResultOfMethodCallIgnored
                            project.delete();
                        }
                    }
                }
            }
        }
    }

    public void deleteFile(@NotNull String fileName, @NotNull String projectName) {
        File file = getLogFile(projectName, fileName);
        if (file == null || !file.exists()) {
            return;
        }
        //noinspection ResultOfMethodCallIgnored
        file.renameTo(Paths.get(logDirPath.toString(), DELETED_FILES, fileName).toFile());
    }

    static class FileNameAndDate {
        private static final Pattern nameWithoutDate = Pattern.compile(".*(?=-\\d\\d\\d\\d-\\d\\d-\\d\\d-\\d\\d_\\d\\d_\\d\\d(.*)?)");
        @SuppressWarnings("unused")
        private final String name;
        private final String fullName;
        @SuppressWarnings("unused")
        private final String date;
        @SuppressWarnings("unused")
        private final String id;

        FileNameAndDate(@NotNull File file) {
            this.fullName = file.getName();
            this.id = this.fullName.replaceAll("\\.", "").replaceAll(":", "");
            Matcher matcher = nameWithoutDate.matcher(this.fullName);
            if (matcher.find()) {
                this.name = matcher.group();
            } else {
                this.name = fullName;
            }
            this.date = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date(file.lastModified()));
        }
    }

    public class FileSaver {
        final Path dir;

        FileSaver(Path dir) {
            this.dir = dir;
        }

        @Nullable
        public File save(byte[] bytes, String fileName) {
            File file = Paths.get(dir.toString(), fileName).toFile();
            try (OutputStream outputStream = new FileOutputStream(file)) {
                outputStream.write(bytes);
                return file;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class FlamegraphSaver extends FileSaver {
        FlamegraphSaver(Path dir) {
            super(dir);
        }

        public File save(@NotNull Map<String, Integer> stacks, @NotNull String fileName) {
            File file = Paths.get(dir.toString(), fileName).toFile();
            try (OutputStream outputStream = new FileOutputStream(file)) {
                for (Map.Entry<String, Integer> stack : stacks.entrySet()) {
                    outputStream.write((stack.getKey() + " " + stack.getValue() + "\n").getBytes());
                }
                return file;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
