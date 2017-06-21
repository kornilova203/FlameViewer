package com.github.kornilova_l.server.trees;

import com.github.kornilova_l.protos.EventProtos;
import com.github.kornilova_l.protos.TreesProtos;

import java.io.*;
import java.util.HashMap;
import java.util.stream.Collectors;

public class TreeConstructor {
    private final File file;

    public TreeConstructor(File file) {
        this.file = file;
    }

    public TreesProtos.Trees constructOriginalTrees() {
        try (InputStream inputStream = new FileInputStream(file)) {
            HashMap<Long, OriginalTree> trees = new HashMap<>();
            EventProtos.Event event = EventProtos.Event.parseDelimitedFrom(inputStream);
            while (event != null) {
                EventProtos.Event finalEvent = event;
                trees.computeIfAbsent(
                        event.getThreadId(),
                        k -> new OriginalTree(finalEvent.getTime(), finalEvent.getThreadId())
                ).addEvent(event);
                event = EventProtos.Event.parseDelimitedFrom(inputStream);
            }
            return HashMapToTrees(trees);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static TreesProtos.Trees HashMapToTrees(HashMap<Long, OriginalTree> trees) {
        return TreesProtos.Trees.newBuilder()
                .addAllTrees(
                        trees.values().stream()
                                .peek(OriginalTree::buildTree)
                                .map(OriginalTree::getBuiltTree)
                                .collect(Collectors.toSet())
                )
                .build();
    }

    public static void main(String[] args) throws IOException {
        TreeConstructor treeConstructor = new TreeConstructor(
                new File("out/events173.ser")
        );
        TreesProtos.Trees trees = treeConstructor.constructOriginalTrees();
        try (OutputStream outputStream = new FileOutputStream("out/trees01.ser")) {
            trees.writeTo(outputStream);
        }
    }
}

