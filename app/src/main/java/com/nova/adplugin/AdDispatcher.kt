package com.nova.adplugin

import com.nova.adplugin.ext.getFromSP
import com.nova.adplugin.ext.saveToSP
import com.nova.adplugin.base.AdConfigBase
import com.nova.adplugin.base.buildAdCallback

class AdDispatcher(private val providers: List<AdProvider>) {
    var currentProviderIndex:Int = 0 // 当前提供商索引
    private var retryCount = 0 // 连续失败次数
    private val maxRetryLimit = providers.size // 最大重试次数

    // 用 Map 保存广告位的广告显示状态，key 为广告 ID，value 为广告显示状态
     val adVisibilityMap = mutableMapOf<String, Boolean>()

    // 添加方法检查广告 ID 是否正在显示
    fun isAdVisible(adId: String): Boolean {
        return adVisibilityMap[adId] == true
    }

    // 记录广告 ID 的显示状态
    fun setAdVisibility(adId: String, isVisible: Boolean) {
        adVisibilityMap[adId] = isVisible
    }

    // 轮询选择广告 SDK
     fun getNextProvider(): AdProvider? {
        if (retryCount >= maxRetryLimit) return null // 超过限制，返回 null
        val provider = providers[currentProviderIndex]
        currentProviderIndex = (currentProviderIndex + 1) % providers.size
        currentProviderIndex.toString().saveToSP("sdk_index")
        return provider
    }

    // 加载广告
     inline fun <reified T : AdConfigBase> loadAd(config: T.() -> Unit){
        val (adConfig,callback) = buildAdCallback(config)
        currentProviderIndex = getFromSP<Int>("sdk_index") ?:0
        // 如果相同的广告 ID 已经在显示，则不加载
        /*if (isAdVisible(adConfig.adId)) {
            return
        }*/
        val provider = getNextProvider()
        loadAdWithRetry( adConfig,callback, provider)
    }

    fun removeAd():Boolean {
        return providers.any { it.removeAd() }
    }


    // 递归实现重试逻辑
     fun loadAdWithRetry(
        adConfig: AdConfigBase,
        callback: AdLoadCallbackBuilder,
        provider: AdProvider?
    ) {
        if (provider == null) { // 没有更多提供商
            callback.onAdLoadFailed("All providers failed after $retryCount attempts.")
            retryCount = 0 // 重置重试次数
            //setAdVisibility(adConfig.adId,false)
            return
        }

        provider.loadAd{
            applyConfig(adConfig)
            onAdCallback {
                onAdLoadSuccess {
                    //setAdVisibility(adConfig.adId,true)
                    retryCount = 0 // 成功加载，重置重试次数
                    callback.onAdLoadSuccess() // 通知调用者成功
                }
                onAdLoadFailed {
                    retryCount++ // 累加失败次数
                    loadAdWithRetry(adConfig, callback, getNextProvider())    // 尝试下一个提供商
                }
                onAdCountdownFinished {
                    callback.onAdCountdownFinished()
                }
                onNoLocalAd {
                    callback.onNoLocalAd()
                }
            }
        }
    }



}
