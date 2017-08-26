package com.github.kornilova_l.flamegraph.javaagent.agent;

import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


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
        if (mv != null &&
                !methodName.equals("<clinit>") &&
                !methodName.equals("toString") &&
                (access & Opcodes.ACC_SYNTHETIC) == 0) { // exclude synthetic includingMethodConfigs
            MethodConfig trueMethodConfig = AgentConfigurationManager.newMethodConfig(className, methodName, desc);
            if (!configurationManager.isMethodExcluded(trueMethodConfig)) {
                List<MethodConfig> includingConfigsForMethod = AgentConfigurationManager.findIncludingConfigs(
                        this.includingConfigs,
                        trueMethodConfig
                );
                if (includingConfigsForMethod.size() != 0) {
                    AgentConfigurationManager.setSaveParameters(trueMethodConfig, includingConfigsForMethod);
                    return new ProfilingMethodVisitor(access, methodName, desc, mv, className, hasSystemCL, trueMethodConfig);
                }
            }
        }
        return mv;
    }
}
