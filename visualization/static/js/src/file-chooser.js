function getPageName() {
    return /[^\/]*(?=\?)/.exec(window.location.href)[0];
}

function showChooseFile() {
    console.log("choose file");
}

$(window).on("load", () => {
    showProjectsList();
});

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
        if (fileName !== "") {
            $("#" + fileName.replace(/\./, "\\.")).addClass("current-file");
        }
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
    const newElem = `<a href='${link}'>${project}</a>`;
    $(newElem).appendTo($(".projects-dropdown-content"));
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
