package com.github.korniloval.flameviewer.server

import java.util.regex.Pattern

const val NAME = "FlameViewer"
const val MAIN_NAME = "/$NAME"
const val FILE_LIST = "$MAIN_NAME/file-list"
const val CALL_TREE = "$MAIN_NAME/call-tree"
const val CALL_TREE_JS_REQUEST = "$MAIN_NAME/trees/call-tree"
const val CALL_TREE_COUNT = "$CALL_TREE_JS_REQUEST/count"
const val CALL_TREE_PREVIEW_JS_REQUEST = "$CALL_TREE_JS_REQUEST/preview"
const val OUTGOING_CALLS = "outgoing-calls"
const val OUTGOING_CALLS_FULL = "$MAIN_NAME/$OUTGOING_CALLS"
const val OUTGOING_CALLS_JS_REQUEST = "$MAIN_NAME/trees/$OUTGOING_CALLS"
const val OUTGOING_CALLS_COUNT = "$OUTGOING_CALLS_JS_REQUEST/count"
const val INCOMING_CALLS = "incoming-calls"
const val INCOMING_CALLS_FULL = "$MAIN_NAME/$INCOMING_CALLS"
const val INCOMING_CALLS_JS_REQUEST = "$MAIN_NAME/trees/$INCOMING_CALLS"
const val INCOMING_CALLS_COUNT = "$INCOMING_CALLS_JS_REQUEST/count"
const val CONNECTION_ALIVE = "$MAIN_NAME/trees/alive"
const val HOT_SPOTS = "$MAIN_NAME/hot-spots"
const val HOT_SPOTS_JS_REQUEST = "$MAIN_NAME/hot-spots-json"
const val UPLOAD_FILE = "$MAIN_NAME/upload-file"
const val DOES_FILE_EXIST = "$MAIN_NAME/does-file-exist"
const val DELETE_FILE = "$MAIN_NAME/delete-file"
const val UNDO_DELETE_FILE = "$MAIN_NAME/undo-delete-file"
@JvmField val CSS_PATTERN = Pattern.compile("$MAIN_NAME/css.+css$")!!
@JvmField val JS_PATTERN = Pattern.compile("$MAIN_NAME/js.+js$")!!
@JvmField val FONT_PATTERN = Pattern.compile("$MAIN_NAME/.+ttf$")!!
@JvmField val PNG_PATTERN = Pattern.compile("$MAIN_NAME/.+png$")!!

