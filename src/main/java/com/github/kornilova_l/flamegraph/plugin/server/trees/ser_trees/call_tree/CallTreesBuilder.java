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
import java.util.Map;

public class CallTreesBuilder {
    private static final com.intellij.openapi.diagnostic.Logger LOG =
            com.intellij.openapi.diagnostic.Logger.getInstance(CallTreesBuilder.class);
    private Map<Long, CTBuilder> treesMap = new HashMap<>();
    private Map<Long, String> classNames = new HashMap<>();
    private Map<Long, String> threadsNames = new HashMap<>();
    @Nullable
    private TreesProtos.Trees trees = null;

    public CallTreesBuilder(File logFile) {
        try (InputStream inputStream = new FileInputStream(logFile)) {
            EventProtos.Event event = EventProtos.Event.parseDelimitedFrom(inputStream);
            if (event == null) { // if no event was written
                trees = null;
                return;
            }
            long timeOfLastEvent = processEvents(event, inputStream);
            if (timeOfLastEvent == 0) {
                trees = null;
                return;
            }
            long startTimeOfFirstThread = getStartTimeOfFirstThread(treesMap);
            trees = HashMapToTrees(treesMap, timeOfLastEvent, startTimeOfFirstThread);
        } catch (IOException e) {
            LOG.error(e);
        }
    }

    private static TreesProtos.@Nullable Trees HashMapToTrees(Map<Long, CTBuilder> trees,
                                                              long timeOfLastEvent,
                                                              long startTimeOfFirstThread) {
        TreesProtos.Trees.Builder treesBuilder = TreesProtos.Trees.newBuilder();
        for (CTBuilder oTBuilder : trees.values()) {
            oTBuilder.subtractFromThreadStartTime(startTimeOfFirstThread);
            timeOfLastEvent = timeOfLastEvent - startTimeOfFirstThread;
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

    private long processEvents(EventProtos.Event event, InputStream inputStream) throws IOException {
        long timeOfLastEvent = 0;
        while (event != null) {
            switch (event.getTypeCase()) {
                case METHODEVENT:
                    timeOfLastEvent = addMethodEvent(event);
                    break;
                case NEWCLASS:
                    classNames.put(event.getNewClass().getId(), event.getNewClass().getName());
                    break;
                case NEWTHREAD:
                    threadsNames.put(event.getNewThread().getId(), event.getNewThread().getName());
                    break;
                case TYPE_NOT_SET:
                default:
                    throw new RuntimeException("Event without type");
            }
            event = EventProtos.Event.parseDelimitedFrom(inputStream);
        }
        return timeOfLastEvent;
    }

    private long addMethodEvent(EventProtos.Event event) {
        EventProtos.Event.MethodEvent methodEvent = event.getMethodEvent();
        CTBuilder ctBuilder = getCTBuilder(methodEvent);
        switch (methodEvent.getInfoCase()) {
            case ENTER:
                String className = classNames.get(methodEvent.getEnter().getClassNameId());
                if (className == null) {
                    throw new RuntimeException("Class name is not known");
                }
                ctBuilder.addEvent(methodEvent, className);
                break;
            case EXIT:
            case EXCEPTION:
                ctBuilder.addEvent(methodEvent);
                break;
            case INFO_NOT_SET:
            default:
                throw new RuntimeException("Method event without type");
        }
        return methodEvent.getTime();
    }

    private CTBuilder getCTBuilder(EventProtos.Event.MethodEvent methodEvent) {
        String threadName = threadsNames.get(methodEvent.getThreadId());
        if (threadName == null) {
            throw new RuntimeException("Thread name is not known");
        }
        return treesMap.computeIfAbsent(
                methodEvent.getThreadId(),
                k -> new CTBuilder(
                        methodEvent.getTime(),
                        threadName
                )
        );
    }

    private long getStartTimeOfFirstThread(Map<Long, CTBuilder> treesMap) {
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
}
