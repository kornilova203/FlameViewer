// This file was automatically generated from tree-templates.soy.
// Please don't edit this file by hand.

/**
 * @fileoverview Templates in namespace templates.tree.
 * @public
 */

if (typeof templates == 'undefined') { var templates = {}; }
if (typeof templates.tree == 'undefined') { templates.tree = {}; }


templates.tree.getSectionForThread = function(opt_data, opt_ignored) {
  return soydata.VERY_UNSAFE.ordainSanitizedHtml('<section><h2>' + soy.$$escapeHtml(opt_data.threadId) + '</h2><canvas id="canvas-' + soy.$$escapeHtmlAttribute(opt_data.threadId) + '" width="700" height="' + soy.$$escapeHtmlAttribute(opt_data.canvasHeight) + '"></canvas></section>');
};
if (goog.DEBUG) {
  templates.tree.getSectionForThread.soyTemplateName = 'templates.tree.getSectionForThread';
}


templates.tree.popupInOriginalTree = function(opt_data, opt_ignored) {
  return soydata.VERY_UNSAFE.ordainSanitizedHtml('<div class="detail"><h3>' + soy.$$escapeHtml(opt_data.className) + '.<b>' + soy.$$escapeHtml(opt_data.methodName) + '</b></h3><p>Start time: ' + soy.$$escapeHtml(opt_data.startTime) + 'ms</p><p>Duration: ' + soy.$$escapeHtml(opt_data.duration) + 'ms</p></div>');
};
if (goog.DEBUG) {
  templates.tree.popupInOriginalTree.soyTemplateName = 'templates.tree.popupInOriginalTree';
}
