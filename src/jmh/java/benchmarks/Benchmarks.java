package benchmarks;

import org.jetbrains.test.SimpleExample;
import org.jetbrains.test.Test;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class Benchmarks {

    private Test taws = new Test();

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void emptyBenchmark() {
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void test() {
        taws.start();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(jvmArgs = "-javaagent:/home/lk/JetBrains/profiler/build/libs/javaagent.jar")
    public void testProfiled() {
        taws.start();
    }
}
