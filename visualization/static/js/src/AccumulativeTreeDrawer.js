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
        this.zoomedStage = null;
        this.header = null;
        this.nodesCount = 0;
        this.baseNode = this.tree.getBaseNode();
        this.baseNode.depth = 0;
        this.popup = null;
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
        console.log("start drawing");
        const startTime = new Date().getTime();
        this.section = this._createSectionWithCanvas();
        this.stage = new createjs.Stage("canvas");
        this.stage.id = "canvas";
        this.zoomedStage = new createjs.Stage("canvas-zoomed");
        this.zoomedStage.id = "canvas-zoomed";
        this.stage.enableMouseOver(20);
        this.zoomedStage.enableMouseOver(20);

        this._createPopup(); // one for all nodes

        const childNodes = this.baseNode.getNodesList();
        if (childNodes.length === 0) {
            return;
        }
        const maxDepth = this._drawFullTree();
        this._moveCanvas(maxDepth);
        this._updateDim(this.baseNode);
        // this._enableZoom();
        console.log("Drawing took " + (new Date().getTime() - startTime));
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
     * @param {createjs.Stage} stage
     * @private
     */
    _drawNode(node, color, scaleX, offsetX, isMostFirst, stage) {
        const shape = this._drawRectangle(node, color, scaleX, offsetX, isMostFirst, stage);
        this._addShowPopupEvent(shape, offsetX, node.depth, node);
        if (scaleX * this.canvasWidth > 5) {
            this._drawLabel(AccumulativeTreeDrawer._getLabelText(node), shape, scaleX, offsetX, node.depth, stage);
        }
    }

    /**
     * @param node
     * @param {String} color
     * @param {Number} scaleX
     * @param {Number} offsetX
     * @param {Boolean} isMostFirst
     * @param {createjs.Stage} stage
     * @returns {*}
     * @private
     */
    _drawRectangle(node, color, scaleX, offsetX, isMostFirst, stage) {
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
        // noinspection JSUnresolvedFunction
        stage.addChild(shape);
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

    _createPopup() {
        const popupContent = templates.tree.accumulativeTreePopup().content;
        this.popup = $(popupContent).appendTo(this.section);
    }

    /**
     * @param {String} labelText
     * @param {createjs.Shape} shape
     * @param scaleX
     * @param offsetX
     * @param {Number} nodeDepth
     * @param {createjs.Stage} stage
     * @return {createjs.Text}
     * @private
     */
    _drawLabel(labelText, shape, scaleX, offsetX, nodeDepth, stage) {
        const text = new createjs.Text(
            labelText,
            (LAYER_HEIGHT - 2) + "px Arial",
            "#fff"
        );
        text.x = offsetX + 2;
        text.y = this.flipY(nodeDepth * (LAYER_GAP + LAYER_HEIGHT));
        AccumulativeTreeDrawer._setTextMask(text, shape, scaleX);
        // noinspection JSUnresolvedFunction
        stage.setChildIndex(text, this.stage.getNumChildren() - 1);
        // noinspection JSUnresolvedFunction
        stage.addChild(text);
        return text;
    }

    /**
     * @param {Number} offsetX
     * @param depth
     * @private
     */
    _setPopupPosition(offsetX, depth) {
        this.popup
            .css("left", offsetX)
            .css("margin-top", -AccumulativeTreeDrawer._calcNormaOffsetY(depth) - POPUP_MARGIN)
    }

    static _calcNormaOffsetY(depth) {
        return depth * (LAYER_GAP + LAYER_HEIGHT);
    }

    /**
     * @param {createjs.Shape} shape
     * @param {Number} offsetX
     * @param {Number} depth
     * @param node
     * @private
     */
    _addShowPopupEvent(shape, offsetX, depth, node) {
        let isPopupHovered = false;
        let isMethodHovered = false;
        this.popup.hover(
            () => {
                isPopupHovered = true;
            },
            () => {
                isPopupHovered = false;
                if (!isMethodHovered) {
                    this.popup.hide();
                }
            });
        // noinspection JSUnresolvedFunction
        shape.addEventListener("mouseover", () => {
            isMethodHovered = true;
            AccumulativeTreeDrawer._setPopupContent(node);
            this._setPopupPosition(offsetX, depth);
            this.popup.show();
        });
        // noinspection JSUnresolvedFunction
        shape.addEventListener("mouseout", () => {
            isMethodHovered = false;
            if (!isPopupHovered) {
                this.popup.hide();
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
            this._drawNode(parent, ZOOMED_PARENT_COLOR, 1, 0, true, this.zoomedStage);
            parent = parent.parent;
        }
    }

    /**
     * @param {Function} callback
     */
    static showLoader(callback) {
        $(".loader-background").fadeIn(200, callback);
    }

    static hideLoader() {
        $(".loader-background").fadeOut();
    }

    _setNodeZoomed(node) {
        let maxDepth = 0;
        if (node !== this.baseNode) {
            AccumulativeTreeDrawer.showLoader(() => {
                this.zoomedStage.removeAllChildren();
                this._expandParents(node);
                maxDepth = this._drawNodesRecursively(
                    node,
                    0,
                    this._countScaleXForNode(node),
                    this._countOffsetXForNode(node),
                    node.depth,
                    true,
                    this.zoomedStage
                );
                this._addResetButton();
                $("#" + this.stage.id).hide();
                this.zoomedStage.update();
                $("#" + this.zoomedStage.id).show();
            });
            AccumulativeTreeDrawer.hideLoader()
        } else { // if reset zoom
            $("#" + this.zoomedStage.id).hide();
            $("#" + this.stage.id).show();
        }
        if (this.isDimSet) {
            this._moveCanvas(maxDepth);
            this._updateDim(node, node.depth);
        }
    }

    /**
     * @param node this node will be drawn
     * @param {Number} drawnLayerCount
     * @param {Number} newFullScaleX
     * @param {Number} newOffsetX
     * @param {Number} maxDepth
     * @param {Boolean} isMostFirst
     * @param {createjs.Stage} stage
     * @private
     * @return {Number} max depth
     */
    _drawNodesRecursively(node, drawnLayerCount, newFullScaleX, newOffsetX, maxDepth, isMostFirst, stage) {
        if (drawnLayerCount === this.LAYER_COUNT) {
            return maxDepth;
        }
        this._drawNode(
            node,
            COLORS[0],
            this._countScaleXForNode(node) / newFullScaleX,
            (this._countOffsetXForNode(node) - newOffsetX) / newFullScaleX,
            isMostFirst,
            stage
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
                i === 0 && isMostFirst,
                stage
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
        const shape = this._drawRectangle(this.baseNode, RESET_ZOOM_BUTTON_COLOR, 1, 0, true, this.zoomedStage);
        this._drawLabel("Reset zoom", shape, 1, 0, 0, this.zoomedStage);
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
                true,
                this.stage
            );
            if (depth > maxDepth) {
                maxDepth = depth;
            }
        }
        this.stage.update();
    }

    /**
     * @param node
     * @private
     */
    static _setPopupContent(node) {
        const desc = node.getNodeInfo().getDescription()
            .split(".").join("%2F")
            .split("(").join("%28")
            .split(")").join("%29")
            .split(";").join("%3B");
        $(".popup h3").text(`${node.getNodeInfo().getClassName().split("/").join(".")}.${node.getNodeInfo().getMethodName()}`);
        $(".popup .outgoing-link").attr("href", `/flamegraph-profiler/outgoing-calls?` +
            `file=${fileName}&` +
            `project=${projectName}&` +
            `method=${node.getNodeInfo().getMethodName()}&` +
            `class=${node.getNodeInfo().getClassName()}&` +
            `desc=${desc}&` +
            `isStatic=${node.getNodeInfo().getIsStatic() === true ? "true" : "false"}`
        );
        $(".popup .incoming-link").attr("href", `/flamegraph-profiler/incoming-calls?` +
            `file=${fileName}&` +
            `project=${projectName}&` +
            `method=${node.getNodeInfo().getMethodName()}&` +
            `class=${node.getNodeInfo().getClassName()}&` +
            `desc=${desc}&` +
            `isStatic=${node.getNodeInfo().getIsStatic() === true ? "true" : "false"}`
        );
    }
}