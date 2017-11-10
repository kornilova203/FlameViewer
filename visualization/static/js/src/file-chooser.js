const KEYCODE_ESC = 27;
const KEYCODE_ENTER = 13;
const KEYCODE_DELETE = 46;

/**
 * @type {string|null}
 */
let lastSelectedFileId = null;
/**
 * @type {string|null}
 */
let lastDeselectedFileId = null;

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
    $(input).insertBefore(".file-list-actions");
    $(".file-list").css("height", "calc(100vh - 217px)")
}

/**
 * @param {number} KEY_CODE
 * @param {function} callback
 */
function bindKey(KEY_CODE, callback) {
    // noinspection JSUnresolvedFunction
    $(document).keyup(function (e) {
        if (e.keyCode === KEY_CODE) {
            callback();
        }
    });
}

/**
 * @param {Set<string>} selectedFilesIds
 * @param {Array<{id: String, fullName: String}>} filesList
 */
function getFileNames(selectedFilesIds, filesList) {
    const arr = [];
    selectedFilesIds.forEach((id) => {
        for (let i = 0; i < filesList.length; i++) {
            if (filesList[i].id === id) {
                arr.push(filesList[i].fullName);
                return;
            }
        }
    });
    return arr;
}

/**
 * @return {Array} array of html representations of these files
 * (they are used to undo remove)
 */
function removeFromList($list, selectedFilesIds) {
    const removedFiles = [];
    selectedFilesIds.forEach((id) => {
        const $removedFile = $list.find("#" + id);
        removedFiles.push($removedFile);
        $removedFile.remove();
    });
    return removedFiles;
}

/**
 * @param $list
 * @param {Array} removedFiles
 */
function addToList($list, removedFiles) {
    for (let i = 0; i < removedFiles.length; i++) {
        const $file = removedFiles[i];
        $file.find("input").prop('checked', false);
        $list.append($file);
    }
}

/**
 * @param {Array} fileNames
 */
function sendRequestUndoDelete(fileNames) {
    for (let i = 0; i < fileNames.length; i++) {
        const request = new XMLHttpRequest();
        request.open("POST", "/flamegraph-profiler/undo-delete-file", true);
        request.setRequestHeader('File-Name', fileNames[i]);
        request.setRequestHeader('Project-Name', constants.projectName);
        request.send();
    }
}

/**
 * @param fileNames
 * @param removedFiles
 * @param $list
 * @param selectedFilesIds
 */
function showUndoDelete(fileNames, removedFiles, $list, selectedFilesIds) {
    const $undoDeleteButton = $(".undo-delete");
    $undoDeleteButton.addClass("visible");
    setTimeout(() => {
        $undoDeleteButton.removeClass("visible");
        $undoDeleteButton.off();
    }, 4000);

    $undoDeleteButton.click(() => { // undo delete
        sendRequestUndoDelete(fileNames);
        addToList($list, removedFiles);
        for (let i = 0; i < removedFiles.length; i++) {
            bindCheckboxEvent(removedFiles[i], selectedFilesIds, $list)
        }
        $undoDeleteButton.off();
        $undoDeleteButton.removeClass("visible");
    });
}

/**
 * @param $list
 * @param {Set} selectedFilesIds
 * @param filesList
 * @return {function()}
 */
function deleteSelectedFilesDecorator($list, selectedFilesIds, filesList) {
    return () => {
        if (selectedFilesIds.size === 0) {
            return;
        }

        const fileNames = getFileNames(selectedFilesIds, filesList);
        const removedFiles = removeFromList($list, selectedFilesIds);
        for (let i = 0; i < fileNames.length; i++) {
            const request = new XMLHttpRequest();
            request.open("POST", "/flamegraph-profiler/delete-file", true);
            request.setRequestHeader('File-Name', fileNames[i]);
            request.setRequestHeader('Project-Name', constants.projectName);
            request.send();
        }
        showUndoDelete(fileNames, removedFiles, $list, selectedFilesIds);
        selectedFilesIds.clear();
        lastDeselectedFileId = null;
        lastSelectedFileId = null;
        constants.$removeFilesButton.removeClass("active-gray-button");
    }
}

/**
 * @param $list
 * @param {Set<string>} selectedFilesIds
 * @param {Array<{id: String, fullName: String}>} filesList
 */
function bindDelete($list, selectedFilesIds, filesList) {
    const deleteSelectedFiles = deleteSelectedFilesDecorator($list, selectedFilesIds, filesList);
    bindKey(KEYCODE_DELETE, deleteSelectedFiles);
    constants.$removeFilesButton.click(() => {
        deleteSelectedFiles();
    });
}

/**
 * Function is called after checking $file with shift and if previouslyToggledFileId is not null
 * @param $toggledFile
 * @param $list
 * @param {Set<string>} selectedFilesIds
 * @param {string} previouslyToggledFileId
 * @param {function} callback
 */
function doSmthWithRange($toggledFile, $list, selectedFilesIds, previouslyToggledFileId, callback) {
    if (previouslyToggledFileId === null) { // this should not happen but who knows
        return;
    }
    let start = false;
    let stop = false;
    const id1 = $toggledFile.attr("id");
    const id2 = previouslyToggledFileId;
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
            }
        }
        if (id === id1) { // this will be checked automatically
            return;
        }
        if (start) {
            callback($file, selectedFilesIds);
        }
    });
}

function selectFile($file, selectedFilesIds) {
    $file.find("input").prop('checked', true);
    selectedFilesIds.add($file.attr("id"));
}

/**
 * @param $file
 * @param {Set<string>} selectedFilesIds
 */
function deselectFile($file, selectedFilesIds) {
    $file.find("input").prop('checked', false);
    selectedFilesIds.delete($file.attr("id"));
}

/**
 * @param $file
 * @param {Set} selectedFilesIds
 * @param $list
 */
function bindCheckboxEvent($file, selectedFilesIds, $list) {
    const fileId = $file.attr("id");
    const $checkbox = $file.find("input");
    const $label = $file.find("label");
    $label.click((event) => {
        console.log(lastSelectedFileId);
        if (!$checkbox.is(":checked")) { // inversion because click event works strangely
            console.log("checked");
            constants.$removeFilesButton.addClass("active-gray-button");
            if (event.shiftKey && lastSelectedFileId !== null) { // select range
                doSmthWithRange($file, $list, selectedFilesIds, lastSelectedFileId, selectFile);
            }
            lastSelectedFileId = fileId;
            lastDeselectedFileId = null;
            selectedFilesIds.add(fileId);
        } else {
            console.log("unchecked");
            if (event.shiftKey && lastDeselectedFileId !== null) { // deselect range
                doSmthWithRange($file, $list, selectedFilesIds, lastDeselectedFileId, deselectFile);
            }
            selectedFilesIds.delete(fileId);
            lastSelectedFileId = null;
            lastDeselectedFileId = fileId;
            if (selectedFilesIds.size === 0) {
                constants.$removeFilesButton.removeClass("active-gray-button");
            }
        }
    });
}

/**
 * @param $list
 * @param {Array<{id: String, fullName: String}>} filesList
 */
function listenCheckbox($list, filesList) {
    /**
     * Save ids because jquery instances of same element are different
     * @type {Set<string>}
     */
    const selectedFilesIds = new Set();

    $list.children().each(function () {
        const $file = $(this);
        bindCheckboxEvent($file, selectedFilesIds, $list);
    });

    bindDelete($list, selectedFilesIds, filesList);
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
        listenCheckbox($list, filesList);
        $list.appendTo($(".file-menu"));
        if (constants.fileName !== undefined) {
            // get current file id. Like server forms id's
            $("#" + constants.fileName.split(":").join("").split(".").join("")).addClass("current-file");
        }
    }
    if (constants.projectName === "uploaded-files") {
        appendInput();
        listenInput();
    } else {
        $(".file-list").css("height", "calc(100vh - 126px)")
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
