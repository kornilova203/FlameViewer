/**
 * Main function
 */
const TreeProto = require('../generated/tree_pb');

function drawTree(tree) {
    const drawer = new AccumulativeTreeDrawer(tree);
    drawer.draw();
}

$(window).on("load", function () {
    const request = new XMLHttpRequest();
    const parameters = window.location.href.split("?")[1];
    const urlParts = window.location.href.split("?")[0].split("/");
    const treeType = urlParts[urlParts.length - 1];
    if (parameters === undefined) {
        request.open("GET", "/flamegraph-profiler/trees/" + treeType, true);
    } else {
        request.open("GET", "/flamegraph-profiler/trees/" + treeType + "?" + parameters, true);
    }
    request.responseType = "arraybuffer";

    request.onload = function () {
        const arrayBuffer = request.response;
        const byteArray = new Uint8Array(arrayBuffer);
        //noinspection JSUnresolvedVariable
        const tree = TreeProto.Tree.deserializeBinary(byteArray);
        drawTree(tree);
    };
    request.send();
});
