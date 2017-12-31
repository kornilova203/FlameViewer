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
    private final int proxyClassLocal;
    private final int startDataClassLocal;
    private final Label startTryCatchForReflection = new Label();

    SystemClassMethodVisitor(int access,
                             String methodName,
                             String desc,
                             MethodVisitor mv,
                             String className,
                             boolean hasSystemCL,
                             MethodConfig methodConfig) {
        super(access, methodName, desc, mv, className, hasSystemCL, methodConfig);
        proxyClassLocal = newLocal(org.objectweb.asm.Type.getType("L" + Class.class.getName() + ";"));
        startDataClassLocal = newLocal(org.objectweb.asm.Type.getType("L" + Class.class.getName() + ";"));
    }

    @Override
    protected void onMethodEnter() {
        addTryCatchForReflection();
        super.onMethodEnter();
    }

    @Override
    protected void onMethodExit(int opcode) {
        super.onMethodExit(opcode);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitMaxs(maxStack, maxLocals);
        endTryCatchForReflection();
    }

    private void endTryCatchForReflection() {
        Label end = new Label(); // end of try-catch block (after it there is goto operation to endOfHandler
        Label handler = new Label(); // start of exception handler
        Label endOfHandler = new Label();
        mv.visitTryCatchBlock(startTryCatchForReflection, end, handler, "java/lang/ClassNotFoundException");
        mv.visitTryCatchBlock(startTryCatchForReflection, end, handler, "java/lang/NoSuchMethodException");
        mv.visitTryCatchBlock(startTryCatchForReflection, end, handler, "java/lang/IllegalAccessException");
        mv.visitTryCatchBlock(startTryCatchForReflection, end, handler, "java/lang/reflect/InvocationTargetException");
        mv.visitLabel(end);
        mv.visitJumpInsn(GOTO, endOfHandler);
        mv.visitLabel(handler);
        printStackTrace();
        mv.visitLabel(endOfHandler);
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
