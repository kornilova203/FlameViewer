package Profiler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Profiler {

    static synchronized void writeToFile(String str) {
        try (FileWriter fw = new FileWriter("out.txt", true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(str);
        } catch (IOException ignored) {

        }
    }

    /**
     * Record start of method
     */
    public static State methodStart(String arg) {
        long time = System.nanoTime();
        Thread thread = Thread.currentThread();
        StackTraceElement method = thread.getStackTrace()[2];
        String name = method.getClassName() + "." + method.getMethodName();
        int threadHashCode = thread.hashCode();

        writeToFile(threadHashCode + " s " + name + " " + time + " " + arg);

        return new State(name, threadHashCode);
    }

    //    private static final HashMap<Integer, ThreadInfo> threadsInfo = new HashMap<>(); // map thread's hashCodes to ThreadInfo
//    private static int lastThreadId = -1; // it is probably faster than using threadsInfo.size()
//
//    private static class ThreadInfo {
//        int threadId;
//        HashMap<String, Integer> methodIds = new HashMap<>(); // method name -> method id
//        int lastMethodId = -1; // it is probably faster than using methodIds.size()
//    }
}