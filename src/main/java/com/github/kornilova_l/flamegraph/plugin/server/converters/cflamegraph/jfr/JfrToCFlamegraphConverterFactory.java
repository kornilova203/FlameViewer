package com.github.kornilova_l.flamegraph.plugin.server.converters.cflamegraph.jfr;

import com.github.kornilova_l.flamegraph.plugin.server.converters.cflamegraph.ProfilerToCFlamegraphConverter;
import com.github.kornilova_l.flamegraph.plugin.server.converters.cflamegraph.ProfilerToCFlamegraphConverterFactory;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.PathUtil;
import org.apache.commons.lang.SystemUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;

public class JfrToCFlamegraphConverterFactory implements ProfilerToCFlamegraphConverterFactory {
    private static final Logger LOG = Logger.getInstance(JfrToCFlamegraphConverterFactory.class);
    @SuppressWarnings("FieldCanBeLocal")
    private int allowedSize = 20000000; // in bytes. It is 20MB

    @Override
    public boolean isSupported(@NotNull File file) {
        return StringUtil.equalsIgnoreCase(PathUtil.getFileExtension(file.getName()), "jfr");
    }

    @NotNull
    @Override
    public ProfilerToCFlamegraphConverter create(@NotNull File file) {
        byte[] unzippedBytes = getUnzippedBytes(file);
        File newFile = getFileNear(file);
        saveToFile(newFile, unzippedBytes);
        if (unzippedBytes.length <= allowedSize) {
            return new JfrToCFlamegraphConverter(newFile);
        }
        /* if this code is executed in test request it will fail
         * because compiled classes for tests are stored in out/ directory
         * and there are no jar libraries */
        LOG.info("File " + file + " is too big. It will be converted in separate process");
        // todo: convert in remote process
        return null;
    }

    private static File getFileNear(@NotNull File file) {
        Path dir = Paths.get(file.toURI()).toAbsolutePath().getParent();
        return Paths.get(dir.toString(), "temp-" + System.currentTimeMillis()).toFile();
    }

    private static void saveToFile(@NotNull File file, byte[] unzippedBytes) {
        try (OutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(unzippedBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startProcess(File file) {
        ProcessBuilder processBuilder = createProcessBuilder(file);
        String dirPath = getDirPath(JfrToStacksConverter.class);
        System.out.println(dirPath);
        if (dirPath == null) {
            LOG.error("Cannot find directory of JfrToStacksConverter.class. Please submit the report to https://github.com/kornilova-l/FlameViewer/issues");
            return;
        }
        processBuilder.redirectError(new File("error.txt"));
        processBuilder.directory(new File(dirPath));
        try {
            processBuilder.start().waitFor();
        } catch (InterruptedException | IOException e) {
            LOG.error(e);
        }
    }

    private ProcessBuilder createProcessBuilder(File file) {
        String delimiter = SystemUtils.IS_OS_WINDOWS ? ";" : ":";
        return new ProcessBuilder(
                "java",
                "-Xmx2048m",
                "-cp",
                getPathToJar("com.jrockit.mc.flightrecorder_5.5.1.172852.jar")
                        + delimiter +
                        getPathToJar("com.jrockit.mc.common_5.5.1.172852.jar")
                        + delimiter +
                        getPathToJar("flight-recorder-parser-for-java-9.jar")
                        + delimiter,
                ParseInSeparateProcess.class.getCanonicalName(),
                Paths.get(file.toURI()).toAbsolutePath().toString()
        );
    }

    @Nullable
    private String getPathToJar(@NotNull String jarFileName) {
        String classesDir = getDirPath(JfrToCFlamegraphConverterFactory.class);
        if (classesDir == null) {
            return null;
        }
        String pluginDir = classesDir.substring(0, classesDir.lastIndexOf("classes"));
        return Paths.get(pluginDir, "lib", jarFileName).toString();
    }

    @Nullable
    private String getDirPath(Class<?> myClass) {
        URL fullPathUrl = myClass.getClassLoader().getResource(getResourcePath(myClass.getName()));
        if (fullPathUrl == null) {
            return null;
        }
        String relativePath = getRelativeClassFilePath(myClass.getName());
        try {
            String fullPath = Paths.get(fullPathUrl.toURI()).toString();
            return fullPath.substring(0, fullPath.indexOf(relativePath));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getResourcePath(String qualifiedName) {
        return qualifiedName.replace('.', '/') + ".class";
    }

    /**
     * com.github.kornilova_l.flamegraph.plugin.converters.jmc.FlightRecorderConverterEight ->
     * com/github/kornilova_l/flamegraph/plugin/converters/jmc/FlightRecorderConverterEight.class
     */
    @NotNull
    private String getRelativeClassFilePath(String qualifiedName) {
        String[] nameParts = qualifiedName.split("\\.");
        Path path = Paths.get(nameParts[0]);
        for (int i = 1; i < nameParts.length; i++) {
            path = Paths.get(path.toString(), nameParts[i]);
        }
        return path.toString() + ".class";
    }

    public static byte[] getBytes(File file) {
        try (InputStream inputStream = new FileInputStream(file)) {
            byte[] bytes = new byte[inputStream.available()];
            //noinspection ResultOfMethodCallIgnored
            inputStream.read(bytes);
            return bytes;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    private static byte[] getUnzippedBytes(File file) {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            try (GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream)) {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len = gzipInputStream.read(buffer);
                while (len > 0) {
                    bout.write(buffer, 0, len);
                    len = gzipInputStream.read(buffer);
                }
                return bout.toByteArray();

            } catch (ZipException zip) {
                return getBytes(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }
}
