const AccumulativeTreeDrawer = require('./AccumulativeTreeDrawer');

module.exports.BackTracesDrawer = class BackTracesDrawer extends AccumulativeTreeDrawer.AccumulativeTreeDrawer {
    constructor(tree) {
        super(tree);
    }

    // noinspection JSMethodCanBeStatic
    /**
     * Get canvas Y coordinate (it start from top)
     * @param y
     * @returns {number}
     * @protected
     * @override
     */
    flipY(y) {
        return y;
    }

    /**
     * @override
     */
    _getTreeType() {
        return "back-traces";
    }

    /**
     * @param {Number} offsetX
     * @param depth
     * @override
     */
    _setPopupPosition(offsetX, depth) {
        this.$popup
            .css("left", offsetX)
            .css("margin-top", -this.canvasHeight + BackTracesDrawer._calcNormaOffsetY(depth + 1) - constants.POPUP_MARGIN)
    }

    /**
     * Do not move section. Move only fog
     * @override
     */
    _moveSectionUp(visibleLayersCount) {
        this.$fog.css("top", visibleLayersCount * (constants.LAYER_HEIGHT + constants.LAYER_GAP) + 59);
        return 0;
    }
};