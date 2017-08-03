package com.github.kornilova_l.flamegraph.javaagent.agent;

import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AdviceAdapter;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ProfilingMethodVisitor extends AdviceAdapter {
    private final static Pattern returnTypePattern = Pattern.compile("(?<=\\)).*"); // (?<=\)).*
    private final static String LOGGER_PACKAGE_NAME = "com/github/kornilova_l/flamegraph/javaagent/logger/";
    private final String methodName;
    private final String className;
    private final boolean hasSystemCL;
    private final MethodConfig methodConfig;

    ProfilingMethodVisitor(int access, String methodName, String desc,
                           MethodVisitor mv, String className, boolean hasSystemCL, MethodConfig methodConfig) {
        super(ASM5, mv, access, methodName, desc);
        this.className = className;
        this.methodName = methodName;
        this.hasSystemCL = hasSystemCL;
        this.methodConfig = methodConfig;
    }

    private static int getSizeOfRetVal(int opcode) {
        if (opcode == LRETURN || // long
                opcode == DRETURN) { // double
            return 2;
        }
        return 1;
    }

    @Override
    protected void onMethodEnter() {
        getThread();
        getTime();
        getClassNameAndMethodName();
        mv.visitLdcInsn(methodDesc);
        getIsStatic();
        int countEnabledParams = (int) methodConfig.getParameters().stream()
                .filter((MethodConfig.Parameter::isEnabled))
                .count();
        if (countEnabledParams > 0) { // if at least one parameter is enabled
            getArrayWithParameters(countEnabledParams);
        } else {
            loadNull();
        }
        addToQueue(Type.Enter);
    }

    private void addToQueue(Type type) {
        String description = null;
        switch (type) {
            case Enter:
                description = "(Ljava/lang/Thread;JLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Z[Ljava/lang/Object;)V";
                break;
            case Exit:
                description = "(Ljava/lang/Object;Ljava/lang/Thread;J)V";
                break;
            case Exception:
                description = "(Ljava/lang/Thread;Ljava/lang/Throwable;J)V";
                break;
        }
        if (hasSystemCL) {
            mv.visitMethodInsn(INVOKESTATIC, LOGGER_PACKAGE_NAME + "LoggerQueue", "addToQueue",
                    description, false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, LOGGER_PACKAGE_NAME + "Proxy", "addToQueue",
                    description, false);
        }
    }

    private void getArrayWithParameters(int arraySize) {
        createObjArray(arraySize);
        int posOfParam = 0;
        if (!isStatic()) {
            posOfParam = 1;
        }
        int index = 0;
        int countParams = methodConfig.getParameters().size();
        List<String> jvmParameters = MethodConfig.splitDesc(
                methodDesc.substring(methodDesc.indexOf("(") + 1, methodDesc.indexOf(")"))
        );
        for (int i = 0; i < countParams; i++) {
            if (methodConfig.getParameters().get(i).isEnabled()) {
                mv.visitInsn(DUP); // array reference
                getIConst(index++); // index of element
                paramToObj(jvmParameters.get(i), posOfParam);
                visitInsn(AASTORE); // load obj to array
            }
            posOfParam = getObjSize(jvmParameters.get(i));
        }
    }

    private void loadNull() {
        mv.visitInsn(ACONST_NULL);
    }

    private void createObjArray(int arraySize) {
        getIConst(arraySize);
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
    }

    private void loadThisToArr() {
        mv.visitInsn(DUP); // array reference
        getIConst(0); // index of element
        visitVarInsn(ALOAD, 0);
        visitInsn(AASTORE); // load obj to array
    }

    private void paramToObj(String paramDesc, int pos) {
        switch (paramDesc) {
            case "I": // int
                mv.visitVarInsn(ILOAD, pos);
                intToObj();
                break;
            case "J": // long
                mv.visitVarInsn(LLOAD, pos);
                longToObj();
                break;
            case "Z": // boolean
                mv.visitVarInsn(ILOAD, pos);
                booleanToObj();
                break;
            case "C": // char
                mv.visitVarInsn(ILOAD, pos);
                charToObj();
                break;
            case "S": // short
                mv.visitVarInsn(ILOAD, pos);
                shortToObj();
                break;
            case "B": // byte
                mv.visitVarInsn(ILOAD, pos);
                byteToObj();
                break;
            case "F": // float
                mv.visitVarInsn(FLOAD, pos);
                floatToObj();
                break;
            case "D": // double
                mv.visitVarInsn(DLOAD, pos);
                doubleToObj();
                break;
            default: // object
                mv.visitVarInsn(ALOAD, pos);
        }
    }

    private static int getObjSize(String paramDesc) {
        switch (paramDesc) {
            case "Z": // boolean
            case "I": // int
            case "C": // char
            case "S": // short
            case "B": // byte
            case "F": // float
                return 1;
            case "J": // long
            case "D": // double
                return 2;
            default: // object
                return 1;
        }
    }

    private void doubleToObj() {
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf",
                "(D)Ljava/lang/Double;", false);
    }

    private void floatToObj() {
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf",
                "(F)Ljava/lang/Float;", false);
    }

    private void byteToObj() {
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf",
                "(B)Ljava/lang/Byte;", false);
    }

    private void shortToObj() {
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf",
                "(S)Ljava/lang/Short;", false);
    }

    private void charToObj() {
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf",
                "(C)Ljava/lang/Character;", false);
    }

    private void booleanToObj() {
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf",
                "(Z)Ljava/lang/Boolean;", false);
    }

    private void longToObj() {
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf",
                "(J)Ljava/lang/Long;", false);
    }

    private void intToObj() {
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf",
                "(I)Ljava/lang/Integer;", false);
    }

    private void getIConst(int i) {
        if (i < 6) {
            mv.visitInsn(ICONST_0 + i);
        } else {
            mv.visitIntInsn(BIPUSH, i);
        }
    }

    private void getIsStatic() {
        if (isStatic()) {
            mv.visitInsn(ICONST_1);
        } else {
            mv.visitInsn(ICONST_0);
        }
    }

    private void getClassNameAndMethodName() {
        mv.visitLdcInsn(className);
        mv.visitLdcInsn(methodName);
    }

    private void getTime() {
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System",
                "currentTimeMillis", "()J", false);
    }

    private void getThread() {
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "currentThread",
                "()Ljava/lang/Thread;", false);
    }

    private boolean isStatic() {
        return (methodAccess & ACC_STATIC) != 0;
    }

    @Override
    protected void onMethodExit(int opcode) {
        if (opcode == ATHROW) {
            dup();
            getThread();
            getTime();
            addToQueue(Type.Exception);
            return;
        }
        if (opcode != RETURN) { // return some value
            int sizeOfRetVal = getSizeOfRetVal(opcode);
            dupRetVal(sizeOfRetVal);
        }
        retValToObj(); // load throwable
        getThread();
        getTime();
        addToQueue(Type.Exit);
    }

    private void retValToObj() {
        Matcher m = returnTypePattern.matcher(methodDesc);
        if (!m.find()) {
            throw new IllegalArgumentException("Description does not have return value");
        }
        String retType = m.group();
        if (Objects.equals(retType, "V")) {
            loadNull();
        } else {
            convertToObj(retType);
        }
    }

    private void convertToObj(String type) {
        switch (type) {
            case "I": // int
                intToObj();
                break;
            case "J": // long
                longToObj();
                break;
            case "Z": // boolean
                booleanToObj();
                break;
            case "C": // char
                charToObj();
                break;
            case "S": // short
                shortToObj();
                break;
            case "B": // byte
                byteToObj();
                break;
            case "F": // float
                floatToObj();
                break;
            case "D": // double
                doubleToObj();
                break;
        }
    }

    private void dupRetVal(int sizeOfRetVal) {
        if (sizeOfRetVal == 1) {
            dup();
        } else {
            dup2();
        }

    }

    private enum Type {
        Exit,
        Enter,
        Exception
    }
}
