class CallTracesDrawer extends AccumulativeTreeDrawer {
    constructor(tree) {
        super(tree);
        const visibleDepth = tree.getVisibleDepth();
        if (visibleDepth !== 0) { // if only part of tree displayed
            this.moveSectionUp();
        }
    }

    moveSectionUp() {
        const invisibleLayersCount = this.tree.getDepth() - this.tree.getVisibleDepth();
        this.$section.css("bottom", invisibleLayersCount * (LAYER_HEIGHT + LAYER_GAP) + "px");
    }

    /**
     * @override
     * @param $header
     */
    appendHeader($header) {
        $header.insertAfter(this.$section.find(".canvas-wrapper"));
        $header.addClass("header-call-traces");
    }
}