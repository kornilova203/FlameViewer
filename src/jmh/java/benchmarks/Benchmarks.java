package benchmarks;

import org.jetbrains.test.SimpleExample;
import org.jetbrains.test.SimpleExampleWithProfiler;
import org.jetbrains.test.TestApplication;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class Benchmarks {

    private TestApplication ta = new TestApplication();
    private SimpleExample sa = new SimpleExample();
    private SimpleExampleWithProfiler sawp = new SimpleExampleWithProfiler();

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
    public void simpleExampleWithProfiler() {
        sawp.start();
    }
}
