const currentFileName = getParameter("file");

function getPageName() {
    return /[^\/]*(?=\?)/.exec(window.location.href)[0];
}

function updateFilesList(filesList) {
    if (filesList.length === 0) {
        $("<p class='no-file-found'>No file was found</p>").appendTo($(".file-menu"));
    } else {
        const list = templates.tree.listOfFiles({
            fileNames: filesList,
            projectName: getParameter("project"),
            pageName: getPageName()
        }).content;
        $(list).appendTo($(".file-menu"));
        $("#" + currentFileName.replace(/\./, "\\.")).addClass("current-file");
    }
}

function getFilesList(projectName, callback) {
    const request = new XMLHttpRequest();
    request.open("GET", "/flamegraph-profiler/file-list?project=" + projectName, true);
    request.responseType = "json";

    request.onload = function () {
        const fileNames = request.response;
        if (fileNames.length === 0) {
            callback([]);
        } else {
            callback(fileNames);
        }
    };
    request.send();
}

function getParameter(parameterName) {
    const parameters = window.location.href.split("?")[1]
        .split("&");
    for (let i = 0; i < parameters.length; i++) {
        if (parameters[i].startsWith(parameterName)) {
            return parameters[i].substring(parameters[i].indexOf("=") + 1, parameters[i].length);
        }
    }
    return "";
}
