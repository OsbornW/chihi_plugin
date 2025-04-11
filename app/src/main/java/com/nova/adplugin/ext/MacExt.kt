package com.nova.adplugin.ext

import android.content.Context
import android.net.wifi.WifiManager
import android.provider.Settings
import com.nova.adplugin.base.appContext
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.net.NetworkInterface
import java.util.Collections
import java.util.Locale
import java.util.UUID

// 扩展函数：读取文件内容为 String
@Throws(IOException::class)
fun File.loadAsString(): String {
    val fileData = StringBuilder()
    BufferedReader(FileReader(this)).use { reader ->
        val buf = CharArray(1024)
        var numRead: Int
        while (reader.read(buf).also { numRead = it } != -1) {
            fileData.append(buf, 0, numRead)
        }
    }
    return fileData.toString()
}

// 判断 MAC 地址是否有效（排除默认值）
private fun isValidMacAddress(mac: String?): Boolean {
    if (mac.isNullOrEmpty()) return false
    // 常见的默认 MAC 地址，通常是 "02:00:00:00:00:00" 或全零
    val invalidMacs = listOf(
        "02:00:00:00:00:00",
        "00:00:00:00:00:00",
        "FF:FF:FF:FF:FF:FF" // 全广播地址也视为无效
    )
    return mac !in invalidMacs && mac.matches(Regex("([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})"))
}

/**
 * 综合获取 MAC 地址的扩展函数
 * 依次尝试多种方法，返回第一个有效的 MAC 地址
 * 如果所有方法都返回默认值或失败，则返回 null
 */
fun getMacAddress(): String? {
    // 方法 1: 从 /sys/class/net/eth0/address 获取
    try {
        val eth0Mac = File("/sys/class/net/eth0/address").loadAsString().trim().uppercase().substring(0, 17)
        if (isValidMacAddress(eth0Mac)) return eth0Mac
    } catch (e: IOException) {
        e.printStackTrace()
    }

    // 方法 2: 从 /sys/class/net/wlan0/address 获取
    try {
        val wlan0Mac = File("/sys/class/net/wlan0/address").loadAsString().trim().uppercase().substring(0, 17)
        if (isValidMacAddress(wlan0Mac)) return wlan0Mac
    } catch (e: IOException) {
        e.printStackTrace()
    }

    // 方法 3: 使用 NetworkInterface 获取 (需要 Android 10+ 系统权限)
    try {
        val networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
        for (networkInterface in networkInterfaces) {
            if (networkInterface.name.equals("wlan0", ignoreCase = true)) {
                val macBytes = networkInterface.hardwareAddress ?: continue
                val mac = macBytes.joinToString(":") { byte -> "%02X".format(byte) }
                if (isValidMacAddress(mac)) return mac
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    // 方法 4: 使用 WifiManager 获取 (需要 Android 10+ 系统权限)
    try {
        val wifiManager = appContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val mac = wifiManager.connectionInfo.macAddress?.uppercase(Locale.US)
        if (isValidMacAddress(mac)) return mac
    } catch (e: Exception) {
        e.printStackTrace()
    }

    // 所有方法都失败，返回 null
    return null
}

// 原有独立函数保持不变
fun getEth0MacAdrress(): String? {
    return try {
        val macFile = File("/sys/class/net/eth0/address")
        macFile.loadAsString().trim().uppercase().substring(0, 17)
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

fun getWlan0MacAddress(): String? {
    return try {
        val macFile = File("/sys/class/net/wlan0/address")
        macFile.loadAsString().trim().uppercase().substring(0, 17)
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

fun getNetMacAddress(): String? {
    try {
        val networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
        for (networkInterface in networkInterfaces) {
            if (networkInterface.name.equals("wlan0", ignoreCase = true)) {
                val macBytes = networkInterface.hardwareAddress ?: return null
                return macBytes.joinToString(":") { byte -> "%02X".format(byte) }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

fun getMacBySysApi(): String? {
    val wifiManager = appContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    return wifiManager.connectionInfo.macAddress?.uppercase(Locale.US)
}





/**
 * 生成设备唯一 ID。
 * 优先级：MAC 地址 -> Android ID。
 * 格式：{类型}{UUID(值)}。
 *
 * @return 设备唯一 ID，如果无法获取则返回 null。
 */
fun Context.getUniqueDeviceId(): String {
    // 尝试获取 MAC 地址
    val macAddress = getMacAddress()
    if (macAddress != null) {
        val uuid = UUID.nameUUIDFromBytes(macAddress.toByteArray())
        return "{1}{$uuid}" // 类型 1：MAC 地址
    }

    // 尝试获取 Android ID
    val androidId = getAndroidId(this)
    if (androidId != null) {
        val uuid = UUID.nameUUIDFromBytes(androidId.toByteArray())
        return "{2}{$uuid}" // 类型 2：Android ID
    }

    // 如果以上都无法获取，生成一个随机 UUID
    val randomUuid = UUID.randomUUID()
    return "{9}{$randomUuid}" // 类型 9：随机 UUID
}

/**
 * 获取设备的 Android ID。
 *
 * @return Android ID，如果无法获取则返回 null。
 */
private fun getAndroidId(context: Context): String? {
    return try {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
