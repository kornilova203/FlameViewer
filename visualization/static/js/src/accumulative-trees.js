/**
 * Main function
 */
const TreeProto = require('../generated/tree_pb');

function drawTree(tree, className, methodName, desc) {
    const drawer = new AccumulativeTreeDrawer(tree);
    if (className !== undefined && methodName !== undefined && desc !== undefined) {
        drawer.setHeader(className.split("%2F").join(".") + "." +
            methodName +
            desc.split("%2F").join(".").split("%28").join("(").split("%29").join(")").split("%3B").join(";"));
    }
    drawer.draw();
}

$(window).on("load", function () {
    const request = new XMLHttpRequest();
    const parameters = window.location.href.split("?")[1];
    let className;
    let methodName;
    let desc;
    const urlParts = window.location.href.split("?")[0].split("/");
    const treeType = urlParts[urlParts.length - 1];
    if (parameters.indexOf("method=") === -1) {
        request.open("GET", `/flamegraph-profiler/trees/${treeType}?file=${fileName}`, true);
    } else {
        request.open("GET", `/flamegraph-profiler/trees/${treeType}?${parameters}&file=${fileName}`, true);
        className = /(?:class=)([^&]+)(?:&)/.exec(parameters)[1];
        methodName = /(?:method=)([^&]+)(?:&)/.exec(parameters)[1];
        desc = /(?:desc=)([^&]+)(?:&)/.exec(parameters)[1];
    }
    request.responseType = "arraybuffer";

    request.onload = function () {
        const arrayBuffer = request.response;
        const byteArray = new Uint8Array(arrayBuffer);
        //noinspection JSUnresolvedVariable
        const tree = TreeProto.Tree.deserializeBinary(byteArray);
        drawTree(tree, className, methodName, desc);
    };
    request.send();
});
