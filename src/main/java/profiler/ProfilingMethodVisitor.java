package profiler;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ProfilingMethodVisitor extends AdviceAdapter {
    private final String methodName;
    private final String className;
    private final static Pattern allParamsPattern = Pattern.compile("(\\(.*\\))");
    private final static Pattern paramsPattern = Pattern.compile("(\\[?)(C|Z|S|I|J|F|D|B|(:?L[^;]+;))");
//    private final static Pattern returnType = Pattern.compile("(?<=\\)).*"); // (?<=\)).*
//    private final static Pattern baseTypes = Pattern.compile("([CZSIJFDB])");

    ProfilingMethodVisitor(int access, String methodName, String desc,
                           MethodVisitor mv, String className) {
        super(Opcodes.ASM5, mv, access, methodName, desc);
        this.className = className;
        this.methodName = methodName;
    }


    @Override
    protected void onMethodEnter() {
        createLogger();
        createEnterEventData();
        getThreadId();
        getTime();
        getClassNameAndMethodName();
        getIsStatic();
        getArrayWithParameters();
        initEnterEventData();
        addToQueue();
    }

    @Override
    protected void onMethodExit(int opcode) {
        super.onMethodExit(opcode);
    }

    private void addToQueue() {
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/concurrent/LinkedBlockingDeque", "add",
                "(Ljava/lang/Object;)Z", false);
        mv.visitInsn(POP); // ignore return value
    }

    private void initEnterEventData() {
        mv.visitMethodInsn(INVOKESPECIAL, "profiler/EnterEventData", "<init>",
                "(JJLjava/lang/String;Ljava/lang/String;Z[Ljava/lang/Object;)V", false);
    }

    private void createEnterEventData() {
        mv.visitTypeInsn(NEW, "profiler/EnterEventData");
        mv.visitInsn(DUP);
    }

    private void getArrayWithParameters() {
        String[] parametersDesc = getParamsDesc();
        int arraySize = 0;
        if (parametersDesc != null) {
            arraySize = parametersDesc.length;
        }
        if (arraySize == 0 && isStatic()) { // null instead of Object[]
            mv.visitInsn(ACONST_NULL);
        } else {
            if (!isStatic()) {
                arraySize++;
            }
            createObjArray(arraySize);
            int index = 0;
            int posOfFirstParam = 0;
            if (!isStatic()) { // appendThis
                loadThisToArr();
                index = 1;
                posOfFirstParam = 1;
            }
            if (parametersDesc != null) {
                loadParametersToArray(parametersDesc, index, posOfFirstParam);
            }
        }
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
                intToObj(pos);
                pos++;
                break;
            case "J": // long
                longToObj(pos);
                pos += 2;
                break;
            case "Z": // boolean
                booleanToObj(pos);
                pos++;
                break;
            case "C": // char
                charToObj(pos);
                pos++;
                break;
            case "S": // short
                shortToObj(pos);
                pos++;
                break;
            case "B": // byte
                byteToObj(pos);
                pos++;
                break;
            case "F": // float
                floatToObj(pos);
                pos++;
                break;
            case "D": // double
                doubleToObj(pos);
                pos += 2;
                break;
            default: // object
                mv.visitVarInsn(ALOAD, pos);
                pos++;
        }
        return pos;
    }

    private void doubleToObj(int pos) {
        mv.visitVarInsn(DLOAD, pos);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf",
                "(D)Ljava/lang/Double;", false);
    }

    private void floatToObj(int pos) {
        mv.visitVarInsn(FLOAD, pos);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf",
                "(F)Ljava/lang/Float;", false);
    }

    private void byteToObj(int pos) {
        mv.visitVarInsn(ILOAD, pos);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf",
                "(B)Ljava/lang/Byte;", false);
    }

    private void shortToObj(int pos) {
        mv.visitVarInsn(ILOAD, pos);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf",
                "(S)Ljava/lang/Short;", false);
    }

    private void charToObj(int pos) {
        mv.visitVarInsn(ILOAD, pos);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf",
                "(C)Ljava/lang/Character;", false);
    }

    private void booleanToObj(int pos) {
        mv.visitVarInsn(ILOAD, pos);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf",
                "(Z)Ljava/lang/Boolean;", false);
    }

    private void longToObj(int pos) {
        mv.visitVarInsn(LLOAD, pos);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf",
                "(J)Ljava/lang/Long;", false);
    }

    private void intToObj(int pos) {
        mv.visitVarInsn(ILOAD, pos);
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
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
    }

    private void createLogger() {
        mv.visitFieldInsn(GETSTATIC, "profiler/Logger", "queue",
                "Ljava/util/concurrent/LinkedBlockingDeque;");
    }

    private void getThreadId() {
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "currentThread",
                "()Ljava/lang/Thread;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Thread", "getId", "()J", false);
    }


    private String[] getParamsDesc() {
        ArrayList<String> paramsDesc = new ArrayList<>();
        String desc = getPartOfDescWithParam();
        System.out.println("part: " + desc);
        Matcher m = paramsPattern.matcher(desc);
        while (m.find()) {
            System.out.println("group: " + m.group());
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
        return (methodAccess & ACC_STATIC) != 0 || methodName.equals("<init>"); // do not print 'this' in <init>
    }


    private boolean isI(String type) {
        return Objects.equals(type, "I") ||
                Objects.equals(type, "Z") || // boolean
                Objects.equals(type, "C") ||
                Objects.equals(type, "B") || // byte
                Objects.equals(type, "S"); // short
    }


//    @Override
//    protected void onMethodExit(int opcode) {
//        convertReturnValToString(opcode);
//        mv.visitMethodInsn(INVOKEVIRTUAL, "profiler/State", "methodFinish",
//                "(Ljava/lang/String;)V", false);
//    }

//    private void convertReturnValToString(int opcode) {
//        if (opcode == RETURN) {
//            mv.visitVarInsn(ALOAD, state);
//            mv.visitLdcInsn(""); // no return param
//            return;
//        }
//        if (opcode == IRETURN) {
//            insertStateBeforeSmallRetVal();
//            invokeStringValueOf("I");
//        } else if (opcode == LRETURN) {
//            insertStateBeforeLargeRetVal();
//            invokeStringValueOf("J");
//        } else if (opcode == FRETURN) {
//            insertStateBeforeSmallRetVal();
//            invokeStringValueOf("F");
//        } else if (opcode == DRETURN) {
//            insertStateBeforeLargeRetVal();
//            invokeStringValueOf("D");
//        } else if (opcode == ARETURN) { // object or array
//            insertStateBeforeSmallRetVal();
//            aReturnToString();
//        } else { // ATHROW
//            dup();
//            mv.visitVarInsn(ALOAD, state);
//            mv.visitInsn(SWAP);
//            invokeToString();
//        }
//    }


}
