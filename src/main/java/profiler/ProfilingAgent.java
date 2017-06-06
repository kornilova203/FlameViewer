package profiler;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import static org.objectweb.asm.Opcodes.ACC_STATIC;

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
        if (className.startsWith("org/jetbrains/test")) {
            ClassReader cr = new ClassReader(classfileBuffer);
            // TODO: compute maxs and frames manually
            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
            // uncomment for debugging
            TraceClassVisitor cv = new TraceClassVisitor(cw, new PrintWriter(System.out));
            cr.accept(new AddProfilerClassVisitor(cv), 0);
//            cr.accept(new AddProfilerClassVisitor(cw), 0);

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
        if (mv != null && !name.equals("toString") && !name.equals("main")) {
            mv = new AddProfilerMethodVisitor(access, name, desc, mv);
        }
        return mv;
    }
}

class AddProfilerMethodVisitor extends AdviceAdapter {
    private final int state = newLocal(Type.LONG_TYPE);
    private final static Pattern allParamsPattern = Pattern.compile("(\\(.*\\))");
    private final static Pattern paramsPattern = Pattern.compile("(\\[?)(C|Z|S|I|J|F|D|(:?L[^;]+;))");

    AddProfilerMethodVisitor(int access, String name, String desc,
                             MethodVisitor mv) {
        super(Opcodes.ASM5, mv, access, name, desc);
    }

    @Override
    protected void onMethodEnter() {
//        LDC "desc"
//        INVOKESTATIC profiler/Profiler.methodStart (Ljava/lang/String;)Lprofiler/State;
        System.out.println("onMethodEnter " + methodDesc);
        mv.visitLdcInsn(methodDesc);
        mv.visitMethodInsn(INVOKESTATIC, "profiler/Profiler",
                "methodStart", "(Ljava/lang/String;)Lprofiler/State;", false);
        // TODO: check is it correct to use `LONG_TYPE` for object
        mv.visitVarInsn(ASTORE, state);
        addParamLogging();
    }

    /**
     * Insert instructions for logging all method parameters
     */
    private void addParamLogging() {
        Matcher m = allParamsPattern.matcher(methodDesc);
        if (!m.find()) {
            throw new IllegalArgumentException("Method signature does not contain parameters");
        }
        String paramsDescriptor = m.group(1);
        Matcher mParam = paramsPattern.matcher(paramsDescriptor);

        int pos = 0;
//        if ((methodAccess & ACC_STATIC) == ACC_STATIC) { // if method is static
//            pos++;
//        }
        while (mParam.find()) {
            pos = paramLog(mParam.group(), pos);
        }
    }

    private int paramLog(String type, int pos) {
//        ILOAD 0
//        INVOKESTATIC java/lang/String.valueOf (I)Ljava/lang/String;
//        INVOKESTATIC profiler/Profiler.log (Ljava/lang/String;)V
        if (Objects.equals(type, "I")) {
            visitVarInsn(ILOAD, pos);
            pos++;
        } else if (Objects.equals(type, "J")) {
            visitVarInsn(LLOAD, pos);
            pos += 2;
        } else if (Objects.equals(type, "F")) {
            visitVarInsn(FLOAD, pos);
            pos += 1;
        } else if (Objects.equals(type, "D")) {
            visitVarInsn(DLOAD, pos);
            pos += 2;
        } else { // object
            visitVarInsn(ALOAD, pos);
            pos += 1;
        }
        convertToString(type);
        log();

        return pos;
    }

    private void convertToString(String type) {
        if (Objects.equals(type, "I") ||
                Objects.equals(type, "J") ||
                Objects.equals(type, "F") ||
                Objects.equals(type, "D")) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/String",
                    "valueOf", "(" + type + ")Ljava/lang/String;", false);
        } else { // object
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "toString",
                    "()Ljava/lang/String;", false);
        }
    }

    private void log() {
        mv.visitMethodInsn(INVOKESTATIC, "profiler/Profiler", "log",
                "(Ljava/lang/String;)V", false);
    }

    // opcode:
    // RETURN
    // IRETURN int
    // FRETURN
    // ARETURN object
    // LRETURN long integer
    // DRETURN
    // ATHROW
    @Override
    protected void onMethodExit(int opcode) {
//        ILOAD 0
//        INVOKESTATIC java/lang/String.valueOf (I)Ljava/lang/String;
//        INVOKESTATIC profiler/Profiler.log (Ljava/lang/String;)V
//        ILOAD 0
//        IRETURN
        mv.visitVarInsn(ALOAD, state);
        mv.visitMethodInsn(INVOKEVIRTUAL, "profiler/State", "methodFinish",
                "()V", false);
        if (opcode == RETURN) {
            return;
        }
        if (opcode == IRETURN) {
            dup();
            convertToString("I");
        } else if (opcode == LRETURN) {
            dup2();
            convertToString("J");
        } else if (opcode == FRETURN) {
            dup();
            convertToString("F");
        } else if (opcode == DRETURN) {
            dup2();
            convertToString("D");
        } else if (opcode == ARETURN) {
//            INVOKEVIRTUAL java/lang/Object.toString ()Ljava/lang/String;
//            INVOKESTATIC profiler/Profiler.log (Ljava/lang/String;)V
//            INVOKEVIRTUAL org/jetbrains/test/SimpleExample$TestClass.toString ()Ljava/lang/String;
            dup();
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "toString",
                    "()Ljava/lang/String;", false);
        }
        mv.visitMethodInsn(INVOKESTATIC, "profiler/Profiler", "log",
                "(Ljava/lang/String;)V", false);
    }

//    @Override
//    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
//        System.out.println(name + " " + desc + " " + signature + " " + start);
//        super.visitLocalVariable(name, desc, signature, start, end, index);
//    }
}
