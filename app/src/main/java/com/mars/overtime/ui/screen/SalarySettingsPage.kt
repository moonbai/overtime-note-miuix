package com.mars.overtime.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.content.Context
import com.mars.overtime.OvertimeApplication
import com.mars.overtime.database.AppConfig
import com.mars.overtime.util.BackupManager
import kotlinx.coroutines.launch

private suspend fun triggerAutoBackup(context: Context) {
    try {
        val db = OvertimeApplication.database
        val overtimeDao = db.overtimeDao()
        val configDao = db.configDao()
        val records = overtimeDao.getAllRecordsSync()
        val allConfigs = configDao.getAllConfigsSync()
        val webdavUrl = allConfigs.find { it.key == "webdav_url" }?.value
        val webdavUsername = allConfigs.find { it.key == "webdav_username" }?.value
        val webdavPassword = allConfigs.find { it.key == "webdav_password" }?.value
        val webdavPath = allConfigs.find { it.key == "webdav_path" }?.value
        BackupManager.performAutoBackup(
            context = context,
            records = records,
            configs = allConfigs,
            webdavUrl = webdavUrl,
            webdavUsername = webdavUsername,
            webdavPassword = webdavPassword,
            webdavPath = webdavPath
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalarySettingsPage(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val db = OvertimeApplication.database
    val configDao = db.configDao()

    val scope = rememberCoroutineScope()

    val allConfigs by configDao.getAllConfigs().collectAsState(initial = emptyList())

    var baseSalary by remember { mutableStateOf(allConfigs.find { it.key == "base_salary" }?.value ?: "2200") }
    var workdayRate by remember { mutableStateOf(allConfigs.find { it.key == "workday_rate" }?.value ?: "1.5") }
    var restdayRate by remember { mutableStateOf(allConfigs.find { it.key == "restday_rate" }?.value ?: "2.0") }
    var holidayRate by remember { mutableStateOf(allConfigs.find { it.key == "holiday_rate" }?.value ?: "3.0") }

    LaunchedEffect(allConfigs) {
        baseSalary = allConfigs.find { it.key == "base_salary" }?.value ?: "2200"
        workdayRate = allConfigs.find { it.key == "workday_rate" }?.value ?: "1.5"
        restdayRate = allConfigs.find { it.key == "restday_rate" }?.value ?: "2.0"
        holidayRate = allConfigs.find { it.key == "holiday_rate" }?.value ?: "3.0"
    }

    val baseSalaryNum = baseSalary.toDoubleOrNull() ?: 0.0
    val workdayRateNum = workdayRate.toDoubleOrNull() ?: 1.5
    val restdayRateNum = restdayRate.toDoubleOrNull() ?: 2.0
    val holidayRateNum = holidayRate.toDoubleOrNull() ?: 3.0
    
    val hourlyRate = if (baseSalaryNum > 0) baseSalaryNum / 21.75 / 8 else 0.0

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("薪资设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("基础薪资基数 (元/月)", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = baseSalary,
                onValueChange = { baseSalary = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("加班工资计算", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "小时基础工资: ¥${"%.2f".format(hourlyRate)}/小时",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "工作日加班: ¥${"%.2f".format(hourlyRate * workdayRateNum)}/小时",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "休息日加班: ¥${"%.2f".format(hourlyRate * restdayRateNum)}/小时",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "法定节假日: ¥${"%.2f".format(hourlyRate * holidayRateNum)}/小时",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text("工作日延时倍率", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = workdayRate,
                onValueChange = { workdayRate = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("休息日倍率", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = restdayRate,
                onValueChange = { restdayRate = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("法定节假日倍率", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = holidayRate,
                onValueChange = { holidayRate = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    scope.launch {
                        configDao.saveConfigs(
                            listOf(
                                AppConfig("base_salary", baseSalary),
                                AppConfig("workday_rate", workdayRate),
                                AppConfig("restday_rate", restdayRate),
                                AppConfig("holiday_rate", holidayRate)
                            )
                        )
                        triggerAutoBackup(context)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存")
            }
        }
    }
}
