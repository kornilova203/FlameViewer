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
            this._drawNodesRecursively(childNodes[i], 0, 1, 0);
        }

        this.stage.update();
        // this._enableZoom();
    };

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
     * @param {String} color
     * @param {Number} scaleX
     * @param {Number} offsetX
     * @private
     */
    _drawNode(node, color, scaleX, offsetX) {
        const shape = this._drawRectangle(node, color, scaleX, offsetX);
        this._drawLabel(node, shape);
    }

    /**
     * @param node
     * @param {String} color
     * @param {Number} scaleX
     * @param {Number} offsetX
     * @returns {*}
     * @private
     */
    _drawRectangle(node, color, scaleX, offsetX) {
        const shape = new createjs.Shape();
        shape.fillCommand = shape.graphics.beginFill(color).command;
        shape.originalColor = color;
        shape.graphics.drawRect(0, 0, this.canvasWidth, LAYER_HEIGHT);
        const offsetY = this.flipY(AccumulativeTreeDrawer._calcNormaOffsetY(node.depth));
        shape.setTransform(offsetX, offsetY, scaleX);
        this._createPopup(node, shape, node.depth);
        this.stage.addChild(shape);
        this.listenScale(node, shape);
        return shape;
    }

    /**
     * @param node
     * @param {createjs.Shape} shape
     */
    listenScale(node, shape) {
        //noinspection JSUnresolvedFunction
        shape.addEventListener("click", () => {
            this._setNodeZoomed(node);
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
        const children = node.getNodesList();
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
        let parent = node.parent;
        while (parent !== this.tree.getBaseNode()) {
            this._drawNode(parent, ZOOMED_PARENT_COLOR, 1, 0);
            parent = parent.parent;
        }
    }

    _setNodeZoomed(node) {
        this.stage.removeAllChildren();
        this._expandParents(node);
        this._drawNodesRecursively(node, 0, node.getWidth() / this.width, this._getOffsetXForNode(node));
        // this._drawRecursively(node, scale, this._getOffsetXForNode(node));
        this.stage.update();
    }

    /**
     * @param node this node will be drawn
     * @param {Number} drawnLayerCount
     * @param {Number} newFullWidth
     * @param {Number} newOffset
     * @private
     */
    _drawNodesRecursively(node, drawnLayerCount, newFullWidth, newOffset) {
        if (drawnLayerCount === LAYER_COUNT) {
            return;
        }
        this._drawNode(
            node,
            COLORS[0],
            node.getWidth() / this.width / newFullWidth,
            this._getOffsetXForNode(node) - newOffset
        );
        const children = node.getNodesList();
        if (children === undefined) {
            return;
        }
        for (let i = 0; i < children.length; i++) {
            this._drawNodesRecursively(children[i], drawnLayerCount + 1, newFullWidth, newOffset);
        }
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