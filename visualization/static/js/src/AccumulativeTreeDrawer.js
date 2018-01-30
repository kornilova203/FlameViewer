const deserializer = require('./deserializer');
const TreeDrawer = require('./TreeDrawer');

/**
 * @abstract
 */
module.exports.AccumulativeTreeDrawer = class AccumulativeTreeDrawer extends TreeDrawer.TreeDrawer {
    constructor(tree) {
        super(tree);
    }
    /**
     * @param node
     * @override
     */
    _setNodeZoomed(node) {
        this.zoomedNode = node;
        const pathToNode = AccumulativeTreeDrawer._getPathToNode(node);
        const treeRequest = AccumulativeTreeDrawer._createTreeRequest(pathToNode);
        treeRequest.onload = () => {
            const arrayBuffer = treeRequest.response;
            const byteArray = new Uint8Array(arrayBuffer);
            // noinspection JSUnresolvedFunction
            const zoomedTree = deserializer.deserializeTree(byteArray);
            this._prepareTree(zoomedTree);
            this.currentCanvasWidth = AccumulativeTreeDrawer._getCanvasWidth(this.$section.find(".canvas-zoomed"));
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
        };
        treeRequest.send();
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
        return request;
    }

    /**
     * @param {Array<number>} pathToNode
     * @return {string}
     */
    static getTreeGETParameters(pathToNode) {
        return common.getParametersString({
            project: constants.projectName,
            file: constants.fileName
        });
    }
};