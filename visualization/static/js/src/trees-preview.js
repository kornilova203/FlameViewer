const deserializer = require('./deserializer');

function sendRequestForTreesPreview() {
    const extension = common.getExtension(constants.fileName);
    if (extension === "ser" || extension === "fierix") {
        getAndShowTreesPreview();
    } else {
        common.showMessage(constants.pageMessages.callTreeUnavailable)
    }
}

$(window).on("load", function () {
    if (constants.fileName === undefined) return;
    common.doCallbackIfFileExists(
        constants.fileName,
        sendRequestForTreesPreview,
        () => common.hideLoader()
    );
});

function getAndShowTreesPreview() {
    const msg = constants.loaderMessages.buildingTrees;
    common.showLoader(msg.msg, msg.width, () => {
        const parameters = window.location.href.split("?")[1];
        let url = serverNames.CALL_TREE_PREVIEW_JS_REQUEST + "?" + parameters;

        common.sendGetRequest(url, "arraybuffer")
            .then(response => {
                common.hideLoader(0);
                const msg = constants.loaderMessages.deserialization;
                common.showLoader(msg.msg, msg.width, () => {
                    const byteArray = new Uint8Array(response);
                    const treesPreview = deserializer.deserializeTreesPreview(byteArray);
                    common.hideLoader(0);
                    if (treesPreview.getTreesPreviewList().length !== 0) {
                        drawTreesPreview(treesPreview);
                    } else {
                        showNoDataFound();
                    }
                });
            });
    });
}

/**
 * @param {Object} treesPreview
 */
function drawTreesPreview(treesPreview) {
    const msg = constants.loaderMessages.drawing;
    common.showLoader(msg.msg, msg.width, () => {
        const treesPreviewList = treesPreview.getTreesPreviewList();
        for (let i = 0; i < treesPreviewList.length; i++) {
            const drawer = new PreviewDrawer(
                treesPreviewList[i],
                i,
                treesPreview.getFullduration(),
                1, // TreePreviewProtos.TreePreview.Vector.VectorCase.X
                2  // TreePreviewProtos.TreePreview.Vector.VectorCase.Y
            );
            drawer.draw();
        }
        common.hideLoader(0);
    });
}
