package com.github.kornilova203.flameviewer.converters.cflamegraph

/**
 * @author Liudmila Kornilova
 **/
class CFlamegraph(val lines: List<CFlamegraphLine>,
                  val classNames: Array<String>, // a "map" from id to class name
                  val methodNames: Array<String>, // a "map" from id to method name
                  val descriptions: Array<String>) // a "map" from id to description
