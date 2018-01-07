const KEYCODE_DELETE = 46;

class FilesListManager {
    /**
     * @param {String} projectName
     * @param $fileList a place to append project list
     */
    constructor(projectName, $fileList) {
        this.projectName = projectName;
        this.$fileList = $fileList;

        /**
         * array of jquery objects
         * each object also contains fileName, id and date
         * @type {Array}
         */
        this.filesArray = [];

        /**
         * @type {String|null}
         */
        this.lastSelectedFileId = null;
        /**
         * @type {String|null}
         */
        this.lastDeselectedFileId = null;

        this.selectedFilesIds = new Set();

        /**
         * @type {string}
         */
        this.currentSearch = "";

        this.filesSearchInput = $("#search-file-form").find("input");

        this.bindDelete();
        this.enableFilesSearch();

        if (constants.projectName === "uploaded-files") {
            appendInput(this.$fileList); // 'upload new file' input
            listenInput();
        } else {
            $fileList.css("height", "calc(100vh - 126px)")
        }
    }

    bindDelete() {
        const deleteSelectedFiles = this.deleteSelectedFilesDecorator(this.$fileList, this.selectedFilesIds,
            this.filesArray, this);
        bindKey(KEYCODE_DELETE, deleteSelectedFiles);
        constants.$removeFilesButton.click(() => {
            deleteSelectedFiles();
        });
    }

    /**
     * @param $list
     * @param {Set} selectedFilesIds
     * @param {Array} filesArray array of jquery objects that contain id and fullName
     * @param filesListManager
     * @return {function()}
     */
    deleteSelectedFilesDecorator($list, selectedFilesIds, filesArray, filesListManager) {
        return () => {
            if (selectedFilesIds.size === 0) {
                return;
            }

            const fileNames = this.getFileNames(selectedFilesIds, filesArray);
            FilesListManager.removeFromLists($list, selectedFilesIds, filesArray);
            for (let i = 0; i < fileNames.length; i++) {
                const request = new XMLHttpRequest();
                request.open("POST", "/flamegraph-profiler/delete-file", true);
                request.setRequestHeader('File-Name', fileNames[i]);
                request.setRequestHeader('Project-Name', constants.projectName);
                request.send();
            }
            this.showUndoDelete(fileNames, $list, selectedFilesIds);
            selectedFilesIds.clear();
            filesListManager.lastDeselectedFileId = null;
            filesListManager.lastSelectedFileId = null;
            constants.$removeFilesButton.removeClass("active-gray-button");
            this.enableFilesInput();
        }
    }

    /**
     * @param $list
     * @param selectedFilesIds
     * @param {Array} filesArray
     */
    static removeFromLists($list, selectedFilesIds, filesArray) {
        selectedFilesIds.forEach((id) => {
            const $removedFile = $list.find("#" + id);
            $removedFile.remove();
            /* remove from filesArray */
            for (let i = 0; i < filesArray.length; i++) {
                if (filesArray[i].id === id) {
                    filesArray.splice(i, 1);
                    break;
                }
            }
        });
    }

    /**
     * @param fileNames
     * @param $list
     * @param selectedFilesIds
     */
    showUndoDelete(fileNames, $list, selectedFilesIds) {
        const $undoDeleteButton = $(".undo-delete");
        $(".search-form").removeClass("visible");
        $undoDeleteButton.addClass("visible");
        setTimeout(() => {
            $undoDeleteButton.removeClass("visible");
            $(".search-form").addClass("visible");
            $undoDeleteButton.off();
        }, 4000);

        $undoDeleteButton.click(() => { // undo delete
            for (let i = 0; i < fileNames.length; i++) {
                const request = new XMLHttpRequest();
                request.open("POST", "/flamegraph-profiler/undo-delete-file", true);
                request.setRequestHeader('File-Name', fileNames[i]);
                request.setRequestHeader('Project-Name', constants.projectName);
                request.onload = () => {
                    this.updateFilesList();
                };
                request.send();
            }
            $undoDeleteButton.off();
            $undoDeleteButton.removeClass("visible");
            $(".search-form").addClass("visible");
        });
    }

    /**
     * @param {Set<string>} selectedFilesIds
     * @param {Array} filesArray array of jquery objects that contain id and fullName
     */
    getFileNames(selectedFilesIds, filesArray) {
        const arr = [];
        selectedFilesIds.forEach((id) => {
            for (let i = 0; i < filesArray.length; i++) {
                if (filesArray[i].id === id) {
                    arr.push(filesArray[i].fullName);
                    return;
                }
            }
        });
        return arr;
    }

    /**
     * @param {Array<{id: String, fullName: String}>} filesNames
     */
    appendToList(filesNames) {
        for (let i = filesNames.length - 1; i >= 0; i--) {
            const fileName = filesNames[i];
            if (!this.hasThisFile(fileName.id)) {
                this.appendFile(fileName)
            }
        }
        if (this.filesArray.length === 0) {
            $("<p class='no-file-found'>No file was found</p>").appendTo($(".file-menu"));
        }
        if (constants.fileName !== undefined) {
            // get current file id. Like server forms id's
            $("#" + constants.fileName.split(":").join("").split(".").join("")).addClass("current-file");
        }
    }

    /**
     * Gets list of files from server
     * appends new files to list of files
     */
    updateFilesList() {
        const request = new XMLHttpRequest();
        request.open("GET", "/flamegraph-profiler/file-list?project=" + this.projectName, true);
        request.responseType = "json";

        request.onload = () => {
            const fileNames = request.response;
            this.appendToList(fileNames);
        };
        request.send();
    }

    showFullNameOnHover($file) {
        $file.mouseenter(() => {
            constants.$fullFileName.find("*").remove();
            constants.$fullFileName.append($file.find("p").clone());
            constants.$fullFileName.offset({top: $file.offset().top});
            constants.$fullFileName.addClass("visible-without-pointer-events");
        });

        $file.mouseleave(() => {
            constants.$fullFileName.removeClass("visible-without-pointer-events");
        });

        this.$fileList.scroll(() => {
            constants.$fullFileName.removeClass("visible-without-pointer-events");
        });
    }

    /**
     * @param {String} date1
     * @param {String} date2
     */
    static isLater(date1, date2) {
        const year1 = FilesListManager.getYear(date1);
        const year2 = FilesListManager.getYear(date2);
        if (year1 > year2) {
            return true;
        } else if (year1 < year2) {
            return false;
        }
        const month1 = FilesListManager.getMonth(date1);
        const month2 = FilesListManager.getMonth(date2);
        if (month1 > month2) {
            return true;
        } else if (month1 < month2) {
            return false;
        }
        const day1 = FilesListManager.getDay(date1);
        const day2 = FilesListManager.getDay(date2);
        if (day1 > day2) {
            return true;
        } else if (day1 < day2) {
            return false;
        }
        const time1 = FilesListManager.getTime(date1);
        const time2 = FilesListManager.getTime(date2);
        if (time1 > time2) {
            return true;
        } else if (time1 < time2) {
            return false;
        }
        return false;
    }

    static getYear(date) {
        return parseInt(date.split(' ')[0].split('.')[2]);
    }

    static getMonth(date) {
        return parseInt(date.split(' ')[0].split('.')[1]);
    }

    static getDay(date) {
        return parseInt(date.split(' ')[0].split('.')[0]);
    }

    static getTime(date) {
        return date.split(' ')[1];
    }

    appendFile(fileName) {
        const $file = $(templates.tree.file({
            file: fileName,
            projectName: constants.projectName,
            pageName: getPageName()
        }).content);
        if (!fileName.fullName.toLowerCase().includes(this.currentSearch)) { // if is excluded by search
            $file.hide();
        }
        let wasInserted = false;
        for (let i = 0; i < this.filesArray.length; i++) {
            if (FilesListManager.isLater(fileName.date, this.filesArray[i].date)) {
                $file.insertBefore($(`#${this.filesArray[i].id}`));
                this.filesArray.splice(i, 0, $file); // insert before
                wasInserted = true;
                break;
            }
        }
        if (!wasInserted) {
            /* insert to end */
            this.filesArray.push($file);
            $file.appendTo(this.$fileList);
        }
        $file.id = fileName.id; // save id
        $file.fullName = fileName.fullName; // save full name
        $file.date = fileName.date; // save date
        this.listenCheckbox($file);
        this.showFullNameOnHover($file);
    }

    /**
     * @param $file jquery object that also contains id and fullName
     */
    listenCheckbox($file) {
        const $checkbox = $file.find("input");
        const $label = $file.find("label");
        $label.click((event) => {
            if (!$checkbox.is(":checked")) { // inversion because click event works strangely
                constants.$removeFilesButton.addClass("active-gray-button"); // enable delete button
                if (event.shiftKey && this.lastSelectedFileId !== null) { // select range
                    this.doActionWithRange($file, this.lastSelectedFileId, FilesListManager.selectFile, this.selectedFilesIds);
                }
                this.selectedFilesIds.add($file.id);
                this.lastSelectedFileId = $file.id;
                this.lastDeselectedFileId = null;
            } else {
                if (event.shiftKey && this.lastDeselectedFileId !== null) { // deselect range
                    this.doActionWithRange($file, this.lastDeselectedFileId, FilesListManager.deselectFile, this.selectedFilesIds);
                }
                this.selectedFilesIds.delete($file.id);
                this.lastSelectedFileId = null;
                this.lastDeselectedFileId = $file.id;
                if (this.selectedFilesIds.size === 0) {
                    constants.$removeFilesButton.removeClass("active-gray-button");
                }
            }
            /* allow files search change only if none file is selected */
            if (this.selectedFilesIds.size === 0) {
                this.enableFilesInput();
            } else {
                this.disableFilesInput();
            }
        });
    }

    enableFilesInput() {
        this.filesSearchInput.removeClass("disabled-field"); // enable files search
    }

    disableFilesInput() {
        this.filesSearchInput.addClass("disabled-field"); // disable files search
    }

    /**
     * Function is called after checking $file with shift and if previouslyToggledFileId is not null
     * @param $toggledFile
     * @param {String} previouslyToggledFileId
     * @param {function} callback
     * @param selectedFilesIds
     */
    doActionWithRange($toggledFile, previouslyToggledFileId, callback, selectedFilesIds) {
        if (previouslyToggledFileId === null) { // this should not happen but who knows
            return;
        }
        let start = false;
        let stop = false;
        const id1 = $toggledFile.id;
        const id2 = previouslyToggledFileId;
        this.$fileList.children().each(function () {
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
            if (id === id1) { // this will be checked or unchecked automatically
                return;
            }
            if (start) {
                if ($file.css("display") !== "none") { // if it is not hidden by search
                    callback($file, selectedFilesIds);
                }
            }
        });
    }

    static selectFile($file, selectedFilesIds) {
        $file.find("input").prop('checked', true);
        selectedFilesIds.add($file.attr("id"));
    }

    /**
     * @param $file
     * @param selectedFilesIds
     */
    static deselectFile($file, selectedFilesIds) {
        $file.find("input").prop('checked', false);
        selectedFilesIds.delete($file.id);
    }

    hasThisFile(fileId) {
        for (let i = 0; i < this.filesArray.length; i++) {
            if (this.filesArray[i].id === fileId) {
                return true;
            }
        }
        return false;
    }

    /**
     * Enable files search
     */
    enableFilesSearch() {
        this.filesSearchInput.on("change keyup copy paste cut", () => {
            this.currentSearch = this.filesSearchInput.val().toLowerCase();
            for (let i = 0; i < this.filesArray.length; i++) {
                const fullName = this.filesArray[i].fullName.toLowerCase();
                if (fullName.includes(this.currentSearch)) { // if file is in search result
                    this.filesArray[i].show();
                } else {
                    this.filesArray[i].hide();
                }
            }
        });
    }
}

$(window).on("load", () => {
    const filesListManager = new FilesListManager(constants.projectName, $(".file-list"));
    filesListManager.updateFilesList();

    /* check for new files */
    setInterval(() => {
        filesListManager.updateFilesList();
    }, 1000);

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

function showProjectsOnClick($projectsDropdown) {
    const $content = $projectsDropdown.find(".projects-dropdown-content");
    let isOpen = false;

    function closeContent() {
        isOpen = false;
        $content.off();
        $content.hide();
        $projectsDropdown.unbind("mouseleave");
    }

    $projectsDropdown.click(() => {
        if (!isOpen) {
            $content.show();
            isOpen = true;
            $projectsDropdown.mouseleave(closeContent)
        } else {
            closeContent();
        }
    });
}

function enableHideFilesList() {
    const $projectsDropdown = $(".projects-dropdown");
    showProjectsOnClick($projectsDropdown);

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
