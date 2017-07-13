package com.github.kornilova_l.flamegraph.javaagent.agent;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


class ProfilingClassVisitor extends ClassVisitor {
    private final String className;
    private final boolean hasSystemCL;
//    private final List<MethodConfig> methodConfigs;

    ProfilingClassVisitor(ClassVisitor cv, String className, boolean hasSystemCL) {
        super(Opcodes.ASM5, cv);
        this.className = className;
        this.hasSystemCL = hasSystemCL;
//        this.methodConfigs = methodConfigs;
    }

    @Override
    public MethodVisitor visitMethod(int access, String methodName, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, methodName, desc, signature, exceptions);
//        System.out.println("visit method: " + classPatternString + "." + methodPatternString +
//                " included? " + Configuration.isMethodIncluded(classPatternString + "." + methodPatternString) +
//                " excluded? " + Configuration.isMethodExcluded(classPatternString + "." + methodPatternString));
        System.out.println("method: " + methodName);
        if (mv != null &&
                !methodName.equals("<init>") &&
                !methodName.equals("toString") &&
                (access & Opcodes.ACC_SYNTHETIC) == 0) { // exclude synthetic includingMethodConfigs
            System.out.println("check method: " + className + " " + methodName);
//            if (!AgentConfigurationManager.isMethodExcluded(className, methodName, desc)) {
                System.out.println("is not excluded");
//                List<MethodConfig> finalMethodConfigs = AgentConfigurationManager.findIncludingConfigs(
//                        this.methodConfigs,
//                        methodName,
//                        desc.substring(desc.indexOf("(") + 1, desc.indexOf(")")));
//                System.out.println("including configs: " + finalMethodConfigs);
//                if (finalMethodConfigs.size() != 0) {
                    return new ProfilingMethodVisitor(access, methodName, desc, mv, className, hasSystemCL);
//                }
//            }
        }
        return mv;
    }
}
