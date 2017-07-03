$(window).on("load", function () {
    const request = new XMLHttpRequest();
    request.open("GET", "/flamegraph-profiler/file-list", true);
    request.responseType = "json";

    request.onload = function () {
        const fileNames = request.response;
        if (fileNames.length === 0) {
            $("<p class='no-file-found'>No file was found</p>").appendTo($("main"));
        } else {
            const list = templates.tree.listOfFiles({
                fileNames: fileNames
            }).content;
            $(list).appendTo($("main"));
        }
    };
    request.send();
});