let parametersWithoutFilter;
let CURRENT_PREFIX;
let $callTreeA;
let $callTracesA;
let $backTracesA;
let $filteredNodesCountSpan;
let CURRENT_INCLUDED = common.getParameter("include");
CURRENT_INCLUDED = CURRENT_INCLUDED === undefined ? "" : CURRENT_INCLUDED;
let methodsCountInCurrentTree = null;

$(window).on("load", () => {
    // noinspection JSValidateTypes
    if (common.getParameter("file") === undefined) {
        // TODO: disable filter button
        return;
    }

    const $filterContent = $(".filter-content");
    const $applyAnchor = $filterContent.find("a");
    $callTreeA = $(".call-tree-a");
    $callTracesA = $(".call-traces-a");
    $backTracesA = $(".back-traces-a");
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
        setValueFromParameters($includedInput);
        $filterContent.toggle();
        setButtonInactive($applyAnchor);
    });

    const $includedInput = $filterContent.find("#included-input");

    setValueFromParameters($includedInput);
    updateFilterButton();

    $includedInput.on('change keyup copy paste cut', common.updateRareDecorator(500, () => {
        console.log("update");
        updateFilterLink($applyAnchor, $includedInput.val());
    }));
    setClearAction($filterContent.find(".clear-filter"), $includedInput, $applyAnchor);
});

function setCurrentPrefix() {
    CURRENT_PREFIX = serverNames.MAIN_NAME + "/" + constants.pageName;
}

/**
 * @param $clearFilterButton
 * @param $includedInput
 * @param $applyAnchor
 */
function setClearAction($clearFilterButton, $includedInput, $applyAnchor) {
    $clearFilterButton.click(() => {
        $includedInput.val("");
        updateFilterLink($applyAnchor, $includedInput.val());
    })
}

function setValueFromParameters($includedInput) {
    $includedInput.val(decodeURIComponent(CURRENT_INCLUDED));
}

function updateFilterButton() {
    if (CURRENT_INCLUDED !== "") {
        $(".filter").addClass("filter-applied");
    }
}

function isApplyActive(includingInputText) {
    return CURRENT_INCLUDED !== includingInputText
}

function setButtonInactive($applyAnchor) {
    $applyAnchor.removeAttr("href");
    $applyAnchor.find("button").removeClass("active-apply")
}

/**
 * @param {String} include
 * @return {String}
 */
function getParametersWithFilter(include) {
    if (include === "" || include === undefined) {
        return parametersWithoutFilter;
    }
    return parametersWithoutFilter + "&include=" + encodeURIComponent(include);
}

function setMethodsCount(nodesCount) {
    $filteredNodesCountSpan.text(nodesCount);
}

/**
 * Send request to server to count how much nodes will be in tree with filter
 * @param {String} includingInputText
 */
function countMethodsForFilter(includingInputText) {
    let url = serverNames.MAIN_NAME + "/trees/" + constants.pageName + "/count?" + getParametersWithFilter(includingInputText);

    common.sendGetRequest(url, "json")
        .then(response => {
            setMethodsCount(response.nodesCount);
        });
}


function countMethodsInCurrentTree() {
    const url = serverNames.MAIN_NAME + "/trees/" + constants.pageName + "/count?" + getParametersWithFilter(CURRENT_INCLUDED);
    common.sendGetRequest(url, "json")
        .then(response => {
            const nodesCount = response.nodesCount;
            methodsCountInCurrentTree = nodesCount;
            setMethodsCount(nodesCount);
        });
}

/**
 * @param {Object} $applyAnchor
 * @param {String} includingInputText
 */
function updateFilterLink($applyAnchor, includingInputText) {
    countMethodsForFilter(includingInputText);
    if (isApplyActive(includingInputText)) {
        let link = CURRENT_PREFIX + "?" + getParametersWithFilter(includingInputText);
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
        if (parameters[i].startsWith("include")) {
            parameters.splice(i, 1);
            i--;
        }
    }
    return parameters.join("&");
}
