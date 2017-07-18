package com.github.kornilova_l.flamegraph.plugin.server.trees;

import com.github.kornilova_l.flamegraph.proto.TreeProtos;

public interface TreeBuilder {
    TreeProtos.Tree getTree();
}
