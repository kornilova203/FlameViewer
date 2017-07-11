package com.github.kornilova_l.profiler.agent;

import com.github.kornilova_l.config.MethodConfig;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;

class ProfilingClassVisitor extends ClassVisitor {
    private final String className;
    private final boolean hasSystemCL;
    private final List<MethodConfig> methodConfigs;

    ProfilingClassVisitor(ClassVisitor cv, String className, boolean hasSystemCL, List<MethodConfig> methodConfigs) {
        super(Opcodes.ASM5, cv);
        this.className = className;
        this.hasSystemCL = hasSystemCL;
        this.methodConfigs = methodConfigs;
    }

    @Override
    public MethodVisitor visitMethod(int access, String methodName, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, methodName, desc, signature, exceptions);
//        System.out.println("visit method: " + classPattern + "." + methodPattern +
//                " included? " + Configuration.isMethodIncluded(classPattern + "." + methodPattern) +
//                " excluded? " + Configuration.isMethodExcluded(classPattern + "." + methodPattern));
        if (mv != null &&
                !methodName.equals("<init>") &&
                !methodName.equals("toString") &&
                (access & Opcodes.ACC_SYNTHETIC) == 0) { // exclude synthetic methods
            MethodConfig methodConfig = Configuration.getMethodIfPresent(methodConfigs, methodName, desc);
            if (methodConfig != null) {
                return new ProfilingMethodVisitor(access, methodName, desc, mv, className, hasSystemCL, methodConfig);
            }
        }
        return mv;
    }
}
