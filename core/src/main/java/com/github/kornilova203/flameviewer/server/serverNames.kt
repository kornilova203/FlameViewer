package com.github.kornilova203.flameviewer.server

import java.util.regex.Pattern

const val NAME = "FlameViewer"
private const val MAIN_NAME = "/$NAME"

const val BACK_TRACES_NAME = "back-traces"
const val CALL_TRACES_NAME = "call-traces"

const val CALL_TREE_PAGE = "$MAIN_NAME/call-tree"
const val CALL_TRACES_PAGE = "$MAIN_NAME/$CALL_TRACES_NAME"
const val BACK_TRACES_PAGE = "$MAIN_NAME/$BACK_TRACES_NAME"

const val SERIALIZED_CALL_TREE = "$MAIN_NAME/trees/call-tree"
const val SERIALIZED_BACK_TRACES = "$MAIN_NAME/trees/$BACK_TRACES_NAME"
const val SERIALIZED_CALL_TRACES = "$MAIN_NAME/trees/$CALL_TRACES_NAME"

const val CALL_TREE_COUNT = "$SERIALIZED_CALL_TREE/count"
const val CALL_TRACES_COUNT = "$SERIALIZED_CALL_TRACES/count"
const val BACK_TRACES_COUNT = "$SERIALIZED_BACK_TRACES/count"

const val CALL_TREE_PREVIEW = "$SERIALIZED_CALL_TREE/preview"
const val FILE_LIST = "$MAIN_NAME/file-list"
const val CONNECTION_ALIVE = "$MAIN_NAME/alive"
const val SUPPORTS_CLEARING_CACHES = "$MAIN_NAME/supports-clearing-caches"
const val SUPPORTS_FILE_LIST = "$MAIN_NAME/supports-file-list"
const val HOT_SPOTS_PAGE = "$MAIN_NAME/hot-spots"
const val HOT_SPOTS_JSON = "$MAIN_NAME/hot-spots-json"
const val DOES_FILE_EXIST = "$MAIN_NAME/does-file-exist"
const val FILE = "$MAIN_NAME/file"
const val UNDO_DELETE_FILE = "$MAIN_NAME/undo-delete-file"

@JvmField val CSS_PATTERN = Pattern.compile("$MAIN_NAME/css.+css$")!!
@JvmField val JS_PATTERN = Pattern.compile("$MAIN_NAME/js.+js$")!!
@JvmField val FONT_PATTERN = Pattern.compile("$MAIN_NAME/.+ttf$")!!
@JvmField val PNG_PATTERN = Pattern.compile("$MAIN_NAME/.+png$")!!

