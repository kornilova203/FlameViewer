package com.github.kornilova_l.flamegraph.javaagent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AgentFileManager {
    private static final String DELIMITER = System.getProperty("os.name").startsWith("Windows") ? "\\" : "/";
    private static final int FILE_NAME_LENGTH = 4;
    private final File logDir;

    public AgentFileManager(@NotNull String logDirPath) {
        logDir = new File(logDirPath);
        assert logDir.exists() && logDir.isDirectory();
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

    public File createLogFile() {
        int max = getLargestFileNum();
        return new File(logDir.getAbsolutePath() + DELIMITER + intToString(max + 1) + ".ser");
    }

    @Nullable
    private File getLatestFile() {
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
}
