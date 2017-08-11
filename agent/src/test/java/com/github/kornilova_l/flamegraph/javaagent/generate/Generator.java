package com.github.kornilova_l.flamegraph.javaagent.generate;

import com.github.kornilova_l.flamegraph.javaagent.TestHelper;
import com.github.kornilova_l.flamegraph.javaagent.generate.test_classes.HasCatch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.*;

/**
 * Get bytecode from source with inserted calls to LoggerQueue and
 * try-catch block.
 * Get readable representation and save to file
 */
public class Generator {
    private static final Class testedClass = HasCatch.class;

    public static void main(String[] args) {
        TestHelper.createDir("expected");
        String fullName = testedClass.getName();
        InputStream inputStream =
                Generator.class.getResourceAsStream(
                        "/" + fullName.replace('.', '/') + ".class");
        try {
            // recompute frames to get result similar to ProfilerClassVisitor
            byte[] bytes = TestHelper.getBytes(inputStream);
            ClassReader cr = new ClassReader(bytes);
            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
            cr.accept(
                    new TraceClassVisitor(cw, new PrintWriter(System.out)),
                    ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG
            );

            bytes = cw.toByteArray();
            cr = new ClassReader(bytes);
            cw = new ClassWriter(cr, 0);
            OutputStream outputStream = new FileOutputStream(
                    new File("agent/src/test/resources/expected/" +
                            TestHelper.removePackage(fullName) +
                            ".txt"));
            cr.accept(
                    new TraceClassVisitor(cw, new PrintWriter(outputStream)),
                    0
            );


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
