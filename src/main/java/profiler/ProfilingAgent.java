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
    private final static Pattern paramsPattern = Pattern.compile("(\\[?)(C|Z|S|I|J|F|D|B|(:?L[^;]+;))");
    private final static Pattern returnType = Pattern.compile("(?<=\\)).*"); // (?<=\)).*
    private final static Pattern baseTypes = Pattern.compile("([CZSIJFDB])");

    AddProfilerMethodVisitor(int access, String name, String desc,
                             MethodVisitor mv) {
        super(Opcodes.ASM5, mv, access, name, desc);
    }

    @Override
    protected void onMethodEnter() {
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
        if ((methodAccess & ACC_STATIC) == 0) { // if method is not static
            pos++;
            logThis();
        }
        while (mParam.find()) {
            pos = logParam(mParam.group(), pos);
        }
    }

    private void logThis() {

    }

    private int logParam(String type, int pos) {
        if (isI(type)) { // if I S B C Z
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
        } else if (type.startsWith("[")) { // array
            visitVarInsn(ALOAD, pos);
            invokeArraysToString(type);
            log();
            return pos + 1;
        } else { // object
            visitVarInsn(ALOAD, pos);
            invokeToString();
            log();
            return pos + 1;
        }
        invokeStringValueOf(type);
        log();

        return pos;
    }

    /**
     * Inserts method Arrays.toString(..)
     * @param type type of parameter (starts with '[')
     * INVOKESTATIC java/util/Arrays.toString ([I)Ljava/lang/String;
     */
    private void invokeArraysToString(String type) {
        if (baseTypes.matcher(type.substring(1)).matches()) { // if it is base type
            mv.visitMethodInsn(INVOKESTATIC, "java/util/Arrays",
                    "toString", "(" + type + ")Ljava/lang/String;", false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, "java/util/Arrays",
                    "toString", "([L)Ljava/lang/String;", false);
        }

    }

    private boolean isI(String type) {
        return Objects.equals(type, "I") ||
                Objects.equals(type, "Z") || // boolean
                Objects.equals(type, "C") ||
                Objects.equals(type, "B") || // byte
                Objects.equals(type, "S"); // short
    }

    private void invokeStringValueOf(String type) {
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/String",
                "valueOf", "(" + type + ")Ljava/lang/String;", false);
    }

    private void invokeToString() {
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "toString",
                "()Ljava/lang/String;", false);
    }

    private void log() {
        mv.visitMethodInsn(INVOKESTATIC, "profiler/Profiler", "log",
                "(Ljava/lang/String;)V", false);
    }

    @Override
    protected void onMethodExit(int opcode) {
        mv.visitVarInsn(ALOAD, state);
        mv.visitMethodInsn(INVOKEVIRTUAL, "profiler/State", "methodFinish",
                "()V", false);
        logReturnValue(opcode);
    }

    private void logReturnValue(int opcode) {
        if (opcode == RETURN) {
            return;
        }
        if (opcode == IRETURN) {
            dup();
            invokeStringValueOf("I");
        } else if (opcode == LRETURN) {
            dup2();
            invokeStringValueOf("J");
        } else if (opcode == FRETURN) {
            dup();
            invokeStringValueOf("F");
        } else if (opcode == DRETURN) {
            dup2();
            invokeStringValueOf("D");
        } else if (opcode == ARETURN) { // object or array
            dup();
            aReturnToString();
        } else { // ATHROW

        }
        log();
    }

    private void aReturnToString() {
        Matcher matcher = returnType.matcher(methodDesc);
        if (!matcher.find()) { //
            throw new IllegalArgumentException("Method signature does not contain return value");
        }
        if (matcher.group().startsWith("[")) { // if it is an array
            invokeArraysToString(matcher.group());
        } else { // object
            invokeToString();
        }
    }
}
