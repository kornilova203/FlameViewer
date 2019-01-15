const ZOOMED_PARENT_COLOR = "#94bcff";
const RESET_ZOOM_BUTTON_COLOR = "#9da1ff";
const HIGHLIGHT_NOT_SET_COLOR = "#d1d1d1";

/**
 * Draws tree without:
 * - parameters
 * - start time
 * @abstract
 */
module.exports.TreeDrawer = class TreeDrawer {
    constructor(tree) {
        this.tree = tree;
        this.treeWidth = this.tree.getWidth();
        this.currentCanvasWidth = 0;
        this.canvasHeight = (constants.LAYER_HEIGHT + constants.LAYER_GAP) * (this.tree.getDepth() + 1);
        this.stage = null;
        this.zoomedStage = null;
        this.zoomedNode = null;
        this.$fileMenu = $(".file-menu");
        this.baseNode = this.tree.getBaseNode();
        this.packageList = {}; // map package name to color
        this._buildPackageListRecursively(this.baseNode);
        this._setPackageColors();
        this.baseNode.depth = 0;
        this.$popup = null;
        this.$popupClassIcon = null;
        this.$popupClassName = null;
        this.$canvasWrapper = null;
        this.$popupParameters = null;
        this.$popupIcon = null;
        this.canvasOffset = 0;
        this.enableZoom = true;
        this.nodesCount = tree.getTreeInfo().getNodesCount();
        // for search:
        this.isHighlightedFunction = null;
        this.currentlyShownNodes = [];
        this.baseNode.fillCommand = {};
        this.wasMainStageHighlighted = false;
        this.$section = this._createSection();
    }

    draw() {
        console.log("start drawing");
        this._prepareDraw();
        const startTime = new Date().getTime();

        const childNodes = this.baseNode.getNodesList();
        if (childNodes.length === 0) {
            return;
        }
        this._drawFullTree();
        console.log("Drawing took " + (new Date().getTime() - startTime));
        this._enableResizeZoomedCanvas();
    };

    /**
     * @protected
     */
    _prepareDraw() {
        this._prepareTree(this.tree, 0);
        this._enableSearch();
        this._createStages();
        this._createPopup(); // one for all nodes
    }

    /**
     * Get canvas Y coordinate (it start from top)
     * @param y
     * @returns {number}
     * @protected
     */
    flipY(y) {
        return this.canvasHeight - y - constants.LAYER_HEIGHT;
    };

    _createSection() {
        const sectionContent = templates.tree.getAccumulativeTreeSection({}).content;
        return $(sectionContent).appendTo($("main"));
    };

    /**
     * @param node
     * @param {String} color
     * @param {Number} scaleX
     * @param {Number} offsetX
     * @param {Boolean} isMostFirst
     * @param {createjs.Stage} stage
     * @param {number} zoomedNodeDepth
     * @private
     */
    _drawNode(node, color, scaleX, offsetX, isMostFirst, stage, zoomedNodeDepth) {
        const shape = this._drawRectangle(node, color, scaleX, offsetX, isMostFirst, stage);
        if (scaleX * this.currentCanvasWidth > 8 || node.depth - zoomedNodeDepth <= 1) {
            this.listenScale(node, shape);
            this._addShowPopupEvent(shape, offsetX + this.canvasOffset, node.depth, node);
            this._drawLabel(TreeDrawer._getLabelText(node), shape, scaleX, offsetX, node.depth, stage);
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
        shape.graphics.drawRect(0, 0, this.currentCanvasWidth, constants.LAYER_HEIGHT);
        const offsetY = this.flipY(TreeDrawer._calcNormaOffsetY(node.depth));
        const pixSizeX = Math.floor(scaleX * this.currentCanvasWidth);
        if (!isMostFirst) {
            offsetX = offsetX + 1;
        }
        if (!(pixSizeX < 2 || isMostFirst)) {
            scaleX = (pixSizeX - 1) / this.currentCanvasWidth;
        }
        if (pixSizeX <= 2) {
            scaleX = 1 / this.currentCanvasWidth;
        }
        if (pixSizeX === 0) {
            offsetX = offsetX - 1;
        }
        if (offsetX < 0) {
            offsetX = 0;
        }
        shape.setTransform(offsetX, offsetY, scaleX);
        // noinspection JSUnresolvedFunction
        stage.addChild(shape);
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
                this._changeZoom(node);
            })
        }
    }

    _countOffsetXForNode(node) {
        return (node.getOffset() / this.treeWidth) * this.currentCanvasWidth;
    }

    _countScaleXForNode(node) {
        return node.getWidth() / this.treeWidth;
    }

    _createPopup() {
        const popupContent = templates.tree.accumulativeTreePopup().content;
        this.$canvasWrapper = this.$section.find(".canvas-wrapper");
        this.$popup = $(popupContent).appendTo(this.$canvasWrapper);
        this.$popupClassIcon = this.$popup.find(".class-icon");
        this.$popupClassName = this.$popup.find(".class-name");
        this.$popupParameters = this.$popup.find(".parameters");
        this.$popupIcon = this.$popup.find(".parameter-icon");
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
            (constants.LAYER_HEIGHT - 2) + "px Arial",
            "#fff"
        );
        text.x = offsetX + 2;
        text.y = this.flipY(nodeDepth * (constants.LAYER_GAP + constants.LAYER_HEIGHT));
        this._setTextMask(text, shape, scaleX);
        // noinspection JSUnresolvedFunction
        stage.setChildIndex(text, this.stage.getNumChildren() - 1);
        // noinspection JSUnresolvedFunction
        stage.addChild(text);
        return text;
    }

    /**
     * @param {Number} offsetX
     * @param {Number} depth
     */
    _setPopupPosition(offsetX, depth) {
        this.$popup
            .css("left", offsetX)
            .css("margin-top", -TreeDrawer._calcNormaOffsetY(depth) - constants.POPUP_MARGIN + 2)
    }

    static _calcNormaOffsetY(depth) {
        return depth * (constants.LAYER_GAP + constants.LAYER_HEIGHT);
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
            this._setPopupPosition(this._shiftIfHidden(offsetX), depth);
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

    /**
     * Enable files search
     * @private
     */
    _enableSearch() {
        const $searchMethodForm = $("#search-method-form");
        $searchMethodForm.addClass("visible");
        const $input = $searchMethodForm.find("input");
        $input.off(); // in call tree the input can be reused
        $input.val(""); // clear input
        $input.on('change keyup copy paste cut', common.updateRareDecorator(500, () => {
            const val = $input.val();
            if (!val) {
                this.isHighlightedFunction = null;
                this._resetHighlight();
            } else {
                const lowercaseVal = TreeDrawer.removeTrailingStars(val.toLowerCase());
                if (lowercaseVal.includes("*")) {
                    const pattern = new RegExp(common.escapeRegExp(lowercaseVal).split("*").join(".*"));
                    this.isHighlightedFunction = (testString) => {
                        return pattern.test(testString); // tests if there exist a substring that satisfies pattern
                    };
                } else {
                    this.isHighlightedFunction = (testString) => {
                        return testString.indexOf(lowercaseVal) !== -1;
                    };
                }
                if (this.currentlyShownNodes.length === 0) {
                    this._setHighlightOnMainStage();
                } else {
                    this._setHighlightOnZoomedStage();
                }
            }
        }));
    }

    /**
     * @param {String} s
     */
    static removeTrailingStars(s) {
        if (s[0] === "*") {
            s = s.substring(1, s.length);
        }
        if (s.length !== 0 && s[s.length - 1] === "*") {
            s = s.substring(0, s.length - 1);
        }
        return s;
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
            TreeDrawer._resetHighlightRecursively(children[i]);
        }
    }

    _setTextMask(text, shape, scaleX) {
        const newShape = shape.clone();
        const nodeWidth = scaleX * this.currentCanvasWidth;
        newShape.scaleX = (nodeWidth - 4) / this.currentCanvasWidth;
        text.mask = newShape;
    }

    /**
     * @param node
     * @param {Number} depth
     */
    _assignParentsAndDepthRecursively(node, depth) {
        const children = node.getNodesList();
        node.normalizedName = TreeDrawer._getNormalizedName(node);
        node.depth = depth;
        for (let i = 0; i < children.length; i++) {
            children[i].parent = node;
            this._assignParentsAndDepthRecursively(children[i], depth + 1);
        }
    }

    _expandParents(node) {
        let parent = node.parent;
        while (parent !== this.baseNode) {
            this._drawNode(parent, ZOOMED_PARENT_COLOR, 1, 0, true, this.zoomedStage, node.depth);
            parent = parent.parent;
        }
    }

    _changeZoom(node) {
        this.currentlyShownNodes = [];
        if (node !== this.baseNode) {
            this._setNodeZoomed(node);
        } else { // if reset zoom
            this._resetZoom();
        }
    }

    /**
     * @param node this node will be drawn
     * @param {Number} newFullScaleX
     * @param {Number} newOffsetX
     * @param {Boolean} isMostFirst
     * @param {createjs.Stage} stage
     * @param {Boolean} saveNodesToList
     * @param {number} zoomedNodeDepth
     * @protected
     * @return {Number} max depth
     */
    _drawNodesRecursively(node,
                          newFullScaleX,
                          newOffsetX,
                          isMostFirst,
                          stage,
                          saveNodesToList,
                          zoomedNodeDepth) {
        if (saveNodesToList) {
            this.currentlyShownNodes.push(node);
        }
        this._drawNode(
            node,
            node.originalColor,
            this._countScaleXForNode(node) / newFullScaleX,
            (this._countOffsetXForNode(node) - newOffsetX) / newFullScaleX,
            isMostFirst,
            stage,
            zoomedNodeDepth
        );
        const children = node.getNodesList();
        for (let i = 0; i < children.length; i++) {
            this._drawNodesRecursively(
                children[i],
                newFullScaleX,
                newOffsetX,
                i === 0 && isMostFirst,
                stage,
                saveNodesToList,
                zoomedNodeDepth
            );
        }
    }

    _addResetButton() {
        const shape = this._drawRectangle(this.baseNode, RESET_ZOOM_BUTTON_COLOR, 1, 0, true, this.zoomedStage);
        this.listenScale(this.baseNode, shape);
        this._drawLabel("Reset zoom", shape, 1, 0, 0, this.zoomedStage);
    }

    /**
     * @param node not a baseNode
     * @return {String}
     * @private
     */
    static _getLabelText(node) {
        return node.getNodeInfo().getMethodName();
    }

    _drawFullTree() {
        this.currentCanvasWidth = TreeDrawer._getCanvasWidth(this.$section.find(".original-canvas"));
        const children = this.baseNode.getNodesList();
        for (let i = 0; i < children.length; i++) {
            this._drawNodesRecursively(
                children[i],
                1,
                0,
                true,
                this.stage,
                false,
                0
            );
        }
        this.stage.update();
    }

    /**
     * @param node
     * @protected
     */
    _setPopupContent(node) {
        const className = node.getNodeInfo().getClassName();
        if (className !== "") {
            this.$popupClassIcon.show();
            this.$popupClassName.show();
            this.$popup.find(".class-name").text(className);
        } else {
            this.$popupClassIcon.hide();
            this.$popupClassName.hide();
        }
        this.$popup.find("h3").text(node.getNodeInfo().getMethodName());
        this.$popup.find(".outgoing-link").attr("href",
            serverNames.OUTGOING_CALLS + "?" + TreeDrawer.getGETParameters(node)
        );
        this.$popup.find(".incoming-link").attr("href",
            serverNames.INCOMING_CALLS + "?" + TreeDrawer.getGETParameters(node)
        );
        this._setParameters(node);
        this._setPopupReturnValue(node);

        this.$popup.find(".duration").text(node.getWidth() + this._getTimeUnitDisplay());

    }

    /**
     * Retrieve the time unit (frames, ms, etc) for this node
     * @returns {string}
     * @private
     */
    _getTimeUnit() {
        return "";
    }

    _getTimeUnitDisplay() {
        const timeUnits = this._getTimeUnit();
        if("" !== timeUnits) {
            return " " + timeUnits;
        }

        return "";
    }

    static getGETParameters(node) {
        return common.getParametersString({
            project: constants.projectName,
            file: constants.fileName,
            method: node.getNodeInfo().getMethodName(),
            class: node.getNodeInfo().getClassName(),
            desc: node.getNodeInfo().getDescription()
        });
    }

    /**
     * @param {String} fullDescription
     * @return {null|Array<String>}
     */
    static getParametersTypesList(fullDescription) {
        const parameters = fullDescription.substring(fullDescription.indexOf("(") + 1, fullDescription.lastIndexOf(")"));
        if (parameters === "") {
            return null;
        }
        return parameters.split(", ");
    }

    /**
     * @param node
     */
    _setParameters(node) {
        const parametersList = TreeDrawer.getParametersTypesList(node.getNodeInfo().getDescription());
        this.$popupParameters.find("*").remove();
        if (parametersList !== null) {
            for (let i = 0; i < parametersList.length; i++) {
                this.$popupParameters.append($("<p>" + parametersList[i] + "</p>"))
            }
            this.$popup.find();
            this.$popupIcon.show();
        } else {
            this.$popupIcon.hide();
        }
    }

    /**
     * @param node
     * @param {RegExp} pattern string in lowercase
     * @private
     */
    _updateHighlightRecursively(node, pattern) {
        if (this.isHighlightedFunction(node.normalizedName)) {
            TreeDrawer._setHighlight(node, true, true);
        } else {
            TreeDrawer._setHighlight(node, false, true);
        }
        const children = node.getNodesList();
        for (let i = 0; i < children.length; i++) {
            this._updateHighlightRecursively(children[i], pattern);
        }
    }

    /**
     * @param {Array} currentlyShownNodes
     * @private
     */
    _updateHighlight(currentlyShownNodes) {
        for (let i = 0; i < currentlyShownNodes.length; i++) {
            if (this.isHighlightedFunction(currentlyShownNodes[i].normalizedName)) {
                TreeDrawer._setHighlight(currentlyShownNodes[i], true, false);
            } else {
                TreeDrawer._setHighlight(currentlyShownNodes[i], false, false);
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

    static _resetHighlightList(currentlyShownNodes) {
        for (let i = 0; i < currentlyShownNodes.length; i++) {
            TreeDrawer._setHighlight(currentlyShownNodes[i], true, false);
        }
    }

    /**
     * @private
     */
    _resetHighlight() {
        if (this.currentlyShownNodes.length === 0) { // if not zoomed
            this.wasMainStageHighlighted = false;
            TreeDrawer._resetHighlightRecursively(this.baseNode);
            this.stage.update();
        } else { // if zoomed
            TreeDrawer._resetHighlightList(this.currentlyShownNodes);
            this.zoomedStage.update();
        }
    }

    /**
     * @private
     */
    _setHighlightOnMainStage() {
        this.wasMainStageHighlighted = true;
        this._updateHighlightRecursively(this.baseNode);
        this.stage.update();
    }

    /**
     * @private
     */
    _setHighlightOnZoomedStage() {
        this._updateHighlight(this.currentlyShownNodes);
        this.zoomedStage.update();
    }

    _setOriginalColorRecursively(node) {
        node.originalColor = this._getOriginalColor(node);
        const children = node.getNodesList();
        for (let i = 0; i < children.length; i++) {
            this._setOriginalColorRecursively(children[i]);
        }
    }

    _buildPackageListRecursively(node) {
        const children = node.getNodesList();
        for (let i = 0; i < children.length; i++) {
            const child = children[i];
            this._addPackage(TreeDrawer._getPackageName(child));
            this._buildPackageListRecursively(child);
        }
    }

    /**
     * @param {String} packageName
     * @private
     */
    _addPackage(packageName) {
        if (this.packageList[packageName] === undefined) {
            this.packageList[packageName] = 0;
        }
    }

    _setPackageColors() {
        const packages = Object.keys(this.packageList);
        packages.sort();
        const step = 1 / packages.length;
        if (packages.length === 1) {
            this.packageList[packages[0]] = 0.5;
            return;
        }
        for (let i = 0; i < packages.length; i++) {
            this.packageList[packages[i]] = i * step;
        }
    }

    /**
     * @param node
     * @return {String}
     */
    static _getPackageName(node) {
        if (node.getNodeInfo() === undefined) {
            return "";
        }
        const className = node.getNodeInfo().getClassName();
        const lastDot = className.lastIndexOf(".");
        if (lastDot !== -1) {
            return className.substring(0, lastDot);
        } else {
            return ""
        }
    }

    _setPopupReturnValue(node) {

    }

    _resetZoom() {
        this.zoomedNode = null;
        if (this.isHighlightedFunction !== null) {
            this._setHighlightOnMainStage();
        } else if (this.wasMainStageHighlighted) {
            this._resetHighlight();
        }
        $("#" + this.zoomedStage.id).removeClass("canvas-zoomed-show");
        $("#" + this.stage.id).removeClass("original-canvas-zoomed");
    }

    /**
     * @param tree
     * @param {number} initialDepth
     * @private
     */
    _prepareTree(tree, initialDepth) {
        this._assignParentsAndDepthRecursively(tree.getBaseNode(), initialDepth);
        this._setOriginalColorRecursively(tree.getBaseNode());
    }

    static _getCanvasWidth($canvas) {
        const ratio = window.devicePixelRatio || 1;
        return Number.parseInt($canvas.attr("width")) / ratio;
    }

    /**
     * Returns width that is available for canvas
     * (window width minus file menu and minus paddings)
     * @return {Number}
     */
    _getCanvasWidthForSection() {
        return window.innerWidth -
            TreeDrawer._getElementWidth(this.$fileMenu) -
            constants.CANVAS_PADDING * 2;
    }

    /**
     * @param $element
     * @return {number}
     */
    static _getElementWidth($element) {
        /**
         * @type {String}
         */
        const string = $element.css("width");
        return Number.parseInt(string.substring(0, string.length - 2));
    }

    _enableResizeZoomedCanvas() {
        constants.$arrowLeft.click(this._updateCanvasWidthDecorator());
        constants.$arrowRight.click(this._updateCanvasWidthDecorator());
        const $window = $(window);
        let windowWidth = $window.width();
        $window.resize(common.updateRareDecorator(1000, () => {
            if (windowWidth !== $window.width()) { // if width was changed
                this._updateCanvasWidthDecorator()();
                windowWidth = $window.width();
            }
        }));
    }

    /**
     * @return {Function}
     * @protected
     */
    _updateCanvasWidthDecorator() {
        const that = this;
        return () => {
            setTimeout(() => {
                that._createStages();
                if (that.zoomedNode !== null) {
                    this._changeZoom(that.zoomedNode);
                }
                that._drawFullTree();
            }, 300)
        }
    }

    _createStages() {
        this._doCreateStages();
        this.zoomedStage.addEventListener("drawend", common.hideLoader)
    }

    _doCreateStages() {
        this.$section.find(".canvas-wrapper canvas").remove();
        const canvasContent = templates.tree.getAccumulativeTreeCanvas(
            {
                isNodeZoomed: this.zoomedNode !== null,
                canvasWidth: this._getCanvasWidthForSection(),
                canvasHeight: this.canvasHeight
            }
        ).content;
        this.$section.find(".canvas-wrapper").prepend($(canvasContent));

        this.stage = common.createStage("canvas");
        this.zoomedStage = common.createStage("canvas-zoomed");
        this.stage.enableMouseOver(20);
        this.zoomedStage.enableMouseOver(20);
    }

    /**
     * @param node
     */
    static _getNormalizedName(node) {
        const nodeInfo = node.getNodeInfo();
        if (nodeInfo === undefined) {
            return "";
        }
        let className = nodeInfo.getClassName();
        return (className + "." + nodeInfo.getMethodName()).toLowerCase();
    }

    _getOriginalColor(node) {
        const coefficient = this.packageList[TreeDrawer._getPackageName(node)];
        const h = 195 + coefficient * 40;
        const l = 50 + 10 * coefficient;
        return `hsl(${h}, 94%, ${l}%)`
    }

    /**
     * @param {String} desc
     * @return {String}
     * @private
     */
    static _getReturnValue(desc) {
        const bracket = desc.lastIndexOf(")");
        if (bracket === -1) {
            return "";
        }
        return desc.substring(bracket + 1, desc.length)
    }

    /**
     * @param {String} desc
     * @private
     * @return {Array}
     */
    static _getParameters(desc) {
        const openBracket = desc.indexOf("(");
        const closeBracket = desc.lastIndexOf(")");
        if (openBracket !== -1 && closeBracket !== -1) {
            return desc.substring(openBracket + 1, closeBracket).split(", ");
        }
        return [];
    }

    _getRightCornerPos(offsetX) {
        //noinspection JSValidateTypes
        return offsetX + this.$popup.outerWidth();
    }

    /**
     * @param {number} offsetX
     * @return {number}
     */
    _shiftIfHidden(offsetX) {
        const rightCorner = this._getRightCornerPos(offsetX);
        //noinspection JSValidateTypes
        return rightCorner > this.$canvasWrapper.outerWidth() + constants.CANVAS_PADDING - 6 ?
            offsetX - (rightCorner - this.$canvasWrapper.outerWidth() - constants.CANVAS_PADDING + 6) :
            offsetX;
    }

    static _createHeader(className, methodName, desc, timePercent) {
        return $(templates.tree.methodHeader({
            className: className,
            methodName: methodName,
            returnValue: TreeDrawer._getReturnValue(desc),
            parameters: TreeDrawer._getParameters(desc),
            timePercent: timePercent
        }).content);
    }

    /**
     * @param node
     * @private
     */
    _setNodeZoomed(node) {
        this.zoomedNode = node;
        this.currentCanvasWidth = TreeDrawer._getCanvasWidth(this.$section.find(".canvas-zoomed"));
        common.showLoader(constants.loaderMessages.drawing, () => {
            this._doSetNodeZoomed(node);
        });
    }

    _doSetNodeZoomed(node) {
        this.zoomedStage.removeAllChildren();
        this._expandParents(node);
        this._drawNodesRecursively(
            node,
            this._countScaleXForNode(node),
            this._countOffsetXForNode(node),
            true,
            this.zoomedStage,
            true,
            node.depth
        );
        this._addResetButton();
        if (this.isHighlightedFunction !== null) {
            this._setHighlightOnZoomedStage();
        } else {
            this.zoomedStage.update();
        }
        $("#" + this.stage.id).addClass("original-canvas-zoomed");
        $("#" + this.zoomedStage.id).addClass("canvas-zoomed-show");
    }
};