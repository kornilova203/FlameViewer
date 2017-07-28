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
    };
    request.send();
    console.log("request was sent");
}
