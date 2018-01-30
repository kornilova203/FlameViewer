const TreeProto = require('../generated/tree_pb');


module.exports.deserializeTree = function (byteArray) {
    // noinspection JSUnresolvedVariable
    return TreeProto.Tree.deserializeBinary(byteArray);
};