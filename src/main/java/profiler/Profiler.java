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
     * @param time
     * @param owner
     * @param name
     * @param threadId
     * @param parameters
     */
    public static void methodEnter(long time, String owner, String name, long threadId, Object[] parameters) {
        EventProtos.Event.Enter enter = EventProtos.Event.Enter.newBuilder()
                .setClassName(owner)
                .setMethodName(name)
//                .addAllParameters()
                .build();

        EventProtos.Event event = EventProtos.Event.newBuilder()
                .setThreadId(threadId)
                .setTime(time)
//                .setEnter()
                .build();
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