package com.nova.adplugin.ext

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

fun Context.getDeviceAndAppInfo(): String {
    // 设备信息
    val deviceModel = Build.MODEL
    val deviceBrand = Build.BRAND
    val osVersion = "Android ${Build.VERSION.RELEASE}" // 加上 "Android" 前缀
    val sdkVersion = Build.VERSION.SDK_INT // SDK 版本
    val screenResolution = "${resources.displayMetrics.widthPixels}x${resources.displayMetrics.heightPixels}"
    val screenDensityDpi = resources.displayMetrics.densityDpi // 屏幕密度 DPI
    val deviceLanguage = Locale.getDefault().language
    val timezone = getFormattedTimeZoneWithSeparator() // 格式化时区
    val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) // Android ID
    val carrier = getCarrierName() // 运营商信息
    val supportedAbis = Build.SUPPORTED_ABIS.joinToString(", ") // 设备 ABIs

    // 应用信息
    val packageName = packageName
    val appVersionName = packageManager.getPackageInfo(packageName, 0).versionName // 版本名称
    val appVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        packageManager.getPackageInfo(packageName, 0).longVersionCode // 版本号
    } else {
        packageManager.getPackageInfo(packageName, 0).versionCode.toLong() // 兼容旧版本
    }

    // 网络信息
    val networkType = getNetworkType()
    val ipAddress = getIpAddress()
    val isVpnActive = isVpnActive() // 是否开启 VPN

    // 时间戳（格式化为 yyyy-MM-dd HH:mm:ss）
    val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

    // 构建 JSON 对象
    val jsonObject = JSONObject().apply {
        put("device", JSONObject().apply {
            put("model", deviceModel)
            put("brand", deviceBrand)
            put("os_version", osVersion)
            put("sdk_version", sdkVersion)
            put("screen_resolution", screenResolution)
            put("screen_density_dpi", screenDensityDpi) // 屏幕密度 DPI
            put("language", deviceLanguage)
            put("timezone", timezone)
            put("android_id", androidId)
            put("carrier", carrier)
            put("supported_abis", supportedAbis) // 设备 ABIs
        })
        put("app", JSONObject().apply {
            put("package_name", packageName)
            put("version_name", appVersionName)
            put("version_code", appVersionCode)
        })
        put("network", JSONObject().apply {
            put("type", networkType)
            put("ip_address", ipAddress)
            put("is_vpn_active", isVpnActive)
        })
        put("timestamp", timestamp)
    }

    return jsonObject.toString()
}

// 获取网络类型
private fun Context.getNetworkType(): String {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        when {
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "Wi-Fi"
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Cellular"
            else -> "Unknown"
        }
    } else {
        // 兼容旧版本
        val networkInfo = connectivityManager.activeNetworkInfo
        when (networkInfo?.type) {
            ConnectivityManager.TYPE_WIFI -> "Wi-Fi"
            ConnectivityManager.TYPE_MOBILE -> "Cellular"
            else -> "Unknown"
        }
    }
}

// 获取 IP 地址（简单实现，仅获取本地 IP）
private fun Context.getIpAddress(): String {
    return try {
        val networkInterfaces = java.net.NetworkInterface.getNetworkInterfaces()
        while (networkInterfaces.hasMoreElements()) {
            val networkInterface = networkInterfaces.nextElement()
            val addresses = networkInterface.inetAddresses
            while (addresses.hasMoreElements()) {
                val address = addresses.nextElement()
                if (!address.isLoopbackAddress && address is java.net.Inet4Address) {
                    return address.hostAddress
                }
            }
        }
        "Unknown"
    } catch (e: Exception) {
        "Unknown"
    }
}

// 判断是否开启 VPN
private fun Context.isVpnActive(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
    } else {
        // 兼容旧版本
        val networkInfo = connectivityManager.activeNetworkInfo
        networkInfo?.type == ConnectivityManager.TYPE_VPN
    }
}

// 获取运营商名称
private fun Context.getCarrierName(): String {
    return try {
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.networkOperatorName
    } catch (e: Exception) {
        "Unknown"
    }
}

// 合并后的方法
fun getFormattedTimeZoneWithSeparator(): String {

    // 获取当前时区
    val timeZone = TimeZone.getDefault()

    // 获取时区名称
    val displayName = timeZone.displayName

    // 获取原始偏移量（以毫秒为单位）
    val rawOffset = timeZone.rawOffset
    val hours = rawOffset / (1000 * 60 * 60) // 转换为小时
    val minutes = (rawOffset / (1000 * 60)) % 60 // 剩余分钟

    // 格式化 GMT 偏移量
    val gmtOffset = String.format("GMT%+03d:%02d", hours, Math.abs(minutes))

    // 获取时区ID
    val zoneId = timeZone.id

    // 组合名称、偏移量和时区ID，用 "-" 分隔
    return "$displayName-$gmtOffset-$zoneId"
}
