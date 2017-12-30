package com.github.kornilova_l.flamegraph.javaagent.agent

import com.github.kornilova_l.flamegraph.configuration.MethodConfig
import com.github.kornilova_l.flamegraph.javaagent.getBytes
import org.junit.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.ClassWriter.COMPUTE_FRAMES
import org.objectweb.asm.util.CheckClassAdapter
import java.io.FileOutputStream

class IsCodeValidTest {
    @Test
    fun isCodeValidTest() {
        var bytes = getBytes(FileOutputStream::class.java)

        var cr = ClassReader(bytes)
        var cw = ClassWriter(cr, COMPUTE_FRAMES)
        val methodConfigs = listOf(MethodConfig("java.io.FileOutputStream", "write", "(byte[])"))
        val configManager = AgentConfigurationManager(listOf("java.io.FileOutputStream.write(byte[])"))
        cr.accept(
                ProfilingClassVisitor(cw, "java/io/FileOutputStream", false,
                        methodConfigs, configManager), ClassReader.SKIP_FRAMES
        )

        bytes = cw.toByteArray()
        cr = ClassReader(bytes)
        cw = ClassWriter(cr, 0)

        cr.accept(CheckClassAdapter(cw), 0)
    }
}
