const MAIN_WIDTH = 1200;
const LAYER_HEIGHT = 19;
const LAYER_GAP = 1;
const POPUP_MARGIN = 6; // have no idea why there is a gap between popup and canvas
const ZOOMED_PARENT_COLOR = "#94bcff";
const RESET_ZOOM_BUTTON_COLOR = "#9da1ff";
const HIGHLIGHT_NOT_SET_COLOR = "#e4e4e4";
const SPACE_ABOVE_TREE = 40;

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
        this.canvasHeight = (LAYER_HEIGHT + LAYER_GAP) * (this.tree.getDepth() + 1) + SPACE_ABOVE_TREE;
        this.section = null;
        this.stage = null;
        this.zoomedStage = null;
        this.header = null;
        this.baseNode = this.tree.getBaseNode();
        this.baseNode.depth = 0;
        this.$popup = null;
        this.canvasOffset = 0;
        this.enableZoom = true;
        this.nodesCount = -1;
        // for search:
        this.searchVal = "";
        this.currentlyShownNodes = [];
        this.baseNode.fillCommand = {};
        this.wasMainStageHighlighted = false;
    }

    /**
     * @param node
     * @public
     */
    _countNodesRecursively(node) {
        this.nodesCount++;
        const children = node.getNodesList();
        for (let i = 0; i < children.length; i++) {
            this._countNodesRecursively(children[i]);
        }
    }

    setHeader(newHeader) {
        this.header = "for method " + newHeader;
    }

    draw() {
        console.log("start drawing");
        this._prepareDraw();
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
        console.log("Drawing took " + (new Date().getTime() - startTime));
    };

    /**
     * @protected
     */
    _prepareDraw() {
        this._assignParentsAndDepthRecursively(this.baseNode, 0);
        this._setOriginalColorRecursively(this.baseNode);
        this._enableSearch();
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
     * @param {String} color
     * @param {Number} scaleX
     * @param {Number} offsetX
     * @param {Boolean} isMostFirst
     * @param {createjs.Stage} stage
     * @private
     */
    _drawNode(node, color, scaleX, offsetX, isMostFirst, stage) {
        console.log("draw node: " + node.getNodeInfo().getMethodName());
        console.log("depth: " + node.depth);
        console.log("offset: " + offsetX);
        console.log("scale: " + scaleX);
        const shape = this._drawRectangle(node, color, scaleX, offsetX, isMostFirst, stage);
        this._addShowPopupEvent(shape, offsetX + this.canvasOffset, node.depth, node);
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
     * @return {createjs.Shape}
     * @private
     */
    _drawRectangle(node, color, scaleX, offsetX, isMostFirst, stage) {
        const shape = new createjs.Shape();
        if (stage === this.stage) {
            node.fillCommand = shape.graphics.beginFill(color).command;
        } else {
            node.zoomedFillCommand = shape.graphics.beginFill(color).command;
        }
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
        if (this.enableZoom) {
            //noinspection JSUnresolvedFunction
            shape.addEventListener("click", () => {
                this._setNodeZoomed(node);
            })
        }
    }

    _countOffsetXForNode(node) {
        return (node.getOffset() / this.treeWidth) * this.canvasWidth;
    }

    _countScaleXForNode(node) {
        return node.getWidth() / this.treeWidth;
    }

    _createPopup() {
        const popupContent = templates.tree.accumulativeTreePopup().content;
        this.$popup = $(popupContent).appendTo(this.section);
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
        this._setTextMask(text, shape, scaleX);
        // noinspection JSUnresolvedFunction
        stage.setChildIndex(text, this.stage.getNumChildren() - 1);
        // noinspection JSUnresolvedFunction
        stage.addChild(text);
        return text;
    }

    /**
     * @param {Number} offsetX
     * @param depth
     * @protected
     */
    _setPopupPosition(offsetX, depth) {
        this.$popup
            .css("left", offsetX)
            .css("margin-top", -AccumulativeTreeDrawer._calcNormaOffsetY(depth) - POPUP_MARGIN + 1)
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
        this.$popup.hover(
            () => {
                isPopupHovered = true;
            },
            () => {
                isPopupHovered = false;
                if (!isMethodHovered) {
                    this.$popup.hide();
                }
            });
        // noinspection JSUnresolvedFunction
        shape.addEventListener("mouseover", () => {
            isMethodHovered = true;
            this._setPopupContent(node);
            this._setPopupPosition(offsetX, depth);
            this.$popup.show();
        });
        // noinspection JSUnresolvedFunction
        shape.addEventListener("mouseout", () => {
            isMethodHovered = false;
            if (!isPopupHovered) {
                this.$popup.hide();
            }
        });
    }

    _enableSearch() {
        const input = $("#search-method-form").find("input");
        input.on('change keyup copy paste cut', () => {
            const val = input.val();
            if (!val) {
                this.searchVal = "";
                this._resetHighlight();
            } else {
                const lowercaseVal = val.toLowerCase();
                this.searchVal = lowercaseVal;
                if (this.currentlyShownNodes.length === 0) {
                    this._setHighlightOnMainStage(lowercaseVal);
                } else {
                    this._setHighlightOnZoomedStage(lowercaseVal);
                }
            }
        })
    }

    /**
     * @param node
     * @private
     */
    static _resetHighlightRecursively(node) {
        if (node.fillCommand !== undefined) {
            node.fillCommand.style = node.originalColor;
        }
        const children = node.getNodesList();
        for (let i = 0; i < children.length; i++) {
            AccumulativeTreeDrawer._resetHighlightRecursively(children[i]);
        }
    }

    _setTextMask(text, shape, scaleX) {
        const newShape = shape.clone();
        const nodeWidth = scaleX * this.canvasWidth;
        newShape.scaleX = (nodeWidth - 4) / this.canvasWidth;
        text.mask = newShape;
    }

    /**
     * @param node
     * @param {Number} depth
     */
    _assignParentsAndDepthRecursively(node, depth) {
        const children = node.getNodesList();
        AccumulativeTreeDrawer._assignNormalizedName(node);
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

    _setNodeZoomed(node) {
        let maxDepth = 0;
        this.currentlyShownNodes = [];
        if (node !== this.baseNode) {
            common.showLoader(constants.loaderMessages.drawing, () => {
                this.zoomedStage.removeAllChildren();
                this._expandParents(node);
                maxDepth = this._drawNodesRecursively(
                    node,
                    0,
                    this._countScaleXForNode(node),
                    this._countOffsetXForNode(node),
                    node.depth,
                    true,
                    this.zoomedStage,
                    true,
                    false
                );
                this._addResetButton();
                if (this.searchVal !== "") {
                    this._setHighlightOnZoomedStage(this.searchVal);
                } else {
                    this.zoomedStage.update();
                }
                $("#" + this.stage.id).hide();
                $("#" + this.zoomedStage.id).show();
                common.hideLoader()
            });
        } else { // if reset zoom
            if (this.searchVal !== "") {
                this._setHighlightOnMainStage(this.searchVal);
            } else if (this.wasMainStageHighlighted) {
                this._resetHighlight();
            }
            $("#" + this.zoomedStage.id).hide();
            $("#" + this.stage.id).show();
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
     * @param {Boolean} saveNodesToList
     * @param {Boolean} saveOriginalColor
     * @private
     * @return {Number} max depth
     */
    _drawNodesRecursively(node,
                          drawnLayerCount,
                          newFullScaleX,
                          newOffsetX,
                          maxDepth,
                          isMostFirst,
                          stage,
                          saveNodesToList,
                          saveOriginalColor) {
        if (saveNodesToList) {
            this.currentlyShownNodes.push(node);
        }
        this._drawNode(
            node,
            node.originalColor,
            this._countScaleXForNode(node) / newFullScaleX,
            (this._countOffsetXForNode(node) - newOffsetX) / newFullScaleX,
            isMostFirst,
            stage
        );
        const children = node.getNodesList();
        let newMaxDepth = maxDepth;
        for (let i = 0; i < children.length; i++) {
            const depth = this._drawNodesRecursively(
                children[i],
                drawnLayerCount + 1,
                newFullScaleX,
                newOffsetX,
                maxDepth + 1,
                i === 0 && isMostFirst,
                stage,
                saveNodesToList,
                saveOriginalColor
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

    _addResetButton() {
        const shape = this._drawRectangle(this.baseNode, RESET_ZOOM_BUTTON_COLOR, 1, 0, true, this.zoomedStage);
        this._drawLabel("Reset zoom", shape, 1, 0, 0, this.zoomedStage);
    }

    /**
     * @param node not a baseNode
     * @return {String}
     * @private
     */
    static _getLabelText(node) {
        return `${node.getNodeInfo().getMethodName()} (${node.getNodeInfo().getClassName()})`;
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
                this.stage,
                false,
                true
            );
            if (depth > maxDepth) {
                maxDepth = depth;
            }
        }
        this.stage.update();
    }

    /**
     * @param node
     * @protected
     */
    _setPopupContent(node) {
        const desc = node.getNodeInfo().getDescription()
            .split("/").join("%2F")
            .split("(").join("%28")
            .split(")").join("%29")
            .split(";").join("%3B");
        this.$popup.find("h3").text(`${node.getNodeInfo().getClassName()}.${node.getNodeInfo().getMethodName()}`);
        this.$popup.find(".outgoing-link").attr("href", `/flamegraph-profiler/outgoing-calls?` +
            `file=${constants.fileName}&` +
            `project=${constants.projectName}&` +
            `method=${node.getNodeInfo().getMethodName()}&` +
            `class=${node.getNodeInfo().getClassName()}&` +
            `desc=${desc}&` +
            `isStatic=${node.getNodeInfo().getIsStatic() === true ? "true" : "false"}`
        );
        this.$popup.find(".incoming-link").attr("href", `/flamegraph-profiler/incoming-calls?` +
            `file=${constants.fileName}&` +
            `project=${constants.projectName}&` +
            `method=${node.getNodeInfo().getMethodName()}&` +
            `class=${node.getNodeInfo().getClassName()}&` +
            `desc=${desc}&` +
            `isStatic=${node.getNodeInfo().getIsStatic() === true ? "true" : "false"}`
        );
    }

    /**
     * @param node
     * @param {String} val string in lowercase
     * @private
     */
    _updateHighlightRecursively(node, val) {
        if (AccumulativeTreeDrawer._isNodeHighlighted(node, val)) {
            AccumulativeTreeDrawer._setHighlight(node, true, true);
        } else {
            AccumulativeTreeDrawer._setHighlight(node, false, true);
        }
        const children = node.getNodesList();
        for (let i = 0; i < children.length; i++) {
            this._updateHighlightRecursively(children[i], val);
        }
    }

    /**
     * @param {Array} currentlyShownNodes
     * @param {String} lowercaseVal
     * @private
     */
    static _updateHighlight(currentlyShownNodes, lowercaseVal) {
        for (let i = 0; i < currentlyShownNodes.length; i++) {
            if (AccumulativeTreeDrawer._isNodeHighlighted(currentlyShownNodes[i], lowercaseVal)) {
                AccumulativeTreeDrawer._setHighlight(currentlyShownNodes[i], true, false);
            } else {
                AccumulativeTreeDrawer._setHighlight(currentlyShownNodes[i], false, false);
            }
        }
    }

    /**
     * @param node
     * @param {Boolean} isHighlightSet
     * @param {Boolean} isMainStage
     * @private
     */
    static _setHighlight(node, isHighlightSet, isMainStage) {
        if (isMainStage) {
            node.fillCommand.style = isHighlightSet ? node.originalColor : HIGHLIGHT_NOT_SET_COLOR;
        } else {
            node.zoomedFillCommand.style = isHighlightSet ? node.originalColor : HIGHLIGHT_NOT_SET_COLOR;
        }
    }

    /**
     * @param node
     * @param {String} val
     * @return {boolean}
     * @private
     */
    static _isNodeHighlighted(node, val) {
        if (node.normalizedName !== undefined) { // if not a baseNode
            return node.normalizedName.indexOf(val) !== -1;
        }
    }

    /**
     * @param node
     * @private
     */
    static _assignNormalizedName(node) {
        const nodeInfo = node.getNodeInfo();
        if (nodeInfo === undefined) {
            return;
        }
        let className = nodeInfo.getClassName();
        if (className.indexOf("/") !== -1) {
            className = className.split("/").join(".");
        }
        node.normalizedName = (className + "." + nodeInfo.getMethodName()).toLowerCase();
    }

    static _resetHighlightList(currentlyShownNodes) {
        for (let i = 0; i < currentlyShownNodes.length; i++) {
            AccumulativeTreeDrawer._setHighlight(currentlyShownNodes[i], true, false);
        }
    }

    /**
     * @private
     */
    _resetHighlight() {
        if (this.currentlyShownNodes.length === 0) { // if not zoomed
            this.wasMainStageHighlighted = false;
            AccumulativeTreeDrawer._resetHighlightRecursively(this.baseNode);
            this.stage.update();
        } else { // if zoomed
            AccumulativeTreeDrawer._resetHighlightList(this.currentlyShownNodes);
            this.zoomedStage.update();
        }
    }

    _setHighlightOnMainStage(lowercaseVal) {
        this.wasMainStageHighlighted = true;
        this._updateHighlightRecursively(this.baseNode, lowercaseVal);
        this.stage.update();
    }

    _setHighlightOnZoomedStage(lowercaseVal) {
        AccumulativeTreeDrawer._updateHighlight(this.currentlyShownNodes, lowercaseVal);
        this.zoomedStage.update();
    }

    _setOriginalColorRecursively(node) {
        const lightness = 50;
        node.originalColor = `hsl(205, 94%, ${lightness}%)`;
        const children = node.getNodesList();
        for (let i = 0; i < children.length; i++) {
            this._setOriginalColorRecursively(children[i]);
        }

    }
}