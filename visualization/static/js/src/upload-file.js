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

    uploadFile() {
        if (this.file.size === 0) {
            common.showError("File is empty");
            return;
        }
        const bytesInMB = 1000000;
        const fileSizeMegabytes = this.file.size / bytesInMB;
        common.resizeLoaderBackground(700);
        common.showLoader(this.loaderMessage + this.file.name, () => {
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
                        this.endFileUpload(success)
                    }
                };
                request.open("POST", "/flamegraph-profiler/upload-file", true);
                request.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
                request.setRequestHeader('File-Name', this.file.name);
                request.setRequestHeader('File-Part', (i + 1) + "/" + partsCount);
                request.send(this.file.slice(i * bytesInMB * 100, Math.min((i + 1) * bytesInMB * 100, this.file.size)));
            }
        });
    }

    endFileUpload(success) {
        common.hideLoader();
        if (success) {
            console.log("File was sent");
            this.checkIfFileWasUploaded();
        } else {
            common.showError("File was not sent");
            console.error("File was not sent");
        }
    }

    checkIfFileWasUploaded() {
        const request = new XMLHttpRequest();
        request.onload = () => {
            if (request.status === 200) {
                redirectToFile(this.file.name);
            } else {
                common.showError("File format is unsupported");
                console.error("File was not uploaded");
            }
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

    uploadFile() {
        super.uploadFile();
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
        new JfrUploader(file).uploadFile();
    } else {
        new FileUploader(file).uploadFile();
    }
}

function appendInput($fileList) {
    const input = templates.tree.fileInput().content;
    $(input).insertBefore(".file-list-actions");
    $fileList.css("height", "calc(100vh - 217px)")
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
