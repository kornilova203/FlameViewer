/**
 * Main function
 */
const TreeProto = require('../generated/tree_pb');

/**
 * @param tree
 * @param {String} className
 * @param {String} methodName
 * @param {String} desc
 * @param {Number} percent
 */
function drawTree(tree, className, methodName, desc, percent) {
    if (getPageName() === "incoming-calls") {
        drawIncomingCalls(tree, className, methodName, desc, percent);
    } else {
        drawAccumulativeTree(tree, className, methodName, desc, percent);
    }
}

/**
 * @param tree
 * @param className
 * @param methodName
 * @param desc
 * @param {Number} percent
 */
function drawAccumulativeTree(tree, className, methodName, desc, percent) {
    const drawer = new AccumulativeTreeDrawer(tree);
    common.hideLoader();
    drawAndShowLoader(drawer, className, methodName, desc, percent);
}

/**
 * @param {AccumulativeTreeDrawer} drawer
 * @param {String} className
 * @param {String} methodName
 * @param {String} desc
 * @param {Number} percent
 */
function drawAndShowLoader(drawer, className, methodName, desc, percent) {
    common.showLoader(constants.loaderMessages.drawing, () => {
        if (className !== undefined && methodName !== undefined && desc !== undefined) {
            drawer.setHeader(
                decodeURIComponent(className),
                decodeURIComponent(methodName),
                decodeURIComponent(desc),
                percent
            );
        }
        drawer.draw();
        common.hideLoader();
    });
}

/**
 * @param tree
 * @param className
 * @param methodName
 * @param desc
 * @param {Number} percent
 */
function drawIncomingCalls(tree, className, methodName, desc, percent) {
    const drawer = new IncomingCallsDrawer(tree);
    if (className === undefined && // if common tree
        drawer.getNodesCount() > 20000) {
        common.showMessage("Tree has more than 20 000 nodes. " +
            "You can see incoming calls for particular method (for this go to Hot spots page)");
        common.hideLoader();
        return;
    }
    common.hideLoader();
    drawAndShowLoader(drawer, className, methodName, desc, percent);
}

function treeIsEmpty(tree) {
    return tree.getBaseNode() === undefined;
}

$(window).on("load", function () {
    if (constants.fileName !== undefined) {
        common.showLoader(constants.loaderMessages.buildingTree, () => {
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
                request.open("GET", `/flamegraph-profiler/trees/${treeType}?${parameters}`, true);
            } else {
                console.log(parameters);
                request.open("GET", `/flamegraph-profiler/trees/${treeType}?${parameters}`, true);
                className = common.getParameter("class");
                methodName = common.getParameter("method");
                desc = decodeURIComponent(common.getParameter("desc"));
            }
            request.responseType = "arraybuffer";

            request.onload = function () {
                common.hideLoader(0);
                common.showLoader(constants.loaderMessages.deserialization, () => {
                    console.log("got response");
                    const arrayBuffer = request.response;
                    const byteArray = new Uint8Array(arrayBuffer);
                    //noinspection JSUnresolvedVariable
                    const tree = TreeProto.Tree.deserializeBinary(byteArray);
                    if (!treeIsEmpty(tree)) {
                        let percent = 0;
                        if (tree.getTreeInfo() !== undefined && tree.getTreeInfo().getTimePercent()) {
                            percent = common.roundRelativeTime(
                                tree.getTreeInfo().getTimePercent());
                        }
                        drawTree(tree, className, methodName, desc, percent);
                    } else {
                        common.hideLoader();
                        showNoDataFound();
                    }
                    common.resizeLoaderBackground();
                });
            };
            console.log("send request");
            request.send();

        });
    }
});