const BackTracesDrawer = require('./BackTracesDrawer');

module.exports.MethodBackTracesDrawer = class MethodBackTracesDrawer extends BackTracesDrawer.BackTracesDrawer {
    constructor(tree, className, methodName, desc, timePercent) {
        super(tree);
        this.class = className;
        this.method = methodName;
        this.desc = desc;
        this._setHeader(className, methodName, desc, timePercent);
    }

    _setHeader(className, methodName, desc, timePercent) {
        const $header = MethodBackTracesDrawer._createHeader(className, methodName, desc, timePercent);
        $header.insertBefore(this.$section.find(".canvas-wrapper"));
    }

    /**
     * @override
     */
    getTreeGETParameters(pathToNode) {
        return methodFunctions.getTreeGETParameters.call(this, pathToNode);
    }
};