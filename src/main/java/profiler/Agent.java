package profiler;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.objectweb.asm.*;
import org.objectweb.asm.util.TraceClassVisitor;

public class Agent {

    public static final ConcurrentLinkedQueue<EventData> queue = new ConcurrentLinkedQueue<>();

    public static void premain(String args, Instrumentation inst) throws IOException {
        Thread logger = new Thread(new Logger(), "logging thread");
        logger.setDaemon(true);
        logger.start();

        Runtime.getRuntime().addShutdownHook( new WaitingLoggingToFinish("shutdown-hook"));
//        inst.addTransformer(new ProfilingClassFileTransformer());
    }
}

class ProfilingClassFileTransformer implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {
        if (className.startsWith("samples") && !className.startsWith("samples/Blackhole")) {
            ClassReader cr = new ClassReader(classfileBuffer);
            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
            // uncomment for debugging
            TraceClassVisitor cv = new TraceClassVisitor(cw, new PrintWriter(System.out));
            // SKIP_FRAMES avoids visiting frames that will be ignored and recomputed from scratch in the class writer.
            cr.accept(new ProfilingClassVisitor(cv), ClassReader.SKIP_FRAMES);

            return cw.toByteArray();
        }
        return null;
    }
}
