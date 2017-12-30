package com.github.kornilova_l.flamegraph.javaagent.agent

import com.github.kornilova_l.flamegraph.configuration.MethodConfig
import com.github.kornilova_l.flamegraph.javaagent.compareFiles
import com.github.kornilova_l.flamegraph.javaagent.createDir
import com.github.kornilova_l.flamegraph.javaagent.generate.Generator
import com.github.kornilova_l.flamegraph.javaagent.generate.test_classes.*
import com.github.kornilova_l.flamegraph.javaagent.getBytes
import com.github.kornilova_l.flamegraph.javaagent.removePackage
import org.junit.BeforeClass
import org.junit.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.util.TraceClassVisitor
import java.io.*
import java.util.*


class InstrumentationTest {

    @Test
    fun basicInstrumentation() {
        /* All test fail because it is not possible to generate exactly same bytecode */
        //        classTest(OneMethod.class, configurationManager, methodConfigs, true);
        classTest(ThrowsException::class.java, ThrowsExceptionExpected::class.java, configurationManager, methodConfigs, true)
        //        classTest(UsesThreadPool.class, configurationManager, methodConfigs);
        //        classTest(SeveralReturns.class, configurationManager, methodConfigs, true);
        //        classTest(TwoMethods.class, configurationManager, methodConfigs, true);
    }

    @Test
    fun saveParameters() {
        /* All test fail because it is not possible to generate exactly same bytecode */
        classTest(SaveParameters::class.java, SaveParameters::class.java, configurationManagerSaveParams, methodConfigsSaveParams, true)
        //        classTest(SaveSecondParam.class, configurationManagerSaveSecondParam, methodConfigsSaveSecondParam, true);
    }

    @Test
    fun saveReturnValue() {
        /* All test fail because it is not possible to generate exactly same bytecode */
        classTest(SaveReturnValue::class.java, SaveReturnValueExpected::class.java, configurationManagerSaveReturn, methodConfigsSaveReturn, true)
    }

    @Test
    fun useProxy() {
        /* All test fail because it is not possible to generate exactly same bytecode */
        classTest(UseProxy::class.java, UseProxyExpected::class.java, configurationManagerSaveReturn, methodConfigsSaveReturn, false)
    }

    @Test
    fun systemClassesTest() {
        val configurationManager = AgentConfigurationManager(listOf("java.io.File.*(*)"))
        val methodConfigs = listOf(MethodConfig("java.io.File", "*", "(*)"))
        /* All test fail because it is not possible to generate exactly same bytecode */
        classTest(SystemClass::class.java, SystemClassExpected::class.java, configurationManager, methodConfigs, false)
    }

    @Test
    fun fileOutputStream() {
        val methodConfigs = listOf(MethodConfig("java.io.FileOutputStream", "write", "(byte[])"))
        val configurationManager = AgentConfigurationManager(listOf("java.io.FileOutputStream.write(byte[])"))
        classTest(FileOutputStream::class.java, FileOutputStream::class.java, configurationManager, methodConfigs, false)
    }

    @Test
    fun hasCatch() {
        classTest(HasCatch::class.java, HasCatchExpected::class.java, configurationManagerSaveReturn, methodConfigsSaveReturn, true)
    }

    private fun classTest(testedClass: Class<*>,
                          expectedClass: Class<*>,
                          configurationManager: AgentConfigurationManager?,
                          methodConfigs: List<MethodConfig>,
                          hasSystemCL: Boolean,
                          isSystemClass: Boolean = false) {
        Generator.generate(expectedClass)
        try {
            val fullName = testedClass.name
            val fileName = removePackage(fullName)

            var bytes = getBytes(testedClass)
            var cr = ClassReader(bytes)
            var cw = ClassWriter(cr, ClassWriter.COMPUTE_FRAMES)
            cr.accept(
                    ProfilingClassVisitor(
                            cw,
                            fullName.replace('.', '/'),
                            hasSystemCL,
                            methodConfigs,
                            configurationManager
                    ), ClassReader.SKIP_FRAMES or ClassReader.SKIP_DEBUG
            )

            bytes = cw.toByteArray()

            saveClass(bytes, fileName)

            cr = ClassReader(bytes)
            cw = ClassWriter(cr, 0)
            val outFile = File("src/test/resources/actual/$fileName.txt")
            cr.accept(
                    TraceClassVisitor(cw, PrintWriter(
                            FileOutputStream(outFile)
                    )), ClassReader.SKIP_DEBUG
            )

            compareFiles(File("src/test/resources/expected/" + removePackage(expectedClass.name) + ".txt"),
                    outFile)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

    }

    private fun saveClass(bytes: ByteArray, fileName: String) {
        try {
            FileOutputStream(
                    File("src/test/resources/actual/$fileName.class")
            ).use { outputStream -> outputStream.write(bytes) }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    companion object {
        private var configurationManager: AgentConfigurationManager? = null
        private val methodConfigs = ArrayList<MethodConfig>()
        private var configurationManagerSaveParams: AgentConfigurationManager? = null
        private val methodConfigsSaveParams = ArrayList<MethodConfig>()
        private var configurationManagerSaveReturn: AgentConfigurationManager? = null
        private val methodConfigsSaveReturn = ArrayList<MethodConfig>()

        @BeforeClass
        fun setup() {
            configurationManager = createConfig("*.*(*)", methodConfigs)
            configurationManagerSaveParams = createConfig("*.*(*+)", methodConfigsSaveParams)
            configurationManagerSaveReturn = createConfig("*.*(*)+", methodConfigsSaveReturn)
        }

        private fun createConfig(config: String,
                                 methodConfigs: MutableList<MethodConfig>): AgentConfigurationManager {
            createDir("actual")
            val methodConfigsStrings = LinkedList<String>()
            methodConfigsStrings.add(config)
            methodConfigsStrings.add("!*.<init>(*)")
            val configurationManager = AgentConfigurationManager(
                    methodConfigsStrings
            )
            methodConfigs.addAll(configurationManager.findIncludingConfigs(
                    "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/OneMethod", false))
            return configurationManager
        }
    }
}
