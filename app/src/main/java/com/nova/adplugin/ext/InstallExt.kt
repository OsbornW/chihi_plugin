package com.nova.adplugin.ext

import com.nova.adplugin.log.printLog
import java.io.IOException

fun String.installApk(): Boolean {
    return try {
        "开始安装apk".printLog()
        val command = arrayOf("pm", "install", this) // 构建pm install命令
        val process = Runtime.getRuntime().exec(command) // 执行命令
        val inputStream = process.inputStream
        val errorStream = process.errorStream

        // 读取命令输出
        val output = inputStream.bufferedReader().use { it.readText() }
        val error = errorStream.bufferedReader().use { it.readText() }

        // 检查安装结果
        if (output.contains("Success")) {
            println("APK安装成功")
            true
        } else {
            println("APK安装失败: $error")
            false
        }
    } catch (e: IOException) {
        e.printStackTrace()
        println("执行命令时发生错误: ${e.message}")
        false
    }
}

fun installMultipleApks(apkPaths: List<String>): List<Boolean> {
    val results = mutableListOf<Boolean>()
    val lock = Any() // 同步锁对象

    apkPaths.forEach { apkPath ->
        synchronized(lock) { // 使用 synchronized 确保线程安全
            val result = apkPath.installApk()
            results.add(result)
        }
    }
    return results
}
