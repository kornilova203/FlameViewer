const MAIN_WIDTH = 1200;
const LAYER_HEIGHT = 19;
const LAYER_GAP = 1;
const POPUP_MARGIN = 4; // have no idea why there is a gap between popup and canvas
const COLORS = ["#18A3FA", "#0887d7"];
const ZOOMED_PARENT_COLOR = "#94bcff";
const RESET_ZOOM_BUTTON_COLOR = "#9da1ff";

/**
 * Draws tree without:
 * - parameters
 * - start time
 */
class AccumulativeTreeDrawer {
    constructor(tree) {
        this.tree = tree;
        this.treeWidth = this.tree.getWidth();
        this.canvasWidth = MAIN_WIDTH;
        this.canvasHeight = (LAYER_HEIGHT + LAYER_GAP) * this.tree.getDepth() + 70;
        this.section = null;
        this.stage = null;
        this.header = null;
        this.nodesCount = 0;
        this.baseNode = this.tree.getBaseNode();
        this.baseNode.depth = 0;
        this._assignParentsAndDepthRecursively(this.baseNode, 0);
        this.isDimSet = this.nodesCount > 50000;
        if (!this.isDimSet) {
            $(".dim").hide();
        }
        this.LAYER_COUNT = this.isDimSet ? 30 : this.tree.getDepth();
        console.log(this.nodesCount);
    }

    setHeader(newHeader) {
        this.header = "for method " + newHeader;
    }

    draw() {
        this.section = this._createSectionWithCanvas();
        this.stage = new createjs.Stage("canvas");
        this.stage.enableMouseOver(20);

        const childNodes = this.baseNode.getNodesList();
        if (childNodes.length === 0) {
            return;
        }
        const maxDepth = this._drawFullTree();

        this.stage.update();
        this._moveCanvas(maxDepth);
        this._updateDim(this.baseNode);
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
     * @param {Boolean} isMostFirst
     * @private
     */
    _drawNode(node, color, scaleX, offsetX, isMostFirst) {
        const shape = this._drawRectangle(node, color, scaleX, offsetX, isMostFirst);
        this._createPopup(node, shape, node.depth);
        this._drawLabel(AccumulativeTreeDrawer._getLabelText(node), shape, scaleX, offsetX, node.depth);
    }

    /**
     * @param node
     * @param {String} color
     * @param {Number} scaleX
     * @param {Number} offsetX
     * @param {Boolean} isMostFirst
     * @returns {*}
     * @private
     */
    _drawRectangle(node, color, scaleX, offsetX, isMostFirst) {
        const shape = new createjs.Shape();
        shape.fillCommand = shape.graphics.beginFill(color).command;
        shape.originalColor = color;
        shape.graphics.drawRect(0, 0, this.canvasWidth, LAYER_HEIGHT);
        const offsetY = this.flipY(AccumulativeTreeDrawer._calcNormaOffsetY(node.depth));
        const pixSizeX = Math.floor(scaleX * this.canvasWidth);
        if (!isMostFirst) {
            offsetX = offsetX + 1;
        }
        if (!(pixSizeX < 2 || isMostFirst)) {
            scaleX = (pixSizeX - 1) / this.canvasWidth;
        }
        if (pixSizeX <= 2) {
            scaleX = 1 / this.canvasWidth;
        }
        if (pixSizeX === 0) {
            offsetX = offsetX - 1;
        }
        shape.setTransform(offsetX, offsetY, scaleX);
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

    _countOffsetXForNode(node) {
        return (node.getOffset() / this.treeWidth) * this.canvasWidth;
    }

    _countScaleXForNode(node) {
        return node.getWidth() / this.treeWidth;
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
     * @param {String} labelText
     * @param {createjs.Shape} shape
     * @param scaleX
     * @param offsetX
     * @param {Number} nodeDepth
     * @return {createjs.Text}
     * @private
     */
    _drawLabel(labelText, shape, scaleX, offsetX, nodeDepth) {
        const text = new createjs.Text(
            labelText,
            (LAYER_HEIGHT - 2) + "px Arial",
            "#fff"
        );
        text.x = offsetX + 2;
        text.originalX = text.x;
        text.y = this.flipY(nodeDepth * (LAYER_GAP + LAYER_HEIGHT));
        AccumulativeTreeDrawer._setTextMask(text, shape, scaleX);
        this.stage.setChildIndex(text, this.stage.getNumChildren() - 1);
        if (scaleX * MAIN_WIDTH > 10) {
            this.stage.addChild(text);
        }
        return text;
    }

    _setPopupPosition(popup, node, depth) {
        popup
            .css("left", this._countOffsetXForNode(node))
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

    static _setTextMask(text, shape, scaleX) {
        const newShape = shape.clone();
        newShape.scaleX = scaleX * 0.9;
        text.mask = newShape;
    }

    /**
     * @param node
     * @param {Number} depth
     * @private
     */
    _assignParentsAndDepthRecursively(node, depth) {
        this.nodesCount++;
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
        while (parent !== this.baseNode) {
            this._drawNode(parent, ZOOMED_PARENT_COLOR, 1, 0, true);
            parent = parent.parent;
        }
    }

    _setNodeZoomed(node) {
        this.stage.removeAllChildren();
        let maxDepth = 0;
        if (node !== this.baseNode) {
            this._expandParents(node);
            maxDepth = this._drawNodesRecursively(
                node,
                0,
                this._countScaleXForNode(node),
                this._countOffsetXForNode(node),
                node.depth,
                true
            );
            this._addResetButton();
        } else { // if reset zoom
            maxDepth = this._drawFullTree();
        }
        this._moveCanvas(maxDepth);
        this._updateDim(node, node.depth);
        this.stage.update();
    }

    /**
     * @param node this node will be drawn
     * @param {Number} drawnLayerCount
     * @param {Number} newFullScaleX
     * @param {Number} newOffsetX
     * @param {Number} maxDepth
     * @param {Boolean} isMostFirst
     * @private
     * @return {Number} max depth
     */
    _drawNodesRecursively(node, drawnLayerCount, newFullScaleX, newOffsetX, maxDepth, isMostFirst) {
        if (drawnLayerCount === this.LAYER_COUNT) {
            return maxDepth;
        }
        this._drawNode(
            node,
            COLORS[0],
            this._countScaleXForNode(node) / newFullScaleX,
            (this._countOffsetXForNode(node) - newOffsetX) / newFullScaleX,
            isMostFirst
        );
        const children = node.getNodesList();
        if (children === undefined) {
            return maxDepth;
        }
        let newMaxDepth = maxDepth;
        for (let i = 0; i < children.length; i++) {
            const depth = this._drawNodesRecursively(
                children[i],
                drawnLayerCount + 1,
                newFullScaleX,
                newOffsetX,
                maxDepth + 1,
                i === 0 && isMostFirst
            );
            if (depth > newMaxDepth) {
                newMaxDepth = depth;
            }
        }
        return newMaxDepth;
    }

    _moveCanvas(maxDepth) {
        const main = $("main");
        let oldTopString = main.css("top");
        oldTopString = oldTopString.substring(0, oldTopString.indexOf("p"));
        const oldTop = parseInt(oldTopString);
        const newY = AccumulativeTreeDrawer._calcNormaOffsetY(maxDepth) + 300;
        main.css("top", -this.canvasHeight + newY);
        if (oldTop < 0) {
            window.scrollBy(0, -oldTop - this.canvasHeight + newY);
        }
    }

    _updateDim(node) {
        if (this.isDimSet) {
            const maxDepth = node.depth + this.LAYER_COUNT;
            if (maxDepth > this._getMaxDepth(node, node.depth)) {
                $(".dim").hide();
            } else {
                $(".dim").show();
            }
        }
    }

    /**
     * @param node
     * @param {Number} maxDepth
     * @private
     */
    _getMaxDepth(node, maxDepth) {
        const children = node.getNodesList();
        if (children === undefined) {
            return maxDepth;
        }
        let newMaxDepth = maxDepth;
        for (let i = 0; i < children.length; i++) {
            const depth = this._getMaxDepth(children[i], maxDepth + 1);
            if (depth > newMaxDepth) {
                newMaxDepth = depth;
            }
        }
        return newMaxDepth;
    }

    _addResetButton() {
        const shape = this._drawRectangle(this.baseNode, RESET_ZOOM_BUTTON_COLOR, 1, 0, true);
        this._drawLabel("Reset zoom", shape, 1, 0, 0);
    }

    /**
     * @param node
     * @return {String}
     * @private
     */
    static _getLabelText(node) {
        return `${node.getNodeInfo().getMethodName()} (${node.getNodeInfo().getClassName().split("/").join(".")})`;
    }

    _drawFullTree() {
        let maxDepth = 0;
        const children = this.baseNode.getNodesList();
        for (let i = 0; i < children.length; i++) {
            const depth = this._drawNodesRecursively(
                children[i],
                0,
                1,
                0,
                0,
                true
            );
            if (depth > maxDepth) {
                maxDepth = depth;
            }
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