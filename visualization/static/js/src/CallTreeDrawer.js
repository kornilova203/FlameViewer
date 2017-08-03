class CallTreeDrawer extends AccumulativeTreeDrawer {
    /**
     * @param tree
     * @param {Number} maxDuration
     * @param {Number} id
     */
    constructor(tree, maxDuration, id) {
        super(tree);
        const fullDuration = maxDuration;
        this.canvasWidth = this.treeWidth / fullDuration * MAIN_WIDTH;
        this.threadName = this.tree.getTreeInfo().getThreadName();
        this.canvasOffset = this.tree.getTreeInfo().getStartTime() / fullDuration * MAIN_WIDTH;
        this.id = id;
    }

    draw() {
        this._prepareDraw();

        console.log("draw");
        this.section = this._createSection(this.canvasOffset);
        this.stage = new createjs.Stage("canvas-" + this.id);
        this.stage.id = "canvas-" + this.id;
        this.stage.enableMouseOver(20);

        this.zoomedStage = new createjs.Stage("canvas-zoomed-" + this.id);
        this.zoomedStage.id = "canvas-zoomed-" + this.id;
        this.stage.enableMouseOver(20);

        this._createPopup();

        const childNodes = this.baseNode.getNodesList();
        if (childNodes.length === 0) {
            return;
        }
        const maxDepth = this._drawFullTree();

        this.stage.update();
        this._moveCanvas(maxDepth);
    };

    _createSection(canvasOffset) {
        const sectionContent = templates.tree.getCallTreeSection(
            {
                id: this.id,
                threadName: this.threadName,
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
