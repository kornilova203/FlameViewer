package com.github.kornilova_l.profiler.agent;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ProfilingMethodVisitor extends AdviceAdapter {
    private final static Pattern allParamsPattern = Pattern.compile("(\\(.*\\))");
    private final static Pattern paramsPattern = Pattern.compile("(\\[?)(C|Z|S|I|J|F|D|B|(:?L[^;]+;))");
    private final static Pattern returnTypePattern = Pattern.compile("(?<=\\)).*"); // (?<=\)).*
    private final static String LOGGER_PACKAGE_NAME = "com/github/kornilova_l/profiler/logger/";
    private final String methodName;
    private final String className;
    private final boolean hasSystemCL;

    ProfilingMethodVisitor(int access, String methodName, String desc,
                           MethodVisitor mv, String className, boolean hasSystemCL) {
        super(Opcodes.ASM5, mv, access, methodName, desc);
        this.className = className;
        this.methodName = methodName;
        this.hasSystemCL = hasSystemCL;
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
        getThreadId();
        getTime();
        getClassNameAndMethodName();
        mv.visitLdcInsn(methodDesc);
        getIsStatic();
        getArrayWithParameters();
        addToQueue(Type.Enter);
    }

    private void addToQueue(Type type) {
        String description = null;
        switch (type) {
            case Enter:
                description = "(JJLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Z[Ljava/lang/Object;)V";
                break;
            case Exit:
                description = "(Ljava/lang/Object;JJ)V";
                break;
            case Exception:
                description = "(Ljava/lang/Throwable;JJ)V";
                break;
        }
        if (hasSystemCL) {
            mv.visitMethodInsn(INVOKESTATIC, LOGGER_PACKAGE_NAME + "Logger", "addToQueue",
                    description, false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, LOGGER_PACKAGE_NAME + "Proxy", "addToQueue",
                    description, false);
        }
    }

    private void getArrayWithParameters() {
        String[] parametersDesc = getParamsDesc();
        int arraySize = 0;
        if (parametersDesc != null) {
            arraySize = parametersDesc.length;
        }
        if (arraySize == 0 && isStatic()) { // null instead of Object[]
            loadNull();
        } else {
            if (!isStatic() && !Objects.equals(methodName, "<init>")) {
                arraySize++;
            }
            createObjArray(arraySize);
            int index = 0;
            int posOfFirstParam = 0;
            if (!isStatic()) { // appendThis
                if (!Objects.equals(methodName, "<init>")) {
                    loadThisToArr();
                }
                index = 1;
                posOfFirstParam = 1;
            }
            if (parametersDesc != null) {
                loadParametersToArray(parametersDesc, index, posOfFirstParam);
            }
        }
    }

    private void loadNull() {
        mv.visitInsn(ACONST_NULL);
    }

    private void loadParametersToArray(String[] parametersDesc, int index, int posOfFirstParam) {
        for (String pDesc : parametersDesc) {
            mv.visitInsn(DUP); // array reference
            getIConst(index++); // index of element
            posOfFirstParam = paramToObj(pDesc, posOfFirstParam);
            visitInsn(AASTORE); // load obj to array
        }
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

    private int paramToObj(String paramDesc, int pos) {
        switch (paramDesc) {
            case "I": // int
                mv.visitVarInsn(ILOAD, pos);
                intToObj();
                pos++;
                break;
            case "J": // long
                mv.visitVarInsn(LLOAD, pos);
                longToObj();
                pos += 2;
                break;
            case "Z": // boolean
                mv.visitVarInsn(ILOAD, pos);
                booleanToObj();
                pos++;
                break;
            case "C": // char
                mv.visitVarInsn(ILOAD, pos);
                charToObj();
                pos++;
                break;
            case "S": // short
                mv.visitVarInsn(ILOAD, pos);
                shortToObj();
                pos++;
                break;
            case "B": // byte
                mv.visitVarInsn(ILOAD, pos);
                byteToObj();
                pos++;
                break;
            case "F": // float
                mv.visitVarInsn(FLOAD, pos);
                floatToObj();
                pos++;
                break;
            case "D": // double
                mv.visitVarInsn(DLOAD, pos);
                doubleToObj();
                pos += 2;
                break;
            default: // object
                mv.visitVarInsn(ALOAD, pos);
                pos++;
        }
        return pos;
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

    private void getThreadId() {
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "currentThread",
                "()Ljava/lang/Thread;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Thread", "getId", "()J", false);
    }


    private String[] getParamsDesc() {
        ArrayList<String> paramsDesc = new ArrayList<>();
        String desc = getPartOfDescWithParam();
        Matcher m = paramsPattern.matcher(desc);
        while (m.find()) {
            paramsDesc.add(m.group());
        }
        if (paramsDesc.isEmpty()) {
            return null;
        }
        String[] ret = new String[paramsDesc.size()];
        paramsDesc.toArray(ret);
        return ret;
    }

    private String getPartOfDescWithParam() {
        Matcher m = allParamsPattern.matcher(methodDesc);
        if (!m.find()) {
            throw new IllegalArgumentException("Method signature does not contain parameters");
        }
        return m.group(1);
    }


    private boolean isStatic() {
        return (methodAccess & ACC_STATIC) != 0;
    }

    @Override
    protected void onMethodExit(int opcode) {
        if (opcode == ATHROW) {
            dup();
            getThreadId();
            getTime();
            addToQueue(Type.Exception);
            return;
        }
        if (opcode != RETURN) { // return some value
            int sizeOfRetVal = getSizeOfRetVal(opcode);
            dupRetVal(sizeOfRetVal);
        }
        retValToObj();
        getThreadId();
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
