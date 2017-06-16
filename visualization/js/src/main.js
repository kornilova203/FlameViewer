const TreeProto = require('./tree_pb');
const $ = require('jquery');

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
        console.log(byteArray);
        const tree = TreeProto.Tree.deserializeBinary(byteArray);
        console.log(tree.getStarttime());
    };
    request.send();
});
