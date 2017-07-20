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
        const drawer = new CallTreeDrawer(trees[i], minStartTime, maxFinishTime);
        drawer.draw();
    }
    AccumulativeTreeDrawer.hideLoader();
}

/**
 * Get extension of file
 * @param {string} fileName
 * @returns {string}
 */
function getExtension(fileName) {
    return fileName.substring(fileName.indexOf(".") + 1, fileName.length);
}

function getAndDrawTrees() {
    AccumulativeTreeDrawer.showLoader(() => {
        console.log("prepare request");
        const request = new XMLHttpRequest();
        request.open("GET", "/flamegraph-profiler/trees/call-tree?file=" +
            fileName +
            "&project=" +
            getProjectName(),
            true);
        request.responseType = "arraybuffer";

        request.onload = function () {
            const arrayBuffer = request.response;
            const byteArray = new Uint8Array(arrayBuffer);
            const trees = TreesProto.Trees.deserializeBinary(byteArray).getTreesList();
            drawTrees(trees);
        };
        request.send();
    });
}

$(window).on("load", function () {
    console.log("loaded");
    if (fileName !== undefined) {
        const extension = getExtension(fileName);
        if (extension !== "jfr") {
            console.log("not jfr");
            getAndDrawTrees();
        } else {
            showMessage("This type of tree is unavailable for .jfr files")
        }
    } else {
        showChooseFile();
    }
});

function getProjectName() {
    const parameters = window.location.href.split("?")[1]
        .split("&");
    for (let i = 0; i < parameters.length; i++) {
        if (parameters[i].startsWith("project")) {
            return parameters[i].substring(parameters[i].indexOf("=") + 1, parameters[i].length);
        }
    }
    return "";
}
