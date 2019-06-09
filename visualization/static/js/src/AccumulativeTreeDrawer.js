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
        this.initialCanvasShift = this.canvasHeight; // see _moveSectionUp method
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
            return;
        }
        const pathToNode = AccumulativeTreeDrawer._getPathToNode(node, this.tree.getBaseNode());
        const url = `${serverNames.MAIN_NAME}/trees/${this._getTreeType()}?` + this.getTreeGETParameters(pathToNode);
        common.sendGetRequest(url, "arraybuffer")
            .then(response => {
                const byteArray = new Uint8Array(response);
                const zoomedTree = deserializer.deserializeTree(byteArray);
                const zoomedNode = zoomedTree.getBaseNode().getNodesList()[0];
                this._buildPackageListRecursively(zoomedNode);
                this._setPackageColors();
                this._prepareTree(zoomedTree, node.depth - 1);
                const subTreeDepth = zoomedTree.getVisibleDepth() === 0 ? zoomedTree.getDepth() : zoomedTree.getVisibleDepth();
                this._moveSectionUp(subTreeDepth + node.depth - 1);
                zoomedNode.parent = node.parent;
                super._doSetNodeZoomed(zoomedNode);
            });
    }

    /**
     * @override
     */
    _resetZoom() {
        super._resetZoom();
        if (this.visibleDepth !== 0) { // if only part of tree displayed
            this._moveSectionUp(this.tree.getVisibleDepth());
        }
    }

    /**
     * @param node
     * @return {Array<number>} path to node.
     * Each element of path is an index of child
     * @private
     */
    static _getPathToNode(node, baseNode) {
        const reversedPath = [];
        while (node !== baseNode) {
            reversedPath.push(node.getIndex());
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

    // noinspection JSMethodCanBeStatic
    /**
     * @param {Array<number>} pathToNode
     * @return {string}
     */
    getTreeGETParameters(pathToNode) {
        return common.getParametersString({
            file: constants.fileName,
            include: CURRENT_INCLUDED,
            path: pathToNode
        });
    }
};