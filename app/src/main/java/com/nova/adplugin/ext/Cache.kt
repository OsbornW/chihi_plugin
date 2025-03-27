package com.nova.adplugin.ext

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nova.adplugin.base.appContext
import java.lang.reflect.Type

// 获取 SharedPreferences 实例的扩展函数
fun Context.getPreferences(): SharedPreferences {
    return this.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
}

// 保存数据的扩展函数
inline fun <reified T> T.saveToSP(key: String) {
    val editor = appContext.getPreferences().edit()
    when (this) {
        is String -> editor.putString(key, this)  // 保存字符串
        is Int -> editor.putInt(key, this)       // 保存 Int
        is Boolean -> editor.putBoolean(key, this)  // 保存 Boolean
        is Long -> editor.putLong(key, this)      // 保存 Long
        is Float -> editor.putFloat(key, this)    // 保存 Float
        is Set<*> -> editor.putStringSet(key, this as Set<String>)  // 保存 Set
        else -> {
            // 如果是对象类型，使用 Gson 转换为 JSON 字符串
            val json = Gson().toJson(this)
            editor.putString(key, json)
        }
    }
    editor.apply()
}

// 获取数据的扩展函数，支持通过泛型指定类型
inline fun <reified T> getFromSP(key: String): T? {
    val prefs = appContext.getPreferences()
    val json = prefs.getString(key, null)

    return if (json != null) {
        try {
            when {
                // 如果是基础类型（String, Int, Long, Boolean, Float），直接用 T::class.java
                T::class.java == String::class.java -> json as T
                T::class.java == Int::class.java -> json.toInt() as T
                T::class.java == Long::class.java -> json.toLong() as T
                T::class.java == Boolean::class.java -> json.toBoolean() as T
                T::class.java == Float::class.java -> json.toFloat() as T

                // 如果是 List 或 Map 类型，使用 TypeToken 来解析
                List::class.java.isAssignableFrom(T::class.java) || Map::class.java.isAssignableFrom(T::class.java) -> {
                    val type = getTypeToken<T>()
                    Gson().fromJson(json, type)
                }

                // 其他类型，直接用 T::class.java 来解析
                else -> {
                    val type = T::class.java
                    Gson().fromJson(json, type)
                }
            }
        } catch (e: Exception) {
            null // 如果无法反序列化，返回 null
        }
    } else {
        null // 如果没有保存的值，返回 null
    }
}

// 辅助方法：根据类型返回对应的 TypeToken
inline fun <reified T> getTypeToken(): Type {
    return object : TypeToken<T>() {}.type
}

const val HOME_INFO = "HOME_INFO"
