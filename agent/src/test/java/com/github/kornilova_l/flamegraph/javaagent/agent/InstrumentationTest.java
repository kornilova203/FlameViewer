package com.github.kornilova_l.flamegraph.javaagent.agent;

import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import com.github.kornilova_l.flamegraph.javaagent.TestHelper;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


public class InstrumentationTest {
    private static AgentConfigurationManager configurationManager;
    private static Set<MethodConfig> methodConfigs;

    @BeforeClass
    public static void setup() {
        TestHelper.createDir("actual");
        List<String> methodConfigsStrings = new LinkedList<>();
        methodConfigsStrings.add("*.*(*)");
        configurationManager = new AgentConfigurationManager(
                methodConfigsStrings
        );
        methodConfigs = configurationManager.findIncludingConfigs(
                "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/OneMethod");
    }

    @Test
    public void oneMethodTest() {
        try {
            InputStream inputStream = Instrumentation.class.getResourceAsStream(
                    "/com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/OneMethod.class");
            byte[] bytes = TestHelper.getBytes(inputStream);
            ClassReader cr = new ClassReader(bytes);
            ClassWriter cw = new ClassWriter(cr, 0);
            File outFile = new File("src/test/resources/actual/OneMethod.txt");
            TraceClassVisitor tcv = new TraceClassVisitor(cw, new PrintWriter(
                    new FileOutputStream(outFile)
            ));
            cr.accept(
                    new ProfilingClassVisitor(
                            tcv,
                            "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/OneMethod",
                            true,
                            methodConfigs,
                            configurationManager
                    ), ClassReader.SKIP_DEBUG
            );
            TestHelper.compareFiles(new File("src/test/resources/expected/OneMethod.txt"),
                    outFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
