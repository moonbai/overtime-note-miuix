package com.mars.overtime

import android.app.Application
import androidx.room.Room
import com.mars.overtime.database.AppDatabase
import com.mars.overtime.util.DataMigrationUtil
import java.io.File

class OvertimeApplication : Application() {
    companion object {
        lateinit var database: AppDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "overtime_database"
        ).build()

        migrateDataIfNeeded()
    }

    private fun migrateDataIfNeeded() {
        val oldDir = getDatabasePath("overtime_database").parentFile
        val newDir = DataMigrationUtil.getAppDataDir(this)

        if (oldDir != null && oldDir.exists() && newDir != oldDir) {
            val oldDb = File(oldDir, "overtime_database")
            if (oldDb.exists()) {
                val newDb = File(newDir, "overtime_database")
                if (!newDb.exists()) {
                    try {
                        newDir.mkdirs()
                        oldDb.copyTo(newDb, overwrite = true)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}
