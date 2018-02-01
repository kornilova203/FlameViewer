const deserializer = require('./deserializer');
const TreeDrawer = require('./TreeDrawer');

/**
 * @abstract
 */
module.exports.AccumulativeTreeDrawer = class AccumulativeTreeDrawer extends TreeDrawer.TreeDrawer {
    constructor(tree) {
        super(tree);
        this.isFullTree = this.tree.getVisibleDepth() === 0;
        this.visibleDepth = this.tree.getVisibleDepth();
        if (!this.isFullTree) {
            this._assignChildIndexRecursively(tree.getBaseNode());
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
            this._moveSectionUp();
        }
    }

    /**
     * @abstract
     */
    _moveSectionUp() {

    }

    /**
     * @param node
     * @override
     */
    _doSetNodeZoomed(node) {
        if (this.isFullTree) {
            super._doSetNodeZoomed(node);
        } else {
            const pathToNode = AccumulativeTreeDrawer._getPathToNode(node);
            const treeRequest = AccumulativeTreeDrawer._createTreeRequest(pathToNode);
            treeRequest.onload = () => {
                const arrayBuffer = treeRequest.response;
                const byteArray = new Uint8Array(arrayBuffer);
                const zoomedTree = deserializer.deserializeTree(byteArray);
                const zoomedNode = zoomedTree.getBaseNode().getNodesList()[0];
                this._buildPackageListRecursively(zoomedNode);
                this._setPackageColors();
                this._prepareTree(zoomedTree, node.depth - 1);
                this._assignChildIndexRecursively(zoomedTree.getBaseNode());
                zoomedNode.index = node.index;
                zoomedNode.parent = node.parent;
                super._doSetNodeZoomed(zoomedNode);
            };
            treeRequest.send();
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
     * @param {Array<number>} pathToNode
     * @return {XMLHttpRequest}
     * @private
     */
    static _createTreeRequest(pathToNode) {
        const request = new XMLHttpRequest();
        request.open("GET", `/flamegraph-profiler/trees/outgoing-calls?` + AccumulativeTreeDrawer.getTreeGETParameters(pathToNode));
        request.responseType = "arraybuffer";
        return request;
    }

    /**
     * @param {Array<number>} pathToNode
     * @return {string}
     */
    static getTreeGETParameters(pathToNode) {
        return common.getParametersString({
            project: constants.projectName,
            file: constants.fileName,
            path: pathToNode
        });
    }
};