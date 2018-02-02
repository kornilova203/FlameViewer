package com.github.kornilova_l.flamegraph.plugin

import com.github.kornilova_l.flamegraph.plugin.server.ProfilerHttpRequestHandler.getParameter
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.Logger
import io.netty.handler.codec.http.QueryStringDecoder
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URISyntaxException
import java.nio.file.Path
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

/**
 * IDEA system dir
 * |-- flamegraph-profiler
 * |-- configuration // where configuration is exported after
 * |-- log
 * |-- deleted // deleted files temporary stored in this dir (they are returned back if `undo` is pressed)
 * |-- uploaded-files
 * |-- flamegraph // uploaded files in flamegraph format
 * |-- not-converted // files are stored here before conversion
 * \-- ser // uploaded .ser files
 */
object PluginFileManager {
    private val LOG = Logger.getInstance(PluginFileManager::class.java)

    private const val PLUGIN_DIR_NAME = "flamegraph-profiler"
    private const val LOG_DIR_NAME = "log"
    private const val CONFIG_DIR_NAME = "configuration"
    private const val STATIC_DIR_NAME = "static"
    private const val REQUEST_PREFIX = "/flamegraph-profiler/"
    private const val UPLOADED_FILES = "uploaded-files"
    private const val DELETED_FILES = "deleted"
    private const val NOT_CONVERTED = "not-converted"
    private const val SER_FILES = "ser"
    private const val FLAMEGRAPH_FILES = "flamegraph"

    val serFileSaver: FileSaver
    val tempFileSaver: FileSaver // save files before converting
    val flamegraphFileSaver: FlamegraphSaver
    val logDirPath: Path // for tests
    private val uploadedFilesDir: File
    private val configDirPath: Path
    private val staticDirPath: Path

    init {
        val systemDirPath = PathManager.getSystemPath()
        val systemDir = Paths.get(systemDirPath)
        val pluginDir = Paths.get(systemDir.toString(), PLUGIN_DIR_NAME)
        createDirIfNotExist(pluginDir)
        logDirPath = Paths.get(pluginDir.toString(), LOG_DIR_NAME)
        createDirIfNotExist(logDirPath)
        configDirPath = Paths.get(pluginDir.toString(), CONFIG_DIR_NAME)
        createDirIfNotExist(configDirPath)
        try {
            staticDirPath = Paths.get(javaClass.getResource("/" + STATIC_DIR_NAME).toURI())
        } catch (e: URISyntaxException) {
            throw AssertionError("Cannot find static dir.", e)
        }

        val uploadedFilesPath = Paths.get(logDirPath.toString(), UPLOADED_FILES)
        createDirIfNotExist(uploadedFilesPath)
        uploadedFilesDir = uploadedFilesPath.toFile()

        val deletedFilesPath = Paths.get(logDirPath.toString(), DELETED_FILES)
        createDirIfNotExist(deletedFilesPath)

        val notConvertedFiles = Paths.get(uploadedFilesPath.toString(), NOT_CONVERTED)
        createDirIfNotExist(notConvertedFiles)
        tempFileSaver = FileSaver(notConvertedFiles)
        val serFiles = Paths.get(uploadedFilesPath.toString(), SER_FILES)
        createDirIfNotExist(serFiles)
        serFileSaver = FileSaver(serFiles)
        val flamegraphFiles = Paths.get(uploadedFilesPath.toString(), FLAMEGRAPH_FILES)
        createDirIfNotExist(flamegraphFiles)
        clearDir(File(notConvertedFiles.toString()))
        flamegraphFileSaver = FlamegraphSaver(flamegraphFiles)

        finallyDeleteRemovedFiles()
    }

    val projectList: List<String>
        @Synchronized get() {
            removeEmptyProjects()
            val logDir = logDirPath.toFile()
            if (logDir.exists() && logDir.isDirectory) {
                val files = logDir.listFiles()
                if (files != null) {
                    return files.filter { file ->
                        file.name != UPLOADED_FILES &&
                                file.name != DELETED_FILES &&
                                file.isDirectory
                    }.map { it.name }
                }
            }
            return ArrayList()
        }

    private fun clearDir(dir: File) {
        val files = dir.listFiles() ?: return
        for (file in files) {
            file.delete()
        }
    }

    private fun finallyDeleteRemovedFiles(): Boolean {
        val deletedFilesDir = Paths.get(logDirPath.toString(), DELETED_FILES).toFile()
        if (!deletedFilesDir.exists() || !deletedFilesDir.isDirectory) {
            LOG.debug("Directory with deleted files was not found")
            return false
        }
        val files = deletedFilesDir.listFiles()
        files ?: return true
        return files.map { it.delete() }.all { it } // all files were deleted
    }

    @Synchronized
    fun getLogFile(urlDecoder: QueryStringDecoder): File? {
        val projectName = getParameter(urlDecoder, "project")
        val fileName = getParameter(urlDecoder, "file")
        return if (projectName == null || fileName == null) {
            null
        } else getLogFile(projectName, fileName)
    }

    @Synchronized
    fun getConfigurationFile(projectName: String): File {
        val path = Paths.get(configDirPath.toString(), projectName + ".config")
        return File(path.toString())
    }

    @Synchronized
    fun createLogFile(projectName: String, configurationName: String): File {
        val logDir = getLogDirPath(projectName)
        val logFile = Paths.get(logDir.toString(),
                configurationName + "-" + SimpleDateFormat("yyyy-MM-dd-HH_mm_ss").format(Date()) + ".ser")
        return File(logFile.toString())
    }

    @Synchronized
    fun getFileNameList(projectName: String): List<FileNameAndDate> {
        val projectLogDir = getLogDirPath(projectName)
        val fileNames = ArrayList<FileNameAndDate>()
        if (projectName != UPLOADED_FILES) {
            addFilesFromDirToList(projectLogDir, fileNames)
            return fileNames
        }
        val dirsInsideUploaded = uploadedFilesDir.listFiles() ?: return fileNames
        for (dir in dirsInsideUploaded) {
            if (dir.isDirectory) {
                addFilesFromDirToList(dir, fileNames)
            }
        }
        return fileNames
    }

    private fun addFilesFromDirToList(projectLogDir: File, fileNames: MutableList<FileNameAndDate>) {
        val files = projectLogDir.listFiles() ?: return
        files.sortBy { it.lastModified() }
        for (file in files) {
            if (file.isFile) {
                fileNames.add(FileNameAndDate(file))
            }
        }
    }

    @Synchronized
    fun getStaticFile(staticFileUri: String): File? {
        return Paths.get(
                staticDirPath.toString(),
                staticFileUri.substring(REQUEST_PREFIX.length, staticFileUri.length)
        ).toFile()
    }

    private fun getLogDirPath(projectName: String): File {
        val path = Paths.get(logDirPath.toString(), projectName)
        createDirIfNotExist(path)
        return path.toFile()
    }

    @Synchronized
    fun getPathToJar(jarName: String): String? {
        val url = javaClass.getResource("/" + jarName)
        try {
            return Paths.get(url.toURI()).toString()
        } catch (e: URISyntaxException) {
            LOG.error(e)
        }
        return null
    }

    @Synchronized
    fun getLogFile(projectName: String, fileName: String): File? {
        return if (projectName != UPLOADED_FILES) {
            Paths.get(logDirPath.toString(), projectName, fileName).toFile()
        } else findFileInSubDirectories(fileName, uploadedFilesDir)
    }

    @Synchronized
    fun getLatestFileName(projectName: String): String? {
        val dirPath = Paths.get(logDirPath.toString(), projectName)
        val dir = File(dirPath.toString())
        if (dir.exists() && dir.isDirectory) {
            val latestFile = getLatestFile(dir) ?: return null
            return latestFile.name
        }
        return null
    }

    private fun removeEmptyProjects() {
        val logDir = File(logDirPath.toString())
        if (logDir.exists() && logDir.isDirectory) {
            val projects = logDir.listFiles()
            if (projects != null) {
                for (project in projects) {
                    if (project.isDirectory) {
                        if (project.name == UPLOADED_FILES || project.name == DELETED_FILES) {
                            continue
                        }
                        val projectFiles = project.listFiles()
                        if (projectFiles == null || projectFiles.isEmpty()) {
                            project.delete()
                        }
                    }
                }
            }
        }
    }

    /**
     * Mainly used for test
     */
    @Synchronized
    fun deleteAllUploadedFiles() {
        val dirsInsideUploadedFiles = uploadedFilesDir.listFiles() ?: return
        for (maybeDir in dirsInsideUploadedFiles) {
            if (maybeDir.isDirectory) {
                val files = maybeDir.listFiles() ?: continue
                for (file in files) {
                    val res = file.delete()
                    if (!res) {
                        System.err.println("Cannot delete file: " + file)
                    }
                }
            }
        }
    }

    @Synchronized
    fun deleteFile(fileName: String, projectName: String) {
        val file = getLogFile(projectName, fileName)
        if (file == null || !file.exists()) {
            return
        }
        /* uploaded files are stored in separate directories
         * (name of a directory is an id of converter that is responsible for the file)
         * when we move file to temporal directory we want to save converter id,
         * so if delete action is undone we can move file back to needed directory */
        val res: Boolean
        @Suppress("LiftReturnOrAssignment")
        if (projectName == "uploaded-files") {
            val converterId = file.parentFile.name
            val newDir = Paths.get(logDirPath.toString(), DELETED_FILES, converterId).toFile()
            if (!newDir.exists()) {
                if (!newDir.mkdir()) {
                    LOG.warn("Cannot create directory to move deleted file. File: $file Dir: $newDir")
                    return
                }
            }
            res = file.renameTo(Paths.get(newDir.toString(), fileName).toFile())
        } else {
            res = file.renameTo(Paths.get(logDirPath.toString(), DELETED_FILES, fileName).toFile())
        }
        if (!res) {
            LOG.warn("Cannot move file to DELETED_FILES directory. File: " + file)
        }
    }

    @Synchronized
    fun undoDeleteFile(fileName: String, projectName: String) {
        val deletedFile = getDeletedFile(fileName, projectName)
        if (deletedFile == null || !deletedFile.exists()) {
            LOG.debug("Undo delete. Cannot find file to undo delete: " + fileName)
            return
        }
        val projectDirPath = if (projectName == "uploaded-files") {
            val converterId = deletedFile.parentFile.name
            Paths.get(logDirPath.toString(), UPLOADED_FILES, converterId).toFile()
        } else {
            Paths.get(logDirPath.toString(), projectName).toFile()
        } ?: return

        val res = deletedFile.renameTo(Paths.get(projectDirPath.toString(), fileName).toFile())
        if (!res) {
            LOG.warn("Cannot move file back from temp directory. File: " + fileName)
        }
    }

    private fun getDeletedFile(fileName: String, projectName: String): File? {
        val deletedFilesDir = Paths.get(logDirPath.toString(), DELETED_FILES).toFile()
        return if (projectName == "uploaded-files") {
            findFileInSubDirectories(fileName, deletedFilesDir)
        } else {
            Paths.get(deletedFilesDir.toString(), fileName).toFile()
        }
    }

    private fun findFileInSubDirectories(fileName: String, dir: File): File? {
        val subDirs = dir.listFiles() ?: return null
        for (subDir in subDirs) {
            if (subDir.isDirectory && subDir.name != DELETED_FILES && subDir.name != NOT_CONVERTED) {
                val files = subDir.listFiles() ?: continue
                for (file in files) {
                    if (file.name == fileName) {
                        return file
                    }
                }
            }
        }
        return null
    }

    fun moveFileToUploadedFiles(converterId: String, fileName: String, file: File) {
        val dir = Paths.get(uploadedFilesDir.toString(), converterId).toFile()
        if (!dir.exists()) {
            val res = dir.mkdir()
            if (!res) {
                LOG.error("Cannot save file $fileName to $converterId directory.")
                return
            }
        }
        val newFile = Paths.get(dir.toString(), fileName).toFile()
        val success = file.renameTo(newFile)
        if (!success) {
            LOG.error("Cannot move file $file to $converterId directory.")
        }
    }

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
            this.id = "id-" + stringBuilder.toString()
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

    open class FileSaver internal constructor(internal val dir: Path) {

        fun save(bytes: ByteArray, fileName: String): File? {
            val file = Paths.get(dir.toString(), fileName).toFile()
            try {
                FileOutputStream(file).use { outputStream ->
                    outputStream.write(bytes)
                    return file
                }
            } catch (e: IOException) {
                LOG.error(e)
            }

            return null
        }

        fun moveToDir(file: File, newFileName: String): File? {
            val newFile = Paths.get(dir.toString(), newFileName).toFile()
            val success = file.renameTo(newFile)
            if (!success) {
                LOG.error("Cannot move file " + file)
                return null
            }
            return newFile
        }
    }

    class FlamegraphSaver internal constructor(dir: Path) : FileSaver(dir) {

        fun save(stacks: Map<String, Int>, fileName: String): File? {
            val file = Paths.get(dir.toString(), fileName).toFile()
            try {
                FileOutputStream(file).use { outputStream ->
                    for ((key, value) in stacks) {
                        outputStream.write((key + " " + value + "\n").toByteArray())
                    }
                    return file
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return null
        }
    }

    private fun createDirIfNotExist(path: Path) {
        val dir = File(path.toString())
        if (!dir.exists()) {
            try {
                assert(dir.mkdir())
            } catch (se: SecurityException) {
                LOG.error(se)
            }

        }
    }

    private fun getLatestFile(dir: File): File? {
        val files = dir.listFiles() ?: return null
        return files.maxBy { it.lastModified() } ?: return null
    }

    fun getParentDirName(file: File): String? {
        val parentFile = file.parentFile ?: return null
        return parentFile.name
    }

}