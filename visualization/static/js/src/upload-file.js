function updateUploadedFilesList(filesList) {
    if (filesList.length === 0) {
        $("<p class='no-file-found'>No file was found</p>").appendTo($("main"));
    } else {
        const list = templates.tree.listOfFiles({
            fileNames: filesList,
            projectName: "uploaded-files",
            pageName: "outgoing-calls"
        }).content;
        $(list).appendTo($("main"));
        $("#" + currentFileName.replace(/\./, "\\.")).addClass("current-file");
    }
}

$(window).on("load", function () {
    getFilesList("uploaded-files", updateUploadedFilesList);
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