const deserializer = require('./deserializer');
const CallTreeDrawer = require('./CallTreeDrawer');

/**
 * don't know why but just usual function cannot be seen from PreviewDrawer.
 * That's why I added it to common
 * @param {Number} threadId
 */
common.showCallTree = (threadId) => {
    const parameters = window.location.href.split("?")[1];
    const url = serverNames.CALL_TREE_JS_REQUEST + "?threads=" + threadId + "&" + parameters;
    common.sendGetRequest(url, "arraybuffer")
        .then(response => {
            const byteArray = new Uint8Array(response);
            const trees = deserializer.deserializeTrees(byteArray).getTreesList();
            drawTrees(trees);
        });
};

/**
 * @param {Array<Trees>} trees
 */
function drawTrees(trees) {
    common.showLoader(constants.loaderMessages.drawing.msg, constants.loaderMessages.drawing.width, () => {
        $('.call-tree-wrapper').show();
        for (let i = 0; i < trees.length; i++) {
            const drawer = new CallTreeDrawer.CallTreeDrawer(trees[i], i);
            drawer.draw();
        }
        bindHideDetailView();
        common.hideLoader(0);
    });
}

function bindHideDetailView() {
    const $callTreeWrapper = $('.call-tree-wrapper');
    const $callTreeText = $callTreeWrapper.find("h2 i");
    const $treePreviewWrapper = $(".tree-preview-wrapper");
    $callTreeText.click(() => {
        $treePreviewWrapper.removeClass("hidden-tree-preview");
        $callTreeWrapper.hide();
        $callTreeWrapper.find("*").remove();
        $callTreeText.unbind();
    });
}

common.shrinkTreePreviewWrapper = () => {
    $(".tree-preview-wrapper").addClass("hidden-tree-preview");
};