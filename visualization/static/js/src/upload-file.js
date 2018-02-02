const JFR = "jfr";

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

    tryToUploadFile() {
        if (this.file.size === 0) {
            common.showError("File is empty");
            return;
        }
        /* upload file if it was not previously uploaded */
        this.checkIfFileWasUploaded(FileUploader.uploadFile)
    }

    /**
     * @param {FileUploader} that
     * @param {boolean} ifWasFound
     */
    static uploadFile(that, ifWasFound) {
        if (ifWasFound) { // if file with this name exist
            common.showError("File already exists");
        } else {
            const bytesInMB = 1000000;
            const fileSizeMegabytes = that.file.size / bytesInMB;
            common.resizeLoaderBackground(700);
            common.showLoader(that.loaderMessage + that.file.name, () => {
                /* send file by 100MB parts because IDEA server does not allow to send large files */
                const partsCount = Math.ceil(fileSizeMegabytes / 100);
                let countFilesSent = 0; // how many parts were received by server
                let success = true; // if all parts were successfully sent
                for (let i = 0; i < partsCount; i++) {
                    const request = new XMLHttpRequest();
                    request.onload = () => {
                        countFilesSent++;
                        if (request.status !== 200) { // if something went wrong during upload
                            success = false;
                        }
                        if (countFilesSent === partsCount) { // if all parts uploaded
                            that.endFileUpload(success)
                        }
                    };
                    request.open("POST", "/flamegraph-profiler/upload-file", true);
                    request.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
                    request.setRequestHeader('File-Name', that.file.name);
                    request.setRequestHeader('File-Part', (i + 1) + "/" + partsCount);
                    request.send(that.file.slice(i * bytesInMB * 100, Math.min((i + 1) * bytesInMB * 100, that.file.size)));
                }
            });
        }
    }

    endFileUpload(success) {
        common.hideLoader();
        if (success) {
            console.log("File was sent");
            this.checkIfFileWasUploaded(FileUploader.reloadToNewFile);
        } else {
            common.showError("File was not sent");
            console.error("File was not sent");
        }
    }

    /**
     * @param {FileUploader} that
     * @param {boolean} ifFileWasFound
     */
    static reloadToNewFile(that, ifFileWasFound) {
        if (ifFileWasFound) {
            redirectToFile(that.file.name);
        } else {
            common.showError("File format is unsupported");
            console.error("File was not uploaded");
        }
    }

    /**
     * @param {Function} callback
     */
    checkIfFileWasUploaded(callback) {
        const request = new XMLHttpRequest();
        request.onload = () => {
            callback(this, request.status === 302); // if was found
        };
        request.open("GET", "/flamegraph-profiler/does-file-exist", true);
        request.setRequestHeader('File-Name', this.file.name);
        request.send();
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
}

/**
 * Opens a page with uploaded file
 * @param name
 */
function redirectToFile(name) {
    window.location.href = `/flamegraph-profiler/${constants.pageName}?file=${encodeURIComponent(name)}&project=${encodeURIComponent(constants.projectName)}`
}

/**
 * @param {File} file
 */
function sendToServer(file) {
    if (common.getExtension(file.name) === JFR) {
        new JfrUploader(file).tryToUploadFile();
    } else {
        new FileUploader(file).tryToUploadFile();
    }
}

function showSupportedFormats($input) {
    const $button = $input.find(".supported-formats-help");
    const $info = $input.find(".supported-formats");
    const $closeButton = $input.find(".close-supported-formats");

    $button.click(() => {
        /* toggle visibility */
       if ($info.hasClass("visible")) {
           $info.removeClass("visible");
       } else {
           $info.addClass("visible");
       }
    });

    $closeButton.click(() => {
        $info.removeClass("visible");
    });
}

function appendInput($fileList) {
    const $input = $(templates.tree.fileInput().content);
    $input.insertBefore(".file-list-actions");
    $fileList.css("height", "calc(100vh - 190px)");
    showSupportedFormats($input);
}

function listenInput() {
    $('#file').on('change', (e) => {
        const reader = new FileReader();
        const theFile = e.target.files[0];
        // noinspection JSValidateTypes
        reader.onload = ((file) => {
            if (file !== undefined) { // file is undefined if user clicked 'cancel'
                common.hideMessage();
                sendToServer(file);
            }
        })(theFile);
    });
}
