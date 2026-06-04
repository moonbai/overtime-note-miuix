package com.mars.overtime.util

import com.mars.overtime.database.AppConfig
import com.mars.overtime.database.OvertimeType
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object SalaryCalculator {
    private const val KEY_BASE_SALARY = "base_salary"
    private const val KEY_WORKDAY_RATE = "workday_rate"
    private const val KEY_RESTDAY_RATE = "restday_rate"
    private const val KEY_HOLIDAY_RATE = "holiday_rate"
    private const val DEFAULT_BASE_SALARY = 2200.0
    private const val DEFAULT_WORKDAY_RATE = 1.5
    private const val DEFAULT_RESTDAY_RATE = 2.0
    private const val DEFAULT_HOLIDAY_RATE = 3.0

    fun getBaseSalary(configs: List<AppConfig>): Double {
        return configs.find { it.key == KEY_BASE_SALARY }?.value?.toDoubleOrNull() ?: DEFAULT_BASE_SALARY
    }

    fun getRate(configs: List<AppConfig>, type: OvertimeType): Double {
        return when (type) {
            OvertimeType.WORKDAY -> configs.find { it.key == KEY_WORKDAY_RATE }?.value?.toDoubleOrNull() ?: DEFAULT_WORKDAY_RATE
            OvertimeType.RESTDAY -> configs.find { it.key == KEY_RESTDAY_RATE }?.value?.toDoubleOrNull() ?: DEFAULT_RESTDAY_RATE
            OvertimeType.HOLIDAY -> configs.find { it.key == KEY_HOLIDAY_RATE }?.value?.toDoubleOrNull() ?: DEFAULT_HOLIDAY_RATE
            OvertimeType.LEAVE_HALF -> 0.0
            OvertimeType.LEAVE_FULL -> 0.0
        }
    }

    fun calculateMoney(baseSalary: Double, rate: Double, duration: Double): Double {
        val hourlyRate = baseSalary / 21.75 / 8
        return hourlyRate * rate * duration
    }

    fun calculateMoneyWithConfig(configs: List<AppConfig>, type: OvertimeType, duration: Double): Double {
        val baseSalary = getBaseSalary(configs)
        val rate = getRate(configs, type)
        return calculateMoney(baseSalary, rate, duration)
    }

    fun calculateDuration(startTime: String, endTime: String): Double {
        return try {
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())
            val start = format.parse(startTime) ?: return 0.0
            val end = format.parse(endTime) ?: return 0.0
            val diffMs = end.time - start.time
            if (diffMs <= 0) return 0.0
            val hours = TimeUnit.MILLISECONDS.toHours(diffMs)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMs) % 60
            hours + minutes / 60.0
        } catch (e: Exception) {
            0.0
        }
    }

    fun getSalaryConfigKeys(): List<String> {
        return listOf(KEY_BASE_SALARY, KEY_WORKDAY_RATE, KEY_RESTDAY_RATE, KEY_HOLIDAY_RATE)
    }
}
