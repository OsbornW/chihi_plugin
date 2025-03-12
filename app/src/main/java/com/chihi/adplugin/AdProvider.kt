package com.chihi.adplugin

import android.content.Context

interface AdProvider {
    // 初始化广告 SDK
    fun initialize(context: Context){}

    // 加载广告
    fun loadAd(config: AdConfig.() -> Unit){}

    // 移除广告（针对悬浮窗显示的情况）
    fun removeAd():Boolean = false


}