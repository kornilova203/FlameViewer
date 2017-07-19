const MAIN_WIDTH = 1200;
const LAYER_HEIGHT = 19;
const LAYER_GAP = 1;
const POPUP_MARGIN = 4; // have no idea why there is a gap between popup and canvas
const COLORS = ["#18A3FA", "#0887d7"];
const ZOOMED_PARENT_COLOR = "#94bcff";
const LAYER_COUNT = 30;

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
        this.canvasHeight = (LAYER_HEIGHT + LAYER_GAP) * LAYER_COUNT + 70;
        this.section = null;
        this.stage = null;
        this.header = null;
        this._assignParentsAndDepthRecursively(this.tree.getBaseNode(), 0);
        // this._enableSearch();
    }

    setHeader(newHeader) {
        this.header = "for method " + newHeader;
    }

    draw() {
        this.section = this._createSectionWithCanvas();
        this.stage = new createjs.Stage("canvas");
        this.stage.enableMouseOver(20);

        const childNodes = this.tree.getBaseNode().getNodesList();
        if (childNodes.length === 0) {
            return;
        }
        for (let i = 0; i < childNodes.length; i++) {
            this._drawRecursively(childNodes[i], 1, 0);
        }

        this.stage.update();
        // this._enableZoom();
    };

    /**
     * @param node
     * @param {Number} scale
     * @param {Number} newOffset
     * @private
     */
    _drawRecursively(node, scale, newOffset) {
        if (node.depth < LAYER_COUNT) {
            this._drawNode(node, 0, scale, newOffset);
            const childNodes = node.getNodesList();
            if (childNodes.length === 0) {
                return;
            }
            for (let i = 0; i < childNodes.length; i++) {
                this._drawRecursively(childNodes[i], scale, newOffset);
            }
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

    /**
     * @param node
     * @param {Number} colorId
     * @param {Number} scale
     * @param {Number} newOffset
     * @private
     */
    _drawNode(node, colorId, scale, newOffset) {
        const shape = this._drawRectangle(node, colorId, scale, newOffset);
        this._drawLabel(node, shape);
    }

    /**
     *
     * @param node
     * @param {Number} colorId
     * @param {Number} scale
     * @param {Number} newOffset
     * @returns {*}
     * @private
     */
    _drawRectangle(node, colorId, scale, newOffset) {
        const shape = new createjs.Shape();
        shape.fillCommand = shape.graphics.beginFill(COLORS[colorId]).command;
        shape.originalColor = COLORS[colorId];
        shape.graphics.drawRect(0, 0, this.canvasWidth, LAYER_HEIGHT);
        const offsetX = this._getOffsetXForNode(node) - newOffset;
        const offsetY = this.flipY(AccumulativeTreeDrawer._calcNormaOffsetY(node.depth));
        const scaleX = node.getWidth() / this.width / scale;
        shape.originalX = offsetX;
        shape.originalScaleX = scaleX;
        shape.setTransform(offsetX, offsetY, scaleX);
        this._createPopup(node, shape, node.depth);
        this.stage.addChild(shape);
        this.listenScale(node, shape, node.getWidth() / this.width);
        return shape;
    }

    /**
     * @param node
     * @param {createjs.Shape} shape
     * @param {Number} scale
     */
    listenScale(node, shape, scale) {
        //noinspection JSUnresolvedFunction
        shape.addEventListener("click", () => {
            this.stage.removeAllChildren();
            // this._expandParents(node);
            this._drawRecursively(node, scale, this._getOffsetXForNode(node));
            this.stage.update();
        })
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
                count: node.getNodeInfo().getCount(),
                fileName: fileName,
                projectName: projectName
            }
        ).content;
        const popup = $(popupContent).appendTo(this.section);
        this._setPopupPosition(popup, node, depth);
        AccumulativeTreeDrawer._addMouseEvents(shape, popup);
    }

    /**
     * @param node
     * @param {createjs.Shape} shape
     * @return {createjs.Text}
     * @private
     */
    _drawLabel(node, shape) {
        const text = new createjs.Text(
            node.getNodeInfo().getClassName().split("/").join(".") + "." + node.getNodeInfo().getMethodName(),
            (LAYER_HEIGHT - 2) + "px Arial",
            "#fff"
        );
        text.x = this._getOffsetXForNode(node) + 2;
        text.originalX = text.x;
        text.y = this.flipY(node.depth * (LAYER_GAP + LAYER_HEIGHT));
        AccumulativeTreeDrawer._setTextPosition(text, shape);
        this.stage.setChildIndex(text, this.stage.getNumChildren() - 1);
        if (shape.scaleX * MAIN_WIDTH > 10) {
            this.stage.addChild(text);
        }
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
        const input = $("#search-method").find("input");
        input.on('change keyup copy paste cut', () => {
            const val = input.val();
            if (!val) {
                this._resetHighlight();
                return;
            }
            // TODO: reimplement search
            this.stage.update();
        })
    }

    _resetHighlight() {
        // TODO: implement
        this.stage.update();
    }

    _createResetZoomButton() {
        const resetZoomButton = new createjs.Text(
            "Reset Zoom",
            (LAYER_HEIGHT - 2) + "px Arial",
            "black"
        );
        resetZoomButton.cursor = "pointer";
        resetZoomButton.x = 0;
        resetZoomButton.y = 10;
        const hit = new createjs.Shape();
        hit.graphics.beginFill("#222").drawRect(
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
        AccumulativeTreeDrawer._setTextPosition(text, shape);
    }

    _isParent(mayBeParent, mayBeChild) {
        return (mayBeParent.originalX <= mayBeChild.originalX + 0.01 &&
        mayBeParent.originalX + (mayBeParent.originalScaleX * this.canvasWidth) >=
        (mayBeChild.originalX + (mayBeChild.originalScaleX * this.canvasWidth) - 0.01));
    }

    static _setTextPosition(text, shape) {
        text.scaleX = 1;
        const newShape = shape.clone();
        newShape.scaleX = shape.scaleX * 0.9;
        text.mask = newShape;
        text.x = shape.x + 2;
    }

    /**
     * @param node
     * @param {Number} depth
     * @private
     */
    _assignParentsAndDepthRecursively(node, depth) {
        let children;
        try {
            children = node.getNodesList();
        } catch (err) {
            console.error(err);
            return;
        }
        if (children === undefined) {
            return;
        }
        node.depth = depth;
        for (let i = 0; i < children.length; i++) {
            children[i].parent = node;
            this._assignParentsAndDepthRecursively(children[i], depth + 1);
        }
    }

    _expandParents(node) {

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