package com.mars.overtime.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mars.overtime.OvertimeApplication
import com.mars.overtime.database.AppConfig
import com.mars.overtime.util.HolidayDataSource
import com.mars.overtime.util.HolidayManager
import kotlinx.coroutines.launch
import java.time.Year

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HolidaySettingsPage(
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val db = OvertimeApplication.database
    val configDao = db.configDao()

    val allConfigs by configDao.getAllConfigs().collectAsState(initial = emptyList())

    var dataSource by remember { mutableStateOf(HolidayDataSource.TIMOR) }
    var customApiUrl by remember { mutableStateOf("") }
    var mxnzpAppId by remember { mutableStateOf("") }
    var mxnzpAppSecret by remember { mutableStateOf("") }
    var mxnzpIgnoreHoliday by remember { mutableStateOf(false) }
    var updateResult by remember { mutableStateOf("") }
    var showResultDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(allConfigs) {
        dataSource = allConfigs.find { it.key == "holiday_data_source" }?.value?.let {
            try {
                HolidayDataSource.valueOf(it.uppercase())
            } catch (e: Exception) {
                HolidayDataSource.TIMOR
            }
        } ?: HolidayDataSource.TIMOR
        customApiUrl = allConfigs.find { it.key == "holiday_custom_url" }?.value ?: ""
        mxnzpAppId = allConfigs.find { it.key == "mxnzp_app_id" }?.value ?: ""
        mxnzpAppSecret = allConfigs.find { it.key == "mxnzp_app_secret" }?.value ?: ""
        mxnzpIgnoreHoliday = allConfigs.find { it.key == "mxnzp_ignore_holiday" }?.value?.toBoolean() ?: false
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("节假日管理") },
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
            Text("数据源选择", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            
            HolidayDataSource.values().forEach { source ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = dataSource == source,
                        onClick = { 
                            dataSource = source
                            scope.launch {
                                configDao.saveConfig(AppConfig("holiday_data_source", source.name))
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (source) {
                            HolidayDataSource.TIMOR -> "Timor API"
                            HolidayDataSource.MXNZP -> "MXNZP API"
                            HolidayDataSource.CUSTOM -> "自定义 API"
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (dataSource == HolidayDataSource.MXNZP) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("MXNZP 配置", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = mxnzpAppId,
                            onValueChange = { mxnzpAppId = it },
                            label = { Text("App ID") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = mxnzpAppSecret,
                            onValueChange = { mxnzpAppSecret = it },
                            label = { Text("App Secret") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("忽略节假日", style = MaterialTheme.typography.bodyLarge)
                            Switch(
                                checked = mxnzpIgnoreHoliday,
                                onCheckedChange = {
                                    mxnzpIgnoreHoliday = it
                                    scope.launch {
                                        configDao.saveConfig(AppConfig("mxnzp_ignore_holiday", it.toString()))
                                    }
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (dataSource == HolidayDataSource.CUSTOM) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("自定义 API 配置", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "支持 {year} 或 \${year} 占位符自动替换年份",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = customApiUrl,
                            onValueChange = { customApiUrl = it },
                            label = { Text("API 地址") },
                            placeholder = { Text("https://api.example.com/holiday/year/{year}") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = {
                    scope.launch {
                        configDao.saveConfigs(
                            listOf(
                                AppConfig("holiday_data_source", dataSource.name),
                                AppConfig("holiday_custom_url", customApiUrl),
                                AppConfig("mxnzp_app_id", mxnzpAppId),
                                AppConfig("mxnzp_app_secret", mxnzpAppSecret),
                                AppConfig("mxnzp_ignore_holiday", mxnzpIgnoreHoliday.toString())
                            )
                        )
                        updateResult = "配置已保存"
                        showResultDialog = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存配置")
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            val currentYear = Year.now().value.toString()
            Text("当前年份: $currentYear", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    isLoading = true
                    scope.launch {
                        HolidayManager.clearCache()
                        val success = HolidayManager.fetchHolidays(currentYear)
                        updateResult = if (success) {
                            "$currentYear 年节假日规则已更新"
                        } else {
                            "更新失败，请检查网络连接"
                        }
                        isLoading = false
                        showResultDialog = true
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("手动更新节假日规则")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    HolidayManager.clearCache()
                    updateResult = "节假日缓存已清除"
                    showResultDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text("清除缓存")
            }
        }
    }

    if (showResultDialog) {
        AlertDialog(
            onDismissRequest = { showResultDialog = false },
            title = { Text("提示") },
            text = { Text(updateResult) },
            confirmButton = {
                TextButton(onClick = { showResultDialog = false }) { Text("确定") }
            }
        )
    }
}
