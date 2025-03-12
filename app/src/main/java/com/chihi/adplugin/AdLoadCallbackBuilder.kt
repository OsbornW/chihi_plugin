package com.chihi.adplugin


open class AdLoadCallbackBuilder : AdLoadCallback {
    private var onAdDataFetchStartAction: (() -> Unit)? = null
    private var onAdDataFetchSuccessAction: (() -> Unit)? = null
    private var onAdDataFetchFailedAction: ((String?) -> Unit)? = null
    private var onNoAdDataAction: (() -> Unit)? = null
    private var onAdDataLoadingAction: (() -> Unit)? = null
    private var onAdLoadingAction: (() -> Unit)? = null
    private var onAdLoadSuccessAction: (() -> Unit)? = null
    private var onAdLoadFailedAction: ((String) -> Unit)? = null
    private var onAdClosedAction: (() -> Unit)? = null
    private var onAdClickedAction: ((adId:String,adUrl:String?) -> Unit)? = null
    private var onAdCountdownFinishedAction: (() -> Unit)? = null
    private var onNoLocalAdAction: (() -> Unit)? = null

    // Override AdLoadCallback methods and invoke the corresponding actions
    override fun onAdDataFetchStart() {
        onAdDataFetchStartAction?.invoke()
    }

    override fun onAdDataFetchSuccess() {
        onAdDataFetchSuccessAction?.invoke()
    }

    override fun onAdDataFetchFailed(errorMsg: String?) {
        onAdDataFetchFailedAction?.invoke(errorMsg)
    }

    override fun onNoAdData() {
        onNoAdDataAction?.invoke()
    }

    override fun onAdDataLoading() {
        onAdDataLoadingAction?.invoke()
    }

    override fun onAdLoading() {
        onAdLoadingAction?.invoke()
    }

    override fun onAdLoadSuccess() {
        onAdLoadSuccessAction?.invoke()
    }

    override fun onAdLoadFailed(errorMsg: String) {
        onAdLoadFailedAction?.invoke(errorMsg)
    }

    override fun onAdClosed() {
        onAdClosedAction?.invoke()
    }

    override fun onAdClicked(adId:String,adUrl:String?) {
        onAdClickedAction?.invoke(adId,adUrl)
    }

    override fun onAdCountdownFinished() {
        onAdCountdownFinishedAction?.invoke()
    }

    override fun onNoLocalAd(){
        onNoLocalAdAction?.invoke()
    }

    // Methods to configure each callback action
    fun onAdDataFetchStart(action: () -> Unit) {
        onAdDataFetchStartAction = action
    }

    fun onAdDataFetchSuccess(action: () -> Unit) {
        onAdDataFetchSuccessAction = action
    }

    fun onAdDataFetchFailed(action: (String?) -> Unit) {
        onAdDataFetchFailedAction = action
    }

    fun onNoAdData(action: () -> Unit) {
        onNoAdDataAction = action
    }

    fun onAdDataLoading(action: () -> Unit) {
        onAdDataLoadingAction = action
    }

    fun onAdLoading(action: () -> Unit) {
        onAdLoadingAction = action
    }

    fun onAdLoadSuccess(action: () -> Unit) {
        onAdLoadSuccessAction = action
    }

    fun onAdLoadFailed(action: (String) -> Unit) {
        onAdLoadFailedAction = action
    }

    fun onAdClosed(action: () -> Unit) {
        onAdClosedAction = action
    }

    fun onAdClicked(action: (adId:String,adUrl:String?) -> Unit) {
        onAdClickedAction = action
    }

    fun onAdCountdownFinished(action: () -> Unit) {
        onAdCountdownFinishedAction = action
    }

    fun onNoLocalAd(action: () -> Unit) {
        onNoLocalAdAction = action
        // 无本地缓存的广告
    }

}
