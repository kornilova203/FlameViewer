package com.github.kornilova_l.flamegraph.javaagent.agent;

import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import com.github.kornilova_l.flamegraph.javaagent.TestHelper;
import com.github.kornilova_l.flamegraph.javaagent.generate.test_classes.OneMethod;
import com.github.kornilova_l.flamegraph.javaagent.generate.test_classes.UsesThreadPool;
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

import static com.github.kornilova_l.flamegraph.javaagent.TestHelper.removePackage;


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
    public void instrumentationTest() {
        classTest(OneMethod.class);
        classTest(UsesThreadPool.class);
    }

    private void classTest(Class testedClass) {
        try {
            String fullName = testedClass.getName();
            String fileName = removePackage(fullName);
            InputStream inputStream = Instrumentation.class.getResourceAsStream(
                    "/" + fullName.replace('.', '/') + ".class");
            byte[] bytes = TestHelper.getBytes(inputStream);
            ClassReader cr = new ClassReader(bytes);
            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
            cr.accept(
                    new ProfilingClassVisitor(
                            cw,
                            fullName.replace('.', '/'),
                            true,
                            methodConfigs,
                            configurationManager
                    ), ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG
            );

            bytes = cw.toByteArray();

            cr = new ClassReader(bytes);
            cw = new ClassWriter(cr, 0);
            File outFile = new File("src/test/resources/actual/" + fileName + ".txt");
            cr.accept(
                    new TraceClassVisitor(cw, new PrintWriter(
                            new FileOutputStream(outFile)
                    )), 0
            );

            TestHelper.compareFiles(new File("src/test/resources/expected/" + fileName + ".txt"),
                    outFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
