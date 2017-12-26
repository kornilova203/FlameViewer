package com.github.kornilova_l.flamegraph.proxy;

import com.github.kornilova_l.proxy_test_classes.ReturnsValue;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.RecursiveTask;

import static com.github.kornilova_l.flamegraph.proxy.TestWithProxy.invokeTestMethod;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests proxy with class that is loaded by {@link MyClassLoader}
 * !!!
 * Each test must be run separately
 * because some of them modify classpath
 */
public class TestWithoutProxy {

    @Test
    public void testWithoutProxy() {
        InvocationTargetException exception = null;
        TestWithProxy.loadAgent();
        try {
            invokeTestMethod(ReturnsValue.class);
        } catch (InvocationTargetException e) {
            exception = e;
        }
        assertNotNull(exception);
        /* cannot find proxy */
        assertEquals("com/github/kornilova_l/flamegraph/proxy/Proxy", exception.getTargetException().getMessage());
    }
}
