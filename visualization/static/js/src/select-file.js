$(window).on("load", function () {
    const request = new XMLHttpRequest();
    request.open("GET", "/flamegraph-profiler/file-list", true);
    request.responseType = "json";

    request.onload = function () {
        const fileNames = request.response;
        console.log(fileNames);
        const list = templates.tree.listOfFiles({
            fileNames: fileNames
        }).content;
        $(list).appendTo($("main"));
    };
    request.send();
});