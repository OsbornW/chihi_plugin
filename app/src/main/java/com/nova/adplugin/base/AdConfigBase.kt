package com.nova.adplugin.base

import android.view.ViewGroup
import com.nova.adplugin.AdLoadCallbackBuilder

open class AdConfigBase {

    var adView: ViewGroup?=null               // 广告容器，必传
    var adId: String  = ""                 // 广告ID，必传

    val apiKey: String = ""  // API Key 或其他身份验证信息
    var isLoadFromLocal: Boolean = false
    var isAutoFocus: Boolean = true
    var onAdCallback: AdLoadCallbackBuilder.() -> Unit = {}

    //对所有的广告新增一些统一控制的字段
    var isClosable: Int  = 1             // 是否可以跳过广告（后台返回0或1,1可以跳过）
    var adSkipTime: Long  = 5000             // 多久后可以跳过广告（具体毫秒数）
    var position: Int = 0              // 显示位置：后台返回的数字
    var floatingWidth: Int?  = 500             // 悬浮窗宽度
    var floatingHeight: Int?  = 280             // 悬浮窗高度
    var floatingX: Int  = 0             // 悬浮窗X轴偏移量
    var floatingY: Int  = 0            // 悬浮窗Y轴偏移量
    var isCountdownVisible: Boolean  = true   // 广告倒计时是否可见（默认不可见）

    // 赋值方法
    fun applyConfig(config: AdConfigBase) {
        this.adView = config.adView
        this.adId = config.adId
        this.isLoadFromLocal = config.isLoadFromLocal
        this.isAutoFocus = config.isAutoFocus
        this.onAdCallback = config.onAdCallback
        this.isClosable = config.isClosable
        this.adSkipTime = config.adSkipTime
        this.position = config.position
        this.floatingWidth = config.floatingWidth
        this.floatingHeight = config.floatingHeight
        this.floatingX = config.floatingX
        this.floatingY = config.floatingY
        this.isCountdownVisible = config.isCountdownVisible
    }

    // 使用get方法转换isClosable为Boolean
    val isClosableBoolean: Boolean get() = isClosable == 1

    fun onAdCallback(callback: AdLoadCallbackBuilder.() -> Unit): AdConfigBase {
        this.onAdCallback = callback
        return this
    }

    val positionEnum: Position get() = Position.fromInt(position)

}

inline fun <reified T : AdConfigBase> buildAdCallback(config: T.() -> Unit): Pair<T, AdLoadCallbackBuilder> {
    // 通过反射创建T的实例
    val adConfig = T::class.java.newInstance().apply(config)
    val callback = AdLoadCallbackBuilder().apply(adConfig.onAdCallback)
    return Pair(adConfig, callback)
}

// 广告显示位置：例如居中与右下角
enum class Position(val backendValue: Int) {
    RIGHT_BOTTOM(0),    // 右下角
    LEFT_TOP(1),        // 左上
    TOP_CENTER(2),      // 上中
    RIGHT_TOP(3),       // 右上
    LEFT_BOTTOM(4),     // 左下
    BOTTOM_CENTER(5),   // 下中
    CENTER(6),          // 中间
    LEFT_CENTER(7),     // 左中
    RIGHT_CENTER(8);    // 右中

    companion object {
        fun fromInt(value: Int): Position {
            return entries.firstOrNull { it.backendValue == value }
                ?: RIGHT_BOTTOM // 默认返回右下角
        }
    }
}