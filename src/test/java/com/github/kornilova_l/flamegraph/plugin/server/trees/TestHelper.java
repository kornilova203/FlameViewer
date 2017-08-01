package com.github.kornilova_l.flamegraph.plugin.server.trees;

import com.github.kornilova_l.flamegraph.proto.TreeProtos;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class TestHelper {
    public static void compare(TreeProtos.Tree tree, File res) {
        try (FileInputStream inputStream = new FileInputStream(res)) {
            byte[] data = new byte[(int) res.length()];
            //noinspection ResultOfMethodCallIgnored
            inputStream.read(data);
            String result = new String(data, "UTF-8");
            assertEquals(result,
                    tree.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
