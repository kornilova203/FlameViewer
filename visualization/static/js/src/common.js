const common = {};

/**
 * @param {String} message
 * @param {Function} callback
 * @param {Number} [time]
 */
common.showLoader = (message, callback, time = 100) => {
    setTimeout(() => {
        console.log("show loader " + time + " " + message);
        constants.$loaderMessageP.text(message);
        constants.$loaderBackground.fadeIn(time, callback);
    }, 5);
};

/**
 * @param {Number} [time]
 */
common.hideLoader = (time = 0) => {
    constants.$loaderBackground.fadeOut(time);
};

/**
 * Shrink loader background because it will show only "Drawing tree..." message
 */
common.shrinkLoaderBackground = () => {
    $(".loader-background").css({
        "width": 160,
        "left": "calc((100vw - 250px) / 2 + 250px - 80px)"
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
                return parameters[i].substring(
                    parameters[i].indexOf("=") + 1,
                    parameters[i].length
                );
            }
        }
    }
    return undefined;
};

const constants = {};
constants.$main = null;
constants.$loaderBackground = null;
constants.$loaderMessageP = null;
constants.projectName = common.getParameter("project");
constants.fileName = common.getParameter("file");
constants.loaderMessages = {
    drawing: "Drawing...",
    deserialization: "Deserialization of binary data...",
    buildingTree: "Building tree...",
    buildingTrees: "Building trees...",
    countingTime: "Counting self-time of methods...",
    convertingFile: "Converting file..."
};

$(window).on("load", () => {
    constants.$main = $("main");
    constants.$loaderBackground = $(".loader-background");
    constants.$loaderMessageP = $('.loader-message p');
});