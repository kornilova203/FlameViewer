package com.github.kornilova_l.flamegraph.javaagent.agent;

import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;


class ProfilingClassVisitor extends ClassVisitor {
    private final String className;
    private final boolean hasSystemCL;
    private final List<MethodConfig> includingConfigs;
    private final AgentConfigurationManager configurationManager;

    ProfilingClassVisitor(ClassVisitor cv,
                          String className,
                          boolean hasSystemCL,
                          List<MethodConfig> includingConfigs,
                          AgentConfigurationManager configurationManager) {
        super(Opcodes.ASM5, cv);
        this.className = className;
        this.hasSystemCL = hasSystemCL;
        this.includingConfigs = includingConfigs;
        this.configurationManager = configurationManager;
    }

    @Override
    public MethodVisitor visitMethod(int access, String methodName, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, methodName, desc, signature, exceptions);
//        System.out.println("visit method: " + classPatternString + "." + methodPatternString +
//                " included? " + Configuration.isMethodIncluded(classPatternString + "." + methodPatternString) +
//                " excluded? " + Configuration.isMethodExcluded(classPatternString + "." + methodPatternString));
        if (mv != null &&
                !methodName.equals("<init>") &&
                !methodName.equals("toString") &&
                (access & Opcodes.ACC_SYNTHETIC) == 0) { // exclude synthetic includingMethodConfigs
            MethodConfig methodConfig = AgentConfigurationManager.newMethodConfig(className, methodName, desc);
            if (!configurationManager.isMethodExcluded(methodConfig)) {
                List<MethodConfig> finalMethodConfigs = AgentConfigurationManager.findIncludingConfigs(
                        this.includingConfigs,
                        methodConfig
                );
                if (finalMethodConfigs.size() != 0) {
                    return new ProfilingMethodVisitor(access, methodName, desc, mv, className, hasSystemCL);
                }
            }
        }
        return mv;
    }
}
