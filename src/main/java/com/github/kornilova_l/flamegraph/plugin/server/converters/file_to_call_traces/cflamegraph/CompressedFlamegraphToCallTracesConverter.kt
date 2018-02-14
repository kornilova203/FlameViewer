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
 * C 1
 * Class 0
 * M 6
 * a 0
 * b 1
 * c 2
 * d 3
 * e 4
 * f 5
 * D 1
 * () 0
 * C=0 M=0 w=100 d=1
 * M=1 w=40 d=2
 * M=2 w=5 d=3
 * M=3 w=5 d=3
 * M=4 w=5 d=2
 * M=5 w=5 d=1
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
    val extension = "cflamegraph"

    override fun getId(): String = extension

    /**
     * Each line must contain non-space characters and two numbers.
     * I do not use patterns here because they are slow
     */
    override fun isSupported(file: File): Boolean {
        if (ProfilerToFlamegraphConverter.getFileExtension(file.name) != extension) {
            return false
        }
        /* since nobody knows about this format only my plugin will generate cflamegraph files
         * so there is no need to validate cflamegraph files
         * (there is also another very important reason for this: I am hungry) */
        return true
    }

    override fun convert(file: File): Tree = Converter(file).tree
}