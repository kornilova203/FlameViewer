package benchmarks;

import com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees.SerTreesSet;
import org.openjdk.jmh.annotations.*;

import java.io.File;
import java.util.concurrent.TimeUnit;

@Fork(3)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
public class CallTreeBenchmark {

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void buildTree() {
        new SerTreesSet(new File("src/jmh/resources/idea.ser")).getCallTree(null);
    }

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 600; i ++) {
            new SerTreesSet(new File("src/jmh/resources/idea.ser")).getCallTree(null);
        }
        System.out.println("Time: " + (System.currentTimeMillis() - startTime) / 1000);
    }
}
