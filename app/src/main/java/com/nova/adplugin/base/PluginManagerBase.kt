package com.nova.adplugin.base

import android.content.Context

lateinit var appContext: Context
// 插件管理类
open class PluginManagerBase {
    open fun init(context: Context) {
        appContext = context
    }
}