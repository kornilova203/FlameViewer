package com.github.kornilova_l.server.trees;

import com.github.kornilova_l.protos.EventProtos;
import com.github.kornilova_l.protos.TreeProtos;

import java.io.*;

public class TreeConstructor {
    private final File file;

    public TreeConstructor(File file) {
        this.file = file;
    }

    public TreeProtos.Tree constructOriginalTree() {
        try (InputStream inputStream = new FileInputStream(file)) {
            EventProtos.Event event = EventProtos.Event.parseDelimitedFrom(inputStream);
            OriginalTree originalTree = new OriginalTree(event.getTime());
            while (event != null) {
                System.out.println(event.toString());
                originalTree.addEvent(event);
                event = EventProtos.Event.parseDelimitedFrom(inputStream);
            }
            originalTree.buildTree();
            return originalTree.getBuiltTree();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

