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
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.util.*


class InstrumentationTest {
    @Before
    fun setup() {
        configurationManager = createConfig("*.*(*)", methodConfigs)
        configurationManagerSaveParams = createConfig("*.*(*+)", methodConfigsSaveParams)
        configurationManagerSaveReturn = createConfig("*.*(*)+", methodConfigsSaveReturn)
    }

    @Test
    fun methodThrowsException() {
        classTest(ThrowsException::class.java, configurationManager!!, methodConfigs)
        /* If instrumentation changed uncomment following and check it manually */
//         classTest(ThrowsException::class.java, ThrowsExceptionExpected::class.java, configurationManager, methodConfigs, true)
    }

    @Test
    fun saveParameters() {
        classTest(SaveParameters::class.java, configurationManagerSaveParams!!, methodConfigsSaveParams)
        /* If instrumentation changed uncomment following and check it manually */
//        classTest(SaveParameters::class.java, SaveParametersExpected::class.java, configurationManagerSaveParams!!, methodConfigsSaveParams, true)
    }

    @Test
    fun saveReturnValue() {
        classTest(SaveReturnValue::class.java, configurationManagerSaveReturn!!, methodConfigsSaveReturn)
        /* If instrumentation changed uncomment following and check it manually */
//        classTest(SaveReturnValue::class.java, SaveReturnValueExpected::class.java, configurationManagerSaveReturn!!, methodConfigsSaveReturn, true)
    }

    @Test
    fun useProxy() {
        classTest(UseProxy::class.java, configurationManagerSaveReturn!!, methodConfigsSaveReturn, false)
        /* If instrumentation changed uncomment following and check it manually */
//        classTest(UseProxy::class.java, UseProxyExpected::class.java, configurationManagerSaveReturn!!, methodConfigsSaveReturn, false)
    }

    @Test
    fun systemClassesTest() {
        val configurationManager = AgentConfigurationManager(listOf("${SystemClass::class.java.name}.method(*)"))
        val methodConfigs = listOf(MethodConfig(SystemClass::class.java.name, "method", "(*)"))
        classTest(SystemClass::class.java, SystemClassExpected::class.java, configurationManager, methodConfigs, false, true)
    }

    @Test
    fun fileOutputStream() {
        val methodConfigs = listOf(MethodConfig("java.io.FileOutputStream", "write", "(byte[])"))
        val configurationManager = AgentConfigurationManager(listOf("java.io.FileOutputStream.write(byte[])"))
        classTest(FileOutputStream::class.java, FileOutputStream::class.java, configurationManager, methodConfigs, false, true)
    }

    @Test
    fun hasCatch() {
        classTest(HasCatch::class.java, configurationManagerSaveReturn!!, methodConfigsSaveReturn)
        /* If instrumentation changed uncomment following and check it manually */
//        classTest(HasCatch::class.java, HasCatchExpected::class.java, configurationManagerSaveReturn!!, methodConfigsSaveReturn)
    }

    @Test
    fun hasIf() {
        classTest(HasIf::class.java, configurationManager!!, methodConfigs)
        /* If instrumentation changed uncomment following and check it manually */
//        classTest(HasIf::class.java, HasIfExpected::class.java, configurationManager!!, methodConfigs)
    }

    private fun classTest(testedClass: Class<*>,
                          expectedClass: Class<*>,
                          configurationManager: AgentConfigurationManager,
                          methodConfigs: List<MethodConfig>,
                          hasSystemCL: Boolean = true,
                          isSystemClass: Boolean = false) {
        val expectedBytecodeFile = Generator.generate(expectedClass)
        instrumentAndCompare(testedClass, configurationManager, methodConfigs, expectedBytecodeFile, hasSystemCL,
                isSystemClass, true)
    }

    private fun instrumentAndCompare(testedClass: Class<*>,
                                     configurationManager: AgentConfigurationManager,
                                     methodConfigs: List<MethodConfig>,
                                     expectedBytecodeFile: File,
                                     hasSystemCL: Boolean,
                                     isSystemClass: Boolean,
                                     deleteExpectedFile: Boolean = false) {
        val bytes = instrumentClass(testedClass, configurationManager, methodConfigs, hasSystemCL, isSystemClass)
        val fileName = removePackage(testedClass.name)

        val outFile = File("src/test/resources/actual/$fileName.txt")
        printInstructionsToFile(bytes, outFile)

        compareFiles(expectedBytecodeFile, outFile, deleteExpectedFile)
    }

    private fun classTest(testedClass: Class<*>,
                          configurationManager: AgentConfigurationManager,
                          methodConfigs: List<MethodConfig>,
                          hasSystemCL: Boolean = true,
                          isSystemClass: Boolean = false) {
        val expectedBytecodeFile = File("src/test/resources/expected/" + removePackage(testedClass.name) + ".txt")
        instrumentAndCompare(testedClass, configurationManager, methodConfigs, expectedBytecodeFile, hasSystemCL,
                isSystemClass)
    }

    private fun printInstructionsToFile(bytes: ByteArray, outFile: File) {
        val cr = ClassReader(bytes)
        val cw = ClassWriter(cr, 0)
        cr.accept(
                TraceClassVisitor(cw, PrintWriter(
                        FileOutputStream(outFile)
                )), ClassReader.SKIP_DEBUG
        )
    }

    private fun instrumentClass(testedClass: Class<*>,
                                configurationManager: AgentConfigurationManager?,
                                methodConfigs: List<MethodConfig>,
                                hasSystemCL: Boolean,
                                isSystemClass: Boolean = false): ByteArray {
        val fullName = testedClass.name

        val bytes = getBytes(testedClass)
        val cr = ClassReader(bytes)
        val cw = ClassWriter(cr, ClassWriter.COMPUTE_FRAMES)
        val traceClassVisitor = TraceClassVisitor(
                cw,
                PrintWriter(System.out)
        )
        cr.accept(
                ProfilingClassVisitor(
                        traceClassVisitor,
                        fullName.replace('.', '/'),
                        hasSystemCL,
                        methodConfigs,
                        configurationManager,
                        isSystemClass
                ),
                ClassReader.SKIP_FRAMES or ClassReader.SKIP_DEBUG
        )

        return cw.toByteArray()
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
