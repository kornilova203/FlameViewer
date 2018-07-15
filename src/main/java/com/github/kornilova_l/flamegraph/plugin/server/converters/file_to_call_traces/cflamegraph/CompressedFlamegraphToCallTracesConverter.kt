package com.github.kornilova_l.flamegraph.plugin.server.converters.file_to_call_traces.cflamegraph

import com.github.kornilova_l.flamegraph.plugin.server.converters.file_to_call_traces.FileToCallTracesConverter
import com.github.kornilova_l.flamegraph.plugin.server.converters.file_to_file.ProfilerToFlamegraphConverter
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree
import java.io.File


/**
 * Original flamegraph format consumes a lot of memory on disk and it takes more time to parse
 * because it has lots of duplicate parts of call traces.
 * cflamegraph (compressed flamegraph) solves this problem.
 *
 * Header of file contains ids of class names, method names and descriptions.
 * For example:
 * C
 * ClassName1 0
 * ClassName2 1
 * M
 * method1 0
 * method2 1
 * D
 * () 0
 * (int)int 1
 *
 * Each line of cflamegraph contains information in following format:
 * C=<class name id> M=<method name id> D=<description id> w=<width> d=<depth>
 *
 * C and D are optional.
 *
 * Example:
 * ._ _
 * |c|d|___ _
 * |b()____|e|_______ _
 * |Class.a__________|f|
 *
 * --C-- 1
 * Class 0
 * --M-- 6
 * a 0
 * b 1
 * c 2
 * d 3
 * e 4
 * f 5
 * --D-- 1
 * () 0
 * C0M0w100d1
 * M1w40d2
 * M2w5d3
 * M3w5d3
 * M4w5d2
 * M5w5d1
 *
 * As you can see order of lines matters
 * because if a call has bigger depth than previous it means that
 * the call is a child of previous call.
 *
 * In original flamegraph format this example would look like this:
 * a;b;c 5
 * a;b;d 5
 * a;b 10
 * a;e 5
 * a 50
 * f 5
 *
 * In cflamegraph files strings are not duplicated and equal parts of stacktraces are also not duplicated
 */
class CompressedFlamegraphToCallTracesConverter : FileToCallTracesConverter() {
    private val extension = "cflamegraph"

    override fun getId(): String = extension

    /**
     * Simply checks extension.
     * There is not need to validate the file because cflamegraph format is known only by the plugin.
     */
    override fun isSupported(file: File): Boolean {
        return ProfilerToFlamegraphConverter.getFileExtension(file.name) == extension
    }

    override fun convert(file: File): Tree = Converter(file).tree
}