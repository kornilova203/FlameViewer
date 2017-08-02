package com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees.call_tree;

import com.github.kornilova_l.flamegraph.proto.EventProtos;
import com.github.kornilova_l.flamegraph.proto.TreeProtos;
import com.github.kornilova_l.flamegraph.proto.TreesProtos;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CallTreesBuilder {
    private static final com.intellij.openapi.diagnostic.Logger LOG =
            com.intellij.openapi.diagnostic.Logger.getInstance(CallTreesBuilder.class);
    @Nullable private TreesProtos.Trees trees = null;

    public CallTreesBuilder(File logFile) {
        try (InputStream inputStream = new FileInputStream(logFile)) {
            HashMap<Long, CTBuilder> treesMap = new HashMap<>();
            EventProtos.Event event = EventProtos.Event.parseDelimitedFrom(inputStream);
            if (event == null) { // if no event was written
                trees = TreesProtos.Trees.newBuilder().build();
                return;
            }
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
            long startTimeOfFirstThread = getStartTimeOfFirstThread(treesMap);
            trees = HashMapToTrees(treesMap, timeOfLastEvent, startTimeOfFirstThread);
        } catch (IOException e) {
            LOG.error(e);
        }
    }

    private long getStartTimeOfFirstThread(HashMap<Long, CTBuilder> treesMap) {
        List<CTBuilder> treesList = new ArrayList<>(treesMap.values());
        int size = treesList.size();
        if (size == 0) {
            return 0;
        }
        long startTimeOfFirstThread = treesList.get(0).getThreadStartTime();
        for (int i = 1; i < size; i++) {
            if (treesList.get(i).getThreadStartTime() < startTimeOfFirstThread) {
                startTimeOfFirstThread = treesList.get(i).getThreadStartTime();
            }
        }
        return startTimeOfFirstThread;
    }

    @Nullable
    public TreesProtos.Trees getTrees() {
        return trees;
    }

    private static TreesProtos.@Nullable Trees HashMapToTrees(HashMap<Long, CTBuilder> trees,
                                                              long timeOfLastEvent,
                                                              long startTimeOfFirstThread) {
        TreesProtos.Trees.Builder treesBuilder = TreesProtos.Trees.newBuilder();
        for (CTBuilder oTBuilder : trees.values()) {
            oTBuilder.subtractFromThreadStartTime(startTimeOfFirstThread);
            TreeProtos.Tree tree = oTBuilder.getBuiltTree(timeOfLastEvent);
            if (tree != null) {
                treesBuilder.addTrees(
                        tree
                );
            }
        }
        if (treesBuilder.getTreesCount() == 0) {
            return null;
        }
        return treesBuilder.build();
    }
}
