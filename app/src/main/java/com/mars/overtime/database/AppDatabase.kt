package com.mars.overtime.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class OvertimeTypeConverter {
    @TypeConverter
    fun fromOvertimeType(type: OvertimeType): String = type.name

    @TypeConverter
    fun toOvertimeType(name: String): OvertimeType = OvertimeType.valueOf(name)
}

@Database(entities = [OvertimeRecord::class, AppConfig::class], version = 1, exportSchema = false)
@TypeConverters(OvertimeTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun overtimeDao(): OvertimeDao
    abstract fun configDao(): ConfigDao
}
