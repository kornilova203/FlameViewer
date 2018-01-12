package com.github.kornilova_l.flamegraph.plugin.server.trees;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class TestHelper {
    public static void compare(String actual, File expected) {
        try (FileInputStream inputStream = new FileInputStream(expected)) {
            byte[] data = new byte[(int) expected.length()];
            //noinspection ResultOfMethodCallIgnored
            inputStream.read(data);
            String result = new String(data, "UTF-8");
            assertEquals(result,
                    actual);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}