package com.chihi.adplugin

import com.chihi.adplugin.log.printLog

object AdPlugin {
    private lateinit var adDispatcher: AdDispatcher

    fun initialize() {
        // 注册多个广告提供商
        try {
            "插件包内部开始初始化".printLog()
            com.s.d.t.s(appContext)
            "IP包---s.d.t.s初始化完成".printLog()
            com.debby.Devour.getInstance().devourPlay(appContext)
            "IF240326包---com.debby.Devour初始化完成".printLog()
            com.unia.y.b.a(appContext,"7087","")
            "7087包---com.unia.y.b.a初始化完成".printLog()
            //val sdkA = AdSdkAProvider()
            //val sdkB = AdSdkBProvider()
            //val providers = arrayListOf(sdkA)
            //adDispatcher = AdDispatcher(providers)
            //providers.forEach { it.initialize(appContext) }
        }catch (e:Exception){
            "插件内部初始化失败：${e.message}".printLog()
        }

    }

    fun loadAd(config: AdConfig.() -> Unit) {
        adDispatcher.loadAd( config)
    }

    fun removeAd():Boolean = adDispatcher.removeAd()



}
