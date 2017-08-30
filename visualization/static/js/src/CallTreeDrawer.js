const PIX_IN_MS = 0.5;
const EXCEPTION_COLOR = "#ff1533";

class CallTreeDrawer extends AccumulativeTreeDrawer {
    /**
     * @param tree
     * @param {Number} id
     */
    constructor(tree, id) {
        super(tree);
        this.canvasWidth = Math.ceil(this.treeWidth * PIX_IN_MS);
        this.threadName = this.tree.getTreeInfo().getThreadName();
        this.canvasOffset = 0;
        this.$callTreeWrapper = $(".call-tree-wrapper");
        this.zoomedCanvasMargin = 0;
        this.$zoomedCanvas = null;
        this.$treePreviewWrapper = $(".tree-preview-wrapper");
        this.id = id;
        this.enableZoom = true;
        this._countNodesRecursively(this.baseNode);
    }

    //noinspection JSUnusedGlobalSymbols
    /**
     * @param node
     * @private
     * @override
     */
    _setPopupContent(node) {
        super._setPopupContent(node);
        this.$popup.find(".duration").text("duration: " + node.getWidth() + " ms");
    }

    /**
     * @override
     * @param node
     */
    _setPopupTable(node) {
        const parametersList = AccumulativeTreeDrawer.getParametersTypesList(node.getNodeInfo().getDescription());
        this.$popupTable.find(".parameter-tr").remove();

        if (parametersList !== null) {
            for (let i = 0; i < parametersList.length; i++) {
                this.$popupTable.append($(`<tr class='parameter-tr'><td><p>${parametersList[i]}</p></td><td><p>${CallTreeDrawer.getValueForParameter(i + 1, node.getNodeInfo().getParametersList())}</p></td></tr>`))
            }
        }
    }

    /**
     * @param {Number} index of parameter (0 is `this`)
     * @param {Array} parameters
     */
    static getValueForParameter(index, parameters) {
        for (let i = 0; i < parameters.length; i++) {
            if (parameters[i].getIndex() === index) {
                return CallTreeDrawer._getParameterVal(parameters[i])
            }
        }
        return "-";
    }


    //noinspection JSUnusedGlobalSymbols
    /**
     * @param {Number} offsetX
     * @param depth
     * @override
     */
    _setPopupPosition(offsetX, depth) {
        const callTreeWrapper = $(".call-tree-wrapper");
        offsetX += this.zoomedCanvasMargin;
        //noinspection JSValidateTypes
        if (offsetX < callTreeWrapper.scrollLeft()) {
            //noinspection JSValidateTypes
            offsetX = callTreeWrapper.scrollLeft();
        } else {
            offsetX += 30;
        }
        super._setPopupPosition(offsetX, depth);
    }

    draw() {
        this._prepareDraw();

        this.$section = this._createSection();
        this.stage = new createjs.Stage("canvas-" + this.id);
        this.stage.id = "canvas-" + this.id;
        this.stage.enableMouseOver(20);

        this._updateCanvasWidthDecorator()();

        this._createPopup();

        const childNodes = this.baseNode.getNodesList();
        if (childNodes.length === 0) {
            return;
        }
        this._drawFullTree();
        this._enableResizeZoomedCanvas();
    };

    _createSection() {
        const sectionContent = templates.tree.getCallTreeSection(
            {
                id: this.id,
                threadName: this.threadName,
                nodesCount: this.nodesCount,
                canvasHeight: this.canvasHeight,
                canvasWidth: this.canvasWidth,
                canvasOffset: this.canvasOffset
            }
        ).content;
        const $section = $(sectionContent);
        // scrollHorizontally($section.find(".call-tree-header"), "padding-left");
        if (this.id === 0) {
            $section.find("h2").css("border", "none");
        }
        return $section.appendTo($(".call-tree-wrapper"));
    };

    _createPopup() {
        const popupContent = templates.tree.callTreePopup().content;
        this.$popup = $(popupContent).appendTo(this.$section);
        this.$popupTable = this.$popup.find("table");
    }

    _setPopupReturnValue(node) {
        const returnValueType = this.$popup.find(".return-value-type");
        const returnValue = this.$popup.find(".return-value");
        returnValueType.text("");
        returnValue.text("");
        switch (node.getNodeInfo().getResultCase()) {
            case 5: // return value
                returnValueType.text("Return value:");
                let value = CallTreeDrawer._getType(node.getNodeInfo().getReturnValue());
                if (value !== undefined) {
                    returnValue.text(value);
                }
                break;
            case 6: // exception
                returnValueType.text("Exception:");
                returnValue.text(node.getNodeInfo().getException().getType() + ": " + node.getNodeInfo().getException().getValue())
        }
    }

    static _getType(returnValue) {
        switch (returnValue.getValueCase()) {
            case 1: // int
                return "int: " + returnValue.getI();
            case 2: // long
                return "long: " + returnValue.getJ();
            case 3: // boolean
                return "boolean: " + returnValue.getZ();
            case 4: // char
                return "char: " + returnValue.getC();
            case 5: // short
                return "short: " + returnValue.getS();
            case 6: // byte
                return "byte: " + returnValue.getB();
            case 7: // float
                return "float: " + returnValue.getF();
            case 8: // double
                return "double: " + returnValue.getD();
            case 9: // object
                return returnValue.getObject().getType() + ": " + returnValue.getObject().getValue();
        }
    }

    _resetZoom() {
        this.zoomedNode = null;
        super._resetZoom();
        this.zoomedCanvasMargin = 0;
        this.$callTreeWrapper.removeClass("call-tree-wrapper-zoomed");
    }

    _setNodeZoomed(node) {
        this.zoomedNode = node;
        //noinspection JSValidateTypes
        this.zoomedCanvasMargin = this.$callTreeWrapper.scrollLeft();
        this.$zoomedCanvas.css("margin-left", this.zoomedCanvasMargin);
        super._setNodeZoomed(node);
        this.$callTreeWrapper.addClass("call-tree-wrapper-zoomed");
    }

    /**
     * @return {Function}
     * @protected
     */
    _updateCanvasWidthDecorator() {
        const that = this;
        return () => {
            setTimeout(() => {
                that.$section.find(".canvas-zoomed").remove();

                that._createZoomedCanvas();

                that.zoomedStage = new createjs.Stage("canvas-zoomed-" + this.id);
                that.$zoomedCanvas = $("#canvas-zoomed-" + this.id);
                that.zoomedStage.id = "canvas-zoomed-" + this.id;
                that.zoomedStage.enableMouseOver(20);

                if (that.zoomedNode !== null) {
                    this._changeZoom(that.zoomedNode);
                }
            }, 300)
        }
    }

    _createZoomedCanvas() {
        const zoomedCanvasContent = templates.tree.zoomedCanvas(
            {
                id: this.id,
                canvasHeight: this.canvasHeight,
                canvasWidth: this._getCanvasWidthForSection()
            }
        ).content;

        $(zoomedCanvasContent).insertAfter(this.$section.find(".original-canvas"));
    }

    /**
     * @return {Number}
     * @private
     */
    _getCanvasWidthForSection() {
        return window.innerWidth -
            AccumulativeTreeDrawer._getElementWidth(this.$fileMenu) -
            AccumulativeTreeDrawer._getElementWidth(this.$treePreviewWrapper) -
            70;
    }

    /**
     * @param node
     * @override
     */
    _getNormalizedName(node) {
        const nodeInfo = node.getNodeInfo();
        if (nodeInfo === undefined) {
            return "";
        }
        let className = nodeInfo.getClassName();
        return (className + "." + nodeInfo.getMethodName()).toLowerCase() +
            CallTreeDrawer._getParametersForNormalizedName(node);
    }

    /**
     *
     * @param node
     * @private
     * @return {String}
     */
    static _getParametersForNormalizedName(node) {
        const parameters = node.getNodeInfo().getParametersList();
        let parametersValues = [];
        for (let i = 0; i < parameters.length; i++) {
            parametersValues.push(this._getParameterVal(parameters[i]))
        }
        return ("(" + parametersValues.join(", ") + ")").toLowerCase();
    }

    static _getParameterVal(parameter) {
        switch (parameter.getVar().getValueCase()) {
            case 1: // int
                return parameter.getVar().getI();
            case 2: // long
                return parameter.getVar().getJ();
            case 3: // boolean
                return parameter.getVar().getZ();
            case 4: // char
                return parameter.getVar().getC();
            case 5: // short
                return parameter.getVar().getS();
            case 6: // byte
                return parameter.getVar().getB();
            case 7: // float
                return parameter.getVar().getF();
            case 8: // double
                return parameter.getVar().getD();
            case 9: // object
                return parameter.getVar().getObject().getValue();
            default:
                return "-";
        }
    }

    _getOriginalColor(node) {
        if (node.getNodeInfo() !== undefined && node.getNodeInfo().getResultCase() === 6) { // exception
            return EXCEPTION_COLOR;
        }
        return super._getOriginalColor(node);
    }
}
