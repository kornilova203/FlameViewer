let CURRENT_PREFIX;
let $callTreeA;
let $callTracesA;
let $backTracesA;
let $filteredNodesCountSpan;
const CURRENT_INCLUDED = constants.urlParameters[constants.urlParametersKeys.include]; // may be undefined
const CURRENT_INCLUDE_STACKTRACE = common.chooseNotNull(constants.urlParameters[constants.urlParametersKeys.includeStacktrace] === "true", false);
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
        setValueFromParameters($includedInput, $includeStacktraceCheckbox);
        $filterContent.toggle();
        setButtonInactive($applyAnchor);
    });

    const $includedInput = $filterContent.find("#included-input");
    const $includeStacktraceCheckbox = $filterContent.find("#include-stacktrace");

    setValueFromParameters($includedInput, $includeStacktraceCheckbox);
    updateFilterButton();

    const update = () => {
        console.log("update");
        const includePattern = common.nullize($includedInput.val());
        updateFilterLink($applyAnchor, includePattern, $includeStacktraceCheckbox.prop('checked'));
    };

    $includedInput.on('change keyup copy paste cut', common.updateRareDecorator(500, update));
    $includeStacktraceCheckbox.click(common.updateRareDecorator(500, update));
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
        updateFilterLink($applyAnchor, undefined, false);
    })
}

/**
 * @param {Object} $includedInput
 * @param {Object} $includeStacktraceCheckbox
 */
function setValueFromParameters($includedInput, $includeStacktraceCheckbox) {
    $includedInput.val(common.notNullize(CURRENT_INCLUDED));
    $includeStacktraceCheckbox.prop('checked', CURRENT_INCLUDE_STACKTRACE);
}

function updateFilterButton() {
    if (CURRENT_INCLUDED !== undefined) {
        $(".filter").addClass("filter-applied");
    }
}

/**
 * @param {String|undefined} includePattern
 * @param {Boolean} includeStacktrace
 * @return {boolean}
 */
function isApplyActive(includePattern, includeStacktrace) {
    return CURRENT_INCLUDED !== includePattern || CURRENT_INCLUDE_STACKTRACE !== includeStacktrace;
}

function setButtonInactive($applyAnchor) {
    $applyAnchor.removeAttr("href");
    $applyAnchor.find("button").removeClass("active-apply")
}

/**
 * @param {String|undefined} includePattern
 * @param {Boolean|undefined} includeStacktrace
 * @return {String}
 */
function getParametersWithFilter(includePattern, includeStacktrace) {
    const params = Object.assign({}, constants.urlParameters);
    params[constants.urlParametersKeys.include] = includePattern;
    params[constants.urlParametersKeys.includeStacktrace] = includeStacktrace;
    return common.getParametersString(params);
}

function setMethodsCount(nodesCount) {
    $filteredNodesCountSpan.text(nodesCount);
}

/**
 * Send request to server to count how much nodes will be in tree with filter
 * @param {String|undefined} includePattern
 * @param {Boolean} includeStacktrace
 */
function countMethodsForFilter(includePattern, includeStacktrace) {
    const url = serverNames.MAIN_NAME + "/trees/" + constants.pageName + "/count?" + getParametersWithFilter(includePattern, includeStacktrace);

    common.sendGetRequest(url, "json")
        .then(response => {
            setMethodsCount(response.nodesCount);
        });
}


function countMethodsInCurrentTree() {
    const url = serverNames.MAIN_NAME + "/trees/" + constants.pageName + "/count?" + getParametersWithFilter(CURRENT_INCLUDED, CURRENT_INCLUDE_STACKTRACE);
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
 * @param {Boolean} includeStacktrace
 */
function updateFilterLink($applyAnchor, includePattern, includeStacktrace) {
    countMethodsForFilter(includePattern, includeStacktrace);
    if (isApplyActive(includePattern, includeStacktrace)) {
        let parametersWithFilter = getParametersWithFilter(includePattern, includeStacktrace);
        let link = CURRENT_PREFIX + "?" + parametersWithFilter;
        $applyAnchor.attr("href", link);
        $applyAnchor.find("button").addClass("active-apply")
    } else {
        setButtonInactive($applyAnchor);
    }
}
