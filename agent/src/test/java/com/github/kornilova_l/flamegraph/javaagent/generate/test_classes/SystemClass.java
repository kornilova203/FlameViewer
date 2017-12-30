package com.github.kornilova_l.flamegraph.javaagent.generate.test_classes;

/**
 * Sometimes it is needed to instrument system classes.
 * System classes are loaded by bootstrap and it is not possible to use
 * not system classes in their methods.
 */
public class SystemClass {
    public void method() {
        System.out.println("Hello, I am a method of System Class. " +
                "I do not know about any other classes except system classes");
    }
}
