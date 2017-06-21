package com.github.kornilova_l.server.trees;

import com.github.kornilova_l.protos.EventProtos;
import com.github.kornilova_l.protos.TreeProtos;
import com.github.kornilova_l.protos.TreesProtos;

import java.io.*;
import java.util.HashMap;

public class TreeConstructor {
    private final File file;

    public TreeConstructor(File file) {
        this.file = file;
    }

    public TreesProtos.Trees constructOriginalTrees() {
        try (InputStream inputStream = new FileInputStream(file)) {
            HashMap<Long, OriginalTree> trees = new HashMap<>();
            EventProtos.Event event = EventProtos.Event.parseDelimitedFrom(inputStream);
            long timeOfLastEvent = event.getTime();
            while (event != null) {
                EventProtos.Event finalEvent = event;
                trees.computeIfAbsent(
                        event.getThreadId(),
                        k -> new OriginalTree(finalEvent.getTime(), finalEvent.getThreadId())
                ).addEvent(event);
                timeOfLastEvent = event.getTime();
                event = EventProtos.Event.parseDelimitedFrom(inputStream);
            }
            return HashMapToTrees(trees, timeOfLastEvent);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static TreesProtos.Trees HashMapToTrees(HashMap<Long, OriginalTree> trees, long timeOfLastEvent) {
        TreesProtos.Trees.Builder treesBuilder = TreesProtos.Trees.newBuilder();
        for (OriginalTree originalTree : trees.values()) {
            TreeProtos.Tree tree = originalTree.getBuiltTree(timeOfLastEvent);
            if (tree != null) {
                treesBuilder.addTrees(
                        tree
                );
            }
        }
        return treesBuilder.build();
    }

    public static void main(String[] args) throws IOException {
        TreeConstructor treeConstructor = new TreeConstructor(
                new File("out/events187.ser")
        );
        TreesProtos.Trees trees = treeConstructor.constructOriginalTrees();
        try (OutputStream outputStream = new FileOutputStream("out/trees04.ser")) {
            trees.writeTo(outputStream);
        }
    }
}

