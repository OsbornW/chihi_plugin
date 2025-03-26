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
            AdPlugin.initialize()
        },3000)
    }
}