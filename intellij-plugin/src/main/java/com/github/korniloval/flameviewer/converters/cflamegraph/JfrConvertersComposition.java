package com.github.korniloval.flameviewer.converters.cflamegraph;

import com.github.kornilova_l.flight_parser.FlightParser;
import com.github.korniloval.flameviewer.FlameIndicator;
import com.github.korniloval.flameviewer.FlameLogger;
import com.github.korniloval.flameviewer.LoggerAdapter;
import com.github.korniloval.flameviewer.converters.ConversionException;
import com.github.korniloval.flameviewer.converters.Converter;
import com.github.korniloval.flameviewer.converters.calltraces.StacksParser;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.SimpleJavaParameters;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.projectRoots.SimpleJavaSdkType;
import com.intellij.util.PathUtil;
import com.intellij.util.PathsList;
import com.intellij.util.SystemProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;

import static com.github.korniloval.flameviewer.converters.ConvertersUtilKt.getBytes;

/**
 * @author Liudmila Kornilova
 **/
class JfrConvertersComposition implements Converter<CFlamegraph> {
    private static final FlameLogger logger = new LoggerAdapter(Logger.getInstance(JfrConvertersComposition.class));
    @SuppressWarnings("FieldCanBeLocal")
    private int allowedSize = 20000000; // 20MB
    private final File file;

    JfrConvertersComposition(@NotNull File file) {
        this.file = file;
    }

    @NotNull
    @Override
    public CFlamegraph convert(@Nullable FlameIndicator indicator) throws ConversionException {
        byte[] unzippedBytes = getUnzippedBytes(file);
        if (unzippedBytes == null) {
            throw new ConversionException("Failed to extract data from jfr file: " + file);
        }
        File unzippedFile = getFileNear(file);
        saveToFile(unzippedFile, unzippedBytes);
        if (unzippedBytes.length <= allowedSize) {
            Map<String, Integer> stacks = new JfrToStacksConverter(file).convert(indicator);
            return new StacksToCFlamegraphConverter(stacks).convert(indicator);
        }
        logger.info("File size is bigger than " + (allowedSize / 1000 * 1000) + "MB. It will be converted in separate process to avoid OutOfMemoryException. File: " + file);
        parseInSeparateProcess(unzippedFile);
        Map<String, Integer> stacks = StacksParser.getStacks(unzippedFile, indicator);
        if (stacks == null) throw new ConversionException("Failed to parse stacks from file " + unzippedFile);
        boolean isDeleted = unzippedFile.delete();
        if (!isDeleted) logger.warn("File " + unzippedFile + " was not deleted", null);
        return new StacksToCFlamegraphConverter(stacks).convert(indicator);
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

    private void parseInSeparateProcess(File file) throws ConversionException {
        try {
            Process process = createProcess(file);
            process.waitFor();
            try (InputStream errorStream = process.getErrorStream();
                 InputStream stream = process.getInputStream()) {
                String errors = readStream(errorStream);
                if (!errors.isEmpty()) {
                    throw new ConversionException("Remote process terminated with errors: " + errors);
                }
                String output = readStream(stream);
                if (!output.equals("OK")) {
                    throw new ConversionException("Remote process did not succeed. Output: " + output);
                }
            }
        } catch (InterruptedException | IOException | ExecutionException e) {
            throw new ConversionException("Failed to parse jfr file in remote process", e);
        }
    }

    @NotNull
    private String readStream(InputStream stream) {
        Scanner scanner = new Scanner(stream);
        List<String> lines = new ArrayList<>();
        while (scanner.hasNextLine()) {
            lines.add(scanner.nextLine());
        }
        return String.join("\n", lines);
    }

    private Process createProcess(File file) throws ExecutionException {
        SimpleJavaParameters parameters = new SimpleJavaParameters();
        parameters.setJdk(new SimpleJavaSdkType().createJdk("FlameViewerJdk", SystemProperties.getJavaHome()));
        parameters.setWorkingDirectory(PathManager.getBinPath());
        PathsList cp = parameters.getClassPath();
        cp.add(PathUtil.getJarPathForClass(FlightParser.class));
        cp.add(PathUtil.getJarPathForClass(JfrToStacksConverter.class));
        cp.add(PathUtil.getJarPathForClass(RemoteJfrToStacksConverterServer.class));
        parameters.setMainClass(RemoteJfrToStacksConverterServer.class.getCanonicalName());
        parameters.getProgramParametersList().add(file.toPath().toAbsolutePath().toString());
        parameters.getVMParametersList().add("-Xmx2048m");

        GeneralCommandLine cl = parameters.toCommandLine();
        cl.setRedirectErrorStream(true);
        return cl.createProcess();
    }

    @Nullable
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
                return getBytes(file, logger);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
