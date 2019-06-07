package com.github.korniloval.flameviewer.cli

import com.github.korniloval.flameviewer.server.DEFAULT_MAX_NUM_OF_VISIBLE_NODES
import com.github.korniloval.flameviewer.server.ServerOptions
import com.github.korniloval.flameviewer.server.ServerUtil.validateMaxNumOfVisibleNodes
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger

private val help = Option("help",  "show help message")
private val log = Option("log", true, "logger level e.g. off, info, warning, severe (default is warning)")
private val maxNumOfVisibleNodes = Option("maxNumOfVisibleNodes", true, "Maximum number of nodes that will be seen at any zoom level. Default is $DEFAULT_MAX_NUM_OF_VISIBLE_NODES")
private val defaultLogLevel = Level.WARNING
private val options = Options()
        .addOption(help)
        .addOption(log)
        .addOption(maxNumOfVisibleNodes)

private val helpFormatter = HelpFormatter()
private const val usage = "java -jar FlameViewerCli.jar <file>"

fun main(args: Array<String>) {
    val cl = DefaultParser().parse(options, args)
    if (cl.hasOption(help.opt)) {
        helpFormatter.printHelp(usage, options)
        return
    }
    if (cl.args.isEmpty()) {
        System.err.println("File is not specified")
        helpFormatter.printHelp(usage, options)
        return
    }
    val file = File(cl.args[0])
    if (!file.exists()) {
        System.err.println("File $file is not found")
        helpFormatter.printHelp(usage, options)
        return
    }
    val logLevel = getLogLevel(cl.getOptionValue(log.opt))
    Logger.getLogger("io.netty").level = logLevel
    val maxVisible = validateMaxNumOfVisibleNodes(cl.getOptionValue(maxNumOfVisibleNodes.opt)?.toIntOrNull(), CliLogger())
            ?: DEFAULT_MAX_NUM_OF_VISIBLE_NODES
    try {
        HttpServer.start(file, ServerOptions(maxVisible))
    } catch (throwable: Throwable) {
        throwable.printStackTrace()
    }
}

fun getLogLevel(lvl: String?): Level {
    if (lvl == null) return defaultLogLevel
    return try {
        Level.parse(lvl)
    } catch (ignored: IllegalArgumentException) {
        defaultLogLevel
    }
}
