package com.mars.overtime.push

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.CalendarContract
import com.mars.overtime.database.OvertimeRecord
import com.mars.overtime.database.OvertimeType
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object CalendarSyncManager {
    private const val CALENDAR_DISPLAY_NAME = "加班记"
    private const val ACCOUNT_NAME = "overtime@com.mars.overtime"
    private const val ACCOUNT_TYPE = "LOCAL"

    fun hasCalendarPermission(context: Context): Boolean {
        return android.content.pm.PackageManager.PERMISSION_GRANTED ==
            context.checkSelfPermission(android.Manifest.permission.READ_CALENDAR)
    }

    fun getOrCreateCalendarId(context: Context): Long? {
        if (!hasCalendarPermission(context)) return null

        val projection = arrayOf(CalendarContract.Calendars._ID, CalendarContract.Calendars.ACCOUNT_NAME)
        val uri = CalendarContract.Calendars.CONTENT_URI
        val selection = "${CalendarContract.Calendars.ACCOUNT_NAME} = ?"
        val selectionArgs = arrayOf(ACCOUNT_NAME)

        context.contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID))
            }
        }

        val values = ContentValues().apply {
            put(CalendarContract.Calendars.ACCOUNT_NAME, ACCOUNT_NAME)
            put(CalendarContract.Calendars.ACCOUNT_TYPE, ACCOUNT_TYPE)
            put(CalendarContract.Calendars.NAME, CALENDAR_DISPLAY_NAME)
            put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, CALENDAR_DISPLAY_NAME)
            put(CalendarContract.Calendars.CALENDAR_COLOR, 0xFF4285F4.toInt())
            put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER)
            put(CalendarContract.Calendars.OWNER_ACCOUNT, ACCOUNT_NAME)
            put(CalendarContract.Calendars.VISIBLE, 1)
            put(CalendarContract.Calendars.SYNC_EVENTS, 1)
        }

        val builderUri = uri.buildUpon()
            .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, ACCOUNT_NAME)
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, ACCOUNT_TYPE)
            .build()

        val result = context.contentResolver.insert(builderUri, values)
        return result?.lastPathSegment?.toLongOrNull()
    }

    private fun parseDateTime(date: String, time: String): Long {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return format.parse("$date $time")?.time ?: 0L
    }

    fun addEvent(context: Context, record: OvertimeRecord): Boolean {
        val calendarId = getOrCreateCalendarId(context) ?: return false

        val typeStr = when (record.type) {
            OvertimeType.WORKDAY -> "工作日延时"
            OvertimeType.RESTDAY -> "休息日"
            OvertimeType.HOLIDAY -> "法定节假日"
            OvertimeType.LEAVE_HALF -> "请假(半天)"
            OvertimeType.LEAVE_FULL -> "请假(全天)"
        }

        val startTime = parseDateTime(record.date, record.startTime)
        val endTime = parseDateTime(record.date, record.endTime)

        val values = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, startTime)
            put(CalendarContract.Events.DTEND, endTime)
            put(CalendarContract.Events.TITLE, "$typeStr-${record.duration}小时")
            put(CalendarContract.Events.DESCRIPTION, "金额: ¥${"%.2f".format(record.money)}\n事由: ${record.remark}")
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            put(CalendarContract.Events.HAS_ALARM, 0)
        }

        return try {
            context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values) != null
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun removeEvents(context: Context, record: OvertimeRecord): Boolean {
        val calendarId = getOrCreateCalendarId(context) ?: return false

        val startTime = parseDateTime(record.date, record.startTime)
        val typeStr = when (record.type) {
            OvertimeType.WORKDAY -> "工作日延时"
            OvertimeType.RESTDAY -> "休息日"
            OvertimeType.HOLIDAY -> "法定节假日"
            OvertimeType.LEAVE_HALF -> "请假(半天)"
            OvertimeType.LEAVE_FULL -> "请假(全天)"
        }
        val title = "$typeStr-${record.duration}小时"

        val selection = "${CalendarContract.Events.CALENDAR_ID} = ? AND ${CalendarContract.Events.TITLE} = ? AND ${CalendarContract.Events.DTSTART} = ?"
        val selectionArgs = arrayOf(calendarId.toString(), title, startTime.toString())

        return try {
            context.contentResolver.delete(CalendarContract.Events.CONTENT_URI, selection, selectionArgs) > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
