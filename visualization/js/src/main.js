const TreeProto = require('./tree_pb');
const $ = require('jquery');
function createSectionForThread(threadId) {

}

/**
 * Main function
 */
$(window).on("load", function () {
    console.log(tree.templates.getSectionForThread({threadId: 12345}));
    // console.log(TreeTemplates.getSectionForThread("1234"));
    const request = new XMLHttpRequest();
    request.open("GET", "http://localhost:63343/flamegraph-profiler/trees/original-tree", true);
    request.responseType = "arraybuffer";

    request.onload = function () {
        const arrayBuffer = request.response;
        const byteArray = new Uint8Array(arrayBuffer);
        console.log(byteArray);
        const tree = TreeProto.Tree.deserializeBinary(byteArray);
        console.log(tree.getStarttime());
        createSectionForThread(123);
    };
    request.send();
});
