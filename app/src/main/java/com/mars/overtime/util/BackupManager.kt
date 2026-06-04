package com.mars.overtime.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.mars.overtime.database.AppConfig
import com.mars.overtime.database.OvertimeRecord
import com.mars.overtime.database.OvertimeType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileReader
import java.io.FileWriter

data class BackupData(
    val records: List<OvertimeRecord> = emptyList(),
    val configs: List<AppConfig> = emptyList()
)

object BackupManager {
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .setDateFormat("yyyy-MM-dd HH:mm:ss")
        .create()

    fun exportData(records: List<OvertimeRecord>, configs: List<AppConfig>, filePath: String): Boolean {
        return try {
            val data = BackupData(records, configs)
            val json = gson.toJson(data)
            val file = File(filePath)
            FileWriter(file).use { writer ->
                writer.write(json)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun importData(filePath: String): BackupData? {
        return try {
            val file = File(filePath)
            if (!file.exists()) return null
            val reader = FileReader(file)
            gson.fromJson(reader, BackupData::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun performAutoBackup(
        context: Context,
        records: List<OvertimeRecord>,
        configs: List<AppConfig>,
        webdavUrl: String? = null,
        webdavUsername: String? = null,
        webdavPassword: String? = null,
        webdavPath: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val autoBackupEnabled = configs.find { it.key == "auto_backup_enabled" }?.value?.toBoolean() ?: false
            if (!autoBackupEnabled) {
                return@withContext true
            }

            val location = configs.find { it.key == "auto_backup_location" }?.value ?: "local"
            val fileName = DataMigrationUtil.generateBackupFileName()
            val localFilePath = DataMigrationUtil.getBackupFilePath(context, fileName)

            val exportSuccess = exportData(records, configs, localFilePath)
            if (!exportSuccess) {
                return@withContext false
            }

            if (location == "cloud" && !webdavUrl.isNullOrBlank()) {
                val config = WebDavManager.WebDavConfig(
                    baseUrl = webdavUrl,
                    username = webdavUsername ?: "",
                    password = webdavPassword ?: "",
                    remotePath = webdavPath ?: "/overtime_backup/"
                )
                WebDavManager.uploadFile(config, localFilePath, fileName)
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
