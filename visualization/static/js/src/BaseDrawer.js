const MAIN_WIDTH = 700;
const LAYER_HEIGHT = 19;
const LAYER_GAP = 1;
const POPUP_MARGIN = 4; // have no idea why there is a gap between popup and canvas
const COLORS = ["#18A3FA", "#0887d7"];

/**
 * Draws tree without:
 * - parameters
 * - start time
 */
class BaseDrawer {
    constructor(tree) {
        this.tree = tree;
        this.stage = null;
        this.width = this.tree.getWidth();
        this.canvasWidth = MAIN_WIDTH;
        this.canvasHeight = (LAYER_HEIGHT + LAYER_GAP) * this.tree.getDepth() + 70;

        this.section = this._createSectionWithCanvas();
        this._drawTree();
    }

    _drawTree() {
        this.stage = new createjs.Stage("canvas");
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

    _createSectionWithCanvas() {
        const sectionContent = templates.tree.getBaseSection(
            {
                canvasHeight: this.canvasHeight,
                canvasWidth: this.canvasWidth,
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
        const offsetY = this._flipY(BaseDrawer._calcNormaOffsetY(depth));
        const scaleX = node.getWidth() / this.width;
        shape.setTransform(offsetX, offsetY, scaleX);
        console.log(`draw: ${depth}\t${scaleX}\t${node.getNodeInfo().getMethodName()}`);
        this._createPopup(node, shape, depth);
        this.stage.addChild(shape);
        return shape;
    }

    _getOffsetXForNode(node) {
        return (node.getOffset() / this.width) * this.canvasWidth;
    }

    _createPopup(node, shape, depth) {
        const popupContent = templates.tree.basePopup(
            {
                methodName: node.getNodeInfo().getMethodName(),
                className: node.getNodeInfo().getClassName(),
                desc: node.getNodeInfo().getDescription(),
                isStatic: node.getNodeInfo().getIsStatic(),
                duration: node.getWidth(),
                count: node.getNodeInfo().getCount()
            }
        ).content;
        const popup = $(popupContent).appendTo(this.section);
        this._setPopupPosition(popup, node, depth);
        BaseDrawer._addMouseEvents(shape, popup);
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

    _setPopupPosition(popup, node, depth) {
        popup
            .css("left", this._getOffsetXForNode(node))
            .css("margin-top", - BaseDrawer._calcNormaOffsetY(depth) - POPUP_MARGIN)
    }

    static _calcNormaOffsetY(depth) {
        return depth * (LAYER_GAP + LAYER_HEIGHT);
    }

    static _addMouseEvents(shape, popup) {
        let isPopupHovered = false;
        let isMethodHovered = false;
        popup.hover(
            () => {
                isPopupHovered = true;
            },
            () => {
                isPopupHovered = false;
                if (!isMethodHovered) {
                    popup.hide();
                }
            });
        shape.addEventListener("mouseover", () => {
            isMethodHovered = true;
            popup.show();
        });
        shape.addEventListener("mouseout", () => {
            isMethodHovered = false;
            if (!isPopupHovered) {
                popup.hide();
            }
        });
    }
}
