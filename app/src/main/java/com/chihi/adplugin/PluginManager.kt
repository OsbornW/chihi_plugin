package com.chihi.adplugin

import android.content.Context
import android.util.Log
import com.chihi.adplugin.log.printLog

lateinit var appContext: Context
// 插件管理类
object PluginManager {
    fun init(context: Context) {
        appContext = context
        "插件内部上下文Context赋值完成".printLog()
    }
}