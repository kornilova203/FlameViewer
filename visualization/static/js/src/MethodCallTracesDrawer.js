const CallTracesDrawer = require('./CallTracesDrawer');

module.exports.MethodCallTracesDrawer = class MethodCallTracesDrawer extends CallTracesDrawer.CallTracesDrawer {
    constructor(tree, className, methodName, desc, timePercent) {
        super(tree);
        this.class = className;
        this.method = methodName;
        this.desc = desc;
        this._setHeader(className, methodName, desc, timePercent);
    }

    /**
     * @param {String} className
     * @param {String} methodName
     * @param {String} desc
     * @param {Number} timePercent
     */
    _setHeader(className, methodName, desc, timePercent) {
        const $header = MethodCallTracesDrawer._createHeader(className, methodName, desc, timePercent);
        $header.insertAfter(this.$section.find(".canvas-wrapper"));
        $header.addClass("header-call-traces");
    }

    getTreeGETParameters(pathToNode) {
        return methodFunctions.getTreeGETParameters.call(this, pathToNode);
    }
};