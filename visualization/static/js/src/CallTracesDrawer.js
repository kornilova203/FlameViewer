const AccumulativeTreeDrawer = require('./AccumulativeTreeDrawer');

module.exports.CallTracesDrawer = class CallTracesDrawer extends AccumulativeTreeDrawer.AccumulativeTreeDrawer {
    constructor(tree) {
        super(tree);
    }

    _moveSectionUp(visibleLayersCount) {
        const invisibleLayersCount = this.tree.getDepth() - visibleLayersCount;
        this.$section.css("bottom", invisibleLayersCount * (constants.LAYER_HEIGHT + constants.LAYER_GAP) + "px");
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