package com.nova.adplugin.ext

import com.nova.adplugin.base.appContext


fun Int.colorValue(): Int = appContext.resources.getColor(this, null)
fun Int.dimenValue(): Int = appContext.resources.getDimensionPixelSize(this)
fun Int.stringValue(): String = appContext.resources.getString(this)
fun Int.stringValueWithFormat(name:Any): String = appContext.resources.getString(this,name)

/**
 *
 * 十进制转 16 进制
 */
fun Int.toHex16() = Integer.toHexString(this)

fun Int.monthZero() = (this<10).yes { "0$this" }.otherwise { "$this" }