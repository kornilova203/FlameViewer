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
        if (this.canvasWidth > 32000) {
            this.canvasWidth = 32000;
        }
        this.threadName = this.tree.getTreeInfo().getThreadName();
        this.canvasOffset = 0;
        this.$callTreeWrapper = $(".call-tree-wrapper");
        this.$savedValue = null;
        this.zoomedCanvasMargin = 0;
        this.availableWidth = 0; // for popup position
        this.$zoomedCanvas = null;
        this.id = id;
        this.enableZoom = true;
    }

    //noinspection JSUnusedGlobalSymbols
    /**
     * @param node
     * @private
     * @override
     */
    _setPopupContent(node) {
        super._setPopupContent(node);
        this.$popup.find(".duration").text(node.getWidth() + " ms");
    }

    /**
     * @override
     * @param node
     */
    _setParameters(node) {
        this.$popupParameters.find("*").remove();
        this.$savedValue.text("");
        const parametersList = AccumulativeTreeDrawer.getParametersTypesList(node.getNodeInfo().getDescription());

        if (parametersList !== null) {
            for (let i = 0; i < parametersList.length; i++) {
                const value = CallTreeDrawer.getValueForParameter(i + 1, node.getNodeInfo().getParametersList());
                const $parameter = $(`<p>${parametersList[i]}<span class="parameter-value">${value}</span></p>`);
                if (value.length + parametersList[i].length > 60) {
                    $parameter.hover(() => {
                        this.$savedValue.text(value);
                    });
                }
                this.$popupParameters.append($parameter);
            }
            this.$popupIcon.show();
        } else {
            this.$popupIcon.hide();
        }
    }

    /**
     * @param {Number} index of parameter (0 is `this`)
     * @param {Array} parameters
     */
    static getValueForParameter(index, parameters) {
        for (let i = 0; i < parameters.length; i++) {
            if (parameters[i].getIndex() === index) {
                return " = " + CallTreeDrawer._getParameterVal(parameters[i])
            }
        }
        return "";
    }

    draw() {
        this._prepareDraw();

        this.$section = this._createSection();
        this.availableWidth = Math.max(this.canvasWidth, CallTreeDrawer._getElementWidth(this.$section));
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
        this.$popupParameters = this.$popup.find(".parameters");
        this.$popupIcon = this.$popup.find(".parameter-icon");
        this.$returnValueType = this.$popup.find(".return-value-type");
        this.$returnValue = this.$popup.find(".return-value");
        this.$savedValue = this.$popup.find(".saved-value");
    }

    _setPopupReturnValue(node) {
        this.$returnValueType.show();
        this.$returnValue.show();
        this.$returnValue.off();
        let value;
        switch (node.getNodeInfo().getResultCase()) {
            case 5: // return value
                value = CallTreeDrawer._getType(node.getNodeInfo().getReturnValue());
                if (value !== undefined) {
                    this.$returnValueType.text("Return value:");
                } else {
                    this.$returnValueType.hide();
                    this.$returnValue.hide();
                    return;
                }
                break;
            case 6: // exception
                this.$returnValueType.text("Exception:");
                const message = node.getNodeInfo().getException().getValue();
                value = node.getNodeInfo().getException().getType() + (message ? ": " + message : "");
                value = CallTreeDrawer.removeWordClass(value);
        }
        if (value !== undefined) {
            this.$returnValue.text(value);
            if (value.length > 60) {
                this.$returnValue.hover(() => {
                    this.$savedValue.text(value);
                });
            }
        }
    }

    /**
     * class com.intellij.psi.text.BlockSupport$ReparsedSuccessfullyException ->
     * -> com.intellij.psi.text.BlockSupport$ReparsedSuccessfullyException:
     */
    static removeWordClass(value) {
        const classIndex = value.indexOf("class");
        if (classIndex === 0) {
            return value.substring("class".length, value.length);
        }
        return value;
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

    /**
     * @param {number} offsetX
     * @return {number}
     * @override
     */
    _shiftIfHidden(offsetX) {
        offsetX += this.zoomedCanvasMargin;
        //noinspection JSValidateTypes
        const leftCornerOffset = this.$callTreeWrapper.scrollLeft();
        if (offsetX < leftCornerOffset) { // if left corner is hidden
            //noinspection JSValidateTypes
            return leftCornerOffset;
        } else {
            offsetX += CANVAS_PADDING;
            const rightCorner = super._getRightCornerPos(offsetX);
            if (this.zoomedNode !== null) { // if zoomed
                const canvasRightCorner = leftCornerOffset + this._getCanvasWidthForSection() + CANVAS_PADDING * 2;
                return rightCorner > canvasRightCorner ? // if right corner is hidden
                    offsetX - (rightCorner - canvasRightCorner) :
                    offsetX;
            } else {
                return rightCorner > this.availableWidth + CANVAS_PADDING * 2 ? // if right corner is hidden
                    offsetX - (rightCorner - this.availableWidth - CANVAS_PADDING * 2) :
                    offsetX;
            }
        }
    }
}
