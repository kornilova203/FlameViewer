/**
 * @type {{Trees: Trees}}
 */
const TreesProto = require('../generated/trees_pb');

/**
 * don't know why but just usual function cannot be seen from PreviewDrawer.
 * That's why I added it to common
 * @param {Number} threadId
 */
common.showCallTree = (threadId) => {
    const request = new XMLHttpRequest();
    const parameters = window.location.href.split("?")[1];
    request.open("GET", "/flamegraph-profiler/trees/call-tree?threads=" + threadId + "&" + parameters, true);
    request.responseType = "arraybuffer";
    request.onload = () => {
        const arrayBuffer = request.response;
        const byteArray = new Uint8Array(arrayBuffer);
        const trees = TreesProto.Trees.deserializeBinary(byteArray).getTreesList();
        drawTrees(trees);
    };
    request.send();
};

/**
 * @param {Array<Trees>} trees
 */
function drawTrees(trees) {
    common.showLoader(constants.loaderMessages.drawing, () => {
        $('.call-tree-wrapper').show();
        $("#search-method-form").show();
        for (let i = 0; i < trees.length; i++) {
            const drawer = new CallTreeDrawer(trees[i], i);
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
    const $searchMethodForm = $("#search-method-form").show();
    $callTreeText.click(() => {
        $treePreviewWrapper.removeClass("hidden-tree-preview");
        $callTreeWrapper.hide();
        $searchMethodForm.hide();
        $callTreeWrapper.find("*").remove();
        $callTreeText.unbind();
    });
}

common.shrinkTreePreviewWrapper = () => {
    $(".tree-preview-wrapper").addClass("hidden-tree-preview");
};