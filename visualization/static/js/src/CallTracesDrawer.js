const AccumulativeTreeDrawer = require('./AccumulativeTreeDrawer');

module.exports.CallTracesDrawer = class CallTracesDrawer extends AccumulativeTreeDrawer.AccumulativeTreeDrawer {
    constructor(tree) {
        super(tree);
        this._assignChildIndexRecursively(tree.getBaseNode());
    }

    _moveSectionUp() {
        const invisibleLayersCount = this.tree.getDepth() - this.tree.getVisibleDepth();
        this.$section.css("bottom", invisibleLayersCount * (constants.LAYER_HEIGHT + constants.LAYER_GAP) + "px");
    }

    /**
     * Indices are needed to get hidden nodes when node is zoomed.
     * @param node
     */
    _assignChildIndexRecursively(node) {
        const children = node.getNodesList();
        for (let i = 0; i < children.length; i++) {
            children[i].index = i;
            this._assignChildIndexRecursively(children[i]);
        }
    }

    /**
     * @override
     */
    draw() {
        super.draw();
        const $main = $("main");
        // noinspection JSValidateTypes
        $main.scrollTop($main[0].scrollHeight);
    }
};