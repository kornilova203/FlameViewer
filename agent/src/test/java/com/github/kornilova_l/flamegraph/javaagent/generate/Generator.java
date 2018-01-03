package com.github.kornilova_l.flamegraph.javaagent.generate;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.*;
import java.nio.file.Paths;

import static com.github.kornilova_l.flamegraph.javaagent.TestHelperKt.*;

/**
 * Get bytecode from source with inserted calls to LoggerQueue and
 * try-catch block.
 * Get readable representation and save to file
 */
public class Generator {
    public static File generate(Class<?> testedClass) {
        createDir("expected");
        String fullName = testedClass.getName();
        // recompute frames to get result similar to ProfilerClassVisitor
        byte[] bytes = getBytes(testedClass);
        ClassReader cr = new ClassReader(bytes);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
        cr.accept(
                cw,
                ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG
        );

        cr = new ClassReader(bytes);
        File outputFile = new File("src/test/resources/expected/" +
                removePackage(fullName) +
                ".txt");
        outputFile = Paths.get(outputFile.toURI()).toAbsolutePath().toFile();
        try {
            OutputStream outputStream = new FileOutputStream(outputFile);
            cr.accept(
                    new TraceClassVisitor(null, new PrintWriter(outputStream)),
                    ClassReader.SKIP_DEBUG
            );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return outputFile;
    }
}
