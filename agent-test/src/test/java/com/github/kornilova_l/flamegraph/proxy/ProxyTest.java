package com.github.kornilova_l.flamegraph.proxy;

import com.ea.agentloader.AgentLoader;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Paths;

/**
 * Tests proxy with class that is loaded by {@link MyClassLoader}
 */
public class ProxyTest {
    @BeforeClass
    public static void loadAgent() {
        AgentLoader.loadAgent(Paths.get(new File(".").toPath().toAbsolutePath().getParent().getParent().toString(), "agent", "build", "libs", "javaagent.jar").toAbsolutePath().toString(),
                new File("src/test/resources/output.ser").toPath().toAbsolutePath().toString()
                        + "&" + new File("src/test/resources/config.txt").toPath().toAbsolutePath().toString());
    }

    @Test
    public void doTest() {
        ClassLoader classLoader = new MyClassLoader();
        try {
            Class<?> testModuleClass = classLoader.loadClass(TestModule.class.getCanonicalName());
            Method method = testModuleClass.getMethod("run", long.class, String.class, double.class);

            Class clazz = classLoader.loadClass(ReturnsValue.class.getCanonicalName());
            Object classInstance = clazz.newInstance();
            method.invoke(classInstance, 1L, "hello", .5);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException |
                InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
