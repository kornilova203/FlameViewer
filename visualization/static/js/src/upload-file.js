const JFR = "jfr";

function appendWrongExtension() {
    if ($(".extension-error").length === 0) {
        $(".file-form").append("<p class='extension-error'>Wrong file extension</p>");
    }
}

class FileUploader {
    /**
     * @param {File} file
     */
    constructor(file) {
        /**
         * @type {File}
         */
        this.file = file;
        /**
         * @type {String}
         */
        this.loaderMessage = constants.loaderMessages.uploadingFile;
    }

    uploadFile() {
        common.resizeLoaderBackground(700);
        common.showLoader(this.loaderMessage + this.file.name, () => {
            const request = new XMLHttpRequest();
            request.onload = () => {
                if (request.status === 400) {
                    common.hideLoader();
                    appendWrongExtension();
                } else {
                    // location.reload();
                }
            };
            request.open("POST", "/flamegraph-profiler/upload-file", true);
            request.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
            request.setRequestHeader('File-Name', this.file.name);
            request.send(this.file);
        });
    }
}

class JfrUploader extends FileUploader {
    /**
     * @param {File} file
     */
    constructor(file) {
        super(file);
        this.loaderMessage = constants.loaderMessages.convertingFile;
    }

    uploadFile() {

        super.uploadFile();
    }
}

/**
 * @param {File} file
 */
function sendToServer(file) {
    if (common.getExtension(file.name) === JFR) {
        new JfrUploader(file).uploadFile();
    } else {
        new FileUploader(file).uploadFile();
    }
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
