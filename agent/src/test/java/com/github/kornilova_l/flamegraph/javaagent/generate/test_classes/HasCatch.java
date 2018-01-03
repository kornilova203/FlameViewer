package com.github.kornilova_l.flamegraph.javaagent.generate.test_classes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class HasCatch {
    public static void main(String[] args) {
        try (OutputStream outputStream = new FileOutputStream(new File(""))) {
            outputStream.write(new byte[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
