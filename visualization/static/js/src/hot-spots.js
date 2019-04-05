let hotSpots = null;
let iteration = 0;

$(window).on("load", () => {
    if (constants.fileName !== undefined) {
        loadHotSpots();
    }
});

function getRequestAddress() {
    return serverNames.HOT_SPOTS_JS_REQUEST + "?file=" + constants.fileName;
}

function loadHotSpots() {
    common.showLoader(constants.loaderMessages.countingTime, () => {
        const request = new XMLHttpRequest();
        request.open("GET", getRequestAddress(), true);
        request.responseType = "json";

        request.onload = function () {
            hotSpots = request.response;
            if (hotSpots !== undefined && hotSpots.length > 0) {
                appendHotSpots();
            }
            common.hideLoader();
        };
        request.send();
    });
}

function appendHotSpots() {
    removeShowMore();
    const biggestRelativeTime = hotSpots[0].relativeTime;
    for (let i = iteration * 200; i < hotSpots.length && i < (iteration * 200 + 200); i++) {
        appendHotSpot(hotSpots[i], biggestRelativeTime);
    }
    iteration++;
    if (hotSpots.length > iteration * 200) {
        appendShowMore();
    }
}

function appendShowMore() {
    const $showMoreButton = $("main").append($("<button>Show more</button>"));
    $showMoreButton.click(appendHotSpots);
}

function removeShowMore() {
    $("main").find("button").remove();
}

/**
 * @param {{
 *  className: String,
 *  methodName: String,
 *  retVal: String,
 *  parameters: Array<String>,
 *  relativeTime: Number
 * }} hotSpot
 * @param {Number} biggestRelativeTime
 */
function appendHotSpot(hotSpot, biggestRelativeTime) {
    let relativeTime = common.roundRelativeTime(hotSpot.relativeTime);
    /* Restore original description.
     * If parameters array is empty - the original desc is also empty (it does not even contain brackets)
     * If parameters array contains only one empty string then the original description contains empty brackets */
    const desc = (hotSpot.parameters.length === 0)
        ? ""
        : "(" + hotSpot.parameters.join(', ') + ")" + hotSpot.retVal;
    const hotSpotBlock = $(templates.tree.hotSpot(
        {
            methodName: hotSpot.methodName,
            className: hotSpot.className,
            retVal: hotSpot.retVal,
            parameters: hotSpot.parameters,
            relativeTime: relativeTime,
            fileName: constants.fileName,
            desc: desc
        }
    ).content);
    $(hotSpotBlock).find(".method-time").css("width", Math.round(hotSpot.relativeTime / biggestRelativeTime * 194));

    $("main").append(hotSpotBlock);
}
