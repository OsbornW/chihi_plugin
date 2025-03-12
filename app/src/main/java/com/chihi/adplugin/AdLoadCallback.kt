package com.chihi.adplugin

interface AdLoadCallback {

    fun onAdDataFetchStart() {
        // 广告数据获取成功
    }

    fun onAdDataFetchSuccess() {
        // 广告数据获取成功
    }

    fun onAdDataFetchFailed(errorMsg:String?) {
        // 广告数据获取失败
    }

    fun onNoAdData() {
        // 暂无广告数据
    }

    fun onAdDataLoading() {
        // 广告加载中
    }

    fun onAdLoading() {
        // 广告加载中
    }

    fun onAdLoadSuccess() {
        // 广告加载成功
    }

    fun onAdLoadFailed(errorMsg:String) {
        // 广告加载失败
    }

    fun onAdClosed() {
        // 广告关闭
    }

    fun onAdClicked(adId:String,adUrl:String?=null) {
        // 广告被点击
    }

    fun onAdCountdownFinished() {
        // 广告倒计时结束
    }

    fun onNoLocalAd() {
        // 无本地缓存的广告
    }

}
