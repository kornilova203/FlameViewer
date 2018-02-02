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
     * @override
     * @param node
     * @private
     */
    _setNodeZoomed(node) {
        this.zoomedNode = node;
        this.currentCanvasWidth = BackTracesDrawer._getCanvasWidth(this.$section.find(".canvas-zoomed"));
        common.showLoader(constants.loaderMessages.drawing, () => {
            this.zoomedStage.removeAllChildren();
            this._expandParents(node);
            this._drawNodesRecursively(
                node,
                this._countScaleXForNode(node),
                this._countOffsetXForNode(node),
                true,
                this.zoomedStage,
                true,
                node.depth
            );
            this._addResetButton();
            if (this.isHighlightedFunction !== null) {
                this._setHighlightOnZoomedStage();
            } else {
                this.zoomedStage.update();
            }
            $("#" + this.stage.id).addClass("original-canvas-zoomed");
            $("#" + this.zoomedStage.id).addClass("canvas-zoomed-show");
            common.hideLoader()
        });
    }
};