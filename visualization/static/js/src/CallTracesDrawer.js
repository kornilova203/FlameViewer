const AccumulativeTreeDrawer = require('./AccumulativeTreeDrawer');

module.exports.CallTracesDrawer = class CallTracesDrawer extends AccumulativeTreeDrawer.AccumulativeTreeDrawer {
    constructor(tree) {
        super(tree);
    }

    _moveSectionUp(visibleLayersCount) {
        const $main = $('main');
        // noinspection JSValidateTypes
        const previousScroll = $main.scrollTop();
        const currentShift = this._getMarginBottom(this.$section);

        const invisibleLayersCount = this.tree.getDepth() - visibleLayersCount;

        const newShift = Math.min(this.initialCanvasShift, invisibleLayersCount * (constants.LAYER_HEIGHT + constants.LAYER_GAP));
        this.$section.css("bottom", newShift + "px");

        const delta = currentShift - newShift;
        // noinspection JSValidateTypes
        $main.scrollTop(previousScroll + delta);
        return newShift;
    }

    _getMarginBottom($element) {
        const currentShiftString = $element.css("bottom");
        return Number.parseInt(currentShiftString.substring(0, currentShiftString.length - 2));
    }

    /**
     * @override
     */
    _getTreeType() {
        return "call-traces";
    }

    /**
     * @override
     */
    draw() {
        super.draw();
        // noinspection JSUnresolvedFunction
        const $main = $("main");
        // noinspection JSValidateTypes
        $main.scrollTop($main[0].scrollHeight);
    }
};