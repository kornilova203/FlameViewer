package benchmarks;

import com.github.kornilova_l.flamegraph.javaagent.logger.LoggerQueue;
import com.github.kornilova_l.flamegraph.proxy.Proxy;
import com.github.kornilova_l.flamegraph.proxy.StartData;
import org.openjdk.jmh.annotations.*;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;

@Fork(3)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@State(Scope.Thread)
public class ReflectionBenchmark {

    public static void main(String[] args) throws InterruptedException {
        LoggerQueue.initLoggerQueue();
        new ReflectionBenchmark().withReflection();
    }

    @SuppressWarnings("unused")
    @Setup(Level.Iteration)
    public void initLogger() {
        LoggerQueue.initLoggerQueue();
    }

    @SuppressWarnings("unused")
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void withoutReflection() throws InterruptedException {
        StartData startData = new StartData(System.currentTimeMillis(), new Object[0]);
        try {
            Thread.sleep(2);
            startData.setDuration(System.currentTimeMillis());

            if (startData.getDuration() > 1) {
                Proxy.addToQueue(null,
                        startData.getStartTime(),
                        startData.getDuration(),
                        startData.getParameters(),
                        Thread.currentThread(),
                        "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/SystemClass",
                        "method",
                        "()V",
                        true,
                        ""
                );
            }
        } catch (Throwable t) {
            if (!startData.isThrownByMethod()) {
                startData.setDuration(System.currentTimeMillis());
                if (startData.getDuration() > 1) {
                    Proxy.addToQueue(null,
                            false,
                            startData.getStartTime(),
                            startData.getDuration(),
                            startData.getParameters(),
                            Thread.currentThread(),
                            "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/SystemClass",
                            "method",
                            "()V",
                            true,
                            ""
                    );
                }
            }
            throw t;
        }
    }

    @SuppressWarnings({"RedundantCast", "WeakerAccess"})
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void withReflection() throws InterruptedException {
        try {
            Class<?> proxyClass = ClassLoader.getSystemClassLoader().loadClass("com.github.kornilova_l.flamegraph.proxy.Proxy");
            Class<?> startDataClass = ClassLoader.getSystemClassLoader().loadClass("com.github.kornilova_l.flamegraph.proxy.StartData");
            Object startData = proxyClass.getMethod("createStartData", long.class, Object[].class)
                    .invoke(null, System.currentTimeMillis(), new Object[0]);
            try {
                Thread.sleep(2);
                startDataClass.getMethod("setDuration", long.class).invoke(startData, System.currentTimeMillis());
                if ((long) startDataClass.getMethod("getDuration").invoke(startData) > 1) {
                    proxyClass.getMethod("addToQueue", Object.class, long.class, long.class, Object[].class, Thread.class,
                            String.class, String.class, String.class, boolean.class, String.class)
                            .invoke(null,
                                    null,
                                    (long) startDataClass.getMethod("getStartTime").invoke(startData),
                                    (long) startDataClass.getMethod("getDuration").invoke(startData),
                                    (Object[]) startDataClass.getMethod("getParameters").invoke(startData),
                                    Thread.currentThread(),
                                    "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/SystemClass",
                                    "method",
                                    "()V",
                                    true,
                                    ""
                            );
                }
            } catch (Throwable t) {
                if (!((boolean) startDataClass.getMethod("isThrownByMethod").invoke(startData))) {
                    startDataClass.getMethod("setDuration", long.class).invoke(startData, System.currentTimeMillis());
                    if ((long) startDataClass.getMethod("getDuration").invoke(startData) > 1) {
                        proxyClass.getMethod("addToQueue", Throwable.class, boolean.class, long.class, long.class,
                                Object[].class, Thread.class, String.class, String.class, String.class, boolean.class, String.class)
                                .invoke(null,
                                        null,
                                        false,
                                        (long) startDataClass.getMethod("getStartTime").invoke(startData),
                                        (long) startDataClass.getMethod("getDuration").invoke(startData),
                                        (Object[]) startDataClass.getMethod("getParameters").invoke(startData),
                                        Thread.currentThread(),
                                        "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/SystemClass",
                                        "method",
                                        "()V",
                                        true,
                                        ""
                                );
                    }
                }
                throw t;
            }
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
