/**
 * Main function
 */
const TreeProto = require('../generated/tree_pb');

function drawTree(tree) {
    new BaseDrawer(tree);
}

$(window).on("load", function () {
    const request = new XMLHttpRequest();
    console.log(window.location.href);
    const parameters = window.location.href.split("?")[1];
    if (parameters === undefined) {
        request.open("GET", "/flamegraph-profiler/trees/outgoing-calls", true);
    } else {
        request.open("GET", "/flamegraph-profiler/trees/outgoing-calls?" + parameters, true);
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
