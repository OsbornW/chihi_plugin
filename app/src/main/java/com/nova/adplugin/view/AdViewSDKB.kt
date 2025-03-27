package com.nova.adplugin.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.CountDownTimer
import android.view.KeyEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams
import android.widget.LinearLayout
import android.widget.TextView
import com.nova.adplugin.AdLoadCallback
import com.chihi.adplugin.R
import com.nova.adplugin.ext.stringValue
import com.nova.adplugin.ext.stringValueWithFormat
import com.nova.adplugin.base.AdConfigBase
import com.sjkj.ad.AdPlayManager
import com.sjkj.ad.AdView
import com.sjkj.ad.listener.AdPlayListener

// 视频广告视图
class AdViewSDKB(
    context: Context,
    adData: AdConfigBase,
    mVideoView: AdView,
    viewGroup: ViewGroup? = null,
    val callback: AdLoadCallback? = null,
    val adSkipCallback: (() -> Unit)? = null,
    val destoryCallback: (() -> Unit)? = null
) : FrameLayout(context) {
    var videoView: AdView? = null
    private var countdownTextView: TextView? = null
    private var closeTextView: TextView? = null // 新的 TextView
    private var countDownTimer: CountDownTimer? = null
    private var countDownTimerAdSkip: CountDownTimer? = null
    // 初始化播放器视图

    init {
        callback?.onAdLoading()

        videoView = mVideoView
        initializePlayer(adData)

        viewGroup?.setOnKeyListener { v, keyCode, event ->
            // 只处理 ACTION_DOWN 事件
            if (event.action == KeyEvent.ACTION_UP) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_RIGHT -> {
                        // 处理右键按下事件
                        /* if (adData.isClosableBoolean) {
                             //广告可以跳过
                             FloatingAdManager.removeFloatingAd()
                             release()
                             countDownTimer?.cancel()
                             callback?.onAdCountdownFinished()

                             true  // 事件已处理，返回 true
                         } else false*/
                        false

                    }

                    else -> false     // 对其他按键不处理
                }
            } else false    // 对 ACTION_UP 或其他动作不做处理

        }


        //Log.e("chihi_error", "当前的视频链接是：: ${adData.videoUrl}")

    }

    private fun addViews(adData: AdConfigBase) {
        // 创建并初始化倒计时 TextView
        if (adData.isCountdownVisible) countdownTextView = createTextView(context)


        // 创建并初始化关闭广告的提示 TextView
        if (adData.isClosableBoolean) {
            closeTextView = createTextView(context).apply {
                text = ""
            }
        }

        // 使用 LinearLayout 来水平排列两个 TextView
        val textContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            if (closeTextView != null) addView(closeTextView)
            if (countdownTextView != null) addView(countdownTextView)
            layoutParams =
                LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                    gravity = android.view.Gravity.TOP or android.view.Gravity.END  // 右上角
                }
            // layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }


        addView(textContainer)
    }

    var displayDuration = 10000L
    var manager: AdPlayManager? = null
    private fun initializePlayer(adData: AdConfigBase) {

        //videoView = VideoView(context)
        // 将 PlayerView 添加到当前 FrameLayout 中
        addView(videoView)

        manager = AdPlayManager()
        manager?.playAd(videoView, object : AdPlayListener {
            override fun onError(code: Int, errorMsg: String?) {
                //播放出错
                /*addViews(adData)
                if (displayDuration > 0) {
                    if (secondsRemaining < 0) {
                        countdownTextView?.text =
                            com.chihi.ad_lib.R.string.countdown.stringValueWithFormat(displayDuration)
                        startCountdown(displayDuration)
                    }
                    if (secondsRemainingAdSkip < 0&&adData.adSkipTime>0) {
                        countdownTextView?.text =
                            com.chihi.ad_lib.R.string.ad_skip_time.stringValueWithFormat(adData.adSkipTime/1000)
                        startCountdownAdSkip(adData.adSkipTime)
                    }

                }*/
                callback?.onAdLoadFailed("播放出错")
            }

            override fun onStart(cur: Int, total: Int) {
                if (cur == 1) {
                    addViews(adData)
                    val time = manager?.duration?.times(1000)
                    val total = (time ?: 10000) / 1000L
                    //val cur = videoView?.currentPosition?:0
                    if (total > 0) {
                        if (secondsRemaining < 0) {
                            countdownTextView?.text =
                                com.chihi.ad_lib.R.string.countdown.stringValueWithFormat(total)
                            startCountdown(total * 1000)
                        }

                    }

                    if (secondsRemainingAdSkip < 0 && adData.adSkipTime > 0) {
                        countdownTextView?.text =
                            com.chihi.ad_lib.R.string.ad_skip_time.stringValueWithFormat(adData.adSkipTime / 1000)
                        startCountdownAdSkip(adData.adSkipTime)
                    }
                    callback?.onAdLoadSuccess()
                }
            }

            override fun onComplete(i: Int, total: Int) {
                /*if(i==total){
                    stopCountdown()
                }*/
            }


        })

        secondsRemaining = -1
        secondsRemainingAdSkip = -1

    }


    /**
     * 播放广告视频
     */
    fun play() {
        //videoView!!.start() //开始播放，不调用则不自动播放
    }

    fun resumePlay() {
        // Log.e("chihi_error", "play: 调用播放了")
        //videoView!!.resume() //开始播放，不调用则不自动播放
    }


    /**
     * 暂停广告视频
     */
    fun pause() {
        //videoView?.pause()
    }

    /**
     * 释放播放器资源
     */
    fun release() {
        stopCountdown()
        manager?.endPlay()
        manager = null
    }

    var secondsRemaining = -1L
    private fun startCountdown(duration: Long) {
        // 开始倒计时，持续时间为 AdData 中的 adDuration
        countDownTimer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = millisUntilFinished / 1000
                // Log.e("chihi_error", "onTick: 当前倒计时的秒数：$secondsRemaining  是否主线程：${isMainThread()}")
                countdownTextView?.text =
                    com.chihi.ad_lib.R.string.countdown.stringValueWithFormat(secondsRemaining)
            }

            override fun onFinish() {
                //countdownTextView?.text = "广告结束"
                // 广告结束时，自动释放资源并调用关闭回调
                release()
                destoryCallback?.invoke()

            }
        }.start()
    }

    var secondsRemainingAdSkip = -1L
    private fun startCountdownAdSkip(duration: Long) {
        // 开始倒计时，持续时间为 AdData 中的 adDuration
        countDownTimerAdSkip = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                secondsRemainingAdSkip = millisUntilFinished / 1000
                closeTextView?.text = com.chihi.ad_lib.R.string.ad_skip_time.stringValueWithFormat(
                    secondsRemainingAdSkip
                )
            }

            override fun onFinish() {
                //countdownTextView?.text = "广告结束"
                // 广告结束时，自动释放资源并调用关闭回调
                closeTextView?.text = com.chihi.ad_lib.R.string.close_ad_hint.stringValue()
                adSkipCallback?.invoke()
                countDownTimerAdSkip?.cancel()
                //destoryCallback?.invoke()

            }
        }.start()
    }

    internal fun stopCountdown() {
        countDownTimer?.cancel()
        callback?.onAdCountdownFinished()
        countdownTextView?.text = ""
    }

    override fun requestFocus(
        direction: Int,
        previouslyFocusedRect: android.graphics.Rect?
    ): Boolean {
        return false // Never allow focus
    }

    @SuppressLint("MissingSuperCall")
    override fun onFocusChanged(
        gainFocus: Boolean, direction: Int, previouslyFocusedRect: android.graphics.Rect?
    ) {
        // Prevent focus change on the view
        if (gainFocus) {
            clearFocus()
        }
    }


}

fun Context.adjustedContext(): Context {
    val configuration = resources.configuration
    configuration.fontScale = 1.0f // 忽略字体缩放
    return createConfigurationContext(configuration)
}

fun createTextView(context: Context): TextView {
    val adjustedContext = context.adjustedContext()

    return TextView(adjustedContext).apply {
        textSize = 13f // 设置固定字体大小
        setTextColor(Color.WHITE)
        setBackgroundColor(Color.BLACK)
        setPadding(20, 10, 20, 10)
        layoutParams =
            LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                topMargin = 20
                rightMargin = 20
            }
    }
}