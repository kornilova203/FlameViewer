/**
 * Main function
 */
const TreesProto = require('../generated/trees_pb');

function drawTrees(trees) {
    let maxDuration = trees[0].getTreeInfo().getStartTime() + trees[0].getWidth();
    for (let i = 1; i < trees.length; i++) {
        const startTime = trees[i].getTreeInfo().getStartTime();
        if (startTime + trees[i].getWidth() > maxDuration) {
            maxDuration = startTime + trees[i].getWidth();
        }
    }
    console.log("full duration: " + maxDuration);
    for (let i = 0; i < trees.length; i++) {
        const drawer = new CallTreeDrawer(trees[i], maxDuration, i);
        drawer.draw();
    }
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
    AccumulativeTreeDrawer.showLoader(() => {
        console.log("prepare request");
        const request = new XMLHttpRequest();
        const parameters = window.location.href.split("?")[1];
        request.open("GET", "/flamegraph-profiler/trees/call-tree?" + parameters,
            true);
        request.responseType = "arraybuffer";

        request.onload = function () {
            const arrayBuffer = request.response;
            const byteArray = new Uint8Array(arrayBuffer);
            const trees = TreesProto.Trees.deserializeBinary(byteArray).getTreesList();
            if (trees.length !== 0) {
                drawTrees(trees);
            } else {
                showNoDataFound();
            }
            AccumulativeTreeDrawer.hideLoader();
        };
        request.send();
    });
}

$(window).on("load", function () {
    console.log("loaded");
    if (fileName !== undefined) {
        const extension = getExtension(fileName);
        if (extension !== "jfr") {
            console.log("not jfr");
            getAndDrawTrees();
        } else {
            showMessage("This type of tree is unavailable for .jfr files")
        }
    }
});
