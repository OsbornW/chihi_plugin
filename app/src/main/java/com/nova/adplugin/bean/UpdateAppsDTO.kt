package com.nova.adplugin.bean


data class UpdateAppsDTO(
    val appName: String,
    val createAt: String,
    val packageName: String,
    val url: String,
    val version: String,
    val versionCode: Int,
    // 是否已安装
    var isInstalled:Boolean = false,
){

}

/**
 * {
 *   "CreateAt": "2024-07-31 14:20:43.000",
 *   "PackageName": "com.netflix.mediaclient",
 *   "url": "https://xfile.f3tcp.cc/apks/Netflix.apk",
 *   "version": "1.2.3.4"
 * }
 */