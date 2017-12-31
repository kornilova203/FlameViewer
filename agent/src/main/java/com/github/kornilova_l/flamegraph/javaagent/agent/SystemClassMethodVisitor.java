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
    private final int startDataLocal = newLocal(org.objectweb.asm.Type.getType("L" + Object.class.getName() + ";"));
    @SuppressWarnings("FieldCanBeLocal")
    private final String proxyClassNameWithDots = "com.github.kornilova_l.flamegraph.proxy.Proxy";
    @SuppressWarnings("FieldCanBeLocal")
    private final String startDataClassNameWithDots = "com.github.kornilova_l.flamegraph.proxy.StartData";
    private final Label startTryCatchForReflection = new Label();

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
        createStartData();
        addTryCatchBeginning();
    }

    private void createStartData() {
        getMethodCreateStartData();
        invokeCreateStartData();
        mv.visitVarInsn(ASTORE, startDataLocal);
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
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", false);
    }

    private void getMethodCreateStartData() {
        mv.visitVarInsn(ALOAD, proxyClassLocal);
        mv.visitLdcInsn("createStartData");
        mv.visitInsn(ICONST_2);
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
        dup();
        mv.visitInsn(ICONST_0);
        mv.visitFieldInsn(GETSTATIC, "java/lang/Long", "TYPE", "Ljava/lang/Class;");
        mv.visitInsn(AASTORE);
        dup();
        mv.visitInsn(ICONST_1);
        mv.visitLdcInsn(Type.getObjectType("[Ljava/lang/Object;"));
        mv.visitInsn(AASTORE);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false);
    }

    private void saveClass(String className, int variable) {
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/ClassLoader", "getSystemClassLoader", "()Ljava/lang/ClassLoader;", false);
        mv.visitLdcInsn(className);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/ClassLoader", "loadClass", "(Ljava/lang/String;)Ljava/lang/Class;", true);
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
