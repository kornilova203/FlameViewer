/**
 * Call this methods using .call
 */
const methodFunctions = {
    /**
     * @param {Array<number>} pathToNode
     * @return {string}
     */
    getTreeGETParameters: function (pathToNode) {
        return common.getParametersString({
            file: constants.fileName,
            method: this.method,
            class: this.class,
            desc: this.desc,
            path: pathToNode
        });
    }
};
