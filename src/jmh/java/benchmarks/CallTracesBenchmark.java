package benchmarks;

import com.github.kornilova_l.flamegraph.plugin.server.trees.flamegraph_format_trees.FlamegraphFormatTreesSet;
import org.openjdk.jmh.annotations.*;

import java.io.File;
import java.util.concurrent.TimeUnit;

@Fork(3)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
public class CallTracesBenchmark {

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void buildTree() {
        new FlamegraphFormatTreesSet(new File("src/jmh/resources/idea.jfr"));
    }
}
