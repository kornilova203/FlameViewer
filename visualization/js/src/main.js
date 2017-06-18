const TreeProto = require('./tree_pb');
const $ = require('jquery');

const CANVAS_WIDTH = 700;

function createSectionForThread(threadId) {
    $("main").append(templates.tree.getSectionForThread({threadId: threadId}).content);
}


function drawTree(tree) {
    createSectionForThread(tree.getThreadid());
    const stage = new createjs.Stage("canvas-" + tree.getThreadid());
    const graphics = new createjs.Graphics();
    graphics.beginFill("blue").drawRect(0, 0, CANVAS_WIDTH, 15);

    const shape = new createjs.Shape(graphics);
    stage.addChild(shape);
    stage.update();
}

/**
 * Main function
 */
$(window).on("load", function () {
    // const stage = new createjs.Stage("canvas");
    // const text = new createjs.Text("Hello World", "20px Arial", "#ff7700");
    // // text.x = 100;
    // // text.textBaseline = "alphabetic";
    // stage.addChild(text);
    // stage.update();

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
    const input = document.querySelectorAll('.inputfile')[0];
    const label = input.nextElementSibling;

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
