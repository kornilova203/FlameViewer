const deserializer = require('./deserializer');
const TreeDrawer = require('./TreeDrawer');

/**
 * @abstract
 */
module.exports.AccumulativeTreeDrawer = class AccumulativeTreeDrawer extends TreeDrawer.TreeDrawer {
    constructor(tree) {
        super(tree);
        this.isFullyVisible = this.tree.getVisibleDepth() === 0;
        this.visibleDepth = this.tree.getVisibleDepth();
        this.$fog = $(".fog");
        this.initialCanvasShift = this.canvasHeight; // see _moveSectionUp method
        if (!this.isFullyVisible) { // if some nodes are hidden
            this._assignChildIndexRecursively(tree.getBaseNode());
        }
    }

    draw() {
        super.draw();
        if (!this.isFullyVisible) {
            this._toggleFog(this.tree.getVisibleDepth(), this.tree.getDepth());
        }
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
     * @override
     * @protected
     */
    _prepareDraw() {
        super._prepareDraw();
        if (this.visibleDepth !== 0) { // if only part of tree displayed
            this.initialCanvasShift = this._moveSectionUp(this.tree.getVisibleDepth());
        }
    }

    /**
     * This method shifts canvas so all available nodes are visible
     * and there are no extra space above tree.
     * New shift must not be bigger than this.initialCanvasShift.
     * @param {number} visibleLayersCount
     * @abstract
     * @return {number} value of 'bottom' css attribute of canvas
     */
    _moveSectionUp(visibleLayersCount) {

    }

    /**
     * @param node
     * @override
     */
    _doSetNodeZoomed(node) {
        if (this.isFullyVisible) {
            super._doSetNodeZoomed(node);
        } else {
            const pathToNode = AccumulativeTreeDrawer._getPathToNode(node);
            const treeRequest = this._createTreeRequest(pathToNode);
            treeRequest.onload = () => {
                const arrayBuffer = treeRequest.response;
                const byteArray = new Uint8Array(arrayBuffer);
                const zoomedTree = deserializer.deserializeTree(byteArray);
                const zoomedNode = zoomedTree.getBaseNode().getNodesList()[0];
                this._buildPackageListRecursively(zoomedNode);
                this._setPackageColors();
                this._prepareTree(zoomedTree, node.depth - 1);
                this._assignChildIndexRecursively(zoomedTree.getBaseNode());
                this._moveSectionUp(zoomedTree.getVisibleDepth() + node.depth - 1);
                this._toggleFog(zoomedTree.getVisibleDepth(), zoomedTree.getDepth());
                zoomedNode.index = node.index;
                zoomedNode.parent = node.parent;
                super._doSetNodeZoomed(zoomedNode);
            };
            treeRequest.send();
        }
    }

    /**
     * Hide fog if top nodes does not have children
     * @param visibleLayersCount
     * @param layersCount
     * @private
     */
    _toggleFog(visibleLayersCount, layersCount) {
        if (visibleLayersCount === layersCount) {
            this.$fog.css("opacity", 0)
        } else {
            this.$fog.css("opacity", 1)
        }
    }

    /**
     * @override
     */
    _resetZoom() {
        super._resetZoom();
        if (this.visibleDepth !== 0) { // if only part of tree displayed
            this._moveSectionUp(this.tree.getVisibleDepth());
            this._toggleFog(this.tree.getVisibleDepth(), this.tree.getDepth());
        }
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
            if (node.index !== undefined) { // index of base node is undefined
                reversedPath.push(node.index);
            }
            node = node.parent;
        }
        return AccumulativeTreeDrawer._reverseList(reversedPath);
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
     * @abstract
     * @return {string}
     */
    _getTreeType() {

    }

    /**
     * @param {Array<number>} pathToNode
     * @return {XMLHttpRequest}
     * @private
     */
    _createTreeRequest(pathToNode) {
        const request = new XMLHttpRequest();
        request.open("GET", `${serverNames.MAIN_NAME}/trees/${this._getTreeType()}?` + this.getTreeGETParameters(pathToNode));
        request.responseType = "arraybuffer";
        return request;
    }

    /**
     * @param {Array<number>} pathToNode
     * @return {string}
     */
    getTreeGETParameters(pathToNode) {
        return common.getParametersString({
            project: constants.projectName,
            file: constants.fileName,
            path: pathToNode
        });
    }
};