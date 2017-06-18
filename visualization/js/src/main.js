const TreeProto = require('./tree_pb');
const $ = require('jquery');

const CANVAS_WIDTH = 700;
const RECT_GRAPHICS = new createjs.Graphics()
    .beginFill("#0887d7")
    .drawRect(0, 0, CANVAS_WIDTH, 15);

function createSectionForThread(threadId) {
    $("main").append(templates.tree.getSectionForThread({threadId: threadId}).content);
}

function drawTree(tree) {
    createSectionForThread(tree.getThreadid());
    const stage = new createjs.Stage("canvas-" + tree.getThreadid());

    const shape = new createjs.Shape(RECT_GRAPHICS);
    stage.addChild(shape);
    stage.update();
}

/**
 * Main function
 */
$(window).on("load", function () {
    // const request = new XMLHttpRequest();
    // request.open("GET", "http://localhost:63343/flamegraph-profiler/trees/original-tree", true);
    // request.responseType = "arraybuffer";
    //
    // request.onload = function () {
    //     const arrayBuffer = request.response;
    //     const byteArray = new Uint8Array(arrayBuffer);
    //     const tree = TreeProto.Tree.deserializeBinary(byteArray);
    //     createSectionForThread(tree.getThreadid());
    //     drawTree(tree);
    // };
    // request.send();

    // INPUT form for easier debugging
    $('#file').on('change', function (e) {
        const file = e.target.files[0]; // FileList object
        const reader = new FileReader();

        reader.onload = (function (theFile) {
            reader.readAsArrayBuffer(theFile);
            reader.addEventListener("load", function () {
                const arrayBuffer = reader.result;
                const byteArray = new Uint8Array(arrayBuffer);
                const tree = TreeProto.Tree.deserializeBinary(byteArray);
                drawTree(tree);
            });
        })(file);
    });
});
