package profiler;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ProfilingMethodVisitor extends AdviceAdapter {
    private final int state = newLocal(Type.LONG_TYPE);
    private final static Pattern allParamsPattern = Pattern.compile("(\\(.*\\))");
    private final static Pattern paramsPattern = Pattern.compile("(\\[?)(C|Z|S|I|J|F|D|B|(:?L[^;]+;))");
    private final static Pattern returnType = Pattern.compile("(?<=\\)).*"); // (?<=\)).*
    private final static Pattern baseTypes = Pattern.compile("([CZSIJFDB])");

    ProfilingMethodVisitor(int access, String name, String desc,
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
            // TODO: check if it is correct to print `this` in <init>
            logThis();
        }
        while (mParam.find()) {
            pos = logParam(mParam.group(), pos);
        }
    }

    private void logThis() {
        mv.visitVarInsn(ALOAD, 0);
        invokeToString();
        log();
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
                    "toString", "([Ljava/lang/Object;)Ljava/lang/String;", false);
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
