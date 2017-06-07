package profiler;

import java.io.*;
import java.util.zip.GZIPOutputStream;

public class Profiler {
    private static final GZIPOutputStream zip;
    private static final OutputStreamWriter outputStreamWriter;
    private static File file = new File("out/out.gz");
//    private static BufferedWriter writer = null;

    static {
        // temp variable is for avoiding "might not have been initialized"
        GZIPOutputStream tempZip = null;
        OutputStreamWriter tempOutputStreamWriter = null;
        try {
            tempZip = new GZIPOutputStream(
                    new FileOutputStream(new File("out/out.gz")));
            tempOutputStreamWriter = new OutputStreamWriter(tempZip, "UTF-8");
        } catch (IOException ignored) {
        }
        zip = tempZip;
        outputStreamWriter = tempOutputStreamWriter;
    }


    /**
     * Register start of method
     *
     * @return State object which contains info about method. This object has methodFinish() method
     */
    public static State methodStart(String desc, String parameters) {
        long time = System.nanoTime();
        Thread thread = Thread.currentThread();
        StackTraceElement method = thread.getStackTrace()[2];
        String name = method.getClassName() + "." + method.getMethodName() + "⊗" + desc;
        long threadId = thread.getId();

        log(threadId + "⊗s⊗" + name + "⊗" + parameters + "⊗" + time);

        return new State(name, threadId);
    }

    public static synchronized void log(String str) {
        // TODO: check is there better ways to write data to file (GZIPOutputStream? java.util.logging?)
//        try (BufferedWriter writer =
//                     new BufferedWriter(
//                             new OutputStreamWriter(
//                                     new GZIPOutputStream(
//                                             new FileOutputStream(
//                                                     new File("out/out.gz"))),
//                                     "UTF-8"))) {
//        FileOutputStream fileOutputStream = null;
//        GZIPOutputStream stream = null;
//        try (FileOutputStream fileOutputStream = new FileOutputStream(file, true);
//                GZIPOutputStream stream = new GZIPOutputStream(fileOutputStream)) {
//
//            stream.write("hello".getBytes());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        try (FileWriter fw = new FileWriter("out/out.txt", true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(str);
        } catch (IOException ignored) {

        }
    }

}