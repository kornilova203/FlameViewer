class MethodBackTracesDrawer extends BackTracesDrawer {
    constructor(tree, className, methodName, desc, timePercent) {
        super(tree);
        this.class = className;
        this.method = methodName;
        this.desc = desc;
        this._setHeader(className, methodName, desc, timePercent);
    }

    _setHeader(className, methodName, desc, timePercent) {
        const $header = TreeDrawer._createHeader(className, methodName, desc, timePercent);
        $header.insertBefore(this.$section.find(".canvas-wrapper"));
    }
}