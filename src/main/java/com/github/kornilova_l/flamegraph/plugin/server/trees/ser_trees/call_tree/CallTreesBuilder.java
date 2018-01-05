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
            processEvents(inputStream);
            long startTimeOfFirstThread = getStartTimeOfFirstThread(treesMap);
            trees = HashMapToTrees(treesMap, startTimeOfFirstThread);
        } catch (IOException e) {
            LOG.error(e);
        }
    }

    @Nullable
    private static TreesProtos.Trees HashMapToTrees(Map<Long, CTBuilder> trees,
                                                              long startTimeOfFirstThread) {
        TreesProtos.Trees.Builder treesBuilder = TreesProtos.Trees.newBuilder();
        for (CTBuilder oTBuilder : trees.values()) {
            TreeProtos.Tree tree = oTBuilder.getBuiltTree(startTimeOfFirstThread);
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

    private void processEvents(InputStream inputStream) throws IOException {
        EventProtos.Event event = EventProtos.Event.parseDelimitedFrom(inputStream);
        while (event != null) {
            switch (event.getTypeCase()) {
                case METHODEVENT:
                    addMethodEvent(event);
                    break;
                case NEWCLASS:
                    registerClass(event);
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
    }

    private void registerClass(EventProtos.Event event) {
        String className = event.getNewClass().getName();
        classNames.put(
                event.getNewClass().getId(),
                className.replace('/', '.'));
    }

    private void addMethodEvent(EventProtos.Event event) {
        EventProtos.Event.MethodEvent methodEvent = event.getMethodEvent();
        CTBuilder ctBuilder = getCTBuilder(methodEvent);
        String className = classNames.get(methodEvent.getClassNameId());
        if (className == null) {
            throw new RuntimeException("Class name is not known");
        }
        ctBuilder.addEvent(methodEvent, className);
    }

    private CTBuilder getCTBuilder(EventProtos.Event.MethodEvent methodEvent) {
        String threadName = threadsNames.get(methodEvent.getThreadId());
        if (threadName == null) {
            LOG.debug("Thread name is not known. MethodEvent: " + methodEvent.toString());
            return treesMap.computeIfAbsent(
                    methodEvent.getThreadId(),
                    k -> new CTBuilder(
                            methodEvent.getStartTime(),
                            ""
                    )
            );
        }
        return treesMap.computeIfAbsent(
                methodEvent.getThreadId(),
                k -> new CTBuilder(
                        methodEvent.getStartTime(),
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
