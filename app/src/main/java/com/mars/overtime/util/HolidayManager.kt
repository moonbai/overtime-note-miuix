package com.mars.overtime.util

import android.util.Log
import com.mars.overtime.OvertimeApplication
import com.mars.overtime.database.OvertimeType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

enum class HolidayDataSource {
    TIMOR,
    MXNZP,
    CUSTOM
}

object HolidayManager {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private const val TIMOR_API_URL_TEMPLATE = "https://timor.tech/api/holiday/year/{year}"
    private const val MXNZP_API_URL_TEMPLATE = "https://www.mxnzp.com/api/holiday/list/year/{year}"

    private val holidayCache = mutableMapOf<String, HolidayInfo>()

    data class HolidayInfo(
        val date: String,
        val isHoliday: Boolean,
        val name: String = "",
        val isWorkday: Boolean = false,
        val type: Int = 0 
    )

    private fun isWeekend(dateStr: String): Boolean {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
        format.timeZone = TimeZone.getTimeZone("GMT+8")
        val date = format.parse(dateStr) ?: return false
        val cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"), Locale.CHINA)
        cal.time = date
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
    }

    private suspend fun getDataSource(): HolidayDataSource {
        return try {
            val db = OvertimeApplication.database
            val configDao = db.configDao()
            val config = configDao.getConfig("holiday_data_source")
            config?.value?.let {
                HolidayDataSource.valueOf(it.uppercase())
            } ?: HolidayDataSource.TIMOR
        } catch (e: Exception) {
            HolidayDataSource.TIMOR
        }
    }

    private suspend fun getCustomApiUrl(): String? {
        return try {
            val db = OvertimeApplication.database
            val configDao = db.configDao()
            val config = configDao.getConfig("holiday_custom_url")
            config?.value?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun getMxnzpConfig(): Pair<String, String> {
        return try {
            val db = OvertimeApplication.database
            val configDao = db.configDao()
            val appId = configDao.getConfig("mxnzp_app_id")?.value ?: ""
            val appSecret = configDao.getConfig("mxnzp_app_secret")?.value ?: ""
            Pair(appId, appSecret)
        } catch (e: Exception) {
            Pair("", "")
        }
    }

    private suspend fun getIgnoreHoliday(): Boolean {
        return try {
            val db = OvertimeApplication.database
            val configDao = db.configDao()
            configDao.getConfig("mxnzp_ignore_holiday")?.value?.toBoolean() ?: false
        } catch (e: Exception) {
            false
        }
    }

    private fun buildApiUrl(year: String, dataSource: HolidayDataSource, customUrl: String?): String {
        return when (dataSource) {
            HolidayDataSource.TIMOR -> TIMOR_API_URL_TEMPLATE.replace("{year}", year)
            HolidayDataSource.MXNZP -> MXNZP_API_URL_TEMPLATE.replace("{year}", year)
            HolidayDataSource.CUSTOM -> customUrl?.replace("\${year}", year)?.replace("\${years}", year)?.replace("{year}", year)?.replace("{years}", year) ?: ""
        }
    }

    private suspend fun parseTimorResponse(body: String) {
        val json = org.json.JSONObject(body)
        val code = json.optInt("code", -1)
        if (code == 0) {
            val holidayObj = json.optJSONObject("holiday")
            if (holidayObj != null) {
                val keys = holidayObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val item = holidayObj.optJSONObject(key)
                    if (item != null) {
                        val date = item.optString("date", "")
                        val name = item.optString("name", "")
                        val wage = item.optInt("wage", 0) 
                        val isHoliday = item.optBoolean("holiday", false)
                        val type = wage 
                        val info = HolidayInfo(
                            date = date,
                            isHoliday = isHoliday,
                            name = name,
                            type = type
                        )
                        holidayCache[date] = info
                    }
                }
            }
        }
    }

    private suspend fun parseMxnzpResponse(body: String) {
        val json = org.json.JSONObject(body)
        val code = json.optInt("code", -1)
        if (code == 1) {
            val dataArray = json.optJSONArray("data")
            if (dataArray != null) {
                for (i in 0 until dataArray.length()) {
                    val monthItem = dataArray.optJSONObject(i)
                    val daysArray = monthItem?.optJSONArray("days")
                    if (daysArray != null) {
                        for (j in 0 until daysArray.length()) {
                            val item = daysArray.optJSONObject(j)
                            if (item != null) {
                                val date = item.optString("date", "")
                                val name = item.optString("name", "")
                                val detailsType = item.optInt("detailsType", 0) 
                                val isHoliday = detailsType == 1 || detailsType == 2 || detailsType == 3
                                val type = detailsType
                                val info = HolidayInfo(
                                    date = date,
                                    isHoliday = isHoliday,
                                    name = name,
                                    type = type
                                )
                                holidayCache[date] = info
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun parseCustomResponse(body: String) {
        try {
            val json = org.json.JSONObject(body)
            if (json.has("holiday")) {
                parseTimorResponse(body)
            } else if (json.has("data")) {
                parseMxnzpResponse(body)
            } else {
                parseSimpleResponse(body)
            }
        } catch (e: Exception) {
            Log.e("HolidayManager", "解析自定义API响应失败", e)
        }
    }

    private fun parseSimpleResponse(body: String) {
        val json = org.json.JSONObject(body)
        val dataArray = json.optJSONArray("data") ?: json.optJSONArray("holidays") ?: return
        for (i in 0 until dataArray.length()) {
            val item = dataArray.optJSONObject(i)
            if (item != null) {
                val date = item.optString("date", item.optString("dateStr", ""))
                val name = item.optString("name", "")
                val detailsType = item.optInt("detailsType", item.optInt("type", item.optInt("wage", 0)))
                val isHoliday = item.optBoolean("holiday", item.optBoolean("isHoliday", detailsType != 0))
                val info = HolidayInfo(
                    date = date,
                    isHoliday = isHoliday,
                    name = name,
                    type = detailsType
                )
                holidayCache[date] = info
            }
        }
    }

    suspend fun getOvertimeType(dateStr: String): OvertimeType = withContext(Dispatchers.IO) {
        holidayCache[dateStr]?.let {
            return@withContext getOvertimeTypeFromHolidayInfo(it)
        }

        val year = dateStr.substring(0, 4)
        try {
            val dataSource = getDataSource()
            val customUrl = getCustomApiUrl()
            var url = buildApiUrl(year, dataSource, customUrl)
            
            if (dataSource == HolidayDataSource.MXNZP) {
                val (appId, appSecret) = getMxnzpConfig()
                if (appId.isNotEmpty() && appSecret.isNotEmpty()) {
                    val separator = if (url.contains("?")) "&" else "?"
                    url = "$url${separator}app_id=$appId&app_secret=$appSecret"
                }
            }

            Log.d("HolidayManager", "请求节假日API: $url")
            
            val req = Request.Builder().url(url).build()
            val res = client.newCall(req).execute()
            val body = res.body?.string()
            res.close()

            if (body != null) {
                Log.d("HolidayManager", "API响应: ${body.take(500)}")
                when (dataSource) {
                    HolidayDataSource.TIMOR -> parseTimorResponse(body)
                    HolidayDataSource.MXNZP -> parseMxnzpResponse(body)
                    HolidayDataSource.CUSTOM -> parseCustomResponse(body)
                }
            }

            val info = holidayCache[dateStr]
            if (info != null) {
                return@withContext getOvertimeTypeFromHolidayInfo(info)
            }

            if (isWeekend(dateStr)) {
                holidayCache[dateStr] = HolidayInfo(dateStr, true, type = 1)
                OvertimeType.RESTDAY
            } else {
                holidayCache[dateStr] = HolidayInfo(dateStr, false, type = 0)
                OvertimeType.WORKDAY
            }
        } catch (e: Exception) {
            Log.e("HolidayManager", "获取节假日失败", e)
            if (isWeekend(dateStr)) {
                OvertimeType.RESTDAY
            } else {
                OvertimeType.WORKDAY
            }
        }
    }

    private fun getOvertimeTypeFromHolidayInfo(info: HolidayInfo): OvertimeType {
        return when (info.type) {
            0 -> OvertimeType.WORKDAY 
            1 -> OvertimeType.RESTDAY 
            2 -> OvertimeType.RESTDAY 
            3 -> OvertimeType.HOLIDAY 
            else -> if (info.isHoliday) OvertimeType.RESTDAY else OvertimeType.WORKDAY
        }
    }

    suspend fun isHoliday(dateStr: String): Boolean = withContext(Dispatchers.IO) {
        getOvertimeType(dateStr) != OvertimeType.WORKDAY
    }

    suspend fun isWorkday(dateStr: String): Boolean = withContext(Dispatchers.IO) {
        getOvertimeType(dateStr) == OvertimeType.WORKDAY
    }

    suspend fun fetchHolidays(year: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val dataSource = getDataSource()
            val customUrl = getCustomApiUrl()
            var url = buildApiUrl(year, dataSource, customUrl)
            
            if (dataSource == HolidayDataSource.MXNZP) {
                val (appId, appSecret) = getMxnzpConfig()
                if (appId.isNotEmpty() && appSecret.isNotEmpty()) {
                    val separator = if (url.contains("?")) "&" else "?"
                    url = "$url${separator}app_id=$appId&app_secret=$appSecret"
                }
            }

            Log.d("HolidayManager", "拉取节假日: $url")
            
            val req = Request.Builder().url(url).build()
            val res = client.newCall(req).execute()
            val body = res.body?.string()
            res.close()
            
            if (body != null) {
                when (dataSource) {
                    HolidayDataSource.TIMOR -> parseTimorResponse(body)
                    HolidayDataSource.MXNZP -> parseMxnzpResponse(body)
                    HolidayDataSource.CUSTOM -> parseCustomResponse(body)
                }
            }
            
            body != null
        } catch (e: Exception) {
            Log.e("HolidayManager", "拉取节假日失败", e)
            false
        }
    }

    fun clearCache() {
        holidayCache.clear()
    }
}
