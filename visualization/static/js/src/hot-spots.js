$(window).on("load", () => {
    if (fileName !== undefined &&
        projectName !== undefined) {
        showHotSpots();
    }
});

function showHotSpots() {
    const request = new XMLHttpRequest();
    request.open("GET", "/flamegraph-profiler/hot-spots-json?project=" +
        projectName +
        "&" +
        "file=" +
        fileName,
        true);
    request.responseType = "json";

    request.onload = function () {
        const hotSpots = request.response;
        console.log(hotSpots);
        if (hotSpots !== undefined && hotSpots.length > 0) {
            const biggestRelativeTime = hotSpots[0].relativeTime;
            for (let i = 0; i < hotSpots.length && i < 200; i++) {
                appendHotSpot(hotSpots[i], biggestRelativeTime);
            }
        }
    };
    request.send();
    console.log("request was sent");
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
    const hotSpotBlock = $(templates.tree.hotSpot(
        {
            methodName: hotSpot.methodName,
            className: hotSpot.className,
            retVal: hotSpot.retVal,
            parameters: hotSpot.parameters,
            doBreak: (hotSpot.retVal + hotSpot.className + hotSpot.methodName).length > 80,
            relativeTime: Math.round(hotSpot.relativeTime * 1000) / 10,
            fileName: fileName,
            projectName: projectName,
            desc: "(" + hotSpot.parameters.join(', ') + ")" + hotSpot.retVal
        }
    ).content);
    $(hotSpotBlock).find(".method-time").css("width", Math.round(hotSpot.relativeTime / biggestRelativeTime * 194));

    $("main").append(hotSpotBlock);
}
