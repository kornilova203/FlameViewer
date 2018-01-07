let parametersWithoutFilter;
let CURRENT_PREFIX;
let $callTreeA;
let $outgoingCallsA;
let $incomingCallsA;
let $filteredNodesCountSpan;
let CURRENT_INCLUDED = common.getParameter("include");
CURRENT_INCLUDED = CURRENT_INCLUDED === undefined ? "" : CURRENT_INCLUDED;
let CURRENT_EXCLUDED = common.getParameter("exclude");
CURRENT_EXCLUDED = CURRENT_EXCLUDED === undefined ? "" : CURRENT_EXCLUDED;
let methodsCountInCurrentTree = null;

$(window).on("load", () => {
    if (common.getParameter("file") === undefined) {
        // TODO: disable filter button
        return;
    }

    const $filterContent = $(".filter-content");
    const $applyAnchor = $filterContent.find("a");
    $callTreeA = $(".call-tree-a");
    $outgoingCallsA = $(".outgoing-calls-a");
    $incomingCallsA = $(".incoming-calls-a");
    $filteredNodesCountSpan = $(".filtered-methods-count span");
    parametersWithoutFilter = getParametersWithoutFilter();
    setCurrentPrefix();

    $(".filter").click(() => {
        $filterContent.toggle();
        if (methodsCountInCurrentTree === null) {
            countMethodsInCurrentTree();
        } else {
            setMethodsCount(methodsCountInCurrentTree);
        }
    });

    $filterContent.find(".cancel-filter").click(() => {
        setValueFromParameters($includedInput, $excludedInput);
        $filterContent.toggle();
        setButtonInactive($applyAnchor);
    });

    const $includedInput = $filterContent.find("#included-input");
    const $excludedInput = $filterContent.find("#excluded-input");

    setValueFromParameters($includedInput, $excludedInput);
    updateFilterButton();

    $includedInput.on('change keyup copy paste cut', common.updateRareDecorator(500, () => {
        console.log("update");
        updateFilterLink($applyAnchor, $includedInput.val(), $excludedInput.val());
    }));
    $excludedInput.on('change keyup copy paste cut', common.updateRareDecorator(1000, () => {
        console.log("update");
        updateFilterLink($applyAnchor, $includedInput.val(), $excludedInput.val());
    }));
    setClearAction($filterContent.find(".clear-filter"), $includedInput, $excludedInput, $applyAnchor);
});

function setCurrentPrefix() {
    CURRENT_PREFIX = "/flamegraph-profiler/" + constants.pageName;
}

/**
 * @param $clearFilterButton
 * @param $includedInput
 * @param $excludedInput
 * @param $applyAnchor
 */
function setClearAction($clearFilterButton, $includedInput, $excludedInput, $applyAnchor) {
    $clearFilterButton.click(() => {
        $includedInput.val("");
        $excludedInput.val("");
        updateFilterLink($applyAnchor, $includedInput.val(), $excludedInput.val());
    })
}

function setValueFromParameters($includedInput, $excludedInput) {
    $includedInput.val(decodeURIComponent(CURRENT_INCLUDED));
    $excludedInput.val(decodeURIComponent(CURRENT_EXCLUDED));
}

function updateFilterButton() {
    if (CURRENT_INCLUDED !== "" ||
        CURRENT_EXCLUDED !== "") {
        $(".filter").addClass("filter-applied");
    }
}

function isApplyActive(includingInputText, excludingInputText) {
    return CURRENT_INCLUDED !== includingInputText ||
        CURRENT_EXCLUDED !== excludingInputText
}

function setButtonInactive($applyAnchor) {
    $applyAnchor.removeAttr("href");
    $applyAnchor.find("button").removeClass("active-apply")
}

/**
 * @param {String} include
 * @param {String} exclude
 * @return {String}
 */
function getParametersWithFilter(include, exclude) {
    let link = parametersWithoutFilter;
    if (include !== "" && include !== undefined) {
        link += "&include=" + encodeURIComponent(include);
    }
    if (exclude !== "" && exclude !== undefined) {
        link += "&exclude=" + encodeURIComponent(exclude);
    }
    return link;
}

function setMethodsCount(nodesCount) {
    $filteredNodesCountSpan.text(nodesCount);
}

/**
 * Send request to server to count how much nodes will be in tree with filter
 * @param {String} includingInputText
 * @param {String} excludingInputText
 */
function countMethodsForFilter(includingInputText, excludingInputText) {
    const request = new XMLHttpRequest();
    request.open("GET",
        "/flamegraph-profiler/trees/" + constants.pageName + "/count?" +
        getParametersWithFilter(includingInputText, excludingInputText), true);
    request.responseType = "json";
    request.onload = () => {
        const nodesCount = request.response.nodesCount;
        setMethodsCount(nodesCount);
    };
    request.send();
}


function countMethodsInCurrentTree() {
    const request = new XMLHttpRequest();
    request.open("GET",
        "/flamegraph-profiler/trees/" + constants.pageName + "/count?" +
        getParametersWithFilter(CURRENT_INCLUDED, CURRENT_EXCLUDED), true);
    request.responseType = "json";
    request.onload = () => {
        const nodesCount = request.response.nodesCount;
        methodsCountInCurrentTree = nodesCount;
        setMethodsCount(nodesCount);
    };
    request.send();
}

/**
 * @param {Object} $applyAnchor
 * @param {String} includingInputText
 * @param {String} excludingInputText
 */
function updateFilterLink($applyAnchor, includingInputText, excludingInputText) {
    countMethodsForFilter(includingInputText, excludingInputText);
    if (isApplyActive(includingInputText, excludingInputText)) {
        let link = CURRENT_PREFIX + "?" + getParametersWithFilter(includingInputText, excludingInputText);
        $applyAnchor.attr("href", link);
        $applyAnchor.find("button").addClass("active-apply")
    } else {
        setButtonInactive($applyAnchor);
    }
}

/**
 * @return {String}
 */
function getParametersWithoutFilter() {
    const parametersString = window.location.href.split("?")[1];
    const parameters = parametersString.split("&");
    for (let i = 0; i < parameters.length; i++) {
        if (parameters[i].startsWith("include") ||
            parameters[i].startsWith("exclude")) {
            parameters.splice(i, 1);
            i--;
        }
    }
    return parameters.join("&");
}
