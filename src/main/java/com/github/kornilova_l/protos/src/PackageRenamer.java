package com.github.kornilova_l.protos.src;

import java.io.*;

public class PackageRenamer {
    private static final String srcDirPath = "src/main/java/com/github/kornilova_l/protos/src";
    private static final String outputDirPath = "src/main/java/com/github/kornilova_l/protos/";


    public static void main(String[] args) throws FileNotFoundException {

        File srcDir = new File(srcDirPath);
        if (!srcDir.exists() || !srcDir.isDirectory()) {
            throw new AssertionError("invalid src directory");
        }
        processFiles(srcDir.listFiles());
    }

    private static void processFiles(File[] files) throws FileNotFoundException {
        if (files == null) {
            throw new IllegalArgumentException("Directory is empty");
        }
        for (File file : files) {
            if (!file.getName().endsWith(".java") ||
                    file.getName().startsWith("PackageRenamer")) {
                continue;
            }
            try (
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(
                                    new FileInputStream(file)
                            )
                    );
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(
                                    new FileOutputStream(
                                            new File(outputDirPath + file.getName())
                                    )
                            )
                    )
            ) {
                String line = reader.readLine();
                while (line != null) {
                    writer.write(line
                            .replace("com.github.kornilova_l.protos.src", "com.github.kornilova_l.protos")
                            .replace("com.google.protobuf", "com.github.kornilova_l.libs.com.google.protobuf") +
                            "\n");
                    line = reader.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
