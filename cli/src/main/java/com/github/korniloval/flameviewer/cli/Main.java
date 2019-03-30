package com.github.korniloval.flameviewer.cli;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Specify path to file");
            return;
        }
        File file = new File(args[0]);
        if (!file.exists()) {
            System.out.println("File " + file + " is not found");
            return;
        }
        try {
            HttpServer.start(file);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
