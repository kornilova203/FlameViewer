package benchmarks;

import samples.SimpleExample;
import samples.Test;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class Benchmarks {

    private Test taws = new Test();
    private SimpleExample simpleExample = new SimpleExample();

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void emptyBenchmark() {
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void test() {
        simpleExample.start();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(jvmArgs = "-javaagent:/home/lk/java-profiling-plugin/build/libs/javaagent.jar")
    public void testProfiled() {
        simpleExample.start();
    }
}
