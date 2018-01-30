class CallTracesDrawer extends TreeDrawer {
    constructor(tree) {
        super(tree);
        const visibleDepth = tree.getVisibleDepth();
        if (visibleDepth !== 0) { // if only part of tree displayed
            this.moveSectionUp();
        }
        this._assignChildIndexRecursively(tree.getBaseNode());
    }

    moveSectionUp() {
        const invisibleLayersCount = this.tree.getDepth() - this.tree.getVisibleDepth();
        this.$section.css("bottom", invisibleLayersCount * (LAYER_HEIGHT + LAYER_GAP) + "px");
    }

    /**
     * Indices are needed to get hidden nodes when node is zoomed.
     * @param node
     */
    _assignChildIndexRecursively(node) {
        const children = node.getNodesList();
        for (let i = 0; i < children.length; i++) {
            children[i].index = i;
            this._assignChildIndexRecursively(children[i]);
        }
    }

    /**
     * @param node
     * @override
     */
    _setNodeZoomed(node) {
        this.zoomedNode = node;
        const pathToNode = CallTracesDrawer._getPathToNode(node);
        const treeRequest = this._createTreeRequest(pathToNode);
        this.currentCanvasWidth = TreeDrawer._getCanvasWidth(this.$section.find(".canvas-zoomed"));
        common.showLoader(constants.loaderMessages.drawing, () => {
            this.zoomedStage.removeAllChildren();
            super._expandParents(node);
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
            common.hideLoader()
        });
    }

    /**
     * @param {Array<number>} pathToNode
     * @return {XMLHttpRequest}
     * @private
     */
    _createTreeRequest(pathToNode) {
        const request = new XMLHttpRequest();
        request.open("GET", `/flamegraph-profiler/trees/outgoing-calls?` + this.getTreeGETParameters(pathToNode));
        return request;
    }

    /**
     * @param {Array<number>} pathToNode
     * @return {string}
     */
    getTreeGETParameters(pathToNode) {
        return common.getParametersString({
            project: constants.projectName,
            file: constants.fileName
        });
    }

    /**
     * @param node
     * @return {Array<number>} path to node.
     * Each element of path is an index of child
     * @private
     */
    static _getPathToNode(node) {
        const reversedPath = [];
        while (node !== undefined) {
            reversedPath.push(node.index);
            node = node.parent;
        }
        return CallTracesDrawer._reverseList(reversedPath);
    }

    /**
     * @param {Array} list
     * @return {Array} reversed list
     */
    static _reverseList(list) {
        const reversedPath = [];
        for (let i = list.length - 1; i >= 0; i--) {
            reversedPath.push(list[i]);
        }
        return reversedPath;
    }

    /**
     * @override
     */
    draw() {
        super.draw();
        const $main = $("main");
        // noinspection JSValidateTypes
        $main.scrollTop($main[0].scrollHeight);
    }
}