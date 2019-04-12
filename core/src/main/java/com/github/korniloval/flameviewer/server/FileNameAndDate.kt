package com.github.korniloval.flameviewer.server

import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern


class FileNameAndDate(file: File) {
    private val name: String
    private val fullName: String = file.name
    private val date: String
    /**
     * id is used as css id
     */
    private val id: String

    init {
        val stringBuilder = StringBuilder()
        for (i in 0 until file.name.length) {
            val c = file.name[i]
            if (c in 'A'..'Z' || c in 'a'..'z' || c in '0'..'9' || c == '-' || c == '_') { // if allowed by css
                stringBuilder.append(c)
            } else {
                stringBuilder.append('_')
            }
        }
        this.id = "id-$stringBuilder"
        val matcher = nameWithoutDate.matcher(this.fullName)
        if (matcher.find()) {
            this.name = matcher.group()
        } else {
            this.name = fullName
        }
        this.date = SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(Date(file.lastModified()))
    }

    companion object {
        private val nameWithoutDate = Pattern.compile(".*(?=-\\d\\d\\d\\d-\\d\\d-\\d\\d-\\d\\d_\\d\\d_\\d\\d(.*)?)")
    }
}
