package com.github.kornilova_l.flamegraph.proxy;

import com.github.kornilova_l.proxy_test_classes.ThrowsException;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import static com.github.kornilova_l.flamegraph.proxy.TestWithProxy.*;
import static org.junit.Assert.assertNull;

/**
 * !!!
 * Each test must be run separately
 * because some of them modify classpath
 */
public class TestThrowsException {
    @Test
    public void testThrowsException() {
        addProxy();
        InvocationTargetException exception = null;
        loadAgent();
        clearOutputFile();
        try {
            invokeTestMethod(ThrowsException.class);
        } catch (InvocationTargetException e) {
            exception = e;
        }
        assertNull(exception);
        try (InputStream inputStream = new FileInputStream(outputFile)) {
            assertNewClass(inputStream, "com/github/kornilova_l/proxy_test_classes/ThrowsException");

            assertNewThread(inputStream, "Test worker");

            assertMethodEvent(inputStream, "(JLjava/lang/String;D)I", true);

            assertEndOfEvents(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
