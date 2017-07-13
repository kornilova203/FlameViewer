package com.github.kornilova_l.flamegraph.server.trees;

import com.github.kornilova_l.protos.TreeProtos;

public interface TreeBuilderInterface {
    TreeProtos.Tree getTree();
}
