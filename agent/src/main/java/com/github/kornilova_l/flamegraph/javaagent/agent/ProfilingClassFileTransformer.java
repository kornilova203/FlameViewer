package com.github.kornilova_l.flamegraph.javaagent.agent;

import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import com.github.kornilova_l.flamegraph.proxy.Proxy;
import com.github.kornilova_l.flamegraph.proxy.StartData;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.List;

class ProfilingClassFileTransformer implements ClassFileTransformer {

    private AgentConfigurationManager configurationManager;

    ProfilingClassFileTransformer(AgentConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    private static boolean hasSystemCLInChain(ClassLoader loader) {
        ClassLoader chainLoader = loader;
        while (chainLoader != null) {
            if (chainLoader == ClassLoader.getSystemClassLoader()) {
                return true;
            }
            chainLoader = chainLoader.getParent();
        }
        return false;
    }

    @Override
    public byte[] transform(ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) {
        String classWithoutPackage = getClassWithoutPackage(className);
        if (!isClassOfAgent(className) && // exclude classes of agent
                !classWithoutPackage.toLowerCase().contains("classloader")) { // exclude classloaders
            List<MethodConfig> methodConfigs = configurationManager.findIncludingConfigs(className, loader == null);
            if (methodConfigs.size() != 0) {
                boolean hasSystemClassLoaderInChain = hasSystemCLInChain(loader);
                /* if classloader of the class has system classloader in chain then there will be
                 * no problems with using LoggerQueue and StartData.
                 * if class was loaded by bootstrap (loader == null) then reflection will be used. And it is stable.
                 * in other cases classloader must be able to load Proxy and StartData */
                if (hasSystemClassLoaderInChain || loader == null || classLoaderCanFindProxy(loader)) {
                    ClassReader cr = new ClassReader(classfileBuffer);
                    ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
                    // uncomment for debugging
//                TraceClassVisitor cv = new TraceClassVisitor(cw, new PrintWriter(System.out));
                    // SKIP_FRAMES avoids visiting frames that will be ignored and recomputed from scratch in the class writer.
                    cr.accept(
                            new ProfilingClassVisitor(
                                    cw,
                                    className,
                                    hasSystemClassLoaderInChain,
                                    methodConfigs,
                                    configurationManager,
                                    loader == null
                            ), ClassReader.SKIP_FRAMES);
                    return cw.toByteArray();
                }
            }
        }
        return classfileBuffer; // do not modify classes of the javaagent
    }

    private String getClassWithoutPackage(String className) {
        int lastSlash = className.lastIndexOf('/');
        if (lastSlash == -1) { // does not contain package
            return className;
        }
        return className.substring(lastSlash + 1, className.length());
    }

    private boolean isClassOfAgent(String className) {
        //noinspection SimplifiableIfStatement
        if (className.startsWith("com/github/kornilova_l/")) {
            return className.startsWith("com/github/kornilova_l/flamegraph/javaagent/") ||
                    className.startsWith("com/github/kornilova_l/flamegraph/proxy/") ||
                    className.startsWith("com/github/kornilova_l/flamegraph/proto/") ||
                    className.startsWith("com/github/kornilova_l/libs/com/google/protobuf/");
        }
        return false;
    }

    private boolean classLoaderCanFindProxy(ClassLoader loader) {
        try {
            loader.loadClass(Proxy.class.getCanonicalName());
            loader.loadClass(StartData.class.getCanonicalName());
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }
}
