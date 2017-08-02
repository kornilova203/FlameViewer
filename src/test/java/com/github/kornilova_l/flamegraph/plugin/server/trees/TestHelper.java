package com.github.kornilova_l.flamegraph.plugin.server.trees;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class TestHelper {
    public static void compare(String actual, File res) {
        try (FileInputStream inputStream = new FileInputStream(res)) {
            byte[] data = new byte[(int) res.length()];
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
