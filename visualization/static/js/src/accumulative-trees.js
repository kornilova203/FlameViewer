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
    if (constants.pageName === "back-traces") {
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
    if (methodName !== undefined) {
        drawer = new MethodCallTracesDrawer.MethodCallTracesDrawer(tree, className, methodName, desc, percent);
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
    let msg = constants.loaderMessages.drawing;
    common.showLoader(msg.msg, msg.width, () => {
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
    if (methodName !== undefined) {
        drawer = new MethodBackTracesDrawer.MethodBackTracesDrawer(tree, className, methodName, desc, percent);
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
    const parameters = common.getParametersString(constants.urlParameters);
    request.open("GET", `${serverNames.MAIN_NAME}/trees/${constants.pageName}?${parameters}`, true);
    request.responseType = "arraybuffer";

    let className, methodName, desc;
    if (constants.urlParameters[constants.urlParametersKeys.method] !== undefined) {
        className = constants.urlParameters[constants.urlParametersKeys.class];
        methodName = constants.urlParameters[constants.urlParametersKeys.method];
        desc = constants.urlParameters[constants.urlParametersKeys.desc];
    }

    request.onload = function () {
        common.hideLoader(0);
        const msg = constants.loaderMessages.deserialization;
        common.showLoader(msg.msg, msg.width, () => {
            console.log("got response");
            if (request.status !== 200) {
                common.hideLoader();
                common.showMessage(constants.pageMessages.errorOccurred, "left");
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
        });
    };
    console.log("send request");
    request.send();
}

function setTitle() {
    let slash = constants.fileName.lastIndexOf('/');
    if (slash === -1) slash = constants.fileName.lastIndexOf('\\');
    document.title = slash === -1 || slash + 1 === constants.fileName.length ?
        constants.fileName :
        constants.fileName.substring(slash + 1);
}

$(window).on("load", function () {
    if (constants.fileName === undefined) {
        console.log("File is not specified.");
        return;
    }
    setTitle();
    const msg = constants.loaderMessages.buildingTree;
    common.showLoader(msg.msg, msg.width, () => {
        common.doCallbackIfFileExists(
            constants.fileName,
            sendRequestForTree,
            () => {
                common.showError("File does not exist " + constants.fileName);
                common.hideLoader()
            }
        );
    });
});
