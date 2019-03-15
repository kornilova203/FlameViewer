package com.github.korniloval.flameviewer.trees.calltraces.flamegraph

import com.github.korniloval.flameviewer.converters.calltraces.flamegraph.StacksParser
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StacksParserTest {

    @Test
    fun doCallsContainParametersTest() {
        var stacks = hashMapOf(Pair("1", 1))
        assertFalse(StacksParser.doCallsContainParameters(stacks))

        stacks = hashMapOf(Pair("1;2", 1), Pair("void hello();void hello()", 1))
        assertFalse(StacksParser.doCallsContainParameters(stacks))

        stacks = hashMapOf(Pair("void hello()", 1), Pair("1", 1))
        assertFalse(StacksParser.doCallsContainParameters(stacks))

        stacks = hashMapOf(Pair("void a();void a()", 1))
        assertTrue(StacksParser.doCallsContainParameters(stacks))

        stacks = hashMapOf(Pair(" ()", 1))
        assertTrue(StacksParser.doCallsContainParameters(stacks))

        stacks = hashMapOf(Pair("a ()", 1))
        assertTrue(StacksParser.doCallsContainParameters(stacks))

        stacks = hashMapOf(Pair("void a(); a()", 1))
        assertTrue(StacksParser.doCallsContainParameters(stacks))

        stacks = hashMapOf(Pair("void a();void a)(", 1))
        assertFalse(StacksParser.doCallsContainParameters(stacks))
    }
}
