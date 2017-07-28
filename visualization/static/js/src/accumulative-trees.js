/**
 * Main function
 */
const TreeProto = require('../generated/tree_pb');

function drawTree(tree, className, methodName, desc) {
    console.log("got tree");
    let drawer;
    if (getPageName() === "incoming-calls") {
        drawer = new IncomingCallsDrawer(tree);
        if (className === undefined && // if common tree
            drawer.getNodesCount() > 20000) {
            showMessage("Tree has more than 20 000 nodes. " +
                "You can see incoming calls for particular method (for this go to Hot spots page)");
            return;
        }
    } else {
        drawer = new AccumulativeTreeDrawer(tree);
    }
    if (className !== undefined && methodName !== undefined && desc !== undefined) {
        drawer.setHeader(className.split("%2F").join(".") + "." +
            methodName +
            desc.split("%2F").join(".").split("%28").join("(").split("%29").join(")").split("%3B").join(";"));
    }
    drawer.draw();
}

function treeIsEmpty(tree) {
    return tree.getBaseNode() === undefined;
}

$(window).on("load", function () {
    if (fileName !== undefined) {
        AccumulativeTreeDrawer.showLoader(() => {
            console.log("prepare request");
            const request = new XMLHttpRequest();
            const parameters = window.location.href.split("?")[1];
            let className;
            let methodName;
            let desc;
            const urlParts = window.location.href.split("?")[0].split("/");
            let treeType = urlParts[urlParts.length - 1];
            if (treeType.indexOf(".") !== -1) {
                treeType = treeType.substring(0, treeType.indexOf("."));
            }
            if (parameters.indexOf("method=") === -1) {
                request.open("GET", `/flamegraph-profiler/trees/${treeType}?file=${fileName}&project=${projectName}`, true);
            } else {
                request.open("GET", `/flamegraph-profiler/trees/${treeType}?${parameters}&file=${fileName}`, true);
                className = getParameter("class");
                methodName = getParameter("method");
                desc = getParameter("desc");
            }
            request.responseType = "arraybuffer";

            request.onload = function () {
                console.log("got response");
                const arrayBuffer = request.response;
                const byteArray = new Uint8Array(arrayBuffer);
                //noinspection JSUnresolvedVariable
                const tree = TreeProto.Tree.deserializeBinary(byteArray);
                if (!treeIsEmpty(tree)) {
                    drawTree(tree, className, methodName, desc);
                } else {
                    showNoDataFound();
                }
                AccumulativeTreeDrawer.hideLoader();
            };
            console.log("send request");
            request.send();

        });
    }
});