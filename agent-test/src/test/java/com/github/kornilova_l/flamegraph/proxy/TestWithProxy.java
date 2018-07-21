package com.github.kornilova_l.flamegraph.proxy;

import com.ea.agentloader.AgentLoader;
import com.github.kornilova_l.flamegraph.proto.EventProtos;
import com.github.kornilova_l.proxy_test_classes.ReturnsValue;
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
 * !!!
 * Each test must be run separately
 * because some of them modify classpath
 */
public class TestWithProxy {
    static final File outputFile = new File("src/test/resources/output.ser");
    private static final Path pathToAgentDir = Paths.get(new File(".").toPath().toAbsolutePath().getParent().getParent().toString(), "agent", "build", "libs").toAbsolutePath();

    static void loadAgent() {
        AgentLoader.loadAgent(Paths.get(pathToAgentDir.toString(), "javaagent.jar").toString(),
                outputFile.toPath().toString()
                        + "&" + new File("src/test/resources/config.txt").toPath().toAbsolutePath().toString());
    }

    static void invokeTestMethod(Class testClazz) throws InvocationTargetException {
        ClassLoader classLoader = new MyClassLoader();
        try {
            Class<?> testModuleClass = classLoader.loadClass(testClazz.getCanonicalName());
            Method method = testModuleClass.getMethod("run", long.class, String.class, double.class);

            Class clazz = classLoader.loadClass(testClazz.getCanonicalName());
            Object classInstance = clazz.newInstance();
            method.invoke(classInstance, 1L, "hello", .5);
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            if (!e.getCause().getMessage().equals("I am an exception")) { // if it is exception of Proxy
                throw e;
            }
        }
        try {
            /* wait until everything is written to the file */
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static void addProxy() {
        /* add Proxy and StartData to classpath */
        URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class<?> urlClass = URLClassLoader.class;
        try {
            Method method = urlClass.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(urlClassLoader,
                    new URL(Paths.get(pathToAgentDir.toString(), "proxy.jar").toUri().toString().replaceAll("%20", " ")));
        } catch (NoSuchMethodException | IllegalAccessException | MalformedURLException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    static void clearOutputFile() {
        if (!outputFile.exists()) {
            return;
        }
        if (!outputFile.delete()) {
            throw new RuntimeException("Cannot delete output file");
        }
    }

    @SuppressWarnings("SameParameterValue")
    static void assertMethodEvent(InputStream inputStream, String desc, boolean hasThrowable) throws IOException {
        EventProtos.Event event = EventProtos.Event.parseDelimitedFrom(inputStream);
        assertEquals(EventProtos.Event.TypeCase.METHODEVENT, event.getTypeCase());
        assertEquals(desc, event.getMethodEvent().getDesc());

        EventProtos.Var.Object t = event.getMethodEvent().getThrowable();
        if (hasThrowable) {
            assertEquals("class java.lang.RuntimeException", t.getType());
        } else {
            assertEquals("", t.getType());
        }
    }

    static void assertEndOfEvents(InputStream inputStream) throws IOException {
        EventProtos.Event event = EventProtos.Event.parseDelimitedFrom(inputStream);
        assertNull(event);
    }

    @SuppressWarnings("SameParameterValue")
    static void assertNewThread(InputStream inputStream, String threadName) throws IOException {
        EventProtos.Event event = EventProtos.Event.parseDelimitedFrom(inputStream);
        assertEquals(EventProtos.Event.TypeCase.NEWTHREAD, event.getTypeCase());
        assertEquals(threadName, event.getNewThread().getName());
    }

    static void assertNewClass(InputStream inputStream, String className) throws IOException {
        EventProtos.Event event = EventProtos.Event.parseDelimitedFrom(inputStream);
        assertEquals(EventProtos.Event.TypeCase.NEWCLASS, event.getTypeCase());
        assertEquals(className, event.getNewClass().getName());
    }

    @Test
    public void testReturnValue() {
        addProxy();
        InvocationTargetException exception = null;
        loadAgent();
        clearOutputFile();
        try {
            invokeTestMethod(ReturnsValue.class);
        } catch (InvocationTargetException e) {
            exception = e;
        }
        assertNull(exception);
        try (InputStream inputStream = new FileInputStream(outputFile)) {
            assertNewClass(inputStream, "com/github/kornilova_l/proxy_test_classes/ReturnsValue");

            assertNewThread(inputStream, "Test worker");

            assertMethodEvent(inputStream, "(JLjava/lang/String;D)I", false);

            assertEndOfEvents(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
