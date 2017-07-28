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
            const biggestWidth = hotSpots[0].time;
            for (let i = 0; i < hotSpots.length && i < 200; i++) {
                appendHotSpot(hotSpots[i], biggestWidth);
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
 *  time: Number
 * }} hotSpot
 * @param {Number} biggestWidth
 */
function appendHotSpot(hotSpot, biggestWidth) {
    const hotSpotBlock = $(templates.tree.hotSpot(
        {
            methodName: hotSpot.methodName,
            className: hotSpot.className.split("/").join("."),
            retVal: hotSpot.retVal,
            parameters: hotSpot.parameters,
            break: (hotSpot.retVal + hotSpot.className + hotSpot.methodName).length > 50
        }
    ).content);
    $(hotSpotBlock).find(".method-time").css("width", Math.round(hotSpot.time / biggestWidth * 194));

    $("main").append(hotSpotBlock);
}
