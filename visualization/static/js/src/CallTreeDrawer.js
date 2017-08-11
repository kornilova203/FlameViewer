const PIX_IN_MS = 0.5;

/**
 * @param {{css: Function}} $element
 * @param {String} property
 * @param {Number} startOffset
 */
function scrollHorizontally($element, property, startOffset) {
    const $window = $(window);
    $window.scroll(() => {
        $element.css(property, $window.scrollLeft() + startOffset);
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
                canvasHeight: this.canvasHeight,
                canvasWidth: this.canvasWidth,
                canvasOffset: this.canvasOffset
            }
        ).content;
        const $section = $(sectionContent);
        scrollHorizontally($section.find(".call-tree-header"), "padding-left", 20);
        if (this.id === 0) {
            CallTreeDrawer.removeSeparateLine($section);
        }
        return $section.appendTo($("main"));
    };

    static removeSeparateLine($section) {
        $section.find('h2').css("border", "none");
    }

    _createPopup() {
        const popupContent = templates.tree.accumulativeTreePopup().content;
        this.popup = $(popupContent).appendTo($("main"));
    }
}
