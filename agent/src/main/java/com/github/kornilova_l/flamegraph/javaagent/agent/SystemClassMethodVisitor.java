package com.github.kornilova_l.flamegraph.javaagent.agent;

import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * This method inserts reflection calls
 * because system classes are loaded by bootstrap and therefore
 * they do not see not system classes.
 * It produces bytecode similar to SystemClassExpected
 * (see it in test classes com.github.kornilova_l.flamegraph.javaagent.generate.test_classes.SystemClassExpected)
 */
class SystemClassMethodVisitor extends ProfilingMethodVisitor {
    private final int proxyClassLocal = newLocal(org.objectweb.asm.Type.getType("L" + Class.class.getName() + ";"));
    private final int startDataClassLocal = newLocal(org.objectweb.asm.Type.getType("L" + Class.class.getName() + ";"));
    @SuppressWarnings("FieldCanBeLocal")
    private final String proxyClassNameWithDots = "com.github.kornilova_l.flamegraph.proxy.Proxy";
    @SuppressWarnings("FieldCanBeLocal")
    private final String startDataClassNameWithDots = "com.github.kornilova_l.flamegraph.proxy.StartData";
    private final Label startTryCatchForReflection = new Label();
    private int throwable;

    SystemClassMethodVisitor(int access,
                             String methodName,
                             String desc,
                             MethodVisitor mv,
                             String className,
                             boolean hasSystemCL,
                             MethodConfig methodConfig) {
        super(access, methodName, desc, mv, className, hasSystemCL, methodConfig);
    }

    @Override
    protected void onMethodEnter() {
        addTryCatchForReflection();
        saveClass(proxyClassNameWithDots, proxyClassLocal);
        saveClass(startDataClassNameWithDots, startDataClassLocal);
        super.onMethodEnter();
    }

    @Override
    void getIfTimeIsMoreOneMs() {
        getDuration();
        mv.visitInsn(LCONST_1);
        mv.visitInsn(LCMP);
    }

    @Override
    void setThrownByMethod() {
        // todo: implement
        super.setThrownByMethod();
    }

    @Override
    void retValAddToQueue(int opcode) {
        getMethodAddToQueue();
        invokeAddToQueue();
    }

    @Override
    void throwableAddToQueue() {
        mv.visitVarInsn(ALOAD, proxyClassLocal);
        mv.visitLdcInsn("addToQueue");

        getIConst(11); // size of array
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
        dup(); // duplicate array to add objects to it

        getIConst(0); // index
        mv.visitLdcInsn(Type.getObjectType("java/lang/Throwable"));
        addToArrayAndDup();

        getIConst(1);
        mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "TYPE", "Ljava/lang/Class;");
        addToArrayAndDup();

        getCommonClasses(2);

        invokeGetMethod();

        loadNull(); // static method

        getIConst(11); // size of array
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
        dup(); // duplicate array to add objects to it

        getIConst(0); // index
        mv.visitVarInsn(ALOAD, throwable);
        addToArrayAndDup();

        getIConst(1); // index
        getIConst(saveReturnValue ? 1 : 0);
        booleanToObj();
        addToArrayAndDup();

        getCommonData(2);

        invokeInvoke();
        pop();
    }

    @Override
    void maybeSaveThrowable() {
        /* save throwable */
        throwable = newLocal(org.objectweb.asm.Type.getType("L" + Throwable.class.getName() + ";"));
        mv.visitVarInsn(ASTORE, throwable);
    }

    @Override
    void maybeLoadThrowable() {
        mv.visitVarInsn(ALOAD, throwable);
    }

    @Override
    void getIfWasThrownByMethod() {
        mv.visitVarInsn(ALOAD, startDataClassLocal);
        mv.visitLdcInsn("isThrownByMethod");
        createEmptyArray("Class");
        invokeGetMethod();
        mv.visitVarInsn(ALOAD, startDataLocal);
        createEmptyArray("Object");
        invokeInvoke();
        mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
    }

    private void invokeAddToQueue() {
        loadNull();
        getIConst(10);
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
        dup(); // duplicate array to load new value to it

        getIConst(0); // index 0
        loadNull(); // return value
        addToArrayAndDup();

        getCommonData(1);

        invokeInvoke();
        pop(); // this method leaves null on stack
    }

    /**
     * Loads data to array:
     * * start time
     * * duration
     * * parameters
     * * thread
     * * class name
     * * method name
     * * description
     * * is static
     * * info about what parameters were saved (string)
     */
    private void getCommonData(int startIndex) {
        getIConst(startIndex++); // index 1
        getStartTime();
        addToArrayAndDup();

        getIConst(startIndex++);
        getDuration();
        longToObj(); // convert to object because it is added to array of objects
        addToArrayAndDup();

        getIConst(startIndex++);
        getParameters();
        addToArrayAndDup();

        getIConst(startIndex++);
        getThread();
        addToArrayAndDup();

        getIConst(startIndex++);
        mv.visitLdcInsn(className);
        addToArrayAndDup();

        getIConst(startIndex++);
        mv.visitLdcInsn(methodName);
        addToArrayAndDup();

        getIConst(startIndex++);
        mv.visitLdcInsn(methodDesc);
        addToArrayAndDup();

        getIConst(startIndex++);
        getIConst(isStatic() ? 1 : 0); // true or false
        booleanToObj();
        addToArrayAndDup();

        getIConst(startIndex);
        mv.visitLdcInsn(savedParameters);
        mv.visitInsn(AASTORE);
    }

    private void getParameters() {
        mv.visitVarInsn(ALOAD, startDataClassLocal);
        mv.visitLdcInsn("getParameters");
        createEmptyArray("Class");

        invokeGetMethod();

        mv.visitVarInsn(ALOAD, startDataLocal);
        createEmptyArray("Object");

        invokeInvoke();

        mv.visitTypeInsn(CHECKCAST, "[Ljava/lang/Object;");
        mv.visitTypeInsn(CHECKCAST, "[Ljava/lang/Object;");
    }

    /**
     * Leaves on stack new empty array of size 0
     */
    private void createEmptyArray(String type) {
        mv.visitInsn(ICONST_0); // empty array
        mv.visitTypeInsn(ANEWARRAY, "java/lang/" + type);
    }

    private void getDuration() {
        mv.visitVarInsn(ALOAD, startDataClassLocal);
        mv.visitLdcInsn("getDuration");

        createEmptyArray("Class");

        invokeGetMethod();
        mv.visitVarInsn(ALOAD, startDataLocal);

        createEmptyArray("Object");

        invokeInvoke();
        mv.visitTypeInsn(CHECKCAST, "java/lang/Long");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false);
    }

    private void getStartTime() {
        mv.visitVarInsn(ALOAD, startDataClassLocal);
        mv.visitLdcInsn("getStartTime");

        createEmptyArray("Class");

        invokeGetMethod();
        mv.visitVarInsn(ALOAD, startDataLocal);

        createEmptyArray("Object");

        invokeInvoke();
        mv.visitTypeInsn(CHECKCAST, "java/lang/Long");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false);
        longToObj();
    }

    private void getMethodAddToQueue() {
        mv.visitVarInsn(ALOAD, proxyClassLocal);
        mv.visitLdcInsn("addToQueue");
        getIConst(10); // size of array of types
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
        dup(); // duplicate array to add new object to it

        getIConst(0);
        mv.visitLdcInsn(Type.getObjectType("java/lang/Object")); // retVal
        addToArrayAndDup();

        getCommonClasses(1);

        invokeGetMethod();
    }

    private void getCommonClasses(int startIndex) {
        getIConst(startIndex++); // index 1
        getClassOfLong(); // start time
        addToArrayAndDup();

        getIConst(startIndex++); // index 2
        getClassOfLong(); // duration
        addToArrayAndDup();

        getIConst(startIndex++); // index 3
        getClassOfObjectArray(); // parameters
        addToArrayAndDup();

        getIConst(startIndex++); // index 4
        mv.visitLdcInsn(Type.getObjectType("java/lang/Thread")); // thread
        addToArrayAndDup();

        getIConst(startIndex++); // index 5
        getStringClass(); // class name
        addToArrayAndDup();

        getIConst(startIndex++); // index 6
        getStringClass(); // method name
        addToArrayAndDup();

        getIConst(startIndex++); // index 7
        getStringClass(); // description
        addToArrayAndDup();

        getIConst(startIndex++); // index 8
        mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "TYPE", "Ljava/lang/Class;"); // is static
        addToArrayAndDup();

        getIConst(startIndex); // index 9
        getStringClass(); // description
        mv.visitInsn(AASTORE);
    }

    private void getStringClass() {
        mv.visitLdcInsn(Type.getObjectType("java/lang/String"));
    }

    private void addToArrayAndDup() {
        mv.visitInsn(AASTORE); // add to array
        dup(); // duplicate array to add new object to it
    }

    private void getClassOfObjectArray() {
        mv.visitLdcInsn(Type.getObjectType("[Ljava/lang/Object;"));
    }

    @Override
    protected void saveExitTime() {
        mv.visitVarInsn(ALOAD, startDataClassLocal);
        mv.visitLdcInsn("setDuration");
        mv.visitInsn(ICONST_1);
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
        dup();
        mv.visitInsn(ICONST_0);
        getClassOfLong();
        mv.visitInsn(AASTORE);
        invokeGetMethod();
        mv.visitVarInsn(ALOAD, startDataLocal);
        mv.visitInsn(ICONST_1);
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
        dup();
        mv.visitInsn(ICONST_0);
        getTime();
        longToObj();
        mv.visitInsn(AASTORE);
        invokeInvoke();
        pop(); // remove return value of 'invoke' (it returns null here)
    }

    private void getClassOfLong() {
        mv.visitFieldInsn(GETSTATIC, "java/lang/Long", "TYPE", "Ljava/lang/Class;");
    }

    private void invokeInvoke() {
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", false);
    }

    private void invokeGetMethod() {
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false);
    }

    @Override
    protected void createStartData() {
        getMethodCreateStartData();
        invokeCreateStartData();
    }

    private void invokeCreateStartData() {
        mv.visitInsn(ACONST_NULL);
        mv.visitInsn(ICONST_2);
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
        dup();
        mv.visitInsn(ICONST_0);
        getTime();
        longToObj();
        mv.visitInsn(AASTORE);
        dup();
        mv.visitInsn(ICONST_1);
        mv.visitInsn(ICONST_0);
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
        mv.visitInsn(AASTORE);
        invokeInvoke();
    }

    private void getMethodCreateStartData() {
        mv.visitVarInsn(ALOAD, proxyClassLocal);
        mv.visitLdcInsn("createStartData");
        mv.visitInsn(ICONST_2);
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
        dup();
        mv.visitInsn(ICONST_0);
        getClassOfLong();
        mv.visitInsn(AASTORE);
        dup();
        mv.visitInsn(ICONST_1);
        getClassOfObjectArray();
        mv.visitInsn(AASTORE);
        invokeGetMethod();
    }

    private void saveClass(String className, int variable) {
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/ClassLoader", "getSystemClassLoader", "()Ljava/lang/ClassLoader;", false);
        mv.visitLdcInsn(className);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/ClassLoader", "loadClass", "(Ljava/lang/String;)Ljava/lang/Class;", false);
        mv.visitVarInsn(ASTORE, variable);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitMaxs(maxStack, maxLocals);
        endTryCatchForReflection();
    }

    private void endTryCatchForReflection() {
        Label end = new Label(); // end of try-catch block (after it there is goto operation to endOfHandler
        mv.visitTryCatchBlock(startTryCatchForReflection, end, end, "java/lang/ClassNotFoundException");
        mv.visitTryCatchBlock(startTryCatchForReflection, end, end, "java/lang/NoSuchMethodException");
        mv.visitTryCatchBlock(startTryCatchForReflection, end, end, "java/lang/IllegalAccessException");
        mv.visitTryCatchBlock(startTryCatchForReflection, end, end, "java/lang/reflect/InvocationTargetException");
        mv.visitLabel(end); // this label goes after athrow of try-catch that is added by ProfilingMethodVisitor
        printStackTrace();
        mv.visitInsn(RETURN);
    }

    /**
     * This is for debugging.
     * It is better to print stack trace than to ignore it
     */
    private void printStackTrace() {
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/ReflectiveOperationException", "printStackTrace", "()V", false);
    }

    private void addTryCatchForReflection() {
        mv.visitLabel(startTryCatchForReflection);
    }
}
