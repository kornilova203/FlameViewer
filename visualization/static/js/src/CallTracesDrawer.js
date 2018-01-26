class CallTracesDrawer extends AccumulativeTreeDrawer {
    constructor(tree) {
        super(tree);
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