package com.github.kornilova_l.flamegraph.plugin.server.converters.calltree

import java.io.File

/**
 * @author Liudmila Kornilova
 **/
abstract class FileToCallTreeConverterBase(protected val file: File) : FileToCallTreeConverter
