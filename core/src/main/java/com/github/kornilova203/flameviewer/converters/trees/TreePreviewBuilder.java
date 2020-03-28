package com.github.kornilova203.flameviewer.converters.trees;

import com.github.kornilova_l.flamegraph.proto.TreePreviewProtos;
import com.github.kornilova_l.flamegraph.proto.TreeProtos;
import com.github.kornilova_l.flamegraph.proto.TreesPreviewProtos.TreesPreview;
import com.github.kornilova_l.flamegraph.proto.TreesProtos.Trees;
import org.jetbrains.annotations.NotNull;

public class TreePreviewBuilder {
    private final TreesPreview treesPreview;

    public TreePreviewBuilder(Trees callTree) {
        TreesPreview.Builder treesPreview = TreesPreview.newBuilder();
        for (TreeProtos.Tree tree : callTree.getTreesList()) {
            treesPreview.addTreesPreview(getTreePreview(tree));
        }
        treesPreview.setFullDuration(
                getFullDuration(treesPreview)
        );
        this.treesPreview = treesPreview.build();
    }

    private static TreePreviewProtos.TreePreview getTreePreview(@NotNull TreeProtos.Tree tree) {
        TreePreviewProtos.TreePreview.Builder treePreview = TreePreviewProtos.TreePreview.newBuilder();
        buildTreePreviewRecursively(treePreview, tree.getBaseNode(), 0);
        removeClosingVector(treePreview);
        TreePreviewProtos.TreePreview.Builder simplifiedTreePreview = getSimplifiedTreePreview(treePreview);
        setTreePreviewBasicInfo(simplifiedTreePreview, tree);
        return simplifiedTreePreview.build();
    }

    private static void removeClosingVector(TreePreviewProtos.TreePreview.Builder treePreview) {
        int latestVector = treePreview.getVectorsCount() - 1;
        treePreview.removeVectors(latestVector);
    }

    private static TreePreviewProtos.TreePreview.Builder getSimplifiedTreePreview(TreePreviewProtos.TreePreview.Builder treePreview) {
        TreePreviewProtos.TreePreview.Builder simplifiedTreePreview = TreePreviewProtos.TreePreview.newBuilder();
        int vectorsCount = treePreview.getVectorsCount();
        int currentHeight = 0;
        for (int i = 0; i < vectorsCount; i++) {
            TreePreviewProtos.TreePreview.Vector vector = treePreview.getVectors(i);
            if (vector.getVectorCase() == TreePreviewProtos.TreePreview.Vector.VectorCase.X) {
                setY(simplifiedTreePreview, currentHeight);
                setX(simplifiedTreePreview, vector.getX());
                currentHeight = 0;
                continue;
            }
            currentHeight += vector.getY();
        }
        setY(simplifiedTreePreview, currentHeight);
        return simplifiedTreePreview;
    }

    private static void buildTreePreviewRecursively(TreePreviewProtos.TreePreview.Builder treePreview,
                                                    TreeProtos.Tree.Node node,
                                                    int depth) {
        int nodesCount = node.getNodesCount();
        if (nodesCount == 0) {
            setX(treePreview, node.getWidth()); // top
            return;
        }
        long currentOffset = node.getOffset();
        for (int i = 0; i < nodesCount; i++) {
            TreeProtos.Tree.Node child = node.getNodes(i);
            setX(treePreview, child.getOffset() - currentOffset);
            setY(treePreview, 1);
            buildTreePreviewRecursively(treePreview, child, depth + 1);
            setY(treePreview, -1);
            currentOffset = getRightX(child);
        }
        setX(treePreview, getRightX(node) - currentOffset);
    }

    private static void setX(TreePreviewProtos.TreePreview.Builder treePreview, long x) {
        if (x == 0) {
            return;
        }
        treePreview.addVectors(TreePreviewProtos.TreePreview.Vector.newBuilder().setX(x));
    }

    private static void setY(TreePreviewProtos.TreePreview.Builder treePreview, int y) {
        treePreview.addVectors(TreePreviewProtos.TreePreview.Vector.newBuilder().setY(y));
    }

    private static long getRightX(TreeProtos.Tree.Node node) {
        return node.getWidth() + node.getOffset();
    }

    private static void setTreePreviewBasicInfo(TreePreviewProtos.TreePreview.Builder treePreview, @NotNull TreeProtos.Tree tree) {
        treePreview.setTreeInfo(TreeProtos.Tree.TreeInfo.newBuilder()
                .setThreadName(tree.getTreeInfo().getThreadName())
                .setStartTime(tree.getTreeInfo().getStartTime()));
        treePreview.setWidth(tree.getWidth());
        treePreview.setDepth(tree.getDepth());
    }

    private long getFullDuration(TreesPreview.Builder treesPreview) {
        long fullDuration = 0;
        for (TreePreviewProtos.TreePreview.Builder treePreview : treesPreview.getTreesPreviewBuilderList()) {
            long treeDuration = treePreview.getTreeInfo().getStartTime() + treePreview.getWidth();
            if (treeDuration > fullDuration) {
                fullDuration = treeDuration;
            }
        }
        return fullDuration;
    }

    public TreesPreview getTreesPreview() {
        return treesPreview;
    }
}
