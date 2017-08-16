const pdConstants = {};
pdConstants.CANVAS_WIDTH = document.documentElement.clientWidth - 15 - 250 - 60;
pdConstants.LAYER_HEIGHT = 2;


class PreviewDrawer {
    /**
     * @param treePreview
     * @param {Number} id
     * @param {Number} fullDuration
     * @param VECTOR_CASE_X
     * @param VECTOR_CASE_Y
     */
    constructor(treePreview, id, fullDuration, VECTOR_CASE_X, VECTOR_CASE_Y) {
        this.treePreview = treePreview;
        this.id = id;
        this.VECTOR_CASE_X = VECTOR_CASE_X;
        this.VECTOR_CASE_Y = VECTOR_CASE_Y;
        this.$section = null;
        this.threadName = this.treePreview.getTreeInfo().getThreadName();
        this.canvasHeight = pdConstants.LAYER_HEIGHT * this.treePreview.getDepth();
        this.fullDuration = fullDuration;
        this.canvasWidth = pdConstants.CANVAS_WIDTH;
        this.startTime = this.treePreview.getTreeInfo().getStartTime();
        this.canvasOffset = this.startTime / fullDuration
            * pdConstants.CANVAS_WIDTH;

    }

    draw() {
        this.$section = this._createSection();
        if (this.id === 0) {
            this.$section.css("border", "none");
        }
        this.bindShowCallTree();
        this.stage = new createjs.Stage("canvas-preview-" + this.id);
        this._drawShape();
        this._resizeCanvas();
    }

    _drawShape() {
        const shape = new createjs.Shape();
        shape.graphics.beginFill("#0887d7");
        let x = this.startTime / this.fullDuration * this.canvasWidth;
        let y = this.canvasHeight;
        shape.graphics.moveTo(x, y);
        const vectorsCount = this.treePreview.getVectorsList().length;
        for (let i = 0; i < vectorsCount; i++) {
            const vector = this.treePreview.getVectorsList()[i];
            if (vector.getVectorCase() === this.VECTOR_CASE_X) {
                x += this._getTrueXCoordinate(vector.getX());
            } else if (vector.getVectorCase() === this.VECTOR_CASE_Y) {
                y += PreviewDrawer._getTrueYCoordinate(vector.getY());
            }
            shape.graphics.lineTo(Math.round(x), y);
        }
        this.stage.addChild(shape);
        this.stage.update();
    }

    _getTrueXCoordinate(deltaX) {
        return deltaX / this.fullDuration * this.canvasWidth;
    }

    _createSection() {
        const sectionContent = templates.tree.getTreePreviewSection(
            {
                id: "preview-" + this.id,
                threadName: this.threadName,
                canvasHeight: this.canvasHeight,
                canvasWidth: this.canvasWidth
            }
        ).content;
        return $(sectionContent).appendTo($(".tree-preview-wrapper"));
    }

    static _getTrueYCoordinate(y) {
        return PreviewDrawer._flipY(y);
    }

    static _flipY(y) {
        return -y * pdConstants.LAYER_HEIGHT;
    };

    bindShowCallTree() {
        const showCallTreeFunction = () => {
            common.shrinkTreePreviewWrapper();
            common.showCallTree(this.id);
        };
        this.$section.find(".show-call-tree-button").click(showCallTreeFunction);
        this.$section.find("h2").click(showCallTreeFunction);
    }

    _resizeCanvas() {
        const canvas = $("#canvas-preview-" + this.id);
        canvas.css("width", "calc(100% - 3px)");
        canvas.css("height", this.canvasHeight + "px");
    }
}
