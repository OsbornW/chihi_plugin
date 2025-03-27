package com.chihi.adplugin

import com.nova.adplugin.base.AdPluginBase

object AdPlugin :AdPluginBase(){
    // 新增方法，参数使用具体子类
    fun loadAd(config: AdConfig.() -> Unit) {
        super.loadAd(config) // 调用基类方法
        // 子类特有逻辑
    }
}
