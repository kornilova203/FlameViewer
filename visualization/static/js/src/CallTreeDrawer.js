const PIX_IN_MS = 0.5;

/**
 * @param {{css: Function}} $element
 * @param {String} property
 */
function scrollHorizontally($element, property) {
    const $main = $("main");
    $main.scroll(() => {
        console.log("main scroll");
        //noinspection JSValidateTypes
        $element.css(property, $main.scrollLeft());
    });
}

class CallTreeDrawer extends AccumulativeTreeDrawer {
    /**
     * @param tree
     * @param {Number} id
     */
    constructor(tree, id) {
        super(tree);
        this.canvasWidth = Math.ceil(this.treeWidth * PIX_IN_MS);
        this.threadName = this.tree.getTreeInfo().getThreadName();
        this.canvasOffset = this.tree.getTreeInfo().getStartTime() * PIX_IN_MS;
        this.id = id;
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
        this.$popup.find(".duration").text("duration: " + node.getWidth() + " ms")
    }

    //noinspection JSUnusedGlobalSymbols
    /**
     * @param {Number} offsetX
     * @param depth
     * @override
     */
    _setPopupPosition(offsetX, depth) {
        if (offsetX < $main.scrollLeft()) {
            offsetX = $main.scrollLeft();
        } else {
            offsetX += 20;
        }
        super._setPopupPosition(offsetX, depth);
    }

    draw() {
        this._prepareDraw();

        console.log("draw");
        this.section = this._createSection();
        this.stage = new createjs.Stage("canvas-" + this.id);
        this.stage.id = "canvas-" + this.id;
        this.stage.enableMouseOver(20);

        this.zoomedStage = new createjs.Stage("canvas-zoomed-" + this.id);
        this.zoomedStage.id = "canvas-zoomed-" + this.id;
        this.stage.enableMouseOver(20);

        this._createPopup();

        const childNodes = this.baseNode.getNodesList();
        if (childNodes.length === 0) {
            return;
        }
        const maxDepth = this._drawFullTree();

        this.stage.update();
        this._moveCanvas(maxDepth);
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
        scrollHorizontally($section.find(".call-tree-header"), "padding-left");
        if (this.id === 0) {
            $section.css("border", "none");
        }
        return $section.appendTo($("main"));
    };

    _createPopup() {
        const popupContent = templates.tree.callTreePopup().content;
        this.$popup = $(popupContent).appendTo($("main"));
    }
}
