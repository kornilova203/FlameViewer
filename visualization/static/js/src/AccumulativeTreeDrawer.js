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
        this.shapeAndTextList.push({shape: shape, text: text});
    }

    _drawRectangle(node, depth, colorId) {
        const shape = new createjs.Shape();
        const fillCommand = shape.graphics.beginFill(COLORS[colorId]).command;
        this.searchList.push(new SearchElem(node.getNodeInfo().getMethodName(), fillCommand));
        shape.graphics.drawRect(0, 0, this.canvasWidth, LAYER_HEIGHT);
        const offsetX = this._getOffsetXForNode(node);
        const offsetY = this.flipY(AccumulativeTreeDrawer._calcNormaOffsetY(depth));
        const scaleX = node.getWidth() / this.width;
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
        for (let i in this.shapeAndTextList) {
            this.shapeAndTextList[i].shape.addEventListener("click", () => {
                console.log("clicked");
            })
        }
    }
}

class SearchElem {
    constructor(name, fillCommand) {
        this.name = name;
        this.fillCommand = fillCommand;
        this.originalColor = fillCommand.style;
    }

    matches(val) {
        return this.name.startsWith(val);
    }

    dim() {
        this.fillCommand.style = "#ccc";
    }

    reset() {
        console.log("reset to " + this.originalColor);
        this.fillCommand.style = this.originalColor;
    }
}