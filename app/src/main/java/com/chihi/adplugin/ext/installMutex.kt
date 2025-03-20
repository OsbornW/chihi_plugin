package com.chihi.adplugin.ext

import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.chihi.adplugin.appContext
import java.io.File
import java.io.FileInputStream

private val installMutex = Any() // 使用同步锁代替 Mutex

fun String.silentInstallWithMutex( callback: (Boolean) -> Unit) {

    synchronized(installMutex) {

        silentInstall(callback)
    }
}

fun String.silentInstall(callback: (Boolean) -> Unit) {
    val packageManager = appContext.packageManager
    val packageInstaller = packageManager.packageInstaller
    val apkFile = File(this)

    // 获取 APK 的包名和版本号
    val packageInfo = packageManager.getPackageArchiveInfo(this, 0)
    val packageName = packageInfo?.packageName
        ?: return callback(false)

    // 获取 APK 的版本号
    val apkVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        packageInfo.longVersionCode
    } else {
        @Suppress("DEPRECATION")
        packageInfo.versionCode.toLong()
    }

    // 检查当前安装的版本号
    var installedVersionCode: Long
    try {
        val installedPackageInfo = packageManager.getPackageInfo(packageName, 0)
        installedVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            installedPackageInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            installedPackageInfo.versionCode.toLong()
        }
    } catch (e: PackageManager.NameNotFoundException) {
        installedVersionCode = -1
    }

    if (installedVersionCode >= apkVersionCode) {
        // 如果本地版本号更高，先卸载应用
        packageName.uninstallApp { isUninstallSuccess ->
            if (isUninstallSuccess) {
                performInstallation(packageInstaller, apkFile, callback)
            } else {
                callback(false)
            }
        }
    } else {
        // 直接进行安装
        performInstallation(packageInstaller, apkFile, callback)
    }
}

fun String.uninstallApp(callback: (Boolean) -> Unit) {
    try {
        val packageName = this
        val packageManager = appContext.packageManager
        val packageInstaller = packageManager.packageInstaller
        val params =
            PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
                setAppPackageName(packageName)
            }
        val sessionId = packageInstaller.createSession(params)

        // 创建卸载的 Intent
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = Uri.parse("package:$packageName")
        }
        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            sessionId,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val sender = pendingIntent.intentSender

        // 执行卸载
        packageInstaller.uninstall(packageName, sender)

        // 使用 Handler 进行轮询检查包是否被卸载
        val handler = Handler(Looper.getMainLooper())
        val pollingInterval = 1000L // 轮询间隔时间，单位为毫秒
        val maxRetries = 5 // 最大重试次数
        var retryCount = 0

        val checkUninstallRunnable = object : Runnable {
            override fun run() {
                try {
                    appContext.packageManager.getPackageInfo(packageName, 0)
                    // 应用还在
                    if (retryCount < maxRetries) {
                        retryCount++
                        handler.postDelayed(this, pollingInterval)
                    } else {
                        callback(false) // 超过最大重试次数仍然没卸载成功
                    }
                } catch (e: Exception) {
                    // 应用已经卸载
                    callback(true)
                }
            }
        }

        handler.post(checkUninstallRunnable)
    } catch (e: Exception) {
        callback(false)
    }
}

private fun performInstallation(
    packageInstaller: PackageInstaller,
    apkFile: File,
    callback: (Boolean) -> Unit
) {
    val sessionId =
        packageInstaller.createSession(PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL))
    val session = packageInstaller.openSession(sessionId)
    val out = session.openWrite("app_install", 0, -1)
    val buffer = ByteArray(65536)
    var c: Int
    val inputStream = FileInputStream(apkFile)
    try {
        while (inputStream.read(buffer).also { c = it } != -1) {
            out.write(buffer, 0, c)
        }
        session.fsync(out)
    } finally {
        out.close()
        inputStream.close()
    }

    val installCallback = object : PackageInstaller.SessionCallback() {
        override fun onCreated(sessionId: Int) {}

        override fun onFinished(sessionId: Int, success: Boolean) {
            callback(success) // 返回安装结果
        }

        override fun onActiveChanged(sessionId: Int, active: Boolean) {}
        override fun onBadgingChanged(sessionId: Int) {}
        override fun onProgressChanged(sessionId: Int, progress: Float) {}
    }

    val handler = Handler(Looper.getMainLooper())
    packageInstaller.registerSessionCallback(installCallback, handler)

    val intent = Intent("INSTALL_ACTION").apply {
        // 设置 intent 的 Action 和其他数据
    }
    val pendingIntent =
        PendingIntent.getBroadcast(appContext, sessionId, intent, PendingIntent.FLAG_IMMUTABLE)
    session.commit(pendingIntent.intentSender)
}

fun File.installApkForNormalApp() {
   /* val apkUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        // 适配 Android 7.0 及以上版本，使用 FileProvider 提供文件 URI
        FileProvider.getUriForFile(appContext, "${appContext.packageName}.provider", this)
    } else {
        Uri.fromFile(this)
    }

    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(apkUri, "application/vnd.android.package-archive")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            flags = flags or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
    }

    try {
        appContext.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
    }*/
}

fun String.uninstallApkForNormalApp() {
    val packageUri: Uri = Uri.parse("package:$this")
    val intent = Intent(Intent.ACTION_DELETE).apply {
        data = packageUri
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }

    try {
        appContext.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Log.e("UninstallError", "No activity found to handle APK uninstall", e)
    }
}

fun String.isUninstalled(): Boolean {
    try {
        appContext.packageManager.getPackageInfo(this, 0)
        // 应用还在
        return false
    } catch (e: Exception) {
        // 应用已经卸载
        return true
    }
}