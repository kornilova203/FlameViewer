/**
 * Main function
 */
const TreesProto = require('../generated/trees_pb');

function drawTrees(trees) {
    let minStartTime = trees[0].getTreeInfo().getStartTime();
    let maxFinishTime = trees[0].getTreeInfo().getStartTime() + trees[0].getWidth();
    for (let i = 1; i < trees.length; i++) {
        const startTime = trees[i].getTreeInfo().getStartTime();
        if (startTime < minStartTime) {
            minStartTime = startTime;
        }
        if (startTime + trees[i].getWidth() > maxFinishTime) {
            maxFinishTime = startTime + trees[i].getWidth();
        }
    }
    for (let i = 0; i < trees.length; i++) {
        const drawer = new CallTreeDrawer(trees[i], minStartTime, maxFinishTime);
        drawer.draw();
    }
}

$(window).on("load", function () {
    const request = new XMLHttpRequest();
    request.open("GET", "/flamegraph-profiler/trees/call-tree", true);
    request.responseType = "arraybuffer";

    request.onload = function () {
        const arrayBuffer = request.response;
        const byteArray = new Uint8Array(arrayBuffer);
        const trees = TreesProto.Trees.deserializeBinary(byteArray).getTreesList();
        drawTrees(trees);
    };
    request.send();
});
