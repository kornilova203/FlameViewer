package com.github.kornilova_l.profiler;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.regex.Pattern;

import static com.github.kornilova_l.profiler.Configuration.*;

class ProfilingClassFileTransformer implements ClassFileTransformer {

    ProfilingClassFileTransformer(String configFile) {
        super();
        readPatterns(new File(configFile));
    }

    private void readPatterns(File configFile) {
        try (
                InputStream inputStream = new FileInputStream(configFile);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))
        ) {
            String line = reader.readLine();
            while (line != null) {
                addFullNamePattern(line);
                addClassNamePattern(line);
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] transform(ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {
        if (matchesAnyPattern(className, classNamePatterns)) {
            ClassReader cr = new ClassReader(classfileBuffer);
            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
            // uncomment for debugging
            TraceClassVisitor cv = new TraceClassVisitor(cw, new PrintWriter(System.out));
            // SKIP_FRAMES avoids visiting frames that will be ignored and recomputed from scratch in the class writer.
            cr.accept(new ProfilingClassVisitor(cv, className), ClassReader.SKIP_FRAMES);

            return cw.toByteArray();
        }
        return null;
    }
}
