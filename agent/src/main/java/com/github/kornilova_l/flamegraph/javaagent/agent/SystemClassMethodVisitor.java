package com.github.kornilova_l.flamegraph.javaagent.agent;

import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

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
    private final String proxyClassNameWithDots = "com.github.kornilova_l.flamegraph.proxy.Proxy";
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
        super.onMethodEnter();
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
