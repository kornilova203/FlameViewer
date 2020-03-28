package com.github.kornilova203.flameviewer.converters.trees;

import com.github.kornilova_l.flamegraph.proto.TreeProtos;

public interface TreeBuilder {
    TreeProtos.Tree getTree();
}
