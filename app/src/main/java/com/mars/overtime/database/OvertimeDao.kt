package com.mars.overtime.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface OvertimeDao {
    @Query("SELECT * FROM overtime_record ORDER BY date DESC, createTime DESC")
    fun getAllRecords(): Flow<List<OvertimeRecord>>

    @Query("SELECT * FROM overtime_record ORDER BY date DESC, createTime DESC")
    suspend fun getAllRecordsOnce(): List<OvertimeRecord>

    @Query("SELECT * FROM overtime_record ORDER BY date DESC, createTime DESC")
    suspend fun getAllRecordsSync(): List<OvertimeRecord> = getAllRecordsOnce()

    @Query("SELECT * FROM overtime_record WHERE id = :id")
    suspend fun getRecordById(id: Long): OvertimeRecord?

    @Query("SELECT * FROM overtime_record WHERE date = :date ORDER BY createTime DESC")
    suspend fun getRecordsByDate(date: String): List<OvertimeRecord>

    @Query("SELECT * FROM overtime_record WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getRecordsByDateRange(startDate: String, endDate: String): Flow<List<OvertimeRecord>>

    @Query("SELECT SUM(duration) FROM overtime_record")
    suspend fun getTotalDuration(): Double?

    @Query("SELECT SUM(money) FROM overtime_record")
    suspend fun getTotalMoney(): Double?

    @Query("SELECT SUM(duration) FROM overtime_record WHERE type = :type")
    suspend fun getDurationByType(type: OvertimeType): Double?

    @Query("SELECT SUM(money) FROM overtime_record WHERE type = :type")
    suspend fun getMoneyByType(type: OvertimeType): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: OvertimeRecord): Long

    @Update
    suspend fun updateRecord(record: OvertimeRecord)

    @Delete
    suspend fun deleteRecord(record: OvertimeRecord)

    @Query("DELETE FROM overtime_record")
    suspend fun deleteAllRecords()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRecords(records: List<OvertimeRecord>)
}
