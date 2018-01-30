const TreeProto = require('../generated/tree_pb');
const TreesProto = require('../generated/trees_pb');
const TreesPreviewProtos = require('../generated/trees_preview_pb');


module.exports.deserializeTree = function (byteArray) {
    // noinspection JSUnresolvedVariable
    return TreeProto.Tree.deserializeBinary(byteArray);
};

module.exports.deserializeTrees = function (byteArray) {
    // noinspection JSUnresolvedVariable
    return TreesProto.Trees.deserializeBinary(byteArray);
};

module.exports.deserializeTreesPreview = function (byteArray) {
    // noinspection JSUnresolvedVariable
    return TreesPreviewProtos.TreesPreview.deserializeBinary(byteArray);
};