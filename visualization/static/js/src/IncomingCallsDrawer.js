class IncomingCallsDrawer extends AccumulativeTreeDrawer {
    constructor(tree) {
        super(tree);
        this._countNodesRecursively(this.baseNode);
    }

    // noinspection all
    /**
     * Get canvas Y coordinate (it start from top)
     * @param y
     * @returns {number}
     * @protected
     * @override
     */
    flipY(y) {
        return y + 40;
    }

    /**
     * @return {number}
     */
    getNodesCount() {
        console.log("nodes count: " + this.nodesCount);
        return this.nodesCount;
    }

    /**
     * @param {Number} offsetX
     * @param depth
     * @private
     * @override
     */
    _setPopupPosition(offsetX, depth) {
        this.$popup
            .css("left", offsetX)
            .css("margin-top", -this.canvasHeight + AccumulativeTreeDrawer._calcNormaOffsetY(depth + 3) - POPUP_MARGIN)
    }

}