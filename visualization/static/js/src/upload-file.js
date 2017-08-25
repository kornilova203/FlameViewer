const VALID_EXTENSION = ["jfr", "ser"];

function appendWrongExtension() {
    if ($(".extension-error").length === 0) {
        $(".file-form").append("<p class='extension-error'>Wrong file extension</p>");
    }
}

/**
 * @param {File} file
 */
function sendToServer(file) {
    common.resizeLoaderBackground(700);
    let message;
    if (common.getExtension(file.name) === "jfr") {
        message = constants.loaderMessages.convertingFile;
    } else {
        message = constants.loaderMessages.uploadingFile;
    }
    common.showLoader(message + file.name, () => {
        const request = new XMLHttpRequest();
        request.onload = () => {
            console.log(request.status);
            if (request.status === 400) {
                common.hideLoader();
                appendWrongExtension();
            } else {
                location.reload();
            }
        };
        request.open("POST", "/flamegraph-profiler/upload-file", true);
        request.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
        request.setRequestHeader('File-Name', file.name);
        request.send(file);
    });
}

function listenInput() {
    $('#file').on('change', (e) => {
        const reader = new FileReader();
        const theFile = e.target.files[0];
        reader.onload = ((file) => {
            common.hideMessage();
            sendToServer(file);
        })(theFile);
    });
}
