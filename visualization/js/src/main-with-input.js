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
        new Drawer(trees[i], minStartTime, maxFinishTime);
    }
}

$(window).on("load", function () {
    $('#file').on('change', function (e) {
        const file = e.target.files[0]; // FileList object
        const reader = new FileReader();
        //noinspection JSUnfilteredForInLoop
        reader.onload = (function (theFile) {
            reader.readAsArrayBuffer(theFile);
            reader.addEventListener("load", function () {
                const arrayBuffer = reader.result;
                const byteArray = new Uint8Array(arrayBuffer);
                const trees = TreesProto.Trees.deserializeBinary(byteArray).getTreesList();
                drawTrees(trees);
            });
        })(file);
    });
});
