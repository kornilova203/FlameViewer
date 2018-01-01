package com.github.kornilova_l.flamegraph.javaagent.agent;

import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AdviceAdapter;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ProfilingMethodVisitor extends AdviceAdapter {
    private final static Pattern returnTypePattern = Pattern.compile("(?<=\\)).*"); // (?<=\)).*
    private final static String LOGGER_PACKAGE_NAME = "com/github/kornilova_l/flamegraph/javaagent/logger/";
    private final static String PROXY_PACKAGE_NAME = "com/github/kornilova_l/flamegraph/proxy/";
    private final static String START_DATA_CLASS = PROXY_PACKAGE_NAME + "StartData";
    private final static String START_DATA_TYPE = "L" + START_DATA_CLASS + ";";
    final String methodName;
    final String className;
    private final boolean hasSystemCL;
    private final MethodConfig methodConfig;
    int startDataLocal;
    private final Label start = new Label();
    private final Label endOfTryCatch = new Label();
    final boolean saveReturnValue;
    final String savedParameters;


    ProfilingMethodVisitor(int access, String methodName, String desc,
                           MethodVisitor mv, String className, boolean hasSystemCL, MethodConfig methodConfig) {
        super(ASM5, mv, access, methodName, desc);
        this.className = className;
        this.methodName = methodName;
        this.hasSystemCL = hasSystemCL;
        this.methodConfig = methodConfig;
        this.savedParameters = getSavedParameters();
        saveReturnValue = methodConfig.isSaveReturnValue();
    }

    private String getSavedParameters() {
        List<Integer> indexes = new LinkedList<>();
        List<MethodConfig.Parameter> parameters = methodConfig.getParameters();
        int length = parameters.size();
        for (int i = 0; i < length; i++) {
            if (parameters.get(i).isEnabled()) {
                indexes.add(i + 1);
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        length = indexes.size();
        for (int i = 0; i < length; i++) {
            stringBuilder.append(indexes.get(i));
            if (i < length - 1) {
                stringBuilder.append(",");
            }
        }
        return stringBuilder.toString();
    }

    private static int getSizeOfRetVal(int opcode) {
        if (opcode == LRETURN || // long
                opcode == DRETURN) { // double
            return 2;
        }
        return 1;
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

    @Override
    protected void onMethodEnter() {
        createStartData();
        saveStartData();
        mv.visitLabel(start); // try-catch beginning
    }

    private void saveStartData() {
        startDataLocal = newLocal(org.objectweb.asm.Type.getType(START_DATA_TYPE));
        mv.visitVarInsn(ASTORE, startDataLocal);
    }

    /**
     * Leaves object on stack
     */
    protected void createStartData() {
        getTime();
        int countEnabledParams = 0;
        for (MethodConfig.Parameter parameter : methodConfig.getParameters()) {
            if (parameter.isEnabled()) {
                countEnabledParams++;
            }
        }
        if (countEnabledParams > 0) { // if at least one parameter is enabled
            getArrayWithParameters(countEnabledParams);
        } else {
            loadNull();
        }
        if (hasSystemCL) {
            mv.visitMethodInsn(INVOKESTATIC, LOGGER_PACKAGE_NAME + "LoggerQueue",
                    "createStartData",
                    "(J[Ljava/lang/Object;)" + START_DATA_TYPE,
                    false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, PROXY_PACKAGE_NAME + "Proxy",
                    "createStartData",
                    "(J[Ljava/lang/Object;)" + START_DATA_TYPE,
                    false);
        }
    }

    private void endTryCatch() {
        Label handler = new Label();
        mv.visitTryCatchBlock(start, endOfTryCatch, handler, "java/lang/Throwable");

        mv.visitLabel(endOfTryCatch); // it goes right after RETURN instruction.
                                      // So code after it is executed if exception is thrown
        mv.visitLabel(handler);

        getIfWasThrownByMethod();
        Label athrowLabel = new Label(); // label before ATHROW instruction
        mv.visitJumpInsn(IFNE, athrowLabel); // if value on stack is not zero == if was thrown by method go to ATHROW
        prepareAndAddThrowableToQueue(athrowLabel); // this is executed if value was NOT thrown by current method

        mv.visitLabel(athrowLabel);
        mv.visitInsn(ATHROW);
    }

    void prepareAndAddThrowableToQueue(Label athrowLabel) {
        saveExitTime();
        getIfTimeIsMoreOneMs();
        mv.visitJumpInsn(IFLE, athrowLabel); // if method took < 1ms
        throwableAddToQueue();
    }

    /**
     * Adds boolean value to stack.
     * The value indicates if the throwable was thrown by method itself
     */
    void getIfWasThrownByMethod() {
        getStartData();
        mv.visitMethodInsn(
                INVOKEVIRTUAL,
                START_DATA_CLASS,
                "isThrownByMethod",
                "()Z",
                false
        );
    }

    private void addToQueue(Type type) {
        String description = null;
        switch (type) {
            case RetVal:
                description = "(Ljava/lang/Object;JJ[Ljava/lang/Object;Ljava/lang/Thread;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZLjava/lang/String;)V";
                break;
            case Throwable:
                description = "(Ljava/lang/Throwable;ZJJ[Ljava/lang/Object;Ljava/lang/Thread;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZLjava/lang/String;)V";
                break;
        }
        if (hasSystemCL) {
            mv.visitMethodInsn(INVOKESTATIC, LOGGER_PACKAGE_NAME + "LoggerQueue", "addToQueue",
                    description, false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, PROXY_PACKAGE_NAME + "Proxy", "addToQueue",
                    description, false);
        }
    }

    private void getArrayWithParameters(int arraySize) {
        createObjArray(arraySize);
        int posOfParam = 0;
        if (!isStatic()) {
            posOfParam = 1;
        }
        int index = 0; // index of parameter in array
        int countParams = methodConfig.getParameters().size();
        List<String> jvmParameters = MethodConfig.splitDesc(
                methodDesc.substring(methodDesc.indexOf("(") + 1, methodDesc.indexOf(")"))
        );
        for (int i = 0; i < countParams; i++) {
            if (methodConfig.getParameters().get(i).isEnabled()) {
                mv.visitInsn(DUP); // array reference
                getIConst(index++); // index of element
                paramToObj(jvmParameters.get(i), posOfParam);
                mv.visitInsn(AASTORE); // load obj to array
            }
            posOfParam += getObjSize(jvmParameters.get(i));
        }
    }

    void loadNull() {
        mv.visitInsn(ACONST_NULL);
    }

    private void createObjArray(int arraySize) {
        getIConst(arraySize);
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
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

    void booleanToObj() {
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf",
                "(Z)Ljava/lang/Boolean;", false);
    }

    void longToObj() {
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf",
                "(J)Ljava/lang/Long;", false);
    }

    private void intToObj() {
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf",
                "(I)Ljava/lang/Integer;", false);
    }

    void getIConst(int i) {
        if (i < 6) {
            mv.visitInsn(ICONST_0 + i);
        } else {
            mv.visitIntInsn(BIPUSH, i);
        }
    }

    private void getIsStatic() {
        if (isStatic()) {
            loadTrue();
        } else {
            loadFalse();
        }
    }

    private void loadFalse() {
        mv.visitInsn(ICONST_0);
    }

    private void loadTrue() {
        mv.visitInsn(ICONST_1);
    }

    void getTime() {
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
    }

    void getThread() {
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "currentThread",
                "()Ljava/lang/Thread;", false);
    }

    boolean isStatic() {
        return (methodAccess & ACC_STATIC) != 0;
    }

    @Override
    protected void onMethodExit(int opcode) {
        saveExitTime();
        if (opcode == ATHROW) {
            setThrownByMethod(); // ignore this throwable in catch block
        }
        getIfTimeIsMoreOneMs();

        Label endOfIfBlockThatAddsEvent = addIfLess(); // end of if block
        addToQueue(opcode); // this is executed if duration > 1ms
        mv.visitLabel(endOfIfBlockThatAddsEvent); // end of if-block and try-catch block
        /* here is RETURN instruction. It is visited automatically */
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        endTryCatch(); // visit try-catch and handle exceptions
        super.visitMaxs(maxStack, maxLocals);
    }

    private Label addIfLess() {
        Label endOfIfBlockThatAddsEvent = new Label();
        mv.visitJumpInsn(IFLE, endOfIfBlockThatAddsEvent);
        return endOfIfBlockThatAddsEvent;
    }

    private void addToQueue(int opcode) {
        if (opcode == ATHROW) {
            throwableAddToQueue();
        } else {
            retValAddToQueue(opcode);
        }
    }

    void setThrownByMethod() {
        getStartData();
        mv.visitMethodInsn(
                INVOKEVIRTUAL,
                START_DATA_CLASS,
                "setThrownByMethod",
                "()V",
                false
        );
    }

    void retValAddToQueue(int opcode) {
        if (opcode != RETURN) { // return some value
            if (saveReturnValue) {
                int sizeOfRetVal = getSizeOfRetVal(opcode);
                dupRetVal(sizeOfRetVal);
                retValToObj();
            } else {
                loadNull();
            }
        } else {
            loadNull(); // there is nothing to save
        }
        getCommonExitData();
        mv.visitLdcInsn(savedParameters);
        addToQueue(Type.RetVal);
    }

    /**
     * Throwable must be on stack.
     * It will be duplicated
     */
    void throwableAddToQueue() {
        dup(); // duplicate throwable
        if (methodConfig.isSaveReturnValue()) { // if save message
            loadTrue();
        } else {
            loadFalse();
        }
        getCommonExitData();
        mv.visitLdcInsn(savedParameters);
        addToQueue(Type.Throwable);
    }

    private void getCommonExitData() {
        getStartData();
        mv.visitMethodInsn(INVOKEVIRTUAL, START_DATA_CLASS,
                "getStartTime", "()J", false);
        getStartData();
        mv.visitMethodInsn(INVOKEVIRTUAL, START_DATA_CLASS,
                "getDuration", "()J", false);
        getStartData();
        mv.visitMethodInsn(INVOKEVIRTUAL, START_DATA_CLASS,
                "getParameters", "()[Ljava/lang/Object;", false);
        getThread();
        mv.visitLdcInsn(className);
        mv.visitLdcInsn(methodName);
        mv.visitLdcInsn(methodDesc);
        getIsStatic();
    }

    /**
     * Saves duration to start data.
     * Does not modify stack
     */
    void saveExitTime() {
        getStartData();
        getTime();
        mv.visitMethodInsn(INVOKEVIRTUAL,
                START_DATA_CLASS,
                "setDuration",
                "(J)V",
                false);
    }

    private void getStartData() {
        mv.visitVarInsn(ALOAD, startDataLocal);
    }

    /**
     * Adds boolean value to stack.
     * The value is true if method took > 1ms
     */
    void getIfTimeIsMoreOneMs() {
        getStartData();
        mv.visitMethodInsn(INVOKEVIRTUAL,
                START_DATA_CLASS,
                "getDuration",
                "()J",
                false);
        mv.visitInsn(LCONST_1);
        mv.visitInsn(LCMP);
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
        RetVal,
        Throwable
    }
}
