package com.github.kornilova203.flameviewer.cli

import com.github.kornilova203.flameviewer.server.handlers.FindFile
import java.io.File


val cliFindFile: FindFile = { name ->
    val file = File(name)
    if (!file.exists()) null
    else file
}

fun encodeFileName(name: String): String =
        name.replace(" ", "%20")
                .replace("&", "%26")
                .replace("?", "%3f")
