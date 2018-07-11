const TreeProto = require('../generated/com/github/kornilova_l/flamegraph/proto/tree_pb');
const TreesProto = require('../generated/com/github/kornilova_l/flamegraph/proto/trees_pb');
const TreesPreviewProtos = require('../generated/com/github/kornilova_l/flamegraph/proto/trees_preview_pb');


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