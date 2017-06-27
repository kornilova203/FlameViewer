package com.github.kornilova_l.server.trees.call_tree;

import com.github.kornilova_l.protos.EventProtos;
import com.github.kornilova_l.protos.TreeProtos;
import com.github.kornilova_l.protos.TreesProtos;
import com.github.kornilova_l.server.trees.TreeBuilderInterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class CallTreesBuilder {
    private static final com.intellij.openapi.diagnostic.Logger LOG =
            com.intellij.openapi.diagnostic.Logger.getInstance(CallTreesBuilder.class);
    private TreesProtos.Trees trees = null;

    public CallTreesBuilder(File logFile) {
        try (InputStream inputStream = new FileInputStream(logFile)) {
            HashMap<Long, CTBuilder> treesMap = new HashMap<>();
            EventProtos.Event event = EventProtos.Event.parseDelimitedFrom(inputStream);
            long timeOfLastEvent = event.getTime(); // is used to finish calls which does not have exit events
            while (event != null) {
                EventProtos.Event finalEvent = event;
                treesMap.computeIfAbsent(
                        event.getThreadId(),
                        k -> new CTBuilder(finalEvent.getTime(), finalEvent.getThreadId())
                ).addEvent(event);
                timeOfLastEvent = event.getTime();
                event = EventProtos.Event.parseDelimitedFrom(inputStream);
            }
            trees = HashMapToTrees(treesMap, timeOfLastEvent);
        } catch (IOException e) {
            LOG.error(e);
        }
    }

    public TreesProtos.Trees getTrees() {
        return trees;
    }

    private static TreesProtos.Trees HashMapToTrees(HashMap<Long, CTBuilder> trees, long timeOfLastEvent) {
        TreesProtos.Trees.Builder treesBuilder = TreesProtos.Trees.newBuilder();
        for (CTBuilder oTBuilder : trees.values()) {
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
