const KEYCODE_ESC = 27;
const KEYCODE_ENTER = 13;

$(window).on("load", () => {
    getFilesList(constants.projectName);
    showProjectsList();
    if (constants.fileName === undefined) {
        showChooseFile();
    }
    unableHideFilesList();
});

function showFilesList($projectsDropdown, $searchForm, $arrowRight, $arrowLeft,
                       $verticalProjectName, $loaderBackground) {
    $loaderBackground.css("left", "calc((100vw - 250px) / 2 + 250px - 80px)");
    $(".file-menu").css("width", 250);
    $projectsDropdown.css("transition", "opacity 300ms");
    $projectsDropdown.css("opacity", 1);
    $projectsDropdown.css("pointer-events", "auto");
    $projectsDropdown.css("position", "relative");
    expandMain();
    $searchForm.show();
    $(".file-form").show();
    $arrowLeft.show();
    $arrowRight.hide();
    $verticalProjectName.css("transition", "");
    $verticalProjectName.css("opacity", 0);
    $verticalProjectName.css("pointer-events", "none");
    $(".file-list").show();

}

function unableHideFilesList() {
    const $arrowLeft = $("#arrow-left");
    const $arrowRight = $("#arrow-right");
    const $projectsDropdown = $(".projects-dropdown");
    const $searchForm = $("#search-file-form");
    const $verticalProjectName = $(".vertical-project-name");
    const $loaderBackground = $(".loader-background");
    $verticalProjectName.html(constants.projectName === "uploaded-files" ? "Uploaded files" : constants.projectName);
    $arrowLeft.click(() => { // hide
        $loaderBackground.css("left", "calc((100vw - 40px) / 2 + 40px - 80px)");
        $projectsDropdown.css("opacity", 0);
        $(".file-menu").css("width", 40);
        shrinkMain();
        $projectsDropdown.css("transition", "opacity 50ms");
        $projectsDropdown.css("pointer-events", "none");
        $projectsDropdown.css("position", "absolute");
        // projectsDropdown.hide();
        $searchForm.hide();
        $(".file-form").hide();
        $arrowLeft.hide();
        $arrowRight.show();
        $verticalProjectName.css("transition", "opacity 300ms");
        $verticalProjectName.css("pointer-events", "auto");
        $verticalProjectName.css("opacity", 1);
        $(".file-list").hide();
    });
    $arrowRight.click(() => { // show
        showFilesList($projectsDropdown, $searchForm, $arrowRight, $arrowLeft,
            $verticalProjectName, $loaderBackground);
    });
    $verticalProjectName.click(() => {
        showFilesList($projectsDropdown, $searchForm, $arrowRight, $arrowLeft,
            $verticalProjectName, $loaderBackground);
    });
}

function getPageName() {
    return /[^\/]*((?=\?)|(?=\.html))/.exec(window.location.href)[0];
}

function showChooseFile() {
    if (constants.projectName === "uploaded-files") {
        showMessage("Choose or upload file");
    } else {
        showMessage("Choose file");
    }
}

function showNoDataFound() {
    showMessage("No call was registered or all methods took <1ms");
}

/**
 * @param {string} message
 */
function showMessage(message) {
    $("body").append(`<p class='message'>${message}</p>`);
}

function appendInput() {
    const input = templates.tree.fileInput().content;
    // noinspection all
    $(input).insertBefore("#search-file-form");
}

function deleteFile(popup, li, liFileName) {
    popup.remove();
    const request = new XMLHttpRequest();
    request.open("POST", "/flamegraph-profiler/delete-file", true);
    request.setRequestHeader('File-Name', liFileName);
    request.setRequestHeader('Project-Name', constants.projectName);
    request.send();
    li.remove();
    $(document).unbind("keyup");
}

function bindEscAndCancel(popup) {
    // noinspection JSUnresolvedFunction
    $(document).keyup(function (e) {
        if (e.keyCode === KEYCODE_ENTER) {
            popup.find(".do-delete").click();
        }
        if (e.keyCode === KEYCODE_ESC) {
            console.log("esc");
            popup.find(".confirm-delete-bg").click();
        }
    });
}

function createDeleteFilePopup(li, liFileName) {
    const popupText = templates.tree.confirmDelete({
        fileName: liFileName
    }).content;
    const popup = $(popupText);
    $("body").append(popup);
    let wasPopupClicked = false;
    if (liFileName === constants.fileName) {
        popup.find("a").attr("href", "/flamegraph-profiler/" + getPageName() + "?project=" + constants.projectName);
    }
    popup.find(".do-delete").click(() => {
        deleteFile(popup, li, liFileName);
    });
    popup.find(".do-not-delete").click(() => {
        popup.remove();
        $(document).unbind("keyup");
    });
    popup.find(".confirm-delete-bg").click(() => {
        if (wasPopupClicked) {
            wasPopupClicked = false;
        } else {
            popup.remove();
            $(document).unbind("keyup");
        }
    });
    popup.find(".confirm-delete-popup").click(() => {
        wasPopupClicked = true;
    });
    bindEscAndCancel(popup);
}

function updateFilesList(filesList) {
    if (filesList.length === 0) {
        $("<p class='no-file-found'>No file was found</p>").appendTo($(".file-menu"));
    } else {
        const listString = templates.tree.listOfFiles({
            fileNames: filesList,
            projectName: constants.projectName,
            pageName: getPageName()
        }).content;
        const list = $(listString);
        list.appendTo($(".file-menu"));
        // noinspection JSValidateTypes
        list.children().each(function () {
            const li = $(this);
            const liFileName = li.attr("id");
            li.find("img").click(() => {
                createDeleteFilePopup(li, liFileName);
            })
        });
        if (constants.fileName !== undefined) {
            $("#" + constants.fileName.replace(/\./, "\\.")).addClass("current-file");
        }
    }
    if (constants.projectName === "uploaded-files") {
        appendInput();
        listenInput();
    }
}

function appendProject(project) {
    if (project === constants.projectName ||
        (project === "Uploaded files" && constants.projectName === "uploaded-files")) {
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
    if (constants.projectName === "uploaded-files") {
        $(".project-name").text("Uploaded files");
    } else {
        $(".project-name").text(constants.projectName);
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

function expandMain() {
    if (getPageName() === "call-tree") {
        constants.$main.css("margin-left", 250);
        constants.$main.css("width", "calc(100vw - 250px)")
    } else {
        $("main").css("margin-left", "calc((100vw - 250px - 1200px) / 2 + 250px)");
    }
}

function shrinkMain() {
    if (getPageName() === "call-tree") {
        constants.$main.css("margin-left", 40);
        constants.$main.css("width", "calc(100vw - 40px)")
    } else {
        $("main").css("margin-left", "calc((100vw - 40px - 1200px) / 2 + 40px)");
    }
}
