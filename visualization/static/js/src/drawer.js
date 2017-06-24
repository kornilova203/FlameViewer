const MAIN_WIDTH = 700;
const LAYER_HEIGHT = 19;
const LAYER_GAP = 1;
const COLORS = ["#18A3FA", "#0887d7"];

class Drawer {
    constructor(tree, minStartTime, maxFinishTime) {
        this.tree = tree;
        this.stage = null;
        this.width = this.tree.getWidth();
        const fullDuration = maxFinishTime - minStartTime;
        this.canvasWidth = this.width / fullDuration * MAIN_WIDTH;
        this.canvasHeight = (LAYER_HEIGHT + LAYER_GAP) * this.tree.getDepth() + 70;
        this.threadId = this.tree.getTreeInfo().getThreadId();

        this.canvasOffset = (this.tree.getTreeInfo().getStartTime() - minStartTime) / fullDuration * MAIN_WIDTH;
        this.section = this._createSection(this.canvasOffset);
        this._drawTree();
    }

    _drawTree() {
        this.stage = new createjs.Stage("canvas-" + this.threadId);
        this.stage.enableMouseOver(20);

        this._drawRecursively(this.tree.getBaseNode(), 0);

        this.stage.update();
    };

    _drawRecursively(node, depth) {
        const childNodes = node.getNodesList();
        if (childNodes.length === 0) {
            return;
        }
        for (let i = 0; i < childNodes.length; i++) {
            this._drawNode(childNodes[i], depth, i % 2);
            this._drawRecursively(childNodes[i], depth + 1);
        }
    }

    /**
     * Get canvas Y coordinate (it start from top)
     * @param y
     * @returns {number}
     * @private
     */
    _flipY(y) {
        return this.canvasHeight - y - LAYER_HEIGHT;
    };

    _createSection(canvasOffset) {
        const sectionContent = templates.tree.getSectionForThread(
            {
                threadId: this.threadId,
                canvasHeight: this.canvasHeight,
                canvasWidth: this.canvasWidth,
                canvasOffset: canvasOffset
            }
        ).content;
        return $(sectionContent).appendTo($("main"));
    };

    _drawNode(node, depth, colorId) {
        const shape = this._drawRectangle(node, depth, colorId);
        this._drawLabel(node, depth, shape);
    }

    _drawRectangle(node, depth, colorId) {
        const shape = new createjs.Shape();
        shape.graphics
            .beginFill(COLORS[colorId])
            .drawRect(0, 0, this.canvasWidth, LAYER_HEIGHT);
        const offsetX = this._getOffsetXForNode(node);
        const scaleX = node.getWidth() / this.width;
        shape.setTransform(offsetX, this._flipY(depth * (LAYER_GAP + LAYER_HEIGHT)), scaleX);
        console.log(`draw: ${depth}\t${scaleX}\t${node.getNodeInfo().getMethodName()}`);
        this._createPopup(node, shape);
        this.stage.addChild(shape);
        return shape;
    }

    _getOffsetXForNode(node) {
        return (node.getOffset() / this.width) * this.canvasWidth;
    }

    _createPopup(node, shape) {
        const popupContent = templates.tree.popupInOriginalTree(
            {
                methodName: node.getNodeInfo().getMethodName(),
                className: node.getNodeInfo().getClassName(),
                duration: node.getWidth(),
                startTime: node.getOffset()
            }
        ).content;
        const popup = $(popupContent).appendTo(this.section);
        this._setPopupPosition(popup);
        shape.addEventListener("mouseover", () => {
            popup.show();
        });
        shape.addEventListener("mouseout", () => {
            popup.hide();
        });
    }

    _drawLabel(node, depth, shape) {
        const text = new createjs.Text(
            node.getNodeInfo().getClassName().split("/").join(".") + "." + node.getNodeInfo().getMethodName(),
            (LAYER_HEIGHT - 2) + "px Arial",
            "#fff"
        );
        text.x = this._getOffsetXForNode(node) + 2;
        text.y = this._flipY(depth * (LAYER_GAP + LAYER_HEIGHT));
        const newShape = shape.clone();
        newShape.scaleX = shape.scaleX * 0.9;
        text.mask = newShape;
        this.stage.setChildIndex(text, this.stage.getNumChildren() - 1);

        this.stage.addChild(text);
    }

    _setPopupPosition(popup) {
        popup.css("left", this.canvasOffset + this._getOffsetXForNode(node));
    }
}
