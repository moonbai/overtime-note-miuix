package com.mars.overtime.database

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class OvertimeType {
    WORKDAY,
    RESTDAY,
    HOLIDAY,
    LEAVE_HALF,
    LEAVE_FULL
}

@Entity(tableName = "overtime_record")
data class OvertimeRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val startTime: String,
    val endTime: String,
    val duration: Double,
    val type: OvertimeType,
    val money: Double,
    val remark: String,
    val createTime: Long = System.currentTimeMillis()
)
