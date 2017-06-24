package com.github.kornilova_l.server.trees;

import com.github.kornilova_l.protos.EventProtos;
import com.github.kornilova_l.protos.TreeProtos;
import com.github.kornilova_l.protos.TreesProtos;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

class OriginalTreesBuilder {
    static TreesProtos.Trees buildOriginalTrees(File logFile) {
        try (InputStream inputStream = new FileInputStream(logFile)) {
            HashMap<Long, OTBuilder> trees = new HashMap<>();
            EventProtos.Event event = EventProtos.Event.parseDelimitedFrom(inputStream);
            long timeOfLastEvent = event.getTime(); // is used to finish calls which does not have exit events
            while (event != null) {
                EventProtos.Event finalEvent = event;
                trees.computeIfAbsent(
                        event.getThreadId(),
                        k -> new OTBuilder(finalEvent.getTime(), finalEvent.getThreadId())
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

    private static TreesProtos.Trees HashMapToTrees(HashMap<Long, OTBuilder> trees, long timeOfLastEvent) {
        TreesProtos.Trees.Builder treesBuilder = TreesProtos.Trees.newBuilder();
        for (OTBuilder oTBuilder : trees.values()) {
            TreeProtos.Tree tree = oTBuilder.getBuiltTree(timeOfLastEvent);
            if (tree != null) {
                treesBuilder.addTrees(
                        tree
                );
            }
        }
        return treesBuilder.build();
    }
}
