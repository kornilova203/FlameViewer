package com.github.kornilova_l.flamegraph.javaagent.agent

import com.github.kornilova_l.flamegraph.configuration.MethodConfig
import com.github.kornilova_l.flamegraph.javaagent.compareFiles
import com.github.kornilova_l.flamegraph.javaagent.createDir
import com.github.kornilova_l.flamegraph.javaagent.generate.Generator
import com.github.kornilova_l.flamegraph.javaagent.generate.test_classes.*
import com.github.kornilova_l.flamegraph.javaagent.getBytes
import com.github.kornilova_l.flamegraph.javaagent.removePackage
import org.junit.Before
import org.junit.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.util.TraceClassVisitor
import java.io.*
import java.util.*


class InstrumentationTest {
    @Before
    fun setup() {
        configurationManager = createConfig("*.*(*)", methodConfigs)
        configurationManagerSaveParams = createConfig("*.*(*+)", methodConfigsSaveParams)
        configurationManagerSaveReturn = createConfig("*.*(*)+", methodConfigsSaveReturn)
    }

    @Test
    fun methodThrowsExceptionCheckManually() {
        /* Test fails because it is not possible to generate exactly same bytecode
         * It duplicated exception on stack */
        classTest(ThrowsException::class.java, ThrowsExceptionExpected::class.java, configurationManager, methodConfigs, true)
    }

    @Test
    fun saveParameters() {
        classTest(SaveParameters::class.java, SaveParametersExpected::class.java, configurationManagerSaveParams, methodConfigsSaveParams, true)
    }

    @Test
    fun saveReturnValueCheckManually() {
        /* Test fails because it is not possible to generate exactly same bytecode
         * It duplicates return value on stack */
        classTest(SaveReturnValue::class.java, SaveReturnValueExpected::class.java, configurationManagerSaveReturn, methodConfigsSaveReturn, true)
    }

    @Test
    fun useProxy() {
        classTest(UseProxy::class.java, UseProxyExpected::class.java, configurationManagerSaveReturn, methodConfigsSaveReturn, false)
    }

    @Test
    fun systemClassesTest() {
        val configurationManager = AgentConfigurationManager(listOf("${SystemClass::class.java.name}.method(*)"))
        val methodConfigs = listOf(MethodConfig(SystemClass::class.java.name, "method", "(*)"))
        /* All test fail because it is not possible to generate exactly same bytecode */
        classTest(SystemClass::class.java, SystemClassExpected::class.java, configurationManager, methodConfigs, false, true)
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
                            configurationManager,
                            isSystemClass
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
