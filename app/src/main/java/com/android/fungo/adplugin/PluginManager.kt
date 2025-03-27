package com.android.fungo.adplugin

import android.content.Context
import android.util.Log
import com.chihi.adplugin.log.printLog

lateinit var appContext: Context
// 插件管理类
object PluginManager {
    fun init(context: Context) {
        appContext = context
    }
}