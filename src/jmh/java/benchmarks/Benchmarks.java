package benchmarks;

import org.jetbrains.test.SimpleExample;
import org.jetbrains.test.TestApplicationWithoutSleep;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class Benchmarks {

    private SimpleExample sa = new SimpleExample();
    private TestApplicationWithoutSleep taws = new TestApplicationWithoutSleep();

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void emptyBenchmark() {
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Fork(jvmArgs = "-javaagent:/home/lk/JetBrains/profiler/build/libs/profiler-1.0.jar")
    public void simpleExample() {
        sa.start();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void testApplicationWithoutSleep() {
        taws.start();
    }
}
