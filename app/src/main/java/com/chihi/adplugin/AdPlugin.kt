package com.chihi.adplugin

import com.nova.adplugin.base.AdPluginBase

object AdPlugin :AdPluginBase(){
    // 新增方法，参数使用具体子类
    fun loadAd(config: AdConfig.() -> Unit) {
        super.loadAd(config) // 调用基类方法
        // 子类特有逻辑
    }

    fun test1(){
        println("打印了Test1")
    }

    override fun initialize() {
        println("我是子类的初始化initialize")
        super.initialize()
    }
}
