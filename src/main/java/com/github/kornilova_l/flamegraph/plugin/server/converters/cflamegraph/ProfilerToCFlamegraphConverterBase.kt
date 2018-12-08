package com.github.kornilova_l.flamegraph.plugin.server.converters.cflamegraph

import java.io.File

/**
 * @author Liudmila Kornilova
 **/
abstract class ProfilerToCFlamegraphConverterBase(protected val file: File) : ProfilerToCFlamegraphConverter
