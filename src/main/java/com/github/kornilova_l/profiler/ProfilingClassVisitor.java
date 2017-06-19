package com.github.kornilova_l.profiler;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static com.github.kornilova_l.profiler.Configuration.fullNamePatterns;
import static com.github.kornilova_l.profiler.Configuration.matchesAnyPattern;

class ProfilingClassVisitor extends ClassVisitor {
    private final String className;

    ProfilingClassVisitor(ClassVisitor cv, String className) {
        super(Opcodes.ASM5, cv);
        this.className = className;
    }

    @Override
    public MethodVisitor visitMethod(int access, String methodName, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, methodName, desc, signature, exceptions);
        if (mv != null &&
                !methodName.equals("toString") &&
                (access & Opcodes.ACC_SYNTHETIC) == 0 &&  // exclude synthetic methods
                matchesAnyPattern(
                        className + "." + methodName,
                        fullNamePatterns)) {
            return new ProfilingMethodVisitor(access, methodName, desc, mv, className);
        }
        return mv;
    }
}
