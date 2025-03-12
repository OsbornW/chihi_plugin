package com.chihi.adplugin.manager

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.RelativeLayout
import com.chihi.adplugin.AdConfig
import com.chihi.adplugin.Position
import com.chihi.adplugin.appContext
import com.chihi.adplugin.buildAdCallback
import com.chihi.adplugin.ext.otherwise
import com.chihi.adplugin.ext.yes
import com.chihi.adplugin.view.AdViewSDKB
import com.sjkj.ad.AdView
import com.sjkj.ad.AdViewDynamic
import java.lang.ref.WeakReference

object FloatingAdManager {

    private var floatingAdViewRef: WeakReference<View>? = null
    private var windowManager: WindowManager? = null
   // private var adJob: Job? = null
    var videoAdView:WeakReference<AdViewSDKB>?=null

    var mAdData: WeakReference<AdConfig>?=null
    var isCanSkip:Boolean=false


    // 使用单一 Handler 实例
    private val handler = Handler(Looper.getMainLooper())

    // 显示悬浮广告
    fun showFloatingAD(config: AdConfig.() -> Unit) {

        // 如果当前广告未显示完毕，不能再显示新广告
        if (floatingAdViewRef?.get() != null) {
            return
        }
        val (adConfig,callback) = buildAdCallback(config)

        mAdData = WeakReference(adConfig)
        // 获取 WindowManager 系统服务
        windowManager = appContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // 创建根布局
        val rlWindowRoot = RelativeLayout(appContext).apply {
            id = View.generateViewId() // 动态生成 ID
        }

        // 根据广告类型添加对应视图
        val adView = AdViewDynamic(appContext)
         videoAdView = WeakReference(AdViewSDKB(appContext, adConfig, adView, callback = callback, adSkipCallback = {
             isCanSkip = true
         }, destoryCallback = {
             releaseResources()
         }).apply {
             layoutParams = RelativeLayout.LayoutParams(
                 RelativeLayout.LayoutParams.MATCH_PARENT,
                 RelativeLayout.LayoutParams.MATCH_PARENT
             )
         })
        rlWindowRoot.addView(videoAdView?.get())


        // 创建 WindowManager.LayoutParams 并设置悬浮窗属性
        val layoutParams = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            gravity = when (adConfig.positionEnum) {
                Position.RIGHT_BOTTOM -> Gravity.BOTTOM or Gravity.END  // 右下角（默认）
                Position.LEFT_TOP -> Gravity.TOP or Gravity.START      // 左上角
                Position.RIGHT_TOP -> Gravity.TOP or Gravity.END     // 右上角
                Position.LEFT_BOTTOM -> Gravity.BOTTOM or Gravity.START // 左下角
                Position.CENTER -> Gravity.CENTER // 居中
                Position.LEFT_CENTER -> Gravity.START or Gravity.CENTER_VERTICAL // 左中
                Position.RIGHT_CENTER -> Gravity.END or Gravity.CENTER_VERTICAL // 右中
                Position.TOP_CENTER -> Gravity.TOP or Gravity.CENTER_HORIZONTAL // 上中
                Position.BOTTOM_CENTER -> Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL // 下中
            }
            // 如果宽度为 0，则全屏显示悬浮窗
            if (adConfig.floatingWidth == 0 || adConfig.floatingHeight == 0) {
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            } else {
                width = adaptSize(adConfig.floatingWidth?:400, appContext) // 动态适配宽度
                height = adaptSize(adConfig.floatingHeight?:200, appContext) // 动态适配高度
            }

            x = adaptSize(adConfig.floatingX, appContext) // X 偏移量，从 AdData 提取
            y = adaptSize(adConfig.floatingY, appContext) // Y 偏移量，从 AdData 提取
        }

        // 确保在主线程中执行 UI 更新操作
        if (Looper.myLooper() != Looper.getMainLooper()) {
            handler.post {
                addFloatingView(rlWindowRoot, layoutParams)
            }
        } else {
            addFloatingView(rlWindowRoot, layoutParams)
        }

        // 保存当前的广告视图引用（弱引用）
        floatingAdViewRef = WeakReference(rlWindowRoot)
    }

    private fun addFloatingView(view: View, layoutParams: WindowManager.LayoutParams) {
        try {
            windowManager?.addView(view, layoutParams)
        } catch (e: Exception) {
        }
    }

    // 移除悬浮广告
    fun removeFloatingAd():Boolean {
        FloatingAdManager.isAdVisible().yes {
            if(mAdData!=null&& mAdData?.get()?.isClosableBoolean==false)return false
            if(mAdData!=null&& mAdData!!.get()!!.adSkipTime>0&& isCanSkip){
                videoAdView?.get()?.release()
                releaseResources()
                return true
            }

            return false
        }.otherwise {
            return false
        }


    }

    // 释放资源
    internal fun releaseResources() {
        try {
            floatingAdViewRef?.get()?.let { windowManager?.removeView(it) }
            floatingAdViewRef?.clear() // 清除弱引用，帮助回收
            //adJob?.cancel() // 取消协程任务
            mAdData = null
            isCanSkip = false
        } catch (e: Exception) {
        }
    }

    // 判断是否已有悬浮广告
    fun isAdVisible(): Boolean {
        return floatingAdViewRef?.get() != null
    }


}

// 屏幕适配方法
 fun adaptSize(size: Int, context: Context): Int {
    val density = context.resources.displayMetrics.density
    return (size * density).toInt() // 根据屏幕密度适配大小
}

