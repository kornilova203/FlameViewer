// This file was automatically generated from tree-templates.soy.
// Please don't edit this file by hand.

/**
 * @fileoverview Templates in namespace templates.tree.
 * @public
 */

if (typeof templates == 'undefined') { var templates = {}; }
if (typeof templates.tree == 'undefined') { templates.tree = {}; }


templates.tree.getSectionForThread = function(opt_data, opt_ignored) {
  return soydata.VERY_UNSAFE.ordainSanitizedHtml('<section id="section-' + soy.$$escapeHtmlAttribute(opt_data.threadId) + '"><h2>' + soy.$$escapeHtml(opt_data.threadId) + '</h2><canvas id="canvas-' + soy.$$escapeHtmlAttribute(opt_data.threadId) + '" width="700" height="400"></canvas></section>');
};
if (goog.DEBUG) {
  templates.tree.getSectionForThread.soyTemplateName = 'templates.tree.getSectionForThread';
}
