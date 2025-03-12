package com.chihi.adplugin.log

import android.util.Log

fun String.printLog() {
    if(SYSTEM_PROPERTY_LOG.systemPropertyValueBoolean())Log.e("App_Common",this)
}


fun String.systemPropertyValueBoolean(defaultValue: Boolean = false): Boolean {
    return try {
        val systemProperties = Class.forName("android.os.SystemProperties")
        val getMethod = systemProperties.getMethod(
            "getBoolean",
            String::class.java,
            Boolean::class.javaPrimitiveType
        )
        getMethod.invoke(systemProperties, this, defaultValue) as Boolean
    } catch (e: Exception) {
        defaultValue
    }
}

const val SYSTEM_PROPERTY_LOG = "persist.log.tp.enable"   //是否开启日志