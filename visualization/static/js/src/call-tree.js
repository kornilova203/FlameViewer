const TreesProto = require('../generated/trees_pb');

$(window).on("load", function () {
    if (constants.fileName !== undefined) {
        const extension = getExtension(constants.fileName);
        if (extension !== "jfr") {
            console.log("not jfr");
            getAndDrawTrees();
        } else {
            common.showMessage("This type of tree is unavailable for .jfr files")
        }
    }
});

function drawTrees(trees) {
    common.showLoader(constants.loaderMessages.drawing, () => {
        for (let i = 0; i < trees.length; i++) {
            const drawer = new CallTreeDrawer(trees[i], i);
            drawer.draw();
        }
        common.hideLoader(0);
    });
}

/**
 * Get extension of file
 * @param {string} fileName
 * @returns {string}
 */
function getExtension(fileName) {
    return fileName.substring(fileName.indexOf(".") + 1, fileName.length);
}

function getAndDrawTrees() {
    common.showLoader(constants.loaderMessages.buildingTrees, () => {
        const request = new XMLHttpRequest();
        const parameters = window.location.href.split("?")[1];
        request.open("GET", "/flamegraph-profiler/trees/call-tree?" + parameters,
            true);
        request.responseType = "arraybuffer";

        request.onload = function () {
            common.hideLoader(0);
            common.showLoader(constants.loaderMessages.deserialization, () => {
                const arrayBuffer = request.response;
                const byteArray = new Uint8Array(arrayBuffer);
                const trees = TreesProto.Trees.deserializeBinary(byteArray).getTreesList();
                common.hideLoader(0);
                if (trees.length !== 0) {
                    drawTrees(trees);
                } else {
                    showNoDataFound();
                }
            });
        };
        request.send();
    });
}
