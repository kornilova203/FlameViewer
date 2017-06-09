package samples;

import java.io.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
//        FileWriter fileWriter = new FileWriter("out.txt");
//        fileWriter.close();
        ExecutorService service = Executors.newFixedThreadPool(3);
        for(int i = 0; i < 5; i++) {
            int start = 100 * i;
            List<String> arguments = IntStream.range(start, start + 10)
                    .mapToObj(Integer :: toString)
                    .collect(Collectors.toList());
            service.submit(() -> new DummyApplication(arguments).start());
        }
        service.shutdown();
    }
}
