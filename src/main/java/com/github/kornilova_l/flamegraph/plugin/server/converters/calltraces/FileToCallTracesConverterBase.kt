package com.github.kornilova_l.flamegraph.plugin.server.converters.calltraces

import java.io.File

/**
 * @author Liudmila Kornilova
 **/
abstract class FileToCallTracesConverterBase(protected val file: File) : FileToCallTracesConverter
