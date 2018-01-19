package com.github.kornilova_l.flamegraph.plugin;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.github.kornilova_l.flamegraph.plugin.server.ProfilerHttpRequestHandler.getParameter;

/**
 * IDEA system dir
 * |-- flamegraph-profiler
 * |-- configuration // where configuration is exported after
 * |-- log
 * |-- deleted // deleted files temporary stored in this dir (they are returned back if `undo` is pressed)
 * |-- uploaded-files
 * |-- flamegraph // uploaded files in flamegraph format
 * |-- not-converted // files are stored here before conversion
 * \-- ser // uploaded .ser files
 */
public class PluginFileManager {
    private static final Logger LOG = Logger.getInstance(PluginFileManager.class);

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
    private final File uploadedFilesDir;
    @NotNull
    private final Path configDirPath;
    @NotNull
    private final Path staticDirPath;

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
        uploadedFilesDir = uploadedFilesPath.toFile();

        Path deletedFilesPath = Paths.get(logDirPath.toString(), DELETED_FILES);
        createDirIfNotExist(deletedFilesPath);

        Path notConvertedFiles = Paths.get(uploadedFilesPath.toString(), NOT_CONVERTED);
        createDirIfNotExist(notConvertedFiles);
        tempFileSaver = new FileSaver(notConvertedFiles);
        Path serFiles = Paths.get(uploadedFilesPath.toString(), SER_FILES);
        createDirIfNotExist(serFiles);
        serFileSaver = new FileSaver(serFiles);
        Path flamegraphFiles = Paths.get(uploadedFilesPath.toString(), FLAMEGRAPH_FILES);
        createDirIfNotExist(flamegraphFiles);
        clearDir(new File(notConvertedFiles.toString()));
        flamegraphFileSaver = new FlamegraphSaver(flamegraphFiles);

        finallyDeleteRemovedFiles();
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

    @SuppressWarnings("UnusedReturnValue")
    private boolean finallyDeleteRemovedFiles() {
        File deletedFilesDir = Paths.get(logDirPath.toString(), DELETED_FILES).toFile();
        if (!deletedFilesDir.exists() || !deletedFilesDir.isDirectory()) {
            LOG.debug("Directory with deleted files was not found");
            return false;
        }
        File[] files = deletedFilesDir.listFiles();
        //noinspection SimplifiableIfStatement
        if (files != null) {
            return Arrays.stream(files).map(File::delete).anyMatch(res -> !res);
        }
        return false;
    }

    public synchronized static PluginFileManager getInstance() {
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
    public synchronized File getLogFile(QueryStringDecoder urlDecoder) {
        String projectName = getParameter(urlDecoder, "project");
        String fileName = getParameter(urlDecoder, "file");
        if (projectName == null || fileName == null) {
            return null;
        }
        return getLogFile(projectName, fileName);
    }

    public synchronized File getConfigurationFile(@NotNull String projectName) {
        Path path = Paths.get(configDirPath.toString(), projectName + ".config");
        return new File(path.toString());
    }

    public synchronized File createLogFile(@NotNull String projectName, @NotNull String configurationName) {
        File logDir = getLogDirPath(projectName);
        Path logFile = Paths.get(logDir.toString(),
                configurationName + "-" + new SimpleDateFormat("yyyy-MM-dd-HH_mm_ss").format(new Date()) + ".ser");
        return new File(logFile.toString());
    }

    @NotNull
    public synchronized List<FileNameAndDate> getFileNameList(@NotNull String projectName) {
        File projectLogDir = getLogDirPath(projectName);
        List<FileNameAndDate> fileNames = new ArrayList<>();
        if (!Objects.equals(projectName, UPLOADED_FILES)) {
            addFilesFromDirToList(projectLogDir, fileNames);
            return fileNames;
        }
        File[] dirsInsideUploaded = uploadedFilesDir.listFiles();
        if (dirsInsideUploaded == null) {
            return fileNames;
        }
        for (File dir : dirsInsideUploaded) {
            if (dir.isDirectory()) {
                addFilesFromDirToList(dir, fileNames);
            }
        }
        return fileNames;
    }

    private void addFilesFromDirToList(File projectLogDir, List<FileNameAndDate> fileNames) {
        File[] files = projectLogDir.listFiles();
        if (files == null) {
            return;
        }
        Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
        for (File file : files) {
            if (file.isFile()) {
                fileNames.add(new FileNameAndDate(file));
            }
        }
    }

    public synchronized File getStaticFile(String staticFileUri) {
        return Paths.get(
                staticDirPath.toString(),
                staticFileUri.substring(REQUEST_PREFIX.length(), staticFileUri.length())
        ).toFile();
    }

    @NotNull
    private File getLogDirPath(@NotNull String projectName) {
        Path path = Paths.get(logDirPath.toString(), projectName);
        createDirIfNotExist(path);
        return path.toFile();
    }

    @Nullable
    public synchronized String getPathToJar(String jarName) {
        URL url = getClass().getResource("/" + jarName);
        try {
            return Paths.get(url.toURI()).toString();
        } catch (URISyntaxException e) {
            LOG.error(e);
        }
        return null;
    }

    @Nullable
    public synchronized File getLogFile(String projectName, String fileName) {
        if (!Objects.equals(projectName, UPLOADED_FILES)) {
            return Paths.get(logDirPath.toString(), projectName, fileName).toFile();
        }
        return findFileInSubDirectories(fileName, uploadedFilesDir);
    }

    @Nullable
    public synchronized String getLatestFileName(@NotNull String projectName) {
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
    public synchronized List<String> getProjectList() {
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

    /**
     * Mainly used for test
     */
    public synchronized void deleteAllUploadedFiles() {
        File[] dirsInsideUploadedFiles = uploadedFilesDir.listFiles();
        if (dirsInsideUploadedFiles == null) {
            return;
        }
        for (File maybeDir : dirsInsideUploadedFiles) {
            if (maybeDir.isDirectory()) {
                File[] files = maybeDir.listFiles();
                if (files == null) {
                    continue;
                }
                for (File file : files) {
                    boolean res = file.delete();
                    if (!res) {
                        System.err.println("Cannot delete file: " + file);
                    }
                }
            }
        }
    }

    public synchronized void deleteFile(@NotNull String fileName, @NotNull String projectName) {
        File file = getLogFile(projectName, fileName);
        if (file == null || !file.exists()) {
            return;
        }
        /* uploaded files are stored in separate directories
         * (name of a directory is an id of converter that is responsible for the file)
         * when we move file to temporal directory we want to save converter id,
         * so if delete action is undone we can move file back to needed directory */
        boolean res;
        if (projectName.equals("uploaded-files")) {
            String converterId = file.getParentFile().getName();
            File newDir = Paths.get(logDirPath.toString(), DELETED_FILES, converterId).toFile();
            if (!newDir.exists()) {
                if (!newDir.mkdir()) {
                    LOG.warn("Cannot create directory to move deleted file. File: " + file + " Dir: " + newDir);
                    return;
                }
            }
            res = file.renameTo(Paths.get(newDir.toString(), fileName).toFile());
        } else {
            res = file.renameTo(Paths.get(logDirPath.toString(), DELETED_FILES, fileName).toFile());
        }
        if (!res) {
            LOG.warn("Cannot move file to DELETED_FILES directory. File: " + file);
        }
    }

    public synchronized void undoDeleteFile(String fileName, String projectName) {
        File deletedFile = getDeletedFile(fileName, projectName);
        if (deletedFile == null || !deletedFile.exists()) {
            LOG.debug("Undo delete. Cannot find file to undo delete: " + fileName);
            return;
        }
        File projectDirPath;
        if (projectName.equals("uploaded-files")) {
            String converterId = deletedFile.getParentFile().getName();
            projectDirPath = Paths.get(logDirPath.toString(), UPLOADED_FILES, converterId).toFile();
        } else  {
            projectDirPath = Paths.get(logDirPath.toString(), projectName).toFile();
        }
        if (projectDirPath == null) {
            return;
        }
        boolean res = deletedFile.renameTo(Paths.get(projectDirPath.toString(), fileName).toFile());
        if (!res) {
            LOG.warn("Cannot move file back from temp directory. File: " + fileName);
        }
    }

    @Nullable
    private File getDeletedFile(String fileName, String projectName) {
        File deletedFilesDir = Paths.get(logDirPath.toString(), DELETED_FILES).toFile();
        if (projectName.equals("uploaded-files")) {
            return findFileInSubDirectories(fileName, deletedFilesDir);
        } else {
            return Paths.get(deletedFilesDir.toString(), fileName).toFile();
        }
    }

    @Nullable
    private File findFileInSubDirectories(String fileName, File dir) {
        File[] subDirs = dir.listFiles();
        if (subDirs == null) {
            return null;
        }
        for (File subDir : subDirs) {
            if (subDir.isDirectory() && !subDir.getName().equals(DELETED_FILES) && !subDir.getName().equals(NOT_CONVERTED)) {
                File[] files = subDir.listFiles();
                if (files == null) {
                    continue;
                }
                for (File file : files) {
                    if (file.getName().equals(fileName)) {
                        return file;
                    }
                }
            }
        }
        return null;
    }

    public void moveFileToUploadedFiles(@NotNull String converterId, @NotNull String fileName, @NotNull File file) {
        File dir = Paths.get(uploadedFilesDir.toString(), converterId).toFile();
        if (!dir.exists()) {
            boolean res = dir.mkdir();
            if (!res) {
                LOG.error("Cannot save file " + fileName + " to " + converterId + " directory.");
                return;
            }
        }
        File newFile = Paths.get(dir.toString(), fileName).toFile();
        boolean success = file.renameTo(newFile);
        if (!success) {
            LOG.error("Cannot move file " + file + " to " + converterId + " directory.");
        }
    }

    public static String getParentDirName(File file) {
        File parentFile = file.getParentFile();
        if (parentFile == null) {
            return null;
        }
        return parentFile.getName();
    }

    /**
     * For test
     */
    @NotNull
    public Path getLogDirPath() {
        return logDirPath;
    }

    static class FileNameAndDate {
        private static final Pattern nameWithoutDate = Pattern.compile(".*(?=-\\d\\d\\d\\d-\\d\\d-\\d\\d-\\d\\d_\\d\\d_\\d\\d(.*)?)");
        @SuppressWarnings("unused")
        private final String name;
        private final String fullName;
        @SuppressWarnings("unused")
        private final String date;
        /**
         * id is used as css id
         */
        @SuppressWarnings("unused")
        private final String id;

        FileNameAndDate(@NotNull File file) {
            this.fullName = file.getName();
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < file.getName().length(); i++) {
                char c = file.getName().charAt(i);
                if (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '-' || c == '_') { // if allowed by css
                    stringBuilder.append(c);
                } else {
                    stringBuilder.append('_');
                }
            }
            this.id = "id-" + stringBuilder.toString();
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
                LOG.error(e);
            }
            return null;
        }

        @Nullable
        public File moveToDir(@NotNull File file, @NotNull String newFileName) {
            File newFile = Paths.get(dir.toString(), newFileName).toFile();
            boolean success = file.renameTo(newFile);
            if (!success) {
                LOG.error("Cannot move file " + file);
                return null;
            }
            return newFile;
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
