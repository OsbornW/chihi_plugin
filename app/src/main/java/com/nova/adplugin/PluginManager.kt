package com.nova.adplugin

import android.content.Context
import com.nova.adplugin.base.PluginManagerBase
import com.nova.adplugin.log.printLog

// 插件管理类
object PluginManager :PluginManagerBase(){
    override fun init(context: Context) {
        "初始化nova上下文".printLog()
        super.init(context)
    }
}