$(window).on("load", () => {
    const $filterContent = $(".filter-content");
    const $applyAnchor = $filterContent.find("a");
    const locationWithoutFilter = getLocationWithoutFilter();

    $(".filter").click(() => {
        $filterContent.toggle();
    });
    const $includedTextarea = $filterContent.find("#included-textarea");
    const $excludedTextarea = $filterContent.find("#excluded-textarea");

    const currentIncluded = getParameter("include");
    const currentExcluded = getParameter("exclude");
    if (currentIncluded !== undefined && currentIncluded !== "[]") {
        $includedTextarea.val(currentIncluded.split(",").join("\n"));
    }
    if (currentExcluded !== undefined && currentExcluded !== "[]") {
        $excludedTextarea.val(currentExcluded.split(",").join("\n"));
    }

    $includedTextarea.on('change keyup copy paste cut', () => {
        updateFilterLink($applyAnchor, $includedTextarea.val(), $excludedTextarea.val(), locationWithoutFilter);

    });
    $excludedTextarea.on('change keyup copy paste cut', () => {
        updateFilterLink($applyAnchor, $includedTextarea.val(), $excludedTextarea.val(), locationWithoutFilter);
    });
});

/**
 * @param {Object} $applyAnchor
 * @param {String} includingInputText
 * @param {String} excludingInputText
 * @param {String} locationWithoutFilter
 */
function updateFilterLink($applyAnchor, includingInputText, excludingInputText, locationWithoutFilter) {
    let link = locationWithoutFilter;
    if (includingInputText !== "") {
        link += "&include=" + includingInputText.split("\n").join(",");
    }
    if (excludingInputText !== "") {
        link += "&exclude=" + excludingInputText.split("\n").join(",");
    }
    $applyAnchor.attr("href", link);
}

/**
 * @return {String}
 */
function getLocationWithoutFilter() {
    const parametersString = window.location.href.split("?")[1];
    const parameters = parametersString.split("&");
    for (let i = 0; i < parameters.length; i++) {
        if (parameters[i].startsWith("include") ||
            parameters[i].startsWith("exclude")) {
            parameters.splice(i, 1);
            i--;
        }
    }
    return window.location.href.split("?")[0] + "?" + parameters.join("&");
}
