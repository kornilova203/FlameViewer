// This file was automatically generated from tree-templates.soy.
// Please don't edit this file by hand.

/**
 * @fileoverview Templates in namespace templates.tree.
 * @public
 */

if (typeof templates == 'undefined') { var templates = {}; }
if (typeof templates.tree == 'undefined') { templates.tree = {}; }


templates.tree.getCallTreeSection = function(opt_data, opt_ignored) {
  return soydata.VERY_UNSAFE.ordainSanitizedHtml('<section><h2 class="call-tree-header">' + soy.$$escapeHtml(opt_data.threadName) + ' <span>' + soy.$$escapeHtml(opt_data.nodesCount) + ' ' + ((opt_data.nodesCount > 1) ? 'calls' : 'call') + '</span></h2><canvas id="canvas-' + soy.$$escapeHtmlAttribute(opt_data.id) + '" width="' + soy.$$escapeHtmlAttribute(opt_data.canvasWidth) + '" height="' + soy.$$escapeHtmlAttribute(opt_data.canvasHeight) + '" style="margin-left:' + soy.$$escapeHtmlAttribute(soy.$$filterCssValue(opt_data.canvasOffset)) + 'px"></canvas><canvas id="canvas-zoomed-' + soy.$$escapeHtmlAttribute(opt_data.id) + '" width="' + soy.$$escapeHtmlAttribute(opt_data.canvasWidth) + '" height="' + soy.$$escapeHtmlAttribute(opt_data.canvasHeight) + '" style="display: none;"></canvas></section>');
};
if (goog.DEBUG) {
  templates.tree.getCallTreeSection.soyTemplateName = 'templates.tree.getCallTreeSection';
}


templates.tree.getAccumulativeTreeSection = function(opt_data, opt_ignored) {
  return soydata.VERY_UNSAFE.ordainSanitizedHtml('<section>' + ((opt_data.header) ? '<h2 class="accumulative-tree-header">' + soy.$$escapeHtml(opt_data.header) + '</h2>' : '') + '<div class="canvas-wrapper"><canvas id="canvas" width="' + soy.$$escapeHtmlAttribute(opt_data.canvasWidth) + '" height="' + soy.$$escapeHtmlAttribute(opt_data.canvasHeight) + '"></canvas><canvas id="canvas-zoomed" width="' + soy.$$escapeHtmlAttribute(opt_data.canvasWidth) + '" height="' + soy.$$escapeHtmlAttribute(opt_data.canvasHeight) + '" style="display: none;"></canvas></div></section>');
};
if (goog.DEBUG) {
  templates.tree.getAccumulativeTreeSection.soyTemplateName = 'templates.tree.getAccumulativeTreeSection';
}


templates.tree.callTreePopup = function(opt_data, opt_ignored) {
    return soydata.VERY_UNSAFE.ordainSanitizedHtml('<div class="popup"><a class="outgoing-link"><img src="img/outgoing.png"/></a><a class="incoming-link"><img src="img/incoming.png"/></a><h3></h3><p class="duration"></p><h4 class="parameters-header">Parameters:</h4><table><tr><th>Type</th><th>Value</th></tr></table></div>');
};
if (goog.DEBUG) {
  templates.tree.callTreePopup.soyTemplateName = 'templates.tree.callTreePopup';
}


templates.tree.accumulativeTreePopup = function(opt_data, opt_ignored) {
    return soydata.VERY_UNSAFE.ordainSanitizedHtml('<div class="popup"><a class="outgoing-link"><img src="img/outgoing.png"/></a><a class="incoming-link"><img src="img/incoming.png"/></a><h3></h3><p class="p-calls-num"></p><h4 class="parameters-header">Parameters:</h4><table></table></div>');
};
if (goog.DEBUG) {
  templates.tree.accumulativeTreePopup.soyTemplateName = 'templates.tree.accumulativeTreePopup';
}


templates.tree.listOfFiles = function(opt_data, opt_ignored) {
  var output = '<ol class="file-list">';
  var fileNameList61 = opt_data.fileNames;
  var fileNameListLen61 = fileNameList61.length;
  for (var fileNameIndex61 = 0; fileNameIndex61 < fileNameListLen61; fileNameIndex61++) {
    var fileNameData61 = fileNameList61[fileNameIndex61];
    output += '<li id="' + soy.$$escapeHtmlAttribute(fileNameData61) + '"><img class="delete-file" src="img/close-white.png"/><a href="/flamegraph-profiler/' + soy.$$escapeHtmlAttribute(opt_data.pageName) + '?file=' + soy.$$escapeUri(fileNameData61) + '&amp;project=' + soy.$$escapeUri(opt_data.projectName) + '">' + soy.$$escapeHtml(fileNameData61) + '</a></li>';
  }
  output += '</ol>';
  return soydata.VERY_UNSAFE.ordainSanitizedHtml(output);
};
if (goog.DEBUG) {
  templates.tree.listOfFiles.soyTemplateName = 'templates.tree.listOfFiles';
}


templates.tree.fileInput = function(opt_data, opt_ignored) {
    return soydata.VERY_UNSAFE.ordainSanitizedHtml('<form class="file-form"><input type="file" name="file" id="file" class="inputfile"/><label for="file"><span>Upload file</span></label><p class="file-form-header">.jfr, .ser or flamegraph</p></form>');
};
if (goog.DEBUG) {
  templates.tree.fileInput.soyTemplateName = 'templates.tree.fileInput';
}


templates.tree.hotSpot = function(opt_data, opt_ignored) {
    var output = '<div class="hot-spot"><a href="/flamegraph-profiler/outgoing-calls?file=' + soy.$$escapeUri(opt_data.fileName) + '&amp;project=' + soy.$$escapeUri(opt_data.projectName) + '&amp;method=' + soy.$$escapeUri(opt_data.methodName) + '&amp;class=' + soy.$$escapeUri(opt_data.className) + '&amp;desc=' + soy.$$escapeUri(opt_data.desc) + '"><img src="img/outgoing.png"/></a><a href="/flamegraph-profiler/incoming-calls?file=' + soy.$$escapeUri(opt_data.fileName) + '&amp;project=' + soy.$$escapeUri(opt_data.projectName) + '&amp;method=' + soy.$$escapeUri(opt_data.methodName) + '&amp;class=' + soy.$$escapeUri(opt_data.className) + '&amp;desc=' + soy.$$escapeUri(opt_data.desc) + '"><img src="img/incoming.png"/></a><div class="outer-time-div"><p class="relative-time">' + soy.$$escapeHtml(opt_data.relativeTime) + '%</p><div class="total-time"><div class="method-time"></div></div></div><p class="method"><code class="return-value">' + soy.$$escapeHtml(opt_data.retVal) + '</code><code> </code><code class="class-name">' + soy.$$escapeHtml(opt_data.className) + '</code><wbr>.<code class="method-name">' + soy.$$escapeHtml(opt_data.methodName) + '</code><wbr>(';
    var parameterList110 = opt_data.parameters;
    var parameterListLen110 = parameterList110.length;
    for (var parameterIndex110 = 0; parameterIndex110 < parameterListLen110; parameterIndex110++) {
        var parameterData110 = parameterList110[parameterIndex110];
        output += (parameterData110) ? ((opt_data.doBreak) ? '<br/>' : '') + ((opt_data.doBreak) ? '<code>&#160&#160&#160&#160&#160&#160&#160&#160&#160&#160&#160&#160&#160&#160</code>' : '') + '<code class="parameter">' + soy.$$escapeHtml(parameterData110) + '</code>' + ((!(parameterIndex110 == parameterListLen110 - 1)) ? '<code>, </code>' : '') : '';
  }
    output += ')</p></div>';
  return soydata.VERY_UNSAFE.ordainSanitizedHtml(output);
};
if (goog.DEBUG) {
  templates.tree.hotSpot.soyTemplateName = 'templates.tree.hotSpot';
}


templates.tree.confirmDelete = function(opt_data, opt_ignored) {
  return soydata.VERY_UNSAFE.ordainSanitizedHtml('<div><div class="confirm-delete-bg"></div><div class="confirm-delete-popup"><h3>Delete file ' + soy.$$escapeHtml(opt_data.fileName) + '?</h3><div class="do-delete"><a>Ok</a></div><div class="do-not-delete"><p>Cancel</p></div></div></div>');
};
if (goog.DEBUG) {
  templates.tree.confirmDelete.soyTemplateName = 'templates.tree.confirmDelete';
}


templates.tree.getTreePreviewSection = function(opt_data, opt_ignored) {
    return soydata.VERY_UNSAFE.ordainSanitizedHtml('<section class="tree-preview"><h2>' + soy.$$escapeHtml(opt_data.threadName) + '</h2><canvas id="canvas-' + soy.$$escapeHtmlAttribute(opt_data.id) + '" height="' + soy.$$escapeHtmlAttribute(opt_data.canvasHeight) + '" width="' + soy.$$escapeHtmlAttribute(opt_data.canvasWidth) + '"></canvas><button class="show-call-tree-button"><img src="img/view-details-icon.png"/></button></section>');
};
if (goog.DEBUG) {
  templates.tree.getTreePreviewSection.soyTemplateName = 'templates.tree.getTreePreviewSection';
}
