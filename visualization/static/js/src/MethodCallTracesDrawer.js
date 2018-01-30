class MethodCallTracesDrawer extends CallTracesDrawer {
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
        const $header = TreeDrawer._createHeader(className, methodName, desc, timePercent);
        $header.insertAfter(this.$section.find(".canvas-wrapper"));
        $header.addClass("header-call-traces");
    }

    /**
     * @param {Array<number>} pathToNode
     * @return {string}
     * @override
     */
    getTreeGETParameters(pathToNode) {
        return common.getParametersString({
            project: constants.projectName,
            file: constants.fileName,
            method: this.method,
            class: this.class,
            desc: this.desc,
            path: pathToNode
        });
    }
}