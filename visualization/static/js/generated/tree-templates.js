// This file was automatically generated from tree-templates.soy.
// Please don't edit this file by hand.

/**
 * @fileoverview Templates in namespace templates.tree.
 * @public
 */

if (typeof templates == 'undefined') { var templates = {}; }
if (typeof templates.tree == 'undefined') { templates.tree = {}; }


templates.tree.getSectionForThread = function(opt_data, opt_ignored) {
  return soydata.VERY_UNSAFE.ordainSanitizedHtml('<section><h2>' + soy.$$escapeHtml(opt_data.threadId) + '</h2><canvas id="canvas-' + soy.$$escapeHtmlAttribute(opt_data.threadId) + '" width="' + soy.$$escapeHtmlAttribute(opt_data.canvasWidth) + '" height="' + soy.$$escapeHtmlAttribute(opt_data.canvasHeight) + '" style="margin-left:' + soy.$$escapeHtmlAttribute(soy.$$filterCssValue(opt_data.canvasOffset)) + 'px"></canvas></section>');
};
if (goog.DEBUG) {
  templates.tree.getSectionForThread.soyTemplateName = 'templates.tree.getSectionForThread';
}


templates.tree.getBaseSection = function(opt_data, opt_ignored) {
  return soydata.VERY_UNSAFE.ordainSanitizedHtml('<section><canvas id="canvas" width="' + soy.$$escapeHtmlAttribute(opt_data.canvasWidth) + '" height="' + soy.$$escapeHtmlAttribute(opt_data.canvasHeight) + '"></canvas></section>');
};
if (goog.DEBUG) {
  templates.tree.getBaseSection.soyTemplateName = 'templates.tree.getBaseSection';
}


templates.tree.callTreePopup = function(opt_data, opt_ignored) {
  return soydata.VERY_UNSAFE.ordainSanitizedHtml('<div class="popup"><h3>' + soy.$$escapeHtml(opt_data.className) + '.<b>' + soy.$$escapeHtml(opt_data.methodName) + '</b></h3><p>Start time: ' + soy.$$escapeHtml(opt_data.startTime) + 'ms</p><p>Duration: ' + soy.$$escapeHtml(opt_data.duration) + 'ms</p></div>');
};
if (goog.DEBUG) {
  templates.tree.callTreePopup.soyTemplateName = 'templates.tree.callTreePopup';
}


templates.tree.basePopup = function(opt_data, opt_ignored) {
  return soydata.VERY_UNSAFE.ordainSanitizedHtml('<div class="popup"><a href="/flamegraph-profiler/outgoing-calls?class=' + soy.$$escapeUri(opt_data.className) + '&amp;method=' + soy.$$escapeUri(opt_data.methodName) + '&amp;desc=' + soy.$$escapeUri(opt_data.desc) + '&amp;isStatic=' + soy.$$escapeUri(opt_data.isStatic) + '">Outgoing Calls</a><h3>' + soy.$$escapeHtml(opt_data.className) + '.' + soy.$$escapeHtml(opt_data.methodName) + '</h3><p>Duration: ' + soy.$$escapeHtml(opt_data.duration) + 'ms</p><p>' + soy.$$escapeHtml(opt_data.count) + ' call' + ((opt_data.count > 1) ? 's' : '') + '</p></div>');
};
if (goog.DEBUG) {
  templates.tree.basePopup.soyTemplateName = 'templates.tree.basePopup';
}
