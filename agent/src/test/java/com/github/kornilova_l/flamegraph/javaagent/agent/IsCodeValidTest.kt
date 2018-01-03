package com.github.kornilova_l.flamegraph.javaagent.agent

import com.github.kornilova_l.flamegraph.configuration.MethodConfig
import com.github.kornilova_l.flamegraph.javaagent.generate.test_classes.*
import com.github.kornilova_l.flamegraph.javaagent.getBytes
import org.junit.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.ClassWriter.COMPUTE_FRAMES
import org.objectweb.asm.util.CheckClassAdapter

/**
 * Use [org.objectweb.asm.util.CheckClassAdapter] to check if
 * generated instructions are valid
 */
class IsCodeValidTest {
    @Test
    fun isCodeValidTest() {
        doTest(HasCatch::class.java)
        doTest(HasIf::class.java)
        doTest(SaveParameters::class.java)
        doTest(SaveReturnValue::class.java)
        doTest(ThrowsException::class.java)
        doTest(UseProxy::class.java)
        doTest(SystemClass::class.java, true)
    }

    private fun doTest(clazz: Class<*>, isSystemClass: Boolean = false) {
        var bytes = getBytes(clazz)

        var cr = ClassReader(bytes)
        var cw = ClassWriter(cr, COMPUTE_FRAMES)
        val methodConfigs = listOf(MethodConfig("*", "*", "(*)"))
        val configManager = AgentConfigurationManager(listOf("*.*(*)"))
        cr.accept(
                ProfilingClassVisitor(cw, clazz.name.replace('.', '/'), false,
                        methodConfigs, configManager, isSystemClass), ClassReader.SKIP_FRAMES
        )

        bytes = cw.toByteArray()
        cr = ClassReader(bytes)
        cw = ClassWriter(cr, 0)

        cr.accept(CheckClassAdapter(cw), 0)
    }
}
