$(window).on("load", function () {
    getFilesList();
    listenInput();
});

function appendWrongExtension() {
    if ($(".extension-error").length === 0) {
        $(".file-form").append("<p class='extension-error'>Wrong file extension</p>");
    }
}

function removeWrongExtension() {
    $(".extension-error").remove();
}

function sendFile(file) {
    const request = new XMLHttpRequest();
    request.onreadystatechange = (e) => {
        switch (e.currentTarget.status) {
            case 404:
                appendWrongExtension();
                break;
            case 200:
                removeWrongExtension();
                break;
        }
    };
    request.open("POST", "/flamegraph-profiler/upload-file", true);
    request.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    request.setRequestHeader('File-Name', file.name);
    request.send(file);

}

function listenInput() {
    $('#file').on('change', function (e) {
        const file = e.target.files[0]; // FileList object
        const reader = new FileReader();

        reader.onload = (function (theFile) {
            sendFile(theFile);
        })(file);
    });
}

function getFilesList() {
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
}