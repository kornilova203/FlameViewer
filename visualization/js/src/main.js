const TreeProto = require('./tree_pb');
const $ = require('jquery');

function createSectionForThread(threadId) {
    $("main").append(templates.tree.getSectionForThread({threadId: threadId}).content);
}

/**
 * Main function
 */
$(window).on("load", function () {
    const request = new XMLHttpRequest();
    request.open("GET", "http://localhost:63343/flamegraph-profiler/trees/original-tree", true);
    request.responseType = "arraybuffer";

    request.onload = function () {
        const arrayBuffer = request.response;
        const byteArray = new Uint8Array(arrayBuffer);
        const tree = TreeProto.Tree.deserializeBinary(byteArray);
        createSectionForThread(tree.getThreadid());
    };
    request.send();
});
