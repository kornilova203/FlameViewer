/**
 * Main function
 */
const TreeProto = require('./tree_pb');

const trees = [];

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
    //     _drawTree(tree);
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
                trees.push(TreeProto.Tree.deserializeBinary(byteArray));
                if (trees.length === 3) {
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
                // console.log(tree);
                // console.log(TreeProto.Tree.prototype);
                // console.log(TreeProto.Tree);
                // const trees = jspb.Message.getRepeatedWrapperField(
                //     TreeProto,
                //     TreeProto.Tree);
                // console.log(trees);
            });
        })(file);
    });
});
