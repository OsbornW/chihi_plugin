package com.chihi.adplugin.net

import com.chihi.adplugin.log.printLog
import org.json.JSONObject
import org.json.JSONArray
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.concurrent.thread

// DSL 包装类
class NetworkCallbackBuilder<T> {
    private var onSuccess: ((T) -> Unit)? = null
    private var onFailure: ((Throwable) -> Unit)? = null

    fun success(block: (T) -> Unit) {
        onSuccess = block
    }

    fun failed(block: (Throwable) -> Unit) {
        onFailure = block
    }

    fun build(): NetworkCallback<T> {
        return object : NetworkCallback<T> {
            override fun onSuccess(data: T) {
                onSuccess?.invoke(data)
            }

            override fun onFailure(error: Throwable) {
                onFailure?.invoke(error)
            }
        }
    }
}

// NetworkCallback 接口定义
interface NetworkCallback<T> {
    fun onSuccess(data: T)
    fun onFailure(error: Throwable)
}

// 扩展函数，简化 DSL 构建
inline fun <T> networkCallback(builder: NetworkCallbackBuilder<T>.() -> Unit): NetworkCallback<T> {
    return NetworkCallbackBuilder<T>().apply(builder).build()
}

object NetworkHelper {

    // 原有的 GET 请求
    fun <T> makeGetRequest(
        url: String,
        params: Map<String, String>?,
        responseType: Class<T>,
        itemType: Class<*>? = null,
        callback: NetworkCallback<T>
    ) {
        "开始请求: 1".printLog()
        thread {
            try {
                "开始请求: 2".printLog()
                val urlWithParams = buildUrlWithParams(url, params)
                "开始请求: 3".printLog()
                "完整的请求 URL: $urlWithParams".printLog()
                val connection = URL(urlWithParams).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = BufferedReader(InputStreamReader(connection.inputStream))
                        .use { reader ->
                            val sb = StringBuilder()
                            var line: String?
                            while (reader.readLine().also { line = it } != null) {
                                sb.append(line)
                            }
                            sb.toString()
                        }
                    "返回的数据是: $response".printLog()
                    val jsonResponse = JSONObject(response)
                    handleApiResponse(jsonResponse, responseType, itemType, callback)
                } else {
                    callback.onFailure(IOException("Unexpected code $responseCode"))
                }
            } catch (e: Exception) {
                "开始请求: 13--${e.localizedMessage}".printLog()
                callback.onFailure(e)
            }
        }
    }

    // DSL 风格的 GET 请求
    inline fun <T> makeGetRequest(
        url: String,
        params: Map<String, String>?,
        responseType: Class<T>,
        itemType: Class<*>? = null,
        crossinline builder: NetworkCallbackBuilder<T>.() -> Unit
    ) {
        makeGetRequest(url, params, responseType, itemType, networkCallback(builder))
    }

    // 原有的 POST 请求
    fun <T> makePostRequest(
        url: String,
        urlParams: Map<String, String>?,
        bodyParams: Any?,
        isForm: Boolean,
        responseType: Class<T>,
        itemType: Class<*>? = null,
        callback: NetworkCallback<T>
    ) {
        thread {
            try {
                val urlWithParams = buildUrlWithParams(url, urlParams)
                val connection = URL(urlWithParams).openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true

                val requestBody = if (isForm) {
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                    buildFormData(bodyParams as Map<String, String>)
                } else {
                    connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    buildJsonData(bodyParams)
                }

                connection.outputStream.use { os ->
                    val input = requestBody.toByteArray(Charsets.UTF_8)
                    os.write(input, 0, input.size)
                }

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = BufferedReader(InputStreamReader(connection.inputStream))
                        .use { reader ->
                            buildString { reader.forEachLine { append(it) } }
                        }
                    "返回的数据是: $response".printLog()
                    val jsonResponse = JSONObject(response)
                    handleApiResponse(jsonResponse, responseType, itemType, callback)
                } else {
                    callback.onFailure(IOException("Unexpected code $responseCode"))
                }
            } catch (e: Exception) {
                callback.onFailure(e)
            }
        }
    }

    // DSL 风格的 POST 请求
    inline fun <T> makePostRequest(
        url: String,
        urlParams: Map<String, String>?,
        bodyParams: Any?,
        isForm: Boolean,
        responseType: Class<T>,
        itemType: Class<*>? = null,
        crossinline builder: NetworkCallbackBuilder<T>.() -> Unit
    ) {
        makePostRequest(url, urlParams, bodyParams, isForm, responseType, itemType, networkCallback(builder))
    }

    // 原有的下载文件
    fun downloadFile(
        url: String,
        destination: File,
        progressListener: (Int) -> Unit,
        callback: NetworkCallback<String>
    ) {
        thread {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val contentLength = connection.contentLength
                    connection.inputStream.use { input ->
                        FileOutputStream(destination).use { output ->
                            val buffer = ByteArray(8192)
                            var bytesRead: Int
                            var totalRead = 0L
                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                output.write(buffer, 0, bytesRead)
                                totalRead += bytesRead
                                val progress = (totalRead * 100 / contentLength).toInt()
                                progressListener(progress)
                            }
                        }
                    }
                    callback.onSuccess(destination.absolutePath)
                } else {
                    callback.onFailure(IOException("Unexpected code $responseCode"))
                }
            } catch (e: Exception) {
                callback.onFailure(e)
            }
        }
    }

    // DSL 风格的下载文件
    inline fun downloadFile(
        url: String,
        destination: File,
        noinline progressListener: (Int) -> Unit,
        crossinline builder: NetworkCallbackBuilder<String>.() -> Unit
    ) {
        downloadFile(url, destination, progressListener, networkCallback(builder))
    }

    // 构建带参数的 URL
    private fun buildUrlWithParams(url: String, params: Map<String, String>?): String {
        val urlBuilder = StringBuilder(url)
        if (!params.isNullOrEmpty()) {
            urlBuilder.append("?")
            params.forEach { (key, value) ->
                try {
                    urlBuilder.append(URLEncoder.encode(key, "UTF-8"))
                        .append("=")
                        .append(URLEncoder.encode(value, "UTF-8"))
                        .append("&")
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                }
            }
            urlBuilder.deleteCharAt(urlBuilder.length - 1)
        }
        return urlBuilder.toString()
    }

    // 构建表单数据
    private fun buildFormData(params: Map<String, String>): String {
        return params.entries.joinToString("&") { (key, value) ->
            try {
                "${URLEncoder.encode(key, StandardCharsets.UTF_8.toString())}=${URLEncoder.encode(value, StandardCharsets.UTF_8.toString())}"
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
                "$key=$value"
            }
        }
    }

    // 构建 JSON 数据
    private fun buildJsonData(params: Any?): String {
        return when (params) {
            is Map<*, *> -> JSONObject(params as Map<String, String>).toString()
            else -> params.toString()
        }
    }

    // 处理 API 响应（支持 JSONObject 和 JSONArray）
    private fun <T> handleApiResponse(
        jsonResponse: JSONObject,
        responseType: Class<T>,
        itemType: Class<*>?,
        callback: NetworkCallback<T>
    ) {
        try {
            val code = jsonResponse.getInt("code")
            val msg = jsonResponse.getString("msg")

            if (code == 200) {
                val data = jsonResponse.get("data")
                val bean = when {
                    data is JSONObject -> parseJsonToBean(data, responseType)
                    data is JSONArray && responseType == List::class.java -> {
                        if (itemType == null) {
                            throw IllegalArgumentException("itemType must be provided for List response")
                        }
                        @Suppress("UNCHECKED_CAST")
                        parseJsonArrayToList(data, itemType) as T
                    }
                    else -> throw IllegalArgumentException("Unsupported data type: ${data?.javaClass?.simpleName}")
                }
                callback.onSuccess(bean)
            } else {
                callback.onFailure(IOException("API Error: $msg"))
            }
        } catch (e: Exception) {
            callback.onFailure(e)
        }
    }

    // 将 JSONObject 解析为 Bean 对象
    private fun <T> parseJsonToBean(json: JSONObject, beanClass: Class<T>): T {
        val bean = beanClass.getDeclaredConstructor().newInstance()
        beanClass.declaredFields.forEach { field ->
            field.isAccessible = true
            val fieldName = field.name
            if (json.has(fieldName)) {
                val value = json.get(fieldName)
                if (value != JSONObject.NULL) {
                    when (field.type) {
                        Long::class.java -> field.set(bean, (value as? Int)?.toLong() ?: value)
                        Int::class.java -> field.set(bean, (value as? Long)?.toInt() ?: value)
                        Double::class.java -> field.set(bean, (value as? Int)?.toDouble() ?: value)
                        Float::class.java -> field.set(bean, (value as? Double)?.toFloat() ?: value)
                        else -> field.set(bean, value)
                    }
                }
            }
        }
        return bean
    }

    // 将 JSONArray 解析为 List（修复类型推断）
    private fun <T> parseJsonArrayToList(jsonArray: JSONArray, itemType: Class<T>): List<T> {
        val list = mutableListOf<T>()
        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.get(i)
            when (item) {
                is JSONObject -> list.add(parseJsonToBean(item, itemType))
                is JSONArray -> list.add(parseJsonArrayToList(item, itemType) as T)
                else -> list.add(item as T)
            }
        }
        return list
    }
}

// ProgressListener 类型别名
typealias ProgressListener = (Int) -> Unit