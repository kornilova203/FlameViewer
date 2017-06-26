/**
 * Main function
 */
const TreeProto = require('../generated/tree_pb');

function drawTree(tree) {
    new BaseDrawer(tree);
}

$(window).on("load", function () {
    console.log(window.location.href);
    const request = new XMLHttpRequest();
    request.open("GET", "/flamegraph-profiler/trees/outgoing-calls", true);
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
