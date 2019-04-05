/**
 * Main function
 */
const deserializer = require('./deserializer');
const CallTracesDrawer = require('./CallTracesDrawer');
const MethodCallTracesDrawer = require('./MethodCallTracesDrawer');
const BackTracesDrawer = require('./BackTracesDrawer');
const MethodBackTracesDrawer = require('./MethodBackTracesDrawer');

/**
 * @param tree
 * @param {String} className
 * @param {String} methodName
 * @param {String} desc
 * @param {Number} percent
 */
function drawTree(tree, className, methodName, desc, percent) {
    if (constants.pageName === "incoming-calls") {
        drawBackTraces(tree, className, methodName, desc, percent);
    } else {
        drawCallTraces(tree, className, methodName, desc, percent);
    }
}

/**
 * @param tree
 * @param className
 * @param methodName
 * @param desc
 * @param {Number} percent
 */
function drawCallTraces(tree, className, methodName, desc, percent) {
    let drawer;
    if (className !== undefined && methodName !== undefined && desc !== undefined) {
        drawer = new MethodCallTracesDrawer.MethodCallTracesDrawer(tree, decodeURIComponent(className), decodeURIComponent(methodName),
            decodeURIComponent(desc), percent);
    } else {
        drawer = new CallTracesDrawer.CallTracesDrawer(tree);
    }
    common.hideLoader();
    drawAndShowLoader(drawer, className, methodName, desc, percent);
}

/**
 * @param drawer
 * @param {String} className
 * @param {String} methodName
 * @param {String} desc
 * @param {Number} percent
 */
function drawAndShowLoader(drawer, className, methodName, desc, percent) {
    common.showLoader(constants.loaderMessages.drawing, () => {
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
function drawBackTraces(tree, className, methodName, desc, percent) {
    let drawer;
    if (className !== undefined && methodName !== undefined && desc !== undefined) {
        drawer = new MethodBackTracesDrawer.MethodBackTracesDrawer(tree, decodeURIComponent(className), decodeURIComponent(methodName),
            decodeURIComponent(desc), percent);
    } else {
        drawer = new BackTracesDrawer.BackTracesDrawer(tree);
    }
    common.hideLoader();
    drawAndShowLoader(drawer, className, methodName, desc, percent);
}

function treeIsEmpty(tree) {
    return tree.getBaseNode() === undefined;
}

function sendRequestForTree() {
    console.log("prepare request");
    const request = new XMLHttpRequest();
    const parameters = window.location.href.split("?")[1];
    let className, methodName, desc;
    if (parameters.indexOf("method=") === -1) {
        request.open("GET", `${serverNames.MAIN_NAME}/trees/${constants.pageName}?${parameters}`, true);
    } else {
        request.open("GET", `${serverNames.MAIN_NAME}/trees/${constants.pageName}?${parameters}`, true);
        className = common.getParameter("class");
        methodName = common.getParameter("method");
        desc = decodeURIComponent(common.getParameter("desc"));
    }
    request.responseType = "arraybuffer";

    request.onload = function () {
        common.hideLoader(0);
        common.showLoader(constants.loaderMessages.deserialization, () => {
            console.log("got response");
            if (constants.pageName === "incoming-calls" && request.status === 400) { // tree contains too many nodes
                common.hideLoader();
                common.showMessage(constants.pageMessages.backtracesTooBig, "left");
                return;
            }
            const arrayBuffer = request.response;
            const byteArray = new Uint8Array(arrayBuffer);
            const tree = deserializer.deserializeTree(byteArray);
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
}

$(window).on("load", function () {
    if (constants.fileName !== undefined) {
        common.showLoader(constants.loaderMessages.buildingTree, () => {
            common.doCallbackIfFileExists(
                constants.fileName,
                sendRequestForTree,
                () => { // if does not exist
                    common.redirect({})
                }
            );
        });
    } else {
        console.log("File is not specified.");
    }
});