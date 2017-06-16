const TreeProto = require('./tree_pb');
const $ = require('jquery');

function createSectionForThread(threadId) {
    $("main").append(templates.tree.getSectionForThread({threadId: threadId}).content);
}


function drawTree(tree) {
    const stage = new createjs.Stage("canvas-" + tree.getThreadid());
    const graphics = new createjs.Graphics();
    graphics.beginFill("blue").drawRect(20, 20, 100, 50);

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
    //
    // const circle = new createjs.Shape();
    // circle.graphics.beginFill("DeepSkyBlue").drawCircle(0, 0, 50);
    // circle.x = 0;
    // circle.y = 0;
    // stage.addChild(circle);
    // stage.update();


    const request = new XMLHttpRequest();
    request.open("GET", "http://localhost:63343/flamegraph-profiler/trees/original-tree", true);
    request.responseType = "arraybuffer";

    request.onload = function () {
        const arrayBuffer = request.response;
        const byteArray = new Uint8Array(arrayBuffer);
        const tree = TreeProto.Tree.deserializeBinary(byteArray);
        createSectionForThread(tree.getThreadid());
        drawTree(tree);
    };
    request.send();
});
