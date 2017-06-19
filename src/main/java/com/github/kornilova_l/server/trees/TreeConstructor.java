package com.github.kornilova_l.server.trees;

import com.github.kornilova_l.protos.EventProtos;
import com.github.kornilova_l.protos.TreeProtos;

import java.io.*;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

public class TreeConstructor {
    private final File file;

    public TreeConstructor(File file) {
        this.file = file;
    }

    public Set<TreeProtos.Tree> constructOriginalTrees() {
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
            return trees.values().stream()
                    .peek(OriginalTree::buildTree)
                    .map(OriginalTree::getBuiltTree)
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        TreeConstructor treeConstructor = new TreeConstructor(
                new File("out/events81.ser")
        );
        Set<TreeProtos.Tree> trees = treeConstructor.constructOriginalTrees();
        int i = 0;
        for (TreeProtos.Tree tree : trees) {
            OutputStream outputStream = new FileOutputStream("out/original-tree-multithread" + i++ + ".ser");
            tree.writeTo(outputStream);
            System.out.println(tree.toString());
        }
    }
}

