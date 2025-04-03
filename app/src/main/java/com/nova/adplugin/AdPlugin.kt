package com.nova.adplugin

import com.nova.adplugin.base.AdPluginBase
import com.nova.adplugin.log.printLog

object AdPlugin :AdPluginBase(){
    // 新增方法，参数使用具体子类
    fun loadAd(config: AdConfig.() -> Unit) {
        "加载nova广告".printLog()
        super.loadAd(config) // 调用基类方法
        // 子类特有逻辑
    }

    override fun removeAd(): Boolean {
        "移除nova广告".printLog()
        return super.removeAd()
    }

    override fun initialize() {
        "初始化nova插件".printLog()
        super.initialize()
    }
}
