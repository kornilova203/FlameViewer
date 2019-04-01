package com.github.korniloval.flameviewer.converters.calltraces

import com.github.korniloval.flameviewer.converters.calltraces.cflamegraph.CFlamegraphToCallTracesConverter
import com.intellij.openapi.util.text.StringUtil
import com.intellij.util.PathUtil
import java.io.File


/**
 * Original flamegraph format consumes a lot of memory on disk and it takes more
 * time to parse because it has lots of duplicate parts of call traces.
 * cflamegraph (compressed flamegraph) solves this problem.
 * It uses FlatBuffers for data serialization and does not duplicate
 * class names, method names and descriptions.
 *
 * See schema in /src/main/flatbuffers/cflamegraph_schema.fbs
 *
 * Example:
 * ._ _
 * |c|d|____ _
 * |void b()|e|_______ _
 * |Class.a___________|f|
 *
 * In original flamegraph format this example would look like this:
 * a;b();c 5
 * a;void b();d 5
 * a;b() 10
 * a;e 5
 * a 50
 * f 5
 *
 * Serialized with flatbuffers tree will have following structure:
 * Names: {
 *   classNames: ["Class"],
 *   methodNames: ["a", "b", "c", "d", "e", "f"],
 *   descriptions: ["()void"]
 * }
 * Tree: {
 *   nodes: [
 *     { class_name_id: 0, method_name_id: 0, width: 100, depth: 1 },
 *     { method_name_id: 1, description_id: 0, width: 40, depth: 2 },
 *     { method_name_id: 2, width: 5, depth: 3 },
 *     { method_name_id: 3, width: 5, depth: 3 },
 *     { method_name_id: 4, width: 5, depth: 2 },
 *     { method_name_id: 5, width: 5, depth: 1 }
 *   ]
 * }
 *
 * Order of nodes matters.
 * If a call has bigger depth than previous it means that the call is a child
 * of previous call.
 *
 * YourkitCsvToCFlamegraphConverter merges unmerged stacktraces.
 */
class CFlamegraphToCallTracesConverterFactory : ToCallTracesIdentifiedConverterFactory {
    companion object {
        const val EXTENSION = "cflamegraph"
    }

    override val id = EXTENSION

    /**
     * Simply checks EXTENSION.
     * There is not need to validate the file because cflamegraph format is known only by the plugin.
     */
    override fun isSupported(file: File): Boolean {
        return StringUtil.equalsIgnoreCase(PathUtil.getFileExtension(file.name), EXTENSION)
    }

    override fun create(file: File) = CFlamegraphToCallTracesConverter(file)
}