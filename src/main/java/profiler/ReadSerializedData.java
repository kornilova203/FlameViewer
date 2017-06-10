package profiler;

import java.io.*;

public class ReadSerializedData {
    public static void print() {
        File file = new File("out/events.ser");
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);


            EventProtos.Event event = EventProtos.Event.parseDelimitedFrom(inputStream);
            while (event != null) {
                System.out.println(event.toString());
                event = EventProtos.Event.parseDelimitedFrom(inputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        print();
    }
}
