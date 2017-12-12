package com.github.kornilova_l.flamegraph.javaagent.agent;

import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import com.github.kornilova_l.flamegraph.javaagent.TestHelper;
import com.github.kornilova_l.flamegraph.javaagent.generate.Generator;
import com.github.kornilova_l.flamegraph.javaagent.generate.test_classes.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.github.kornilova_l.flamegraph.javaagent.TestHelper.removePackage;


public class InstrumentationTest {
    private static AgentConfigurationManager configurationManager;
    private static List<MethodConfig> methodConfigs = new ArrayList<>();
    private static AgentConfigurationManager configurationManagerSaveParams;
    private static List<MethodConfig> methodConfigsSaveParams = new ArrayList<>();
    private static AgentConfigurationManager configurationManagerSaveReturn;
    private static List<MethodConfig> methodConfigsSaveReturn = new ArrayList<>();

    @BeforeClass
    public static void setup() {
        configurationManager = createConfig("*.*(*)", methodConfigs);
        configurationManagerSaveParams = createConfig("*.*(*+)", methodConfigsSaveParams);
        configurationManagerSaveReturn = createConfig("*.*(*)+", methodConfigsSaveReturn);
    }

    private static AgentConfigurationManager createConfig(String config,
                                                          List<MethodConfig> methodConfigs) {
        TestHelper.createDir("actual");
        List<String> methodConfigsStrings = new LinkedList<>();
        methodConfigsStrings.add(config);
        methodConfigsStrings.add("!*.<init>(*)");
        AgentConfigurationManager configurationManager = new AgentConfigurationManager(
                methodConfigsStrings
        );
        methodConfigs.addAll(configurationManager.findIncludingConfigs(
                "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/OneMethod"));
        return configurationManager;
    }

    @Test
    public void basicInstrumentation() {
        /* All test fail because it is not possible to generate exactly same bytecode */
//        classTest(OneMethod.class, configurationManager, methodConfigs, true);
        classTest(ThrowsException.class, ThrowsExceptionExpected.class, configurationManager, methodConfigs, true);
//        classTest(UsesThreadPool.class, configurationManager, methodConfigs);
//        classTest(SeveralReturns.class, configurationManager, methodConfigs, true);
//        classTest(TwoMethods.class, configurationManager, methodConfigs, true);
    }

    // TODO: check other tests

    @Test
    public void saveParameters() {
        /* All test fail because it is not possible to generate exactly same bytecode */
        classTest(SaveParameters.class, SaveParameters.class, configurationManagerSaveParams, methodConfigsSaveParams, true);
//        classTest(SaveSecondParam.class, configurationManagerSaveSecondParam, methodConfigsSaveSecondParam, true);
    }

    @Test
    public void saveReturnValue() {
        /* All test fail because it is not possible to generate exactly same bytecode */
        classTest(SaveReturnValue.class, SaveReturnValueExpected.class, configurationManagerSaveReturn, methodConfigsSaveReturn, true);
    }

    @Test
    public void useProxy() {
        /* All test fail because it is not possible to generate exactly same bytecode */
        classTest(UseProxy.class, UseProxyExpected.class, configurationManagerSaveReturn, methodConfigsSaveReturn, false);
    }

    @Test
    public void hasCatch() {
        classTest(HasCatch.class, HasCatchExpected.class, configurationManagerSaveReturn, methodConfigsSaveReturn, true);
    }

    private void classTest(Class testedClass,
                           Class expectedClass,
                           AgentConfigurationManager configurationManager,
                           List<MethodConfig> methodConfigs,
                           boolean hasSystemCL) {
        Generator.generate(expectedClass);
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
                            hasSystemCL,
                            methodConfigs,
                            configurationManager
                    ), ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG
            );

            bytes = cw.toByteArray();

            saveClass(bytes, fileName);

            cr = new ClassReader(bytes);
            cw = new ClassWriter(cr, 0);
            File outFile = new File("src/test/resources/actual/" + fileName + ".txt");
            cr.accept(
                    new TraceClassVisitor(cw, new PrintWriter(
                            new FileOutputStream(outFile)
                    )), ClassReader.SKIP_DEBUG
            );

            TestHelper.compareFiles(new File("src/test/resources/expected/" + removePackage(expectedClass.getName()) + ".txt"),
                    outFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void saveClass(byte[] bytes, String fileName) {
        try (OutputStream outputStream = new FileOutputStream(
                new File("src/test/resources/actual/" + fileName + ".class")
        )) {
            outputStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
