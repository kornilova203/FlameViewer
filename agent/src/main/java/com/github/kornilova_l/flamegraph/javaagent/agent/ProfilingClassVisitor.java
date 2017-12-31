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
    private final boolean isSystemClass;

    ProfilingClassVisitor(ClassVisitor cv,
                          String className,
                          boolean hasSystemCL,
                          List<MethodConfig> includingConfigs,
                          AgentConfigurationManager configurationManager,
                          boolean isSystemClass) {
        super(Opcodes.ASM5, cv);
        this.className = className;
        this.hasSystemCL = hasSystemCL;
        this.includingConfigs = includingConfigs;
        this.configurationManager = configurationManager;
        this.isSystemClass = isSystemClass;
    }

    @Override
    public MethodVisitor visitMethod(int access, String methodName, String desc, String signature, String[] exceptions) {
        if (methodName == null) { // it happens with rt.jar classes. I do not know why
            return null;
        }
        MethodVisitor mv = cv.visitMethod(access, methodName, desc, signature, exceptions);
        if (mv != null &&
                !methodName.equals("<clinit>") &&
                !methodName.equals("toString") &&
                (access & Opcodes.ACC_SYNTHETIC) == 0) { // exclude synthetic methods
            MethodConfig trueMethodConfig = AgentConfigurationManager.newMethodConfig(className, methodName, desc);
            if (!configurationManager.isMethodExcluded(trueMethodConfig)) {
                List<MethodConfig> includingConfigsForMethod = AgentConfigurationManager.findIncludingConfigs(
                        this.includingConfigs,
                        trueMethodConfig
                );
                if (includingConfigsForMethod.size() != 0) {
                    AgentConfigurationManager.setSaveParameters(trueMethodConfig, includingConfigsForMethod);
                    if (isSystemClass) {
                        return new SystemClassMethodVisitor(access, methodName, desc, mv, className, hasSystemCL, trueMethodConfig);
                    } else {
                        return new ProfilingMethodVisitor(access, methodName, desc, mv, className, hasSystemCL, trueMethodConfig);
                    }
                }
            }
        }
        return mv;
    }
}
