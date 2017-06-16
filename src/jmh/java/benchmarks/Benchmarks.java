package benchmarks;

import com.github.kornilova_l.samples.Sample;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class Benchmarks {

    private Sample sample = new Sample();

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void emptyBenchmark() {
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void test() {
        sample.start();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(jvmArgs = "-javaagent:/home/lk/java-profiling-plugin/build/libs/javaagent.jar")
    public void testProfiled() {
        sample.start();
    }
}
