package com.mars.overtime.push

import android.util.Base64
import android.util.Log
import com.mars.overtime.database.AppConfig
import com.mars.overtime.database.OvertimeRecord
import com.mars.overtime.database.OvertimeType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object PushManager {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .build()
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    fun buildText(record: OvertimeRecord): String {
        val typeStr = when (record.type) {
            OvertimeType.WORKDAY -> "工作日"
            OvertimeType.RESTDAY -> "休息日"
            OvertimeType.HOLIDAY -> "节假日"
            OvertimeType.LEAVE_HALF -> "请假(半天)"
            OvertimeType.LEAVE_FULL -> "请假(全天)"
        }
        val reason = record.remark.takeIf { it.isNotBlank() } ?: "无"
        return """日期: ${record.date}
类型: $typeStr
时间: ${record.startTime}-${record.endTime}
时长: ${"%.2f".format(record.duration)}
金额: ¥${"%.2f".format(record.money)}
事由: $reason""".trimIndent()
    }

    /**
     * 生成 HMAC-SHA256 签名
     */
    private fun hmacSha256(secret: String, data: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        val secretKeySpec = SecretKeySpec(secret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
        mac.init(secretKeySpec)
        val signData = mac.doFinal(data.toByteArray(StandardCharsets.UTF_8))
        return Base64.encodeToString(signData, Base64.NO_WRAP)
    }

    suspend fun sendDingTalk(url: String, secret: String, record: OvertimeRecord): Boolean = withContext(Dispatchers.IO) {
        val text = buildText(record)
        val json = """{"msgtype":"text","text":{"content":"${escapeJson(text)}"}}"""
        try {
            val body = json.toRequestBody(mediaType)
            val finalUrl = if (secret.isNotBlank()) {
                val timestamp = System.currentTimeMillis().toString()
                val stringToSign = "$timestamp\n$secret"
                val sign = URLEncoder.encode(hmacSha256(secret, stringToSign), "UTF-8")
                if (url.contains("?")) {
                    "${url}&timestamp=$timestamp&sign=$sign"
                } else {
                    "$url?timestamp=$timestamp&sign=$sign"
                }
            } else {
                url
            }
            val req = Request.Builder()
                .url(finalUrl)
                .post(body)
                .header("Content-Type", "application/json; charset=utf-8")
                .build()
            val res = client.newCall(req).execute()
            val response = res.body?.string()
            Log.d("PushManager", "钉钉推送响应: $response")
            res.close()
            res.isSuccessful && response?.contains("\"errcode\":0") == true
        } catch (e: Exception) {
            Log.e("PushManager", "钉钉推送失败", e)
            false
        }
    }

    suspend fun sendFeishu(url: String, secret: String, record: OvertimeRecord): Boolean = withContext(Dispatchers.IO) {
        val text = buildText(record)

        if (url.contains("open.feishu.cn/open-apis/bot/v2/hook")) {
            val json = if (secret.isNotBlank()) {
                val timestamp = System.currentTimeMillis().toString()
                val stringToSign = "$timestamp\n$secret"
                val sign = hmacSha256(secret, stringToSign)
                """{"msg_type":"text","content":{"text":"${escapeJson(text)}"},"timestamp":"$timestamp","sign":"$sign"}"""
            } else {
                """{"msg_type":"text","content":{"text":"${escapeJson(text)}"}}"""
            }
            try {
                val body = json.toRequestBody(mediaType)
                val req = Request.Builder()
                    .url(url)
                    .post(body)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .build()
                val res = client.newCall(req).execute()
                val response = res.body?.string()
                Log.d("PushManager", "飞书推送响应: $response")
                res.close()

                if (response != null) {
                    val jsonResponse = try {
                        org.json.JSONObject(response)
                    } catch (e: Exception) {
                        null
                    }
                    jsonResponse?.let {
                        val code = it.optInt("code", -1)
                        val statusCode = it.optInt("StatusCode", -1)
                        return@withContext code == 0 || statusCode == 0 || res.isSuccessful
                    }
                }
                res.isSuccessful
            } catch (e: Exception) {
                Log.e("PushManager", "飞书推送失败", e)
                false
            }
        } else {
            Log.d("PushManager", "飞书URL格式不正确: $url")
            false
        }
    }

    suspend fun sendWeCom(url: String, secret: String, record: OvertimeRecord): Boolean = withContext(Dispatchers.IO) {
        val text = buildText(record)
        val json = if (secret.isNotBlank()) {
            """{"msgtype":"text","text":{"content":"${escapeJson(text)}"},"mentioned_list":["@all"]}"""
        } else {
            """{"msgtype":"text","text":{"content":"${escapeJson(text)}"}}"""
        }
        try {
            val body = json.toRequestBody(mediaType)
            val req = Request.Builder()
                .url(url)
                .post(body)
                .header("Content-Type", "application/json; charset=utf-8")
                .build()
            val res = client.newCall(req).execute()
            val response = res.body?.string()
            Log.d("PushManager", "企业微信推送响应: $response")
            res.close()
            res.isSuccessful && response?.contains("\"errcode\":0") == true
        } catch (e: Exception) {
            Log.e("PushManager", "企业微信推送失败", e)
            false
        }
    }

    suspend fun sendWxPusher(url: String, record: OvertimeRecord): Boolean = withContext(Dispatchers.IO) {
        val text = buildText(record)
        val json = """{"content":"${escapeJson(text)}","contentType":1,"summary":"加班记录推送"}"""
        try {
            val body = json.toRequestBody(mediaType)
            val req = Request.Builder()
                .url(url)
                .post(body)
                .header("Content-Type", "application/json; charset=utf-8")
                .build()
            val res = client.newCall(req).execute()
            val response = res.body?.string()
            Log.d("PushManager", "WxPusher推送响应: $response")
            res.close()
            res.isSuccessful && response?.contains("\"success\":true") == true
        } catch (e: Exception) {
            Log.e("PushManager", "WxPusher推送失败", e)
            false
        }
    }

    suspend fun sendTelegram(url: String, chatId: String, record: OvertimeRecord): Boolean = withContext(Dispatchers.IO) {
        val text = buildText(record)
        val json = """{"chat_id":"${escapeJson(chatId)}","text":"${escapeJson(text)}"}"""
        try {
            val body = json.toRequestBody(mediaType)
            val req = Request.Builder()
                .url(url)
                .post(body)
                .header("Content-Type", "application/json; charset=utf-8")
                .build()
            val res = client.newCall(req).execute()
            val response = res.body?.string()
            Log.d("PushManager", "Telegram推送响应: $response")
            res.close()
            res.isSuccessful && response?.contains("\"ok\":true") == true
        } catch (e: Exception) {
            Log.e("PushManager", "Telegram推送失败", e)
            false
        }
    }

    suspend fun sendDiscord(url: String, username: String, record: OvertimeRecord): Boolean = withContext(Dispatchers.IO) {
        val text = buildText(record)
        val json = if (username.isNotBlank()) {
            """{"content":"${escapeJson(text)}","username":"${escapeJson(username)}"}"""
        } else {
            """{"content":"${escapeJson(text)}"}"""
        }
        try {
            val body = json.toRequestBody(mediaType)
            val req = Request.Builder()
                .url(url)
                .post(body)
                .header("Content-Type", "application/json; charset=utf-8")
                .build()
            val res = client.newCall(req).execute()
            Log.d("PushManager", "Discord推送响应码: ${res.code}")
            res.close()
            res.isSuccessful
        } catch (e: Exception) {
            Log.e("PushManager", "Discord推送失败", e)
            false
        }
    }

    suspend fun sendCustom(url: String, headers: String, record: OvertimeRecord): Boolean = withContext(Dispatchers.IO) {
        val text = buildText(record)
        try {
            val body = text.toRequestBody("text/plain; charset=utf-8".toMediaType())
            val reqBuilder = Request.Builder()
                .url(url)
                .post(body)
            // 解析自定义请求头，每行格式：HeaderName: HeaderValue
            if (headers.isNotBlank()) {
                headers.lines().forEach { line ->
                    val trimmed = line.trim()
                    if (trimmed.contains(":")) {
                        val parts = trimmed.split(":", limit = 2)
                        if (parts.size == 2) {
                            reqBuilder.header(parts[0].trim(), parts[1].trim())
                        }
                    }
                }
            }
            val req = reqBuilder.build()
            val res = client.newCall(req).execute()
            Log.d("PushManager", "自定义推送响应: ${res.code}")
            res.close()
            res.isSuccessful
        } catch (e: Exception) {
            Log.e("PushManager", "自定义推送失败", e)
            false
        }
    }

    private fun escapeJson(text: String): String {
        return text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
    
    /**
     * 根据配置推送到当前选择的单个渠道
     */
    suspend fun sendToSelectedChannel(configs: List<AppConfig>, record: OvertimeRecord): Boolean = withContext(Dispatchers.IO) {
        val pushEnabled = configs.find { it.key == "push_enabled" }?.value?.toBoolean() ?: false
        if (!pushEnabled) {
            return@withContext false
        }
        
        val selectedChannel = configs.find { it.key == "push_channel" }?.value ?: "dingtalk"
        
        when (selectedChannel) {
            "dingtalk" -> {
                val url = configs.find { it.key == "push_dingtalk" }?.value ?: return@withContext false
                val secret = configs.find { it.key == "push_dingtalk_secret" }?.value ?: ""
                sendDingTalk(url, secret, record)
            }
            "feishu" -> {
                val url = configs.find { it.key == "push_feishu" }?.value ?: return@withContext false
                val secret = configs.find { it.key == "push_feishu_secret" }?.value ?: ""
                sendFeishu(url, secret, record)
            }
            "wecom" -> {
                val url = configs.find { it.key == "push_wecom" }?.value ?: return@withContext false
                val secret = configs.find { it.key == "push_wecom_secret" }?.value ?: ""
                sendWeCom(url, secret, record)
            }
            "wxpusher" -> {
                val url = configs.find { it.key == "push_wxpusher" }?.value ?: return@withContext false
                sendWxPusher(url, record)
            }
            "telegram" -> {
                val url = configs.find { it.key == "push_telegram" }?.value ?: return@withContext false
                val chatId = configs.find { it.key == "push_telegram_chatid" }?.value ?: return@withContext false
                sendTelegram(url, chatId, record)
            }
            "discord" -> {
                val url = configs.find { it.key == "push_discord" }?.value ?: return@withContext false
                val username = configs.find { it.key == "push_discord_username" }?.value ?: ""
                sendDiscord(url, username, record)
            }
            "custom" -> {
                val url = configs.find { it.key == "push_custom" }?.value ?: return@withContext false
                val headers = configs.find { it.key == "push_custom_headers" }?.value ?: ""
                sendCustom(url, headers, record)
            }
            else -> false
        }
    }
}
