const common = {};

/**
 * @param {String} message
 * @param {Function} callback
 * @param {Number} [time]
 */
common.showLoader = (message, callback, time = 100) => {
    setTimeout(() => {
        constants.$loaderMessageP.text(message);
        constants.$loaderBackground.fadeIn(time, callback);
    }, 5);
};

/**
 * Get extension of file
 * @param {string} fileName
 * @returns {string}
 */
common.getExtension = (fileName) => {
    return fileName.substring(fileName.indexOf(".") + 1, fileName.length);
};

/**
 * @param {Number} [time]
 */
common.hideLoader = (time = 0) => {
    constants.$loaderBackground.fadeOut(time);
};

/**
 * Shrink loader background because it will show only "Drawing tree..." message
 * @param {Number} [width]
 */
common.resizeLoaderBackground = (width = 160) => {
    $(".loader-background").css({
        "width": width,
        "left": "calc((100vw - 250px) / 2 + 250px - " + (width / 2) + "px)"
    })
};

/**
 * @param {String} parameterName
 * @return {undefined|string}
 */
common.getParameter = (parameterName) => {
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
};

/**
 * @param {string} message
 */
common.showMessage = (message) => {
    $("body").append(`<p class='message'>${message}</p>`);
};

common.hideMessage = () => {
    $('.message').remove();
};

common.escapeRegExp = (text) => {
    return text.replace(/[-[\]{}()+?.,\\^$|#\s]/g, '\\$&');
};

common.roundRelativeTime = (time) => {
    if (time * 100 > 1) {
        return Math.round(time * 1000) / 10;
    } else {
        return Math.round(time * 10000) / 100;
    }
};

/**
 * Do callback not more often than updateTime ms
 * @param {number} updateTime
 * @param {function} callback
 * @return {function}
 */
common.updateRareDecorator = (updateTime, callback) => {
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
};

/**
 * @param {Object} parameters
 * @return {string}
 */
common.getParametersString = (parameters) => {
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
};

/**
 * Show error message to user
 * @param {String} errorMessage
 */
common.showError = (errorMessage) => {
    const errorMessageBlock = $(".error-message-block");
    errorMessageBlock.find("p").text(errorMessage);
    errorMessageBlock.addClass("visible");
    setTimeout(() => {
        errorMessageBlock.removeClass("visible");
    }, 5000);
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
        LAYER_HEIGHT: 19,
        LAYER_GAP: 1,
        loaderMessages: {
            drawing: "Drawing...",
            deserialization: "Deserialization of binary data...",
            buildingTree: "Building tree...",
            buildingTrees: "Building trees...",
            countingTime: "Counting self-time of methods...",
            convertingFile: "Converting file: ",
            uploadingFile: "Uploading file: "
        }
    }
;

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