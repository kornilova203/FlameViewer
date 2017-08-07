package com.github.kornilova_l.flamegraph.javaagent;

import org.jetbrains.annotations.NotNull;

import java.io.*;

import static org.junit.Assert.assertEquals;

public class TestHelper {
    @NotNull
    public static byte[] getBytes(InputStream inputStream) {
        try {
            byte[] bytes = new byte[inputStream.available()];
            int res = inputStream.read(bytes);
            assert res != -1;
            return bytes;
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("File was not open");
    }

    @NotNull
    private static String getData(File file) {
        try {
            return new String(getBytes(
                    new FileInputStream(file)
            ), "UTF-8");
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("File was not open");
    }

    public static void compareFiles(File expected, File actual) {
        assertEquals(getData(expected), getData(actual));
    }

    public static void createDir(String name) {
        File outFile = new File("src/test/resources/" + name);
        if (!outFile.exists()) {
            boolean res = outFile.mkdir();
            assert res;
        }
    }

    public static String removePackage(String fullName) {
        int dot = fullName.lastIndexOf('.');
        if (dot == -1) {
            return fullName;
        }
        return fullName.substring(dot + 1, fullName.length());
    }
}
