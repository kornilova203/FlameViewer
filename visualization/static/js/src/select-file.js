$(window).on("load", function () {
    const request = new XMLHttpRequest();
    request.open("GET", "/flamegraph-profiler/file-list", true);
    request.responseType = "json";

    request.onload = function () {
        const json = request.response;
        console.log(json);
    };
    request.send();
});