package com.github.korniloval.flameviewer.converters.trees;

import com.github.kornilova_l.flamegraph.proto.TreeProtos;

public interface TreeBuilder {
    TreeProtos.Tree getTree();
}
