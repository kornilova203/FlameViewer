class CallTreeDrawer extends AccumulativeTreeDrawer {
    constructor(tree, minStartTime, maxFinishTime) {
        super(tree);
        const fullDuration = maxFinishTime - minStartTime;
        this.canvasWidth = this.width / fullDuration * MAIN_WIDTH;
        this.threadId = this.tree.getTreeInfo().getThreadId();
        this.canvasOffset = (this.tree.getTreeInfo().getStartTime() - minStartTime) / fullDuration * MAIN_WIDTH;
    }

    draw() {
        this.section = this._createSection(this.canvasOffset);
        this.stage = new createjs.Stage("canvas-" + this.threadId);
        this.stage.enableMouseOver(20);

        super._drawRecursively(this.tree.getBaseNode(), 0);

        this.stage.update();
    };

    _createSection(canvasOffset) {
        const sectionContent = templates.tree.getCallTreeSection(
            {
                threadId: this.threadId,
                canvasHeight: this.canvasHeight,
                canvasWidth: this.canvasWidth,
                canvasOffset: canvasOffset
            }
        ).content;
        return $(sectionContent).appendTo($("main"));
    };

    _createPopup(node, shape, depth) {
        const popupContent = templates.tree.callTreePopup(
            {
                methodName: node.getNodeInfo().getMethodName(),
                className: node.getNodeInfo().getClassName(),
                duration: node.getWidth(),
                startTime: node.getOffset()
            }
        ).content;
        const popup = $(popupContent).appendTo(this.section);
        this._setPopupPosition(popup, node, depth);
        AccumulativeTreeDrawer._addMouseEvents(shape, popup);
    }

    _setPopupPosition(popup, node, depth) {
        popup
            .css("left", this.canvasOffset + this._getOffsetXForNode(node))
            .css("margin-top", - AccumulativeTreeDrawer._calcNormaOffsetY(depth) - POPUP_MARGIN)
    }
}
