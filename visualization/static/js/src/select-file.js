$(window).on("load", function () {
    getFilesList();
    listenInput();
});

function appendWrongExtension() {
    if ($(".extension-error").length === 0) {
        $(".file-form").append("<p class='extension-error'>Wrong file extension</p>");
    }
}

function processStatuses(statuses) {
    if (statuses.some((v) => v === 200)) { // if at least one file was loaded
        location.reload();
    } else {
        appendWrongExtension();
    }
}

function sendFile(file, statuses, fileCount) {
    const request = new XMLHttpRequest();
    request.onreadystatechange = (e) => {
        if (request.readyState === 4) {
            statuses.push(e.target.status);
            if (statuses.length === fileCount) { // if all files was loaded
                processStatuses(statuses);
            }
        }
    };
    request.open("POST", "/flamegraph-profiler/upload-file", true);
    request.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    request.setRequestHeader('File-Name', file.name);
    request.send(file);

}

function listenInput() {
    $('#file').on('change', function (e) {
        const reader = new FileReader();

        const statuses = [];

        const fileCount = e.target.files.length;

        for (let i = 0; i < fileCount; i++) {
            reader.onload = (function (theFile) {
                sendFile(theFile, statuses, fileCount);
            })(e.target.files[i]);
        }
    });
}

function getProjectName() {
    const parameters = window.location.href.split("?")[1]
        .split("&");
    for (let i = 0; i < parameters.length; i++) {
        if (parameters[i].startsWith("project")) {
            return parameters[i].substring(parameters[i].indexOf("=") + 1, parameters[i].length);
        }
    }
    return "";
}

function getFilesList() {
    const projectName = getProjectName();
    const request = new XMLHttpRequest();
    request.open("GET", "/flamegraph-profiler/file-list?project=" + projectName, true);
    request.responseType = "json";

    request.onload = function () {
        const fileNames = request.response;
        if (fileNames.length === 0) {
            $("<p class='no-file-found'>No file was found</p>").appendTo($("main"));
        } else {
            const list = templates.tree.listOfFiles({
                fileNames: fileNames,
                project: projectName
            }).content;
            $(list).appendTo($("main"));
        }
    };
    request.send();
}