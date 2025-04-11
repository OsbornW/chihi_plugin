package com.nova.adplugin.ext

import android.content.Context
import android.content.pm.PackageManager

fun Context.appVersionCode(): String {
    return try {
        val packageInfo = this.packageManager.getPackageInfo(this.packageName, 0)
        val versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            packageInfo.longVersionCode // API 28 及以上，使用 longVersionCode
        } else {
            packageInfo.versionCode.toLong() // 低于 API 28 版本，使用 versionCode
        }
        versionCode.toString()
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        "0"
    }
}
