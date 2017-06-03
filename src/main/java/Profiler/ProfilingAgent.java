package Profiler;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Objects;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.LocalVariablesSorter;
import org.objectweb.asm.util.TraceClassVisitor;

import static org.objectweb.asm.Opcodes.*;

public class ProfilingAgent implements ClassFileTransformer {
    public static void premain(String args, Instrumentation inst) throws IOException {
        FileWriter fileWriter = new FileWriter("out.txt");
        fileWriter.close();
        inst.addTransformer(new ProfilingAgent());
    }

    @Override
    public byte[] transform(ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {
        if (className.startsWith("org/jetbrains/test")) {
            ClassReader cr = new ClassReader(classfileBuffer);
            // TODO: compute maxs and frames manually
            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
            TraceClassVisitor cv = new TraceClassVisitor(cw, new PrintWriter(System.out));
            cr.accept(new AddProfilerClassVisitor(cv), 0);

            return cw.toByteArray();
        }
        return null;
    }
}

class AddProfilerClassVisitor extends ClassVisitor {
    AddProfilerClassVisitor(ClassVisitor cv) {
        super(Opcodes.ASM5, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        if (mv != null && !Objects.equals(name, "<init>")) {
            mv = new AddProfilerMethodVisitor(access, desc, mv);
        }
        return mv;
    }
}

class AddProfilerMethodVisitor extends LocalVariablesSorter {
    private int state;

    public AddProfilerMethodVisitor(int access, String desc,
                                    MethodVisitor mv) {
        super(Opcodes.ASM5, access, desc, mv);
    }

    @Override
    public void visitCode() {
        super.visitCode(); // TODO: check if `visitCode` is necessary here
        mv.visitMethodInsn(INVOKESTATIC, "Profiler/Profiler",
                "methodStart", "()LProfiler/State;", false);
        // TODO: check is it correct to use `LONG_TYPE` for object
        state = newLocal(Type.LONG_TYPE);
        mv.visitVarInsn(ASTORE, state);
    }

    @Override
    public void visitInsn(int opcode) {
        if ((opcode >= IRETURN && opcode <= RETURN) || opcode == ATHROW) {
            mv.visitVarInsn(ALOAD, state);
            mv.visitMethodInsn(INVOKEVIRTUAL, "Profiler/State", "methodFinish", "()V", false);
        }
        mv.visitInsn(opcode);
    }
}
