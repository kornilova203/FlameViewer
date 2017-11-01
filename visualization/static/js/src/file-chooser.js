const KEYCODE_ESC = 27;
const KEYCODE_ENTER = 13;
const selectedFiles = new Set();
let $lastSelectedFile = null;

$(window).on("load", () => {
    getFilesList(constants.projectName);
    showProjectsList();
    if (constants.fileName === undefined) {
        showChooseFile();
    }
    enableHideFilesList();
});

function showFilesList($projectsDropdown, $searchForm,
                       $verticalProjectName, $loaderBackground) {
    $loaderBackground.css("left", "calc((100vw - 250px) / 2 + 250px - 80px)");
    $(".file-menu").css("width", 250);
    $projectsDropdown.css("transition", "opacity 300ms");
    $projectsDropdown.css("opacity", 1);
    $projectsDropdown.css("pointer-events", "auto");
    $projectsDropdown.css("position", "relative");
    $searchForm.show();
    $(".file-form").show();
    constants.$arrowLeft.show();
    constants.$arrowRight.hide();
    $verticalProjectName.css("transition", "");
    $verticalProjectName.css("opacity", 0);
    $verticalProjectName.css("pointer-events", "none");
    $(".file-list").show();
    $(".tree-preview-wrapper").removeClass("tree-preview-wrapper-without-files");
}

function enableHideFilesList() {
    const $projectsDropdown = $(".projects-dropdown");
    const $searchForm = $("#search-file-form");
    const $verticalProjectName = $(".vertical-project-name");
    const $loaderBackground = $(".loader-background");
    $verticalProjectName.html(constants.projectName === "uploaded-files" ? "Uploaded files" : constants.projectName);
    constants.$arrowLeft.click(() => { // hide
        $loaderBackground.css("left", "calc((100vw - 40px) / 2 + 40px - 80px)");
        $projectsDropdown.css("opacity", 0);
        $(".file-menu").css("width", 40);
        $projectsDropdown.css("transition", "opacity 50ms");
        $projectsDropdown.css("pointer-events", "none");
        $projectsDropdown.css("position", "absolute");
        $searchForm.hide();
        $(".file-form").hide();
        constants.$arrowLeft.hide();
        constants.$arrowRight.show();
        $verticalProjectName.css("transition", "opacity 300ms");
        $verticalProjectName.css("pointer-events", "auto");
        $verticalProjectName.css("opacity", 1);
        $(".tree-preview-wrapper").addClass("tree-preview-wrapper-without-files");
        $(".file-list").hide();
    });
    constants.$arrowRight.click(() => { // show
        showFilesList($projectsDropdown, $searchForm,
            $verticalProjectName, $loaderBackground);
    });
    $verticalProjectName.click(() => {
        showFilesList($projectsDropdown, $searchForm,
            $verticalProjectName, $loaderBackground);
    });
}

function getPageName() {
    return /[^\/]*((?=\?)|(?=\.html))/.exec(window.location.href)[0];
}

function showChooseFile() {
    if (constants.projectName === "uploaded-files") {
        common.showMessage("Choose or upload file");
    } else {
        common.showMessage("Choose file");
    }
}

function showNoDataFound() {
    common.showMessage("No call was registered or all methods took <1ms");
}

function appendInput() {
    const input = templates.tree.fileInput().content;
    // noinspection all
    $(input).insertBefore("#search-file-form");
    $(".file-list").css("height", "calc(100vh - 233px)")
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
            popup.find(".do-confirm").click();
        }
        if (e.keyCode === KEYCODE_ESC) {
            console.log("esc");
            popup.find(".confirm-bg").click();
        }
    });
}

function createDeleteFilePopup(li, liFileName) {
    const popupText = templates.tree.confirm({
        question: "Delete file: " + liFileName + "?"
    }).content;
    const popup = $(popupText);
    $("body").append(popup);
    let wasPopupClicked = false;
    if (liFileName === constants.fileName) {
        popup.find("a").attr("href", "/flamegraph-profiler/" + getPageName() + "?project=" + constants.projectName);
    }
    popup.find(".do-confirm").click(() => {
        deleteFile(popup, li, liFileName);
    });
    popup.find(".do-not-confirm").click(() => {
        popup.remove();
        $(document).unbind("keyup");
    });
    popup.find(".confirm-bg").click(() => {
        if (wasPopupClicked) {
            wasPopupClicked = false;
        } else {
            popup.remove();
            $(document).unbind("keyup");
        }
    });
    popup.find(".confirm-popup").click(() => {
        wasPopupClicked = true;
    });
    bindEscAndCancel(popup);
}

/**
 * @param {Array<{id: String, fullName: String}>} filesList
 * @param $list
 */
function bindDelete(filesList, $list) {
    for (let i = 0; i < filesList.length; i++) {
        const file = filesList[i];
        const $li = $list.find("#" + file.id);
        $li.find("img").click(() => {
            createDeleteFilePopup($li, file.fullName);
        });
    }
}

/**
 * Function is called after checking $file with shift and if $lastSelectedFile is not null
 * @param $selectedFile
 * @param $list
 */
function selectRange($selectedFile, $list) {
    if ($lastSelectedFile === null) { // this should not happen but who knows
        return;
    }
    let start = false;
    let stop = false;
    const id1 = $selectedFile.attr("id");
    const id2 = $lastSelectedFile.attr("id");
    $list.children().each(function () {
        if (stop) {
            return;
        }
        const $file = $(this);
        const id = $file.attr("id");
        if (id === id1 || id === id2) {
            if (!start) {
                start = true;
            } else {
                stop = true;
                return;
            }
        }
        if (start) {
            $file.find("input").prop('checked', true);
        }
    })
}

function listenCheckbox($list) {
    $list.children().each(function () {
        const $file = $(this);
        $file.mousedown((event) => {
            const $checkbox = $file.find("input");
            if (!$checkbox.is(":checked")) { // inversion because click event works strangely
                console.log("checked");
                if (event.shiftKey) {
                    console.log("with shift");
                    if ($lastSelectedFile !== null) {
                        selectRange($file, $list);
                        $lastSelectedFile = $file;
                        return;
                    }
                }
                selectedFiles.add($file);
                $lastSelectedFile = $file;
            } else {
                console.log("unchecked");
                selectedFiles.delete($file);
                $lastSelectedFile = null;
            }
        });
    })
}

/**
 * @param {Array<{id: String, fullName: String}>} filesList
 */
function updateFilesList(filesList) {
    if (filesList.length === 0) {
        $("<p class='no-file-found'>No file was found</p>").appendTo($(".file-menu"));
    } else {
        const listString = templates.tree.listOfFiles({
            fileList: filesList,
            projectName: constants.projectName,
            pageName: getPageName()
        }).content;
        const $list = $(listString);
        listenCheckbox($list);
        $list.appendTo($(".file-menu"));
        bindDelete(filesList, $list);
        if (constants.fileName !== undefined) {
            console.log(constants.fileName);
            // get current file id. Like server forms id's
            $("#" + constants.fileName.split(":").join("").split(".").join("")).addClass("current-file");
        }
    }
    if (constants.projectName === "uploaded-files") {
        appendInput();
        listenInput();
    } else {
        $(".file-list").css("height", "calc(100vh - 145px)")
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
        updateFilesList(fileNames);
    };
    request.send();
}
