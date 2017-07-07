package com.github.kornilova_l.profiler.agent;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class ProfilingClassVisitor extends ClassVisitor {
    private final String className;
    private final boolean hasSystemCL;

    ProfilingClassVisitor(ClassVisitor cv, String className, boolean hasSystemCL) {
        super(Opcodes.ASM5, cv);
        this.className = className;
        this.hasSystemCL = hasSystemCL;
    }

    @Override
    public MethodVisitor visitMethod(int access, String methodName, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, methodName, desc, signature, exceptions);
//        System.out.println("visit method: " + className + "." + methodName +
//                " included? " + Configuration.isMethodIncluded(className + "." + methodName) +
//                " excluded? " + Configuration.isMethodExcluded(className + "." + methodName));
        if (mv != null &&
                !methodName.equals("<init>") &&
                !methodName.equals("toString") &&
                !methodName.contains("ClassLoader") &&
                (access & Opcodes.ACC_SYNTHETIC) == 0 &&  // exclude synthetic methods
                Configuration.isMethodIncluded(className + "." + methodName) &&
                !Configuration.isMethodExcluded(className + "." + methodName)) {
            return new ProfilingMethodVisitor(access, methodName, desc, mv, className, hasSystemCL);
        }
        return mv;
    }
}
