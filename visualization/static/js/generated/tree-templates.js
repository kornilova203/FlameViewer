// This file was automatically generated from tree-templates.soy.
// Please don't edit this file by hand.

/**
 * @fileoverview Templates in namespace templates.tree.
 * @public
 */

if (typeof templates == 'undefined') { var templates = {}; }
if (typeof templates.tree == 'undefined') { templates.tree = {}; }


templates.tree.getCallTreeSection = function(opt_data, opt_ignored) {
  return soydata.VERY_UNSAFE.ordainSanitizedHtml('<section><h2 class="call-tree-header"><i>Call Tree Preview</i> > ' + soy.$$escapeHtml(opt_data.threadName) + ' <span>' + soy.$$escapeHtml(opt_data.nodesCount) + ' ' + ((opt_data.nodesCount > 1) ? 'calls' : 'call') + '</span></h2><canvas class="original-canvas" id="canvas-' + soy.$$escapeHtmlAttribute(opt_data.id) + '" width="' + soy.$$escapeHtmlAttribute(opt_data.canvasWidth) + '" height="' + soy.$$escapeHtmlAttribute(opt_data.canvasHeight) + '" style="margin-left:' + soy.$$escapeHtmlAttribute(soy.$$filterCssValue(opt_data.canvasOffset)) + 'px"></canvas></section>');
};
if (goog.DEBUG) {
  templates.tree.getCallTreeSection.soyTemplateName = 'templates.tree.getCallTreeSection';
}


templates.tree.zoomedCanvas = function(opt_data, opt_ignored) {
  return soydata.VERY_UNSAFE.ordainSanitizedHtml('<canvas id="canvas-zoomed-' + soy.$$escapeHtmlAttribute(opt_data.id) + '" class="canvas-zoomed" width="' + soy.$$escapeHtmlAttribute(opt_data.canvasWidth) + '" height="' + soy.$$escapeHtmlAttribute(opt_data.canvasHeight) + '"></canvas>');
};
if (goog.DEBUG) {
  templates.tree.zoomedCanvas.soyTemplateName = 'templates.tree.zoomedCanvas';
}


templates.tree.getAccumulativeTreeSection = function(opt_data, opt_ignored) {
  var output = '<section>';
  if (opt_data.className) {
    output += '<header><div class="class-name"><img src="img/class.png"/><code>' + soy.$$escapeHtml(opt_data.className) + '</code></div><h2><img src="img/method.png"/><code class="return-value">' + soy.$$escapeHtml(opt_data.returnValue) + ' </code><code class="method-name">' + soy.$$escapeHtml(opt_data.methodName) + '</code><wbr>(';
    var parameterList49 = opt_data.parameters;
    var parameterListLen49 = parameterList49.length;
    for (var parameterIndex49 = 0; parameterIndex49 < parameterListLen49; parameterIndex49++) {
      var parameterData49 = parameterList49[parameterIndex49];
      output += (parameterData49) ? '<code class="parameter">' + soy.$$escapeHtml(parameterData49) + '</code>' + ((! (parameterIndex49 == parameterListLen49 - 1)) ? '<code>, </code>' : '') : '';
    }
    output += ')</h2><div class="time-percent"><p>time: ' + soy.$$escapeHtml(opt_data.timePercent) + '%</p></div></header>';
  }
  output += '<div class="canvas-wrapper"></div></section>';
  return soydata.VERY_UNSAFE.ordainSanitizedHtml(output);
};
if (goog.DEBUG) {
  templates.tree.getAccumulativeTreeSection.soyTemplateName = 'templates.tree.getAccumulativeTreeSection';
}


templates.tree.getAccumulativeTreeCanvas = function(opt_data, opt_ignored) {
  return soydata.VERY_UNSAFE.ordainSanitizedHtml('<canvas id="canvas" class="original-canvas' + ((opt_data.isNodeZoomed) ? ' original-canvas-zoomed' : '') + '" width="' + soy.$$escapeHtmlAttribute(opt_data.canvasWidth) + '" height="' + soy.$$escapeHtmlAttribute(opt_data.canvasHeight) + '"></canvas><canvas id="canvas-zoomed" class="canvas-zoomed' + ((opt_data.isNodeZoomed) ? ' canvas-zoomed-show' : '') + '" width="' + soy.$$escapeHtmlAttribute(opt_data.canvasWidth) + '" height="' + soy.$$escapeHtmlAttribute(opt_data.canvasHeight) + '"></canvas>');
};
if (goog.DEBUG) {
  templates.tree.getAccumulativeTreeCanvas.soyTemplateName = 'templates.tree.getAccumulativeTreeCanvas';
}


templates.tree.callTreePopup = function(opt_data, opt_ignored) {
    return soydata.VERY_UNSAFE.ordainSanitizedHtml('<div class="popup"><p class="duration"></p><div class="method-links"><a class="outgoing-link"><img src="img/outgoing.png"/></a><a class="incoming-link"><img src="img/incoming.png"/></a></div><img class="icon" src="img/class.png"/><p class="class-name shift-right"></p><img class="icon" src="img/method.png"/><h3 class="method-name shift-right"></h3><img class="icon parameter-icon" src="img/parameter.png"/><div class="parameters shift-right"></div><h4 class="return-value-type shift-right"></h4><p class="return-value shift-right"></p></div>');
};
if (goog.DEBUG) {
  templates.tree.callTreePopup.soyTemplateName = 'templates.tree.callTreePopup';
}


templates.tree.accumulativeTreePopup = function(opt_data, opt_ignored) {
    return soydata.VERY_UNSAFE.ordainSanitizedHtml('<div class="popup"><div class="method-links"><a class="outgoing-link"><img src="img/outgoing.png"/></a><a class="incoming-link"><img src="img/incoming.png"/></a></div>' + ((opt_data.isFull) ? '<img class="icon" src="img/class.png"/>' : '') + '<p class="class-name' + ((opt_data.isFull) ? ' shift-right' : '') + '"></p>' + ((opt_data.isFull) ? '<img class="icon" src="img/method.png"/>' : '') + '<h3 class="method-name' + ((opt_data.isFull) ? ' shift-right' : '') + '"></h3><p class="p-calls-num"></p>' + ((opt_data.isFull) ? '<img class="icon parameter-icon" src="img/parameter.png"/>' : '') + '<div class="parameters' + ((opt_data.isFull) ? ' shift-right' : '') + '"></div></div>');
};
if (goog.DEBUG) {
  templates.tree.accumulativeTreePopup.soyTemplateName = 'templates.tree.accumulativeTreePopup';
}


templates.tree.listOfFiles = function(opt_data, opt_ignored) {
  var output = '<ol class="file-list">';
    var fileList116 = opt_data.fileList;
    var fileListLen116 = fileList116.length;
    for (var fileIndex116 = 0; fileIndex116 < fileListLen116; fileIndex116++) {
        var fileData116 = fileList116[fileIndex116];
        output += '<li id="' + soy.$$escapeHtmlAttribute(fileData116.id) + '"><img class="delete-file" src="img/close-white.png"/><a href="/flamegraph-profiler/' + soy.$$escapeHtmlAttribute(opt_data.pageName) + '?file=' + soy.$$escapeUri(fileData116.fullName) + '&amp;project=' + soy.$$escapeUri(opt_data.projectName) + '"><p>' + soy.$$escapeHtml(fileData116.name) + '<br><span>' + soy.$$escapeHtml(fileData116.date) + '</span></p></a></li>';
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
  var output = '<div class="hot-spot"><a href="/flamegraph-profiler/outgoing-calls?file=' + soy.$$escapeUri(opt_data.fileName) + '&amp;project=' + soy.$$escapeUri(opt_data.projectName) + '&amp;method=' + soy.$$escapeUri(opt_data.methodName) + '&amp;class=' + soy.$$escapeUri(opt_data.className) + '&amp;desc=' + soy.$$escapeUri(opt_data.desc) + '"><img src="img/outgoing.png"/></a><a href="/flamegraph-profiler/incoming-calls?file=' + soy.$$escapeUri(opt_data.fileName) + '&amp;project=' + soy.$$escapeUri(opt_data.projectName) + '&amp;method=' + soy.$$escapeUri(opt_data.methodName) + '&amp;class=' + soy.$$escapeUri(opt_data.className) + '&amp;desc=' + soy.$$escapeUri(opt_data.desc) + '"><img src="img/incoming.png"/></a><div class="outer-time-div"><p class="relative-time">' + soy.$$escapeHtml(opt_data.relativeTime) + '%</p><div class="total-time"><div class="method-time"></div></div></div><div class="class-name"><code>' + soy.$$escapeHtml(opt_data.className) + '</code></div><p class="method"><code class="return-value">' + soy.$$escapeHtml(opt_data.retVal) + ' </code><code class="method-name">' + soy.$$escapeHtml(opt_data.methodName) + '</code><wbr>(';
    var parameterList159 = opt_data.parameters;
    var parameterListLen159 = parameterList159.length;
    for (var parameterIndex159 = 0; parameterIndex159 < parameterListLen159; parameterIndex159++) {
        var parameterData159 = parameterList159[parameterIndex159];
        output += (parameterData159) ? '<code class="parameter">' + soy.$$escapeHtml(parameterData159) + '</code>' + ((!(parameterIndex159 == parameterListLen159 - 1)) ? '<code>, </code>' : '') : '';
  }
  output += ')</p></div>';
  return soydata.VERY_UNSAFE.ordainSanitizedHtml(output);
};
if (goog.DEBUG) {
  templates.tree.hotSpot.soyTemplateName = 'templates.tree.hotSpot';
}


templates.tree.confirm = function(opt_data, opt_ignored) {
  return soydata.VERY_UNSAFE.ordainSanitizedHtml('<div><div class="confirm-bg"></div><div class="confirm-popup"><h3>' + soy.$$escapeHtml(opt_data.question) + '</h3><button class="do-confirm"><a>Ok</a></button><button class="do-not-confirm">Cancel</button></div></div>');
};
if (goog.DEBUG) {
  templates.tree.confirm.soyTemplateName = 'templates.tree.confirm';
}


templates.tree.getTreePreviewSection = function(opt_data, opt_ignored) {
  return soydata.VERY_UNSAFE.ordainSanitizedHtml('<section class="tree-preview"><h2>' + soy.$$escapeHtml(opt_data.threadName) + '</h2><canvas id="canvas-' + soy.$$escapeHtmlAttribute(opt_data.id) + '" height="' + soy.$$escapeHtmlAttribute(opt_data.canvasHeight) + '" width="' + soy.$$escapeHtmlAttribute(opt_data.canvasWidth) + '"></canvas><button class="show-call-tree-button"><img src="img/view-details-icon.png"/></button></section>');
};
if (goog.DEBUG) {
  templates.tree.getTreePreviewSection.soyTemplateName = 'templates.tree.getTreePreviewSection';
}
