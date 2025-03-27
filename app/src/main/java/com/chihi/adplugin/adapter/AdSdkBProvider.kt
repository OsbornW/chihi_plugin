package com.chihi.adplugin.adapter

import android.app.Application
import android.content.Context
import com.chihi.adplugin.AdIds
import com.chihi.adplugin.AdProvider
import com.chihi.adplugin.manager.FloatingAdManager
import com.nova.adplugin.BaseAdConfig
import com.nova.adplugin.buildAdCallback
import com.sjkj.ad.AdManager

class AdSdkBProvider : AdProvider {
    override fun initialize(context: Context) {
        val config = com.sjkj.ad.common.AdConfig.Builder()
            .appId("AD_SZ_20241213_9949413184") //appId，由sdk提供⽅分配
            .isDebug(true) //可选，是否为debug模式，debug模式时会打印更多log，供调试
            .build()
        AdManager.getInstance().init(context.applicationContext as Application, config)
    }

    override fun loadAd( config: BaseAdConfig.() -> Unit) {
        val (adConfig,callback) = buildAdCallback(config)
        when(adConfig.adId){
            AdIds.AD_ID_SPLASH->{
                callback.onAdLoadFailed("暂无适配的广告")
            }
            AdIds.AD_ID_LIST->{
                //列表Item广告
                callback.onAdLoadFailed("暂无适配的广告")
            }

            AdIds.AD_ID_ENTER_HOME->{
                //进入首页后显示一次
                FloatingAdManager.showFloatingAD(config)
            }
            AdIds.AD_ID_ITEM_FOCUSED->{
                //item获取焦点后显示
                FloatingAdManager.showFloatingAD(config)

            }
            AdIds.AD_ID_VIDEO_LAUNCH->{
                //视频启动
                callback.onAdLoadFailed("暂无适配的广告")
            }
            AdIds.AD_ID_POPUP->{
                //屏幕弹窗广告
            }
            else->{
                callback.onAdLoadFailed("暂无适配的广告")
            }
        }
    }

    override fun removeAd(): Boolean {
        return FloatingAdManager.removeFloatingAd()
    }

}
