package com.github.kornilova_l.flamegraph.proxy;

import com.github.kornilova_l.proxy_test_classes.ReturnsValue;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

import static com.github.kornilova_l.flamegraph.proxy.TestWithProxy.invokeTestMethod;
import static org.junit.Assert.assertNull;

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
            exception = e; // if method cannot find proxy then it will throw an exception
        }
        /* classes that cannot load Proxy are not instrumented so exception will not be thrown */
        assertNull(exception);
    }
}
