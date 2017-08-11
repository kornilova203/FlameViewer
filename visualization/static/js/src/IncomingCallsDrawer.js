class IncomingCallsDrawer extends AccumulativeTreeDrawer {
    constructor(tree) {
        super(tree);
        this.nodesCount = -1;
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
        if (this.nodesCount === -1) {
            this._countNodesRecursively(this.baseNode);
        }
        console.log("nodes count: " + this.nodesCount);
        return this.nodesCount;
    }

    /**
     * @param node
     * @private
     */
    _countNodesRecursively(node) {
        this.nodesCount++;
        const children = node.getNodesList();
        for (let i = 0; i < children.length; i++) {
            this._countNodesRecursively(children[i]);
        }
    }

    /**
     * @param {Number} offsetX
     * @param depth
     * @private
     * @override
     */
    _setPopupPosition(offsetX, depth) {
        this.popup
            .css("left", offsetX)
            .css("margin-top", -this.canvasHeight + AccumulativeTreeDrawer._calcNormaOffsetY(depth + 3) - POPUP_MARGIN)
    }

}