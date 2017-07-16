package com.github.kornilova_l.flamegraph.javaagent.agent;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Liudmila Kornilova
 * on 16.07.17.
 */
public class AgentConfigurationManagerTest {
    @Test
    public void findIncludingConfigs() throws Exception {
    }

    @Test
    public void newMethodConfig() throws Exception {
        assertEquals("MyClass.someMethod()",
        AgentConfigurationManager.newMethodConfig(
                "MyClass",
                "someMethod",
                "()V"
        ).toString());

        assertEquals("my_package.MyClass.someMethod()",
                AgentConfigurationManager.newMethodConfig(
                        "my_package.MyClass",
                        "someMethod",
                        "()V"
                ).toString());

        assertEquals("my_package.MyClass.someMethod(java.lang.String)",
                AgentConfigurationManager.newMethodConfig(
                        "my_package.MyClass",
                        "someMethod",
                        "(Ljava/lang/String;)V"
                ).toString());

        assertEquals("my_package.MyClass.someMethod(java.lang.String[][], int)",
                AgentConfigurationManager.newMethodConfig(
                        "my_package.MyClass",
                        "someMethod",
                        "([[Ljava/lang/String;I)V"
                ).toString());

        // nested class
        assertEquals("my_package.SomeClass$NestedClass.someMethod(boolean, byte, short[][])",
                AgentConfigurationManager.newMethodConfig(
                        "my_package.SomeClass$NestedClass",
                        "someMethod",
                        "(ZB[[S)V"
                ).toString());

        // (LSomeClass$NestedStaticClass;)V
        // nested class as a parameter
        assertEquals("my_package.SomeClass$NestedClass.someMethod(SomeClass$NestedStaticClass)",
                AgentConfigurationManager.newMethodConfig(
                        "my_package.SomeClass$NestedClass",
                        "someMethod",
                        "(LSomeClass$NestedStaticClass;)V"
                ).toString());
    }

    @Test
    public void isMethodExcluded() throws Exception {
    }

    @Test
    public void findIncludingConfigs1() throws Exception {
    }

}