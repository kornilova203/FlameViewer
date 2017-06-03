package Profiler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Profiler {

    /**
     * Register start of method
     *
     * @return State object which contains info about method. This object has methodFinish() method
     */
    public static State methodStart() {
        long time = System.nanoTime();
        Thread thread = Thread.currentThread();
        StackTraceElement method = thread.getStackTrace()[2];
        String name = method.getClassName() + "." + method.getMethodName();
        int threadHashCode = thread.hashCode();

        writeToFile(threadHashCode + " s " + name + " " + time);

        return new State(name, threadHashCode);
    }

    static synchronized void writeToFile(String str) {
        // TODO: check if it is the best way to write data to file
        try (FileWriter fw = new FileWriter("out.txt", true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(str);
        } catch (IOException ignored) {

        }
    }

}