class CallTreeDrawer extends AccumulativeTreeDrawer {
    constructor(tree, minStartTime, maxFinishTime) {
        super(tree);
        const fullDuration = maxFinishTime - minStartTime;
        this.canvasWidth = this.treeWidth / fullDuration * MAIN_WIDTH;
        this.threadId = this.tree.getTreeInfo().getThreadId();
        this.canvasOffset = (this.tree.getTreeInfo().getStartTime() - minStartTime) / fullDuration * MAIN_WIDTH;
    }

    draw() {
        console.log("draw");
        this.section = this._createSection(this.canvasOffset);
        this.stage = new createjs.Stage("canvas-" + this.threadId);
        this.stage.id = "canvas-" + this.threadId;
        this.stage.enableMouseOver(20);

        this.zoomedStage = new createjs.Stage("canvas-zoomed-" + this.threadId);
        this.zoomedStage.id = "canvas-zoomed-" + this.threadId;
        this.stage.enableMouseOver(20);


        this._createPopup();

        const childNodes = this.baseNode.getNodesList();
        if (childNodes.length === 0) {
            return;
        }
        const maxDepth = this._drawFullTree();

        this.stage.update();
        this._moveCanvas(maxDepth);
        this._updateDim(this.baseNode);
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

    _createPopup() {
        const popupContent = templates.tree.accumulativeTreePopup().content;
        this.popup = $(popupContent).appendTo($("main"));
    }
}
