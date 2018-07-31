const common = {
    /**
     * @param {String} message
     * @param {Function} callback
     * @param {Number} [time]
     */
    showLoader: (message, callback, time = 100) => {
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
     * Shrink loader background because it will show only "Drawing tree..." message
     * @param {Number} [width]
     */
    resizeLoaderBackground: (width = 160) => {
        $(".loader-background").css({
            "width": width,
            "left": "calc((100vw - 250px) / 2 + 250px - " + (width / 2) + "px)"
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
     * @param {Object} parameters
     */
    redirect: (parameters) => {
        window.location.href = `/flamegraph-profiler/${constants.pageName}?${common.getParametersString(parameters)}`
    },

    /**
     * @param {String} fileName
     * @param {String} projectName
     * @param {Function} callbackIfExists
     * @param {Function} callbackIfDoesNotExist
     */
    doCallbackIfFileExists: (fileName, projectName, callbackIfExists, callbackIfDoesNotExist) => {
        const request = new XMLHttpRequest();
        request.onload = () => {
            if (request.status === 302) { // if was found
                callbackIfExists();
            } else {
                if (callbackIfDoesNotExist != null) {
                    callbackIfDoesNotExist();
                }
            }
        };
        request.open("GET", "/flamegraph-profiler/does-file-exist", true);
        request.setRequestHeader('Project-Name', projectName);
        try {
            request.setRequestHeader('File-Name', fileName);
        } catch (err) {
            common.showError("File name should contain only ascii characters");
            setTimeout(() => { // it does not work without timeout. I do know why
                constants.$loaderBackground.hide();
            }, 50);
            return; // do not send request
        }
        request.send();
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
    }
};


// noinspection JSValidateTypes
const constants = {
    $main: null,
    $treePreviewWrapper: null,
    $loaderBackground: null,
    $loaderMessageP: null,
    $arrowLeft: null,
    $arrowRight: null,
    projectName: common.getParameter("project") === undefined ? undefined : decodeURIComponent(common.getParameter("project")),
    fileName: common.getParameter("file") === undefined ? undefined : decodeURIComponent(common.getParameter("file")),
    $removeFilesButton: null,
    $fullFileName: null,
    pageName: /[^\/]*((?=\?)|(?=\.html))/.exec(window.location.href)[0],
    CANVAS_PADDING: 35,
    LAYER_HEIGHT: 18,
    LAYER_GAP: 1,
    POPUP_MARGIN: 6, // have no idea why there is a gap between popup and canvas
    METHOD_HEADER_HEIGHT: 80,
    loaderMessages: {
        drawing: "Drawing...",
        deserialization: "Deserialization of binary data...",
        buildingTree: "Building tree...",
        buildingTrees: "Building trees...",
        countingTime: "Counting self-time of methods...",
        convertingFile: "Converting file: ",
        uploadingFile: "Uploading file: "
    },
    pageMessages: {
        backtracesTooBig: "Sorry... this tree contains too many nodes :(</br>" +
        "But you can see back traces for any method, there are two ways to do it:</br>" +
        "* Open Call Traces page, click on back traces icon on method popup</br>" +
        "* Open Hot Spots page, click on back traces icon beside any method that you like",
        chooseFile: "Choose file",
        chooseOrUploadFile: "Choose or upload file",
        noCallRegistered: "No call was registered or all methods took <1ms",
        callTreeUnavailable: "Call tree is unavailable for this file"
    }
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

/**
 * Each 10 seconds tell server that
 * it should not remove trees from memory
 */
setInterval(() => {
    const request = new XMLHttpRequest();
    request.open("POST", "/flamegraph-profiler/trees/alive", true);
    request.onload = () => {

    };
    request.send();
}, 10000);