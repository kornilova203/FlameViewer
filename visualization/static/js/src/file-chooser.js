const projectName = getParameter("project");
if (projectName === undefined) {
    console.log("project is not defined");
}
const fileName = getParameter("file");
if (fileName === undefined) {
    console.log("file is not defined");
}

function getPageName() {
    return /[^\/]*((?=\?)|(?=\.html))/.exec(window.location.href)[0];
}

function showChooseFile() {
    if (projectName === "uploaded-files") {
        showMessage("Choose or upload file");
    } else {
        showMessage("Choose file");
    }
}

/**
 * @param {String} parameterName
 * @return {undefined|string}
 */
function getParameter(parameterName) {
    const parametersString = window.location.href.split("?")[1];
    if (parametersString === undefined) {
        return undefined;
    } else {
        const parameters = parametersString.split("&");
        for (let i = 0; i < parameters.length; i++) {
            if (parameters[i].startsWith(parameterName + "=")) {
                return parameters[i].substring(
                    parameters[i].indexOf("=") + 1,
                    parameters[i].length
                );
            }
        }
    }
    return undefined;
}

function showNoDataFound() {
    showMessage("No call was registered or all methods took <1ms");
}

/**
 * @param {string} message
 */
function showMessage(message) {
    $("main").append(`<p class='message'>${message}</p>`);
}

$(window).on("load", () => {
    getFilesList(projectName);
    showProjectsList();
});

function appendInput() {
    const input = templates.tree.fileInput().content;
    $(input).insertBefore("#search-file");
}

function updateFilesList(filesList) {
    if (filesList.length === 0) {
        $("<p class='no-file-found'>No file was found</p>").appendTo($(".file-menu"));
    } else {
        const list = templates.tree.listOfFiles({
            fileNames: filesList,
            projectName: projectName,
            pageName: getPageName()
        }).content;
        $(list).appendTo($(".file-menu"));
        if (fileName !== undefined) {
            $("#" + fileName.replace(/\./, "\\.")).addClass("current-file");
        }
    }
    if (projectName === "uploaded-files") {
        appendInput();
        listenInput();
    }
}

function appendProject(project) {
    if (project === projectName) {
        return;
    }
    const link = "/flamegraph-profiler/" +
        getPageName() +
        "?project=" +
        (project === "Uploaded files" ? "uploaded-files" : project);
    const newElem = $(`<a href='${link}'>${project}</a>`);
    if (project === "Uploaded files") {
        newElem.addClass("uploaded-files-drop-down");
    }
    newElem.appendTo($(".projects-dropdown-content"));
}

function showProjectsList() {
    if (projectName === "uploaded-files") {
        $(".project-name").text("Uploaded files");
    } else {
        $(".project-name").text(projectName);
    }
    const request = new XMLHttpRequest();
    request.open("GET", "/flamegraph-profiler/list-projects", true);
    request.responseType = "json";

    request.onload = function () {
        const projects = request.response;
        for (let i = 0; i < projects.length; i++) {
            appendProject(projects[i]);
        }
        appendProject("Uploaded files");
    };
    request.send();
}

function getFilesList(projectName) {
    const request = new XMLHttpRequest();
    request.open("GET", "/flamegraph-profiler/file-list?project=" + projectName, true);
    request.responseType = "json";

    request.onload = function () {
        const fileNames = request.response;
        if (fileNames.length === 0) {
            updateFilesList([])
        } else {
            updateFilesList(fileNames);
        }
    };
    request.send();
}
