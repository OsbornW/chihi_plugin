package com.chihi.adplugin

import android.app.Activity
import android.os.Bundle
import android.os.Handler

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        PluginManager.init(this.applicationContext)
        Handler().postDelayed({
            //AdPlugin.initialize()
            println("开始反射")
            callAdPluginInitialize()
        },3000)
    }

    private fun callAdPluginInitialize() {
        try {
            // 1. 获取 ClassLoader（替换为您的实际 ClassLoader）
            // 方式1：使用当前类加载器（适用于主工程中的类）
            //val clazz = Class.forName("com.chihi.adplugin.AdPlugin")
            val clazz = Class.forName("com.chihi.adplugin.AdPlugin")

            //val adInitMethod = clazz.getDeclaredMethod("initialize")
            //val adSdkInstance = clazz.getDeclaredConstructor().newInstance()
            //adInitMethod.invoke(adSdkInstance)

            // 情况2：调用实例方法
            val instance = clazz.getDeclaredConstructor().newInstance()
            val instanceMethod = clazz.getMethod("initialize")
            instanceMethod.invoke(instance)
            println("类加载成功: ${clazz.simpleName}")
            println("初始化完成")

        } catch (e: Exception) {
            println("异常了: ${e.message}")
        }
    }
}