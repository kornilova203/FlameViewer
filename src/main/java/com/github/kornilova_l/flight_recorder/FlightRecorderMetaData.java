package com.github.kornilova_l.flight_recorder;
/*
 * Copyright (C) 2013, Marcus Hirt
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import com.jrockit.mc.flightrecorder.FlightRecording;
import com.jrockit.mc.flightrecorder.FlightRecordingLoader;
import com.jrockit.mc.flightrecorder.spi.IEvent;
import com.jrockit.mc.flightrecorder.spi.IEventType;
import com.jrockit.mc.flightrecorder.spi.IField;
import com.jrockit.mc.flightrecorder.spi.IProducer;
import com.jrockit.mc.flightrecorder.spi.IView;

/**
 * This is a quick HACK showing Flight Recorder metadata. Use at your own peril. All is smacked into one file for easy
 * download. Actually reading this may cause headache and nausea. Pretty please don't copy from this file - it's not
 * meant to be production quality code.
 *
 * @author Marcus Hirt
 */
public class FlightRecorderMetaData {
    public static void main(String[] args) throws ClassNotFoundException {
        try (GZIPInputStream gzipStream = new GZIPInputStream(
                new FileInputStream(
                        new File("src/main/resources/flight-recorder/flight_recording_180121comintellijideaMain17940.jfr")));
        ) {
            FlightRecording recording = FlightRecordingLoader.loadStream(gzipStream);
            Map<IProducer, List<IEventType>> typeMap = new HashMap<>();

            for (IEventType type : recording.getEventTypes()) {
                putType(typeMap, type.getProducer(), type);
            }

            for (Entry<IProducer, List<IEventType>> entry : typeMap.entrySet()) {
                System.out.println(String.format("Producer: %s (%s)", entry.getKey().getName(), entry.getKey()
                        .getURIString()));
                TypeNode root = createTree(entry.getValue(), recording);
                render(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void render(TypeNode node) {
        if (node.getStats() == null || node.getStats().getEventType() == null) {
            Collections.sort(node.getChildren(), Comparator.comparing(TypeNode::getPathElement));
            for (TypeNode child : node.getChildren()) {
                render(child);
            }
            fancyPrint(getTypeNodesWithTypes(node.getChildren()));
        }
    }

    private static void fancyPrint(List<TypeNode> list) {
        if (list.isEmpty()) {
            return;
        }
        System.out.println(String.format(" %s:", printPath(list.get(0))));
        for (TypeNode node : list) {
            System.out.println(String.format("   %s (%s) (%d)", node.getStats().getEventType().getName(), node
                    .getPathElement(), node.getStats().getCount()));
            fancyPrint(node.getStats().getEventType().getFields());
            System.out.println();
        }
    }

    private static void fancyPrint(Collection<IField> fields) {
        for (IField field : fields) {
            System.out.println(String.format("      %s (type:%s, relational key: %s, description:%s)", field.getName(),
                    field.getType().name(), field.getRelationalKey(), field.getDescription()));
        }
    }

    private static Object printPath(TypeNode typeNode) {
        if (typeNode.getParent() == null) {
            return "";
        }
        return typeNode.getParent().toString();
    }

    private static List<TypeNode> getTypeNodesWithTypes(List<TypeNode> children) {
        List<TypeNode> listWithTypes = new ArrayList<TypeNode>();
        for (TypeNode node : children) {
            if (node.getStats() != null) {
                listWithTypes.add(node);
            }
        }
        return listWithTypes;
    }

    private static TypeNode createTree(List<IEventType> value, FlightRecording recording) {
        TypeNode root = new TypeNode("", null, null);
        for (IEventType type : value) {
            String[] pathElements = type.getPath().split("/");
            TypeNode currentNode = root;
            for (int i = 0; i < pathElements.length - 1; i++) {
                TypeNode child = currentNode.getChild(pathElements[i]);
                if (child == null) {
                    child = new TypeNode(pathElements[i], currentNode, null);
                }
                currentNode = child;
            }
            new TypeNode(pathElements[pathElements.length - 1], currentNode, calculateStats(recording, type));
            currentNode = root;
        }
        return root;
    }

    private static void putType(Map<IProducer, List<IEventType>> typeMap, IProducer producer, IEventType type) {
        List<IEventType> typeList = typeMap.computeIfAbsent(producer, k -> new ArrayList<>());
        typeList.add(type);
    }

    private static EventStatistics calculateStats(FlightRecording recording, IEventType type) {
        IView view = recording.createView();
        view.setEventTypes(wrap(type));

        int count = 0;
        for (IEvent event : view) {
            if (event.getEventType() != type) {
                System.out.println("Got mismatching type!");
            }
            count++;
        }
        return new EventStatistics(type, count);
    }

    private static Collection<IEventType> wrap(IEventType type) {
        List<IEventType> collection = new LinkedList<IEventType>();
        collection.add(type);
        return collection;
    }

    private static class TypeNode {
        private final TypeNode parent;
        private final List<TypeNode> children = new ArrayList<TypeNode>();
        private final String pathElement;
        private final EventStatistics stats;

        public TypeNode(String pathElement, TypeNode parent, EventStatistics stats) {
            this.pathElement = pathElement;
            this.parent = parent;
            if (parent != null) {
                parent.addChild(this);
            }
            this.stats = stats;
        }

        public TypeNode getParent() {
            return parent;
        }

        public List<TypeNode> getChildren() {
            return children;
        }

        public void addChild(TypeNode child) {
            children.add(child);
        }

        public String getPathElement() {
            return pathElement;
        }

        public TypeNode getChild(String pathElement) {
            for (TypeNode node : children) {
                if (node.getPathElement().equals(pathElement)) {
                    return node;
                }
            }
            return null;
        }

        public String toString() {
            if (getParent() == null)
                return "";
            return getParent().toString() + "/" + getPathElement();
        }

        public EventStatistics getStats() {
            return stats;
        }
    }

    private static class EventStatistics {
        private final IEventType type;
        private final int count;

        public EventStatistics(IEventType type, int count) {
            this.type = type;
            this.count = count;
        }

        public IEventType getEventType() {
            return type;
        }

        public int getCount() {
            return count;
        }
    }
}