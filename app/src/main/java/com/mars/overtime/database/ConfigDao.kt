package com.mars.overtime.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfigDao {
    @Query("SELECT * FROM app_config WHERE key = :key")
    suspend fun getConfig(key: String): AppConfig?

    @Query("SELECT * FROM app_config")
    fun getAllConfigs(): Flow<List<AppConfig>>

    @Query("SELECT * FROM app_config")
    suspend fun getAllConfigsOnce(): List<AppConfig>

    @Query("SELECT * FROM app_config")
    suspend fun getAllConfigsSync(): List<AppConfig> = getAllConfigsOnce()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: AppConfig)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfigs(configs: List<AppConfig>)

    @Query("DELETE FROM app_config WHERE key = :key")
    suspend fun deleteConfig(key: String)

    @Query("DELETE FROM app_config")
    suspend fun deleteAllConfigs()
}
