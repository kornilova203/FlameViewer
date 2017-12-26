package com.github.kornilova_l.flamegraph.proxy;

import com.ea.agentloader.AgentLoader;
import com.github.kornilova_l.flamegraph.proto.EventProtos;
import com.github.kornilova_l.proxy_test_classes.ReturnsValue;
import com.github.kornilova_l.proxy_test_classes.TestModule;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests proxy with class that is loaded by {@link MyClassLoader}
 */
public class TestWithProxy {
    private static final Path pathToAgentDir = Paths.get(new File(".").toPath().toAbsolutePath().getParent().getParent().toString(), "agent", "build", "libs").toAbsolutePath();
    private static final File outputFile = new File("src/test/resources/output.ser");

    static void loadAgent() {
        AgentLoader.loadAgent(Paths.get(pathToAgentDir.toString(), "javaagent.jar").toString(),
                outputFile.toPath().toString()
                        + "&" + new File("src/test/resources/config.txt").toPath().toAbsolutePath().toString());
    }

    static void invokeTestMethod() throws InvocationTargetException {
        ClassLoader classLoader = new MyClassLoader();
        try {
            Class<?> testModuleClass = classLoader.loadClass(TestModule.class.getCanonicalName());
            Method method = testModuleClass.getMethod("run", long.class, String.class, double.class);

            Class clazz = classLoader.loadClass(ReturnsValue.class.getCanonicalName());
            Object classInstance = clazz.newInstance();
            method.invoke(classInstance, 1L, "hello", .5);
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void addProxy() {
        /* add Proxy and StartData to classpath */
        URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class<?> urlClass = URLClassLoader.class;
        try {
            Method method = urlClass.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(urlClassLoader, Paths.get(pathToAgentDir.toString(), "proxy.jar").toUri().toURL());
        } catch (NoSuchMethodException | IllegalAccessException | MalformedURLException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testWithProxy() {
        addProxy();
        InvocationTargetException exception = null;
        loadAgent();
        try {
            invokeTestMethod();
        } catch (InvocationTargetException e) {
            exception = e;
        }
        assertNull(exception);
        try (InputStream inputStream = new FileInputStream(outputFile)) {
            EventProtos.Event event = EventProtos.Event.parseDelimitedFrom(inputStream);
            assertEquals(EventProtos.Event.TypeCase.NEWCLASS, event.getTypeCase());
            assertEquals("com/github/kornilova_l/proxy_test_classes/ReturnsValue", event.getNewClass().getName());

            event = EventProtos.Event.parseDelimitedFrom(inputStream);
            assertEquals(EventProtos.Event.TypeCase.NEWTHREAD, event.getTypeCase());
            assertEquals("main", event.getNewThread().getName());

            event = EventProtos.Event.parseDelimitedFrom(inputStream);
            assertEquals(EventProtos.Event.TypeCase.METHODEVENT, event.getTypeCase());
            assertEquals("(JLjava/lang/String;D)I", event.getMethodEvent().getDesc());

            event = EventProtos.Event.parseDelimitedFrom(inputStream);
            assertNull(event);

            if (!outputFile.delete()) {
                throw new RuntimeException("Cannot delete output file");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
