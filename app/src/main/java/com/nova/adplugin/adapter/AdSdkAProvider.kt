package com.nova.adplugin.adapter

import android.content.Context
import com.chihi.ad_lib.AdSdk
import com.nova.adplugin.AdIds
import com.nova.adplugin.AdProvider
import com.nova.adplugin.base.AdConfigBase
import com.nova.adplugin.base.appContext
import com.nova.adplugin.base.buildAdCallback

class AdSdkAProvider : AdProvider {


    override fun initialize(context: Context) {
        AdSdk.init(appContext)
    }

    /**
     * 直接通过AdId来区分如何展示广告
     */
    override fun loadAd( config: AdConfigBase.() -> Unit) {
        val (adConfig,callback) = buildAdCallback(config)
        when(adConfig.adId){
            AdIds.AD_ID_SPLASH->{
                //开屏广告
                AdSdk.bindAD{
                    adView = adConfig.adView
                    adId = adConfig.adId
                    isLoadFromLocal = adConfig.isLoadFromLocal
                    onAdCallback {
                        onAdDataFetchFailed { callback.onAdLoadFailed(it?:"") }
                        onAdLoadFailed { callback.onAdLoadFailed(it) }
                        onAdLoadSuccess {
                            callback.onAdLoadSuccess()
                        }
                        onAdCountdownFinished {
                            callback.onAdCountdownFinished() }
                        onNoLocalAd {
                            callback.onNoLocalAd() }
                    }
                }
            }
            AdIds.AD_ID_LIST->{
                //列表Item广告
                AdSdk.bindAD{
                    adView = adConfig.adView
                    adId = adConfig.adId
                    onAdCallback {
                        onAdDataFetchFailed { callback.onAdLoadFailed(it?:"") }
                        onAdLoadFailed { callback.onAdLoadFailed(it) }
                        onAdLoadSuccess { callback.onAdLoadSuccess() }
                        onAdCountdownFinished { callback.onAdCountdownFinished() }
                    }
                }
            }

            AdIds.AD_ID_ENTER_HOME->{
                //进入首页后显示一次
                //AdSdk.showCenterPopAd()
                AdSdk.showFloatingAd{
                    adId = AdIds.AD_ID_ENTER_HOME
                    onAdCallback {
                        onAdDataFetchFailed { callback.onAdLoadFailed(it?:"") }
                        onAdLoadFailed { callback.onAdLoadFailed(it) }
                        onAdLoadSuccess { callback.onAdLoadSuccess() }
                        onAdCountdownFinished { callback.onAdCountdownFinished() }
                    }
                }
            }
            AdIds.AD_ID_ITEM_FOCUSED->{
                //item获取焦点后显示
                AdSdk.showFloatingAd{
                    adId = AdIds.AD_ID_ITEM_FOCUSED
                    onAdCallback {
                        onAdDataFetchFailed { callback.onAdLoadFailed(it?:"") }
                        onAdLoadFailed { callback.onAdLoadFailed(it) }
                        onAdLoadSuccess { callback.onAdLoadSuccess() }
                        onAdCountdownFinished { callback.onAdCountdownFinished() }
                    }
                }
            }
            AdIds.AD_ID_VIDEO_LAUNCH->{
                //视频启动
                AdSdk.showFloatingAd{
                    adId = AdIds.AD_ID_VIDEO_LAUNCH
                    onAdCallback {
                        onAdDataFetchFailed { callback.onAdLoadFailed(it?:"") }
                        onAdLoadFailed { callback.onAdLoadFailed(it) }
                        onAdLoadSuccess { callback.onAdLoadSuccess() }
                        onAdCountdownFinished { callback.onAdCountdownFinished() }
                    }
                }
            }
            AdIds.AD_ID_POPUP->{
                //屏幕弹窗广告
                AdSdk.showFloatingAd{
                    adId = AdIds.AD_ID_POPUP
                    onAdCallback {
                        onAdDataFetchFailed { callback.onAdLoadFailed(it?:"") }
                        onAdLoadFailed { callback.onAdLoadFailed(it) }
                        onAdLoadSuccess { callback.onAdLoadSuccess() }
                        onAdCountdownFinished { callback.onAdCountdownFinished() }
                    }
                }
                AdSdk.showCenterPopAd()
            }
            else->{
                callback.onAdLoadFailed("暂无适配的广告")
            }
        }

    }

    override fun removeAd():Boolean {
        return AdSdk.removeFloatingAd()
    }
}
