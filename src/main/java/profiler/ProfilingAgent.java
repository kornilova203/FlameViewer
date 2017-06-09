package profiler;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import org.objectweb.asm.*;
import org.objectweb.asm.util.TraceClassVisitor;

// TODO: add insertion of try-finally block
public class ProfilingAgent implements ClassFileTransformer {
    public static void premain(String args, Instrumentation inst) throws IOException {
        FileWriter fileWriter = new FileWriter("out/out.txt");
        fileWriter.close();
        inst.addTransformer(new ProfilingAgent());
    }

    @Override
    public byte[] transform(ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {
        if (className.startsWith("samples")) {
            ClassReader cr = new ClassReader(classfileBuffer);
            // TODO: compute maxs and frames manually
            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
            // uncomment for debugging
            TraceClassVisitor cv = new TraceClassVisitor(cw, new PrintWriter(System.out));
            cr.accept(new ProfilingClassVisitor(cv), 0);
//            cr.accept(new ProfilingClassVisitor(cw), 0);

            return cw.toByteArray();
        }
        return null;
    }
}
