let CURRENT_PREFIX;
let $callTreeA;
let $callTracesA;
let $backTracesA;
let $filteredNodesCountSpan;
const CURRENT_INCLUDED = constants.urlParameters[constants.urlParametersKeys.include]; // may be undefined
let methodsCountInCurrentTree = null;

$(window).on("load", () => {
    if (constants.fileName === undefined) {
        // TODO: disable filter button
        return;
    }

    const $filterContent = $(".filter-content");
    const $applyAnchor = $filterContent.find("a");
    $callTreeA = $(".call-tree-a");
    $callTracesA = $(".call-traces-a");
    $backTracesA = $(".back-traces-a");
    $filteredNodesCountSpan = $(".filtered-methods-count span");
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
        const includePattern = common.nullize($includedInput.val());
        updateFilterLink($applyAnchor, includePattern);
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
        updateFilterLink($applyAnchor, undefined);
    })
}

function setValueFromParameters($includedInput) {
    $includedInput.val(common.notNullize(CURRENT_INCLUDED));
}

function updateFilterButton() {
    if (CURRENT_INCLUDED !== undefined) {
        $(".filter").addClass("filter-applied");
    }
}

/**
 * @param {String|undefined} includePattern
 * @return {boolean}
 */
function isApplyActive(includePattern) {
    return CURRENT_INCLUDED !== includePattern
}

function setButtonInactive($applyAnchor) {
    $applyAnchor.removeAttr("href");
    $applyAnchor.find("button").removeClass("active-apply")
}

/**
 * @param {String|undefined} includePattern
 * @return {String}
 */
function getParametersWithFilter(includePattern) {
    const params = Object.assign({}, constants.urlParameters);
    params[constants.urlParametersKeys.include] = includePattern;
    return common.getParametersString(params);
}

function setMethodsCount(nodesCount) {
    $filteredNodesCountSpan.text(nodesCount);
}

/**
 * Send request to server to count how much nodes will be in tree with filter
 * @param {String|undefined} includePattern
 */
function countMethodsForFilter(includePattern) {
    const url = serverNames.MAIN_NAME + "/trees/" + constants.pageName + "/count?" + getParametersWithFilter(includePattern);

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
 * @param {String|undefined} includePattern
 */
function updateFilterLink($applyAnchor, includePattern) {
    countMethodsForFilter(includePattern);
    if (isApplyActive(includePattern)) {
        let parametersWithFilter = getParametersWithFilter(includePattern);
        let link = CURRENT_PREFIX + "?" + parametersWithFilter;
        $applyAnchor.attr("href", link);
        $applyAnchor.find("button").addClass("active-apply")
    } else {
        setButtonInactive($applyAnchor);
    }
}
