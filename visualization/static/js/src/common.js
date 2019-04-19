const common = {
    /**
     * @param {String} message
     * @param {number} loaderWidth
     * @param {Function} callback
     * @param {number} [time]
     */
    showLoader: (message, loaderWidth, callback, time = 100) => {
        common._resizeLoaderBackground(loaderWidth);
        setTimeout(() => {
            constants.$loaderMessageP.text(message);
            constants.$loaderBackground.fadeIn(time, callback);
        }, 5);
    },

    /**
     * Get extension of file
     * @param {string} fileName
     * @returns {string}
     */
    getExtension: (fileName) => {
        return fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length);
    },

    /**
     * @param {Number} [time]
     */
    hideLoader: (time = 0) => {
        constants.$loaderBackground.fadeOut(time);
    },

    /**
     * @param {number} width
     */
    _resizeLoaderBackground: (width) => {
        let fileMenuWidth = common.getElementWidth($(".file-menu"));
        $(".loader-background").css({
            "width": width,
            "left": `calc((100vw - ${fileMenuWidth}px) / 2 + ${fileMenuWidth}px - ${width / 2}px)`
        })
    },

    /**
     * @param {String} parameterName
     * @return {undefined|string}
     */
    getParameter: (parameterName) => {
        const parametersString = window.location.href.split("?")[1];
        if (parametersString === undefined) {
            return undefined;
        } else {
            const parameters = parametersString.split("&");
            for (let i = 0; i < parameters.length; i++) {
                if (parameters[i].startsWith(parameterName + "=")) {
                    return decodeURIComponent(parameters[i].substring(
                        parameters[i].indexOf("=") + 1,
                        parameters[i].length
                    ));
                }
            }
        }
        return undefined;
    },

    /**
     * @param {string} message
     * @param {string} align
     */
    showMessage: (message, align = "center") => {
        $("body").append(`<p class='message'>${message}</p>`);
        const $message = $(".message");
        $message.css("text-align", align);
        if (align === "left") {
            $message.css("padding-left", "700px");
        } else {
            $message.css("padding-left", ""); // clear property
        }
    },

    hideMessage: () => {
        $('.message').remove();
    },

    escapeRegExp: (text) => {
        return text.replace(/[-[\]{}()+?.,\\^$|#\s]/g, '\\$&');
    },

    roundRelativeTime: (time) => {
        if (time * 100 > 1) {
            return Math.round(time * 1000) / 10;
        } else {
            return Math.round(time * 10000) / 100;
        }
    },

    /**
     * Do callback not more often than updateTime ms
     * @param {number} updateTime
     * @param {function} callback
     * @return {function}
     */
    updateRareDecorator: (updateTime, callback) => {
        let lastUpdateTime = 0;
        return () => {
            setTimeout(() => {
                if (new Date().getTime() - lastUpdateTime < updateTime) {
                    return;
                }
                callback();
                lastUpdateTime = new Date().getTime();
            }, updateTime)
        }
    },

    /**
     * @param {Object} parameters
     * @return {string}
     */
    getParametersString: (parameters) => {
        const keys = Object.keys(parameters);
        let string = "";
        for (let i = 0; i < keys.length; i++) {
            const key = keys[i];
            const value = parameters[key];
            if (value instanceof Array) {
                for (let i = 0; i < value.length; i++) {
                    string += `&${encodeURIComponent(key)}=${encodeURIComponent(value[i])}`;
                }
            } else {
                string += `&${encodeURIComponent(key)}=${encodeURIComponent(value)}`;
            }
        }
        return string.substring(1, string.length); // remove first '&'
    },

    /**
     * Show error message to user
     * @param {String} errorMessage
     */
    showError: (errorMessage) => {
        const errorMessageBlock = $(".error-message-block");
        errorMessageBlock.find("p").text(errorMessage);
        errorMessageBlock.addClass("visible");
        setTimeout(() => {
            errorMessageBlock.removeClass("visible");
        }, 5000);
    },

    /**
     * @param {String} fileName
     * @param {Function} callbackIfExists
     * @param {Function} callbackIfDoesNotExist
     */
    doCallbackIfFileExists: (fileName, callbackIfExists, callbackIfDoesNotExist = undefined) => {
        common.sendGetRequest(serverNames.DOES_FILE_EXIST, "json", {'File-Name': fileName})
            .then(response => {
                if (response.result === true) callbackIfExists();
                else if (callbackIfDoesNotExist !== undefined) callbackIfDoesNotExist();
            })
            .catch(() => {
                if (callbackIfDoesNotExist !== undefined) callbackIfDoesNotExist()
            });
    },

    handleError: (e) => {
        console.error(e);
        const msg = "" + e;
        if (msg.indexOf("Value is not a valid ByteString") !== -1) {
            common.showError("File name should contain only ascii characters");
        }
        else {
            common.showError(msg);
        }
        setTimeout(() => { // it does not work without timeout
            constants.$loaderBackground.hide();
        }, 50);
    },

    /**
     * Rescales canvas for retina displays
     * @return {createjs.Stage}
     */
    createStage: (canvasName) => {
        const canvas = document.getElementById(canvasName);
        const ratio = window.devicePixelRatio || 1;
        const stage = new createjs.Stage(canvas);
        if (ratio !== 1) {
            const width = canvas.width;
            const height = canvas.height;
            canvas.width = width * ratio;
            canvas.height = height * ratio;
            canvas.style.width = width + "px";
            canvas.style.height = height + "px";
            stage.scaleX = ratio;
            stage.scaleY = ratio;
            stage.update();
        }
        stage.id = canvasName;
        return stage;
    },

    /**
     * @param {String} url
     * @param {XMLHttpRequestResponseType} responseType
     * @param {Object} headers
     * @return {Promise}
     */
    sendGetRequest: (url, responseType, headers = {}) => {
        return new Promise(function (resolve, reject) {
            const request = new XMLHttpRequest();
            request.open("GET", url, true);
            request.responseType = responseType;

            const headerKeys = Object.keys(headers);
            for (let i = 0; i < headerKeys.length; i++) {
                let header = headerKeys[i];
                request.setRequestHeader(header, headers[header]);
            }

            request.onload = () => {
                if (request.status === 200) {
                    resolve(request.response);
                } else {
                    reject(new Error("Request status is " + request.status + ". Response: " + request.response))
                }
            };
            request.send();
        }).catch(e => {
            common.handleError(e);
            throw e;
        });
    },

    /**
     * @param $element
     * @return {number}
     */
    getElementWidth: ($element) => {
        if ($element.css("display") === "none") return 0;
        const string = $element.css("width");
        return Number.parseInt(string.substring(0, string.length - 2));
    }
};

const _LOADER_BACKGROUND_WIDTH = 160;


// noinspection JSValidateTypes
const constants = {
    $main: null,
    $treePreviewWrapper: null,
    $loaderBackground: null,
    $loaderMessageP: null,
    $arrowLeft: null,
    $arrowRight: null,
    fileName: common.getParameter("file") === undefined ? undefined : decodeURIComponent(common.getParameter("file")),
    $removeFilesButton: null,
    $fullFileName: null,
    pageName: parsePageName(window.location.href),
    CANVAS_PADDING: 55,
    LAYER_HEIGHT: 18,
    LAYER_GAP: 1,
    POPUP_MARGIN: 6, // have no idea why there is a gap between popup and canvas
    METHOD_HEADER_HEIGHT: 80,
    FILE_MENU_WIDTH: 250,
    LOADER_BACKGROUND_WIDTH: _LOADER_BACKGROUND_WIDTH,
    loaderMessages: {
        drawing: {msg: "Drawing...", width: _LOADER_BACKGROUND_WIDTH},
        deserialization: {msg: "Deserialization of binary data...", width: 300},
        buildingTree: {msg: "Building tree...", width: _LOADER_BACKGROUND_WIDTH},
        buildingTrees: {msg: "Building trees...", width: _LOADER_BACKGROUND_WIDTH},
        countingTime: {msg: "Counting self-time of methods...", width: 300},
        uploadingFile: {msg: "Uploading file: ", width: 500}
    },
    pageMessages: {
        backtracesTooBig: "Sorry... this tree contains too many nodes :(</br>" +
            "But you can see back traces for any method, there are two ways to do it:</br>" +
            "* Open Call Traces page, click on back traces icon on method popup</br>" +
            "* Open Hot Spots page, click on back traces icon beside any method that you like",
        chooseOrUploadFile: "Choose or upload file",
        noCallRegistered: "No call was registered or all methods took <1ms",
        callTreeUnavailable: "Call tree is unavailable for this file"
    }
};

/**
 * @param {string} href
 */
function parsePageName(href) {
    const question = href.lastIndexOf("?");
    if (question !== -1) {
        href = href.substring(0, question)
    }
    const lastSlash = href.lastIndexOf("/");
    if (lastSlash === -1) console.error("Cannot parse page name", href);
    return href.substring(lastSlash + 1);
}

const _MAIN_NAME = "/FlameViewer";
const _CALL_TREE_JS_REQUEST = _MAIN_NAME + "/trees/call-tree";

const serverNames = {
    MAIN_NAME: _MAIN_NAME,
    FILE_LIST: _MAIN_NAME + "/file-list",
    SUPPORTS_FILE_LIST: _MAIN_NAME + "/supports-file-list",
    CALL_TREE_JS_REQUEST: _CALL_TREE_JS_REQUEST,
    CALL_TREE_PREVIEW_JS_REQUEST: _CALL_TREE_JS_REQUEST + "/preview",
    CALL_TRACES: _MAIN_NAME + "/call-traces",
    BACK_TRACES: _MAIN_NAME + "/back-traces",
    CONNECTION_ALIVE: _MAIN_NAME + "/alive",
    SUPPORTS_CLEARING_CACHES: _MAIN_NAME + "/supports-clearing-caches",
    HOT_SPOTS_JS_REQUEST: _MAIN_NAME + "/hot-spots-json",
    FILE: _MAIN_NAME + "/file",
    DOES_FILE_EXIST: _MAIN_NAME + "/does-file-exist",
    UNDO_DELETE_FILE: _MAIN_NAME + "/undo-delete-file"
};

$(window).on("load", () => {
    constants.$main = $("main");
    constants.$loaderBackground = $(".loader-background");
    constants.$loaderMessageP = $('.loader-message p');
    constants.$treePreviewWrapper = $('.tree-preview-wrapper');
    constants.$arrowLeft = $("#arrow-left");
    constants.$arrowRight = $("#arrow-right");
    constants.$removeFilesButton = $(".remove-files-button");
    constants.$fullFileName = $(".full-file-name");
});

common.sendGetRequest(serverNames.SUPPORTS_CLEARING_CACHES, "json")
    .then(response => {
        if (response.result !== true) return;
        /**
         * Each 30 seconds tell server that
         * it should not remove trees from memory
         */
        setInterval(() => {
            const request = new XMLHttpRequest();
            request.open("POST", serverNames.CONNECTION_ALIVE, true);
            request.onload = () => {
            };
            request.send();
        }, 30000);
    });
