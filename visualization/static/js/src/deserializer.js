const TreeProto = require('../generated/tree');
const TreesProto = require('../generated/trees');
const TreesPreviewProtos = require('../generated/treespreview');


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