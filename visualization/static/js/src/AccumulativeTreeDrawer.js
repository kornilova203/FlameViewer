const MAIN_WIDTH = 700;
const LAYER_HEIGHT = 19;
const LAYER_GAP = 1;
const POPUP_MARGIN = 4; // have no idea why there is a gap between popup and canvas
const COLORS = ["#18A3FA", "#0887d7"];
const ZOOMED_PARENT_COLOR = "#94bcff";

/**
 * Draws tree without:
 * - parameters
 * - start time
 */
class AccumulativeTreeDrawer {
    constructor(tree) {
        this.tree = tree;
        this.width = this.tree.getWidth();
        this.canvasWidth = MAIN_WIDTH;
        this.canvasHeight = (LAYER_HEIGHT + LAYER_GAP) * this.tree.getDepth() + 70;
        this.section = null;
        this.stage = null;
        this.header = null;
        this.searchList = [];
        this.shapeAndTextList = [];
        this._enableSearch();
    }

    setHeader(newHeader) {
        this.header = "for method " + newHeader;
    }

    draw() {
        this.section = this._createSectionWithCanvas();
        this.stage = new createjs.Stage("canvas");
        this.stage.enableMouseOver(20);

        this._drawRecursively(this.tree.getBaseNode(), 0);

        this.stage.update();
        this._enableZoom();
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
     * @protected
     */
    flipY(y) {
        return this.canvasHeight - y - LAYER_HEIGHT;
    };

    _createSectionWithCanvas() {
        const sectionContent = templates.tree.getAccumulativeTreeSection(
            {
                canvasHeight: this.canvasHeight,
                canvasWidth: this.canvasWidth,
                header: this.header
            }
        ).content;
        return $(sectionContent).appendTo($("main"));
    };

    _drawNode(node, depth, colorId) {
        const shape = this._drawRectangle(node, depth, colorId);
        const text = this._drawLabel(node, depth, shape);
        this.shapeAndTextList.push({
            shape: shape,
            text: text
        });
        this.searchList.push(new SearchElem(shape, node.getNodeInfo().getMethodName()));
    }

    _drawRectangle(node, depth, colorId) {
        const shape = new createjs.Shape();
        shape.fillCommand = shape.graphics.beginFill(COLORS[colorId]).command;
        shape.originalColor = COLORS[colorId];
        shape.graphics.drawRect(0, 0, this.canvasWidth, LAYER_HEIGHT);
        const offsetX = this._getOffsetXForNode(node);
        const offsetY = this.flipY(AccumulativeTreeDrawer._calcNormaOffsetY(depth));
        const scaleX = node.getWidth() / this.width;
        shape.originalX = offsetX;
        shape.originalScaleX = scaleX;
        shape.setTransform(offsetX, offsetY, scaleX);
        // console.log(`draw: ${depth}\t${scaleX}\t${node.getNodeInfo().getMethodName()}`);
        this._createPopup(node, shape, depth);
        this.stage.addChild(shape);
        return shape;
    }

    _getOffsetXForNode(node) {
        return (node.getOffset() / this.width) * this.canvasWidth;
    }

    _createPopup(node, shape, depth) {
        const popupContent = templates.tree.accumulativeTreePopup(
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
        AccumulativeTreeDrawer._addMouseEvents(shape, popup);
    }

    _drawLabel(node, depth, shape) {
        const text = new createjs.Text(
            node.getNodeInfo().getClassName().split("/").join(".") + "." + node.getNodeInfo().getMethodName(),
            (LAYER_HEIGHT - 2) + "px Arial",
            "#fff"
        );
        text.x = this._getOffsetXForNode(node) + 2;
        text.originalX = text.x;
        text.y = this.flipY(depth * (LAYER_GAP + LAYER_HEIGHT));
        const newShape = shape.clone();
        newShape.scaleX = shape.scaleX * 0.9;
        text.mask = newShape;
        this.stage.setChildIndex(text, this.stage.getNumChildren() - 1);

        this.stage.addChild(text);
        return text;
    }

    _setPopupPosition(popup, node, depth) {
        popup
            .css("left", this._getOffsetXForNode(node))
            .css("margin-top", -AccumulativeTreeDrawer._calcNormaOffsetY(depth) - POPUP_MARGIN)
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

    _enableSearch() {
        const input = $("#search").find("input");
        input.on('change keyup copy paste cut', () => {
            const val = input.val();
            if (!val) {
                this._resetHighlight();
                return;
            }
            for (let i in this.searchList) {
                if (this.searchList[i].matches(val)) {
                    this.searchList[i].reset();
                } else {
                    this.searchList[i].dim();
                }
            }
            this.stage.update();
        })
    }

    _resetHighlight() {
        for (let i in this.searchList) {
            this.searchList[i].reset();
        }
        this.stage.update();
    }

    _enableZoom() {
        const resetZoomButton = this._createResetZoomButton();
        for (let i in this.shapeAndTextList) {
            let zoomedShape = this.shapeAndTextList[i].shape;
            zoomedShape.addEventListener("click", () => {
                this._resetZoom();
                resetZoomButton.scaleX = 0;
                if (!(zoomedShape.x === 0 && zoomedShape.scaleX === 1)) { // if it is not base node
                    resetZoomButton.scaleX = 1;
                    for (let j in this.shapeAndTextList) {
                        this._setZoom(this.shapeAndTextList[j], zoomedShape);
                    }
                    this.stage.update();
                }
            })
        }
    }

    _createResetZoomButton() {
        const resetZoomButton = new createjs.Text(
            "Reset Zoom",
            (LAYER_HEIGHT - 2) + "px Arial",
            "black"
        );
        resetZoomButton.x = 0;
        resetZoomButton.y = 10;
        const hit = new createjs.Shape();
        hit.graphics.beginFill("#000").drawRect(
            0,
            0,
            resetZoomButton.getMeasuredWidth(),
            resetZoomButton.getMeasuredHeight()
        );
        resetZoomButton.hitArea = hit;
        resetZoomButton.addEventListener("click", () => {
            resetZoomButton.scaleX = 0;
            this._resetZoom();
        });
        this.stage.addChild(resetZoomButton);
        return resetZoomButton;
    }

    _setZoom(shapeAndText, zoomedShape) {
        const shape = shapeAndText.shape;
        const text = shapeAndText.text;
        if (shape.y > zoomedShape.y) { // shape may be parent
            if (this._isParent(shape, zoomedShape)) { // if it is a parent
                shape.scaleX = 1;
                shape.x = 0;
                shape.fillCommand.style = ZOOMED_PARENT_COLOR;
            } else {
                shape.scaleX = 0;
                text.scaleX = 0;
            }
        } else { // shape may be child
            if (this._isParent(zoomedShape, shape)) { // if it is a child
                shape.scaleX = shape.originalScaleX / zoomedShape.originalScaleX;
                shape.x = (shape.originalX - zoomedShape.originalX) / zoomedShape.originalScaleX;
            } else {
                shape.scaleX = 0;
                text.scaleX = 0;
            }
        }
        const newShape = shape.clone();
        newShape.scaleX = shape.scaleX * 0.9;
        text.mask = newShape;
        text.x = shape.x + 2;
    }

    _isParent(mayBeParent, mayBeChild) {
        return (mayBeParent.originalX <= mayBeChild.originalX + 0.01 &&
        mayBeParent.originalX + (mayBeParent.originalScaleX * this.canvasWidth) >=
            (mayBeChild.originalX + (mayBeChild.originalScaleX * this.canvasWidth) - 0.01));
    }

    _resetZoom() {
        for (let i in this.shapeAndTextList) {
            let shape = this.shapeAndTextList[i].shape;
            let text = this.shapeAndTextList[i].text;
            shape.scaleX = shape.originalScaleX;
            shape.x = shape.originalX;
            shape.fillCommand.style = shape.originalColor;
            text.x = text.originalX;
            text.scaleX = 1;
        }
        this.stage.update();
    }
}

class SearchElem {
    constructor(shape, name) {
        this.name = name;
        this.shape = shape;
    }

    matches(val) {
        return this.name.startsWith(val);
    }

    dim() {
        this.shape.fillCommand.style = "#ccc";
    }

    //noinspection JSUnusedGlobalSymbols
    reset() {
        this.shape.fillCommand.style = this.shape.originalColor;
    }
}