// This file was automatically generated from tree-templates.soy.
// Please don't edit this file by hand.

/**
 * @fileoverview Templates in namespace tree.templates.
 * @public
 */

if (typeof tree == 'undefined') { var tree = {}; }
if (typeof tree.templates == 'undefined') { tree.templates = {}; }


tree.templates.getSectionForThread = function(opt_data, opt_ignored) {
  return soydata.VERY_UNSAFE.ordainSanitizedHtml('<section id="' + soy.$$escapeHtmlAttribute(opt_data.threadId) + '"><h2>' + soy.$$escapeHtml(opt_data.threadId) + '</h2></section>');
};
if (goog.DEBUG) {
  tree.templates.getSectionForThread.soyTemplateName = 'tree.templates.getSectionForThread';
}
