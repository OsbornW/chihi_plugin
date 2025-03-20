package com.chihi.adplugin.ext

import android.util.Log
import com.chihi.adplugin.appContext
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest


fun String.getFileExtension(): String {
    val lastDotIndex = this.lastIndexOf(".")
    return if (lastDotIndex > 0) this.substring(lastDotIndex) else ""
}

fun String.getBasePath(): String {
    val folderPath = "${appContext.filesDir.absolutePath}/$this"
    val folder = File(folderPath)

    // 创建 header 文件夹（如果不存在）
    if (!folder.exists()) {
        folder.mkdirs()
    }
    return folderPath
}


fun deleteApkFilesInPluginDir(): Boolean {
    val pluginDir = File(appContext.filesDir, "plugin")
    if (pluginDir.exists() && pluginDir.isDirectory) {
        // 获取目录下所有 .apk 文件
        val apkFiles = pluginDir.listFiles { file ->
            file.extension == "apk"
        }

        // 删除每个 .apk 文件
        apkFiles?.forEach { file ->
            if (file.exists()) {
                val deleted = file.delete()
                if (deleted) {
                } else {
                }
            }
        }
        return true
    } else {
        Log.e("FileDeletion", "Plugin directory does not exist or is not a directory.")
    }
    return false
}

/**
 * 删除 filesDir 下的 ad 和 plugin 目录
 *
 * @param appContext 应用的 Context
 */
fun deleteAdAndPluginDirectories() {
    val filesDir = appContext.filesDir
    val adDir = File(filesDir, "ad")
    val pluginDir = File(filesDir, "plugin")

    if (adDir.exists() && deleteDirectory(adDir)) {
    } else {
    }

    if (pluginDir.exists() && deleteDirectory(pluginDir)) {
    } else {
    }
}

/**
 * 删除指定目录及其内容
 *
 * @param dir 要删除的目录
 * @return 是否删除成功
 */
fun deleteDirectory(dir: File): Boolean {
    if (dir.exists()) {
        dir.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                deleteDirectory(file) // 递归删除子目录
            } else {
                file.delete() // 删除文件
            }
        }
    }
    return dir.delete() // 删除空目录
}


fun String.md5(): String {
    val file = File(this)
    if (!file.exists()) return ""

    val digest = MessageDigest.getInstance("MD5")
    val inputStream = FileInputStream(file)
    val buffer = ByteArray(1024)
    var bytesRead: Int

    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
        digest.update(buffer, 0, bytesRead)
    }
    inputStream.close()

    return digest.digest().joinToString("") { "%02x".format(it) }
}


fun String.getApkFileNameFromUrl(): String {
    val regex = """([^/]+)\.(apk)""".toRegex()
    val matchResult = regex.find(this)
    return matchResult?.value ?: ""
}
