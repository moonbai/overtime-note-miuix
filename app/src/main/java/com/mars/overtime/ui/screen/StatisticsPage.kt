package com.mars.overtime.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mars.overtime.OvertimeApplication
import com.mars.overtime.database.OvertimeRecord
import com.mars.overtime.database.OvertimeType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsPage() {
    val db = OvertimeApplication.database
    val dao = db.overtimeDao()
    val configDao = db.configDao()
    val scope = rememberCoroutineScope()

    val currentCalendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("GMT+8"))
    var selectedYear by remember { mutableStateOf(currentCalendar.get(java.util.Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(currentCalendar.get(java.util.Calendar.MONTH) + 1) }

    val records = remember { mutableStateListOf<OvertimeRecord>() }

    LaunchedEffect(selectedYear, selectedMonth) {
        scope.launch {
            val monthStart = String.format("%04d-%02d-01", selectedYear, selectedMonth)

            val lastDay = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("GMT+8")).apply {
                set(selectedYear, selectedMonth - 1, 1)
                add(java.util.Calendar.MONTH, 1)
                add(java.util.Calendar.DAY_OF_MONTH, -1)
            }.get(java.util.Calendar.DAY_OF_MONTH)
            val monthEnd = String.format("%04d-%02d-%02d", selectedYear, selectedMonth, lastDay)

            val monthRecords = dao.getRecordsByDateRange(monthStart, monthEnd)
            records.clear()
            records.addAll(monthRecords.first())
        }
    }

    val allConfigs by configDao.getAllConfigs().collectAsState(initial = emptyList())

    val baseSalary = allConfigs.find { it.key == "base_salary" }?.value?.toDoubleOrNull() ?: 0.0
    val workdayRate = allConfigs.find { it.key == "workday_rate" }?.value?.toDoubleOrNull() ?: 1.5
    val restdayRate = allConfigs.find { it.key == "restday_rate" }?.value?.toDoubleOrNull() ?: 2.0
    val holidayRate = allConfigs.find { it.key == "holiday_rate" }?.value?.toDoubleOrNull() ?: 3.0

    val hourlyRate = if (baseSalary > 0) baseSalary / 21.75 / 8 else 0.0

    // 计算请假扣除的休息日时长
    val leaveHalfDayRecords = records.filter { it.type == OvertimeType.LEAVE_HALF }
    val leaveFullDayRecords = records.filter { it.type == OvertimeType.LEAVE_FULL }
    val leaveDeductionHours = (leaveHalfDayRecords.size * 4.0) + (leaveFullDayRecords.size * 8.0)

    val workdayRecords = records.filter { it.type == OvertimeType.WORKDAY }
    val restdayRecords = records.filter { it.type == OvertimeType.RESTDAY }
    val holidayRecords = records.filter { it.type == OvertimeType.HOLIDAY }

    val workdayHours = workdayRecords.sumOf { it.duration }
    val restdayHours = restdayRecords.sumOf { it.duration }
    val holidayHours = holidayRecords.sumOf { it.duration }

    // 计算扣除请假后的有效休息日时长
    val adjustedRestdayHours = (restdayHours - leaveDeductionHours).coerceAtLeast(0.0)

    // 计算总时长和薪资（使用调整后的休息日时长）
    val totalHours = workdayHours + adjustedRestdayHours + holidayHours
    val workdaySalary = workdayHours * hourlyRate * workdayRate
    val restdaySalary = adjustedRestdayHours * hourlyRate * restdayRate
    val holidaySalary = holidayHours * hourlyRate * holidayRate
    val totalSalary = workdaySalary + restdaySalary + holidaySalary

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("统计") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            item {
                // 月份选择器
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            if (selectedMonth == 1) {
                                selectedYear--
                                selectedMonth = 12
                            } else {
                                selectedMonth--
                            }
                        }) {
                            Icon(Icons.Default.ArrowLeft, contentDescription = "上一月")
                        }
                        Text(
                            text = "${selectedYear}年${selectedMonth}月",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = {
                            if (selectedMonth == 12) {
                                selectedYear++
                                selectedMonth = 1
                            } else {
                                selectedMonth++
                            }
                        }) {
                            Icon(Icons.Default.ArrowRight, contentDescription = "下一月")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "月度加班汇总",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = String.format("%.1f", totalHours),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "小时",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = String.format("¥%.2f", totalSalary),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "分类统计",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 16.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem(
                                title = "工作日",
                                hours = workdayHours,
                                salary = workdaySalary,
                                color = MaterialTheme.colorScheme.primary
                            )
                            StatItem(
                                title = "休息日",
                                hours = adjustedRestdayHours,
                                salary = restdaySalary,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            StatItem(
                                title = "节假日",
                                hours = holidayHours,
                                salary = holidaySalary,
                                color = Color(0xFFFF5722)
                            )
                        }

                        // 如果有请假记录，显示扣除说明
                        if (leaveDeductionHours > 0) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Text(
                                        text = "请假扣除说明",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = buildString {
                                            append("本月请假 ")
                                            if (leaveHalfDayRecords.isNotEmpty()) {
                                                append("${leaveHalfDayRecords.size} 个半天")
                                            }
                                            if (leaveHalfDayRecords.isNotEmpty() && leaveFullDayRecords.isNotEmpty()) {
                                                append("、")
                                            }
                                            if (leaveFullDayRecords.isNotEmpty()) {
                                                append("${leaveFullDayRecords.size} 个全天")
                                            }
                                            append("\n")
                                            append("扣除休息日加班时长：${leaveDeductionHours} 小时")
                                            if (restdayHours > 0) {
                                                append("\n")
                                                append("原休息日加班：${String.format("%.1f", restdayHours)} 小时 → 调整后：${String.format("%.1f", adjustedRestdayHours)} 小时")
                                            }
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Text(
                    text = "详细记录",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (records.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "本月暂无加班记录",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(records.sortedByDescending { it.date }) { record ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = record.date,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = when (record.type) {
                                        OvertimeType.WORKDAY -> "工作日"
                                        OvertimeType.RESTDAY -> "休息日"
                                        OvertimeType.HOLIDAY -> "节假日"
                                        OvertimeType.LEAVE_HALF -> "请假(半天)"
                                        OvertimeType.LEAVE_FULL -> "请假(全天)"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = when (record.type) {
                                        OvertimeType.WORKDAY -> MaterialTheme.colorScheme.primary
                                        OvertimeType.RESTDAY -> MaterialTheme.colorScheme.secondary
                                        OvertimeType.HOLIDAY -> Color(0xFFFF5722)
                                        OvertimeType.LEAVE_HALF -> Color(0xFF9E9E9E)
                                        OvertimeType.LEAVE_FULL -> Color(0xFF9E9E9E)
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${record.duration} 小时",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = String.format("¥%.2f", calculateSalary(record, hourlyRate, workdayRate, restdayRate, holidayRate)),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            if (record.remark.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = record.remark,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(
    title: String,
    hours: Double,
    salary: Double,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(horizontal = 8.dp)
    ) {
        Text(
            text = String.format("%.1f", hours),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = "小时",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = String.format("¥%.2f", salary),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun calculateSalary(
    record: OvertimeRecord,
    hourlyRate: Double,
    workdayRate: Double,
    restdayRate: Double,
    holidayRate: Double
): Double {
    return when (record.type) {
        OvertimeType.WORKDAY -> record.duration * hourlyRate * workdayRate
        OvertimeType.RESTDAY -> record.duration * hourlyRate * restdayRate
        OvertimeType.HOLIDAY -> record.duration * hourlyRate * holidayRate
        OvertimeType.LEAVE_HALF -> 0.0
        OvertimeType.LEAVE_FULL -> 0.0
    }
}
