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
        String parameterDesc = getParamDesc();
        boolean isStatic = isStatic();
        if (Objects.equals(parameterDesc, "()") && isStatic) { // if there is no input parameters and it's static (no this)
            mv.visitLdcInsn(methodDesc + "⊗static");
            mv.visitLdcInsn(""); // no input parameters
        } else {
            mv.visitLdcInsn(methodDesc + (isStatic ? "⊗static" : "⊗non-static"));
            createStringBuilder();
            appendParamsToStringBuilder(parameterDesc, isStatic);
            invokeStringBuilderToString();
        }
        mv.visitMethodInsn(INVOKESTATIC, "profiler/Profiler",
                "methodEnter", "(Ljava/lang/String;Ljava/lang/String;)Lprofiler/State;", false);
        // TODO: check is it correct to use `LONG_TYPE` for object
        mv.visitVarInsn(ASTORE, state);
    }

    private void addDelimiter() {
        mv.visitLdcInsn("⇑");
        invokeStringBuilderAppend();
    }

    private void invokeStringBuilderToString() {
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder",
                "toString", "()Ljava/lang/String;", false);
    }

    private String getParamDesc() {
        Matcher m = allParamsPattern.matcher(methodDesc);
        if (!m.find()) {
            throw new IllegalArgumentException("Method signature does not contain parameters");
        }
        return m.group(1);
    }

    private void invokeStringBuilderAppend() {
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder",
                "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
    }

    private void createStringBuilder() {
        mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
        dup();
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
    }

    private boolean isStatic() {
        return (methodAccess & ACC_STATIC) != 0;
    }

    /**
     * Insert instructions for logging all method parameters
     */
    private void appendParamsToStringBuilder(String parametersDesc, boolean isStatic) {
        Matcher mParam = paramsPattern.matcher(parametersDesc);

        int pos = 0;
        if (!isStatic) {
            pos++;
            // TODO: check if it is correct to print `this` in <init>
            appendThisToStringBuilder();
        }
        if (mParam.find()) {
            pos = appendParam(mParam.group(), pos);
        }
        while (mParam.find()) {
            addDelimiter();
            pos = appendParam(mParam.group(), pos);
        }
    }

    private void appendThisToStringBuilder() {
        mv.visitVarInsn(ALOAD, 0);
        invokeToString();
        invokeStringBuilderAppend();
    }

    private int appendParam(String type, int pos) {
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
            invokeStringBuilderAppend();
            return pos + 1;
        } else { // object
            visitVarInsn(ALOAD, pos);
            invokeToString();
            invokeStringBuilderAppend();
            return pos + 1;
        }
        invokeStringValueOf(type);
        invokeStringBuilderAppend();

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

    @Override
    protected void onMethodExit(int opcode) {
        convertReturnValToString(opcode);
        mv.visitMethodInsn(INVOKEVIRTUAL, "profiler/State", "methodFinish",
                "(Ljava/lang/String;)V", false);
    }

    private void convertReturnValToString(int opcode) {
        if (opcode == RETURN) {
            mv.visitVarInsn(ALOAD, state);
            mv.visitLdcInsn(""); // no return param
            return;
        }
        if (opcode == IRETURN) {
            insertStateBeforeSmallRetVal();
            invokeStringValueOf("I");
        } else if (opcode == LRETURN) {
            insertStateBeforeLargeRetVal();
            invokeStringValueOf("J");
        } else if (opcode == FRETURN) {
            insertStateBeforeSmallRetVal();
            invokeStringValueOf("F");
        } else if (opcode == DRETURN) {
            insertStateBeforeLargeRetVal();
            invokeStringValueOf("D");
        } else if (opcode == ARETURN) { // object or array
            insertStateBeforeSmallRetVal();
            aReturnToString();
        } else { // ATHROW
            dup();
            mv.visitVarInsn(ALOAD, state);
            mv.visitInsn(SWAP);
            invokeToString();
        }
    }

    private void insertStateBeforeLargeRetVal() {
        dup2();
        mv.visitVarInsn(ALOAD, state);
        mv.visitInsn(DUP_X2);
        mv.visitInsn(POP);
    }

    private void insertStateBeforeSmallRetVal() {
        dup();
        mv.visitVarInsn(ALOAD, state);
        mv.visitInsn(SWAP);
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
