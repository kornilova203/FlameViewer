package com.github.kornilova_l.flight_recorder;

import com.jrockit.mc.flightrecorder.FlightRecording;
import com.jrockit.mc.flightrecorder.FlightRecordingLoader;
import com.jrockit.mc.flightrecorder.spi.IEvent;
import com.jrockit.mc.flightrecorder.spi.IEventType;
import com.jrockit.mc.flightrecorder.spi.IField;
import com.jrockit.mc.flightrecorder.spi.IView;

import java.io.*;
import java.util.Collection;
import java.util.zip.GZIPInputStream;

public class JmcParser {

    public static void main(String[] args) throws ClassNotFoundException {
        try (GZIPInputStream gzipStream = new GZIPInputStream(
                new FileInputStream(
                        new File("src/main/resources/flight-recorder/flight_recording_180121comintellijideaMain17940.jfr")));
        ) {
            FlightRecording recording = FlightRecordingLoader.loadStream(gzipStream);
            IView view = recording.createView();
            int count = 0;
            for (IEvent event : view) {
                count++;
//                System.out.println(event.getEventType());
            }
            System.out.println("Fount " + count + " events");
            for (IEventType type : recording.getEventTypes()) {
                System.out.println(type.getName());
                printAttributes(type.getFields());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printAttributes(Collection<IField> fields) {
        for (IField field : fields) {
            System.out.println(String.format("   %s (relkey: %s)", field.getName(), field.getRelationalKey()));
        }
    }
}
