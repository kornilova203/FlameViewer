package com.github.kornilova_l.flamegraph.proxy;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

import static com.github.kornilova_l.flamegraph.proxy.TestWithProxy.invokeTestMethod;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests proxy with class that is loaded by {@link MyClassLoader}
 */
public class TestWithoutProxy {

    @Test
    public void testWithoutProxy() {
        InvocationTargetException exception = null;
        TestWithProxy.loadAgent();
        try {
            invokeTestMethod();
        } catch (InvocationTargetException e) {
            exception = e;
        }
        assertNotNull(exception);
        /* cannot find proxy */
        assertEquals("com/github/kornilova_l/flamegraph/proxy/Proxy", exception.getTargetException().getMessage());
    }
}
