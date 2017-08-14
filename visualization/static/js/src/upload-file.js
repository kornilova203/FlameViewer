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
    common.showLoader(constants.loaderMessages.convertingFile + file.name, () => {
        const request = new XMLHttpRequest();
        request.onload = () => {
            location.reload(); // TODO: reload to uploaded file
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
            if (isValidExtension(file.name)) {
                common.hideMessage();
                sendToServer(file);
            } else {
                common.hideLoader();
                appendWrongExtension();
            }
        })(theFile);
    });
}

/**
 * @param {String} fileName
 * @return {Boolean}
 */
function isValidExtension(fileName) {
    let dot = fileName.lastIndexOf('.');
    if (dot === -1) {
        return false;
    }
    const extension = fileName.substring(dot + 1, fileName.length);
    return VALID_EXTENSION.some(validExtension => validExtension === extension);
}