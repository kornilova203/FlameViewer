package com.github.kornilova_l.flamegraph.configuration

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConfigurationTest {
    private val className = "my_package.MyClass"
    private val methodName = "my_method"
    private val allMethods: Configuration = Configuration(listOf("*.*(*)"))
    private val noMethods: Configuration = Configuration(listOf("!*.*(*)"))
    private val someMethods: Configuration = Configuration(listOf("my_package.MyClass.*(*)", "!*.*(int)"))

    @Test
    fun isMethodInstrumented() {
        assertTrue(allMethods.isMethodInstrumented(className, methodName, null))
        assertFalse(noMethods.isMethodInstrumented(className, methodName, null))

        assertTrue(allMethods.isMethodInstrumented(className, methodName, listOf("int")))
        assertFalse(noMethods.isMethodInstrumented(className, methodName, listOf("int")))

        assertTrue(someMethods.isMethodInstrumented(className, methodName, null))
        assertTrue(someMethods.isMethodInstrumented(className, methodName, listOf()))
        assertFalse(someMethods.isMethodInstrumented(className, methodName, listOf("int")))
    }
}