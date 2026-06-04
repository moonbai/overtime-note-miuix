package com.mars.overtime.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(
    onNavigateToAppearanceSettings: () -> Unit,
    onNavigateToPushSettings: () -> Unit,
    onNavigateToSalarySettings: () -> Unit,
    onNavigateToCalendarSettings: () -> Unit,
    onNavigateToBackupSettings: () -> Unit,
    onNavigateToHolidaySettings: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("设置") },
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
                .verticalScroll(rememberScrollState())
        ) {
            SettingsItem("外观设置", "个性化主题与界面样式", onNavigateToAppearanceSettings)
            SettingsItem("推送设置", "配置加班记录推送通知", onNavigateToPushSettings)
            SettingsItem("薪资设置", "加班薪资与倍率计算", onNavigateToSalarySettings)
            SettingsItem("日历同步", "添加加班记录到日历", onNavigateToCalendarSettings)
            SettingsItem("备份恢复", "数据备份与云端同步", onNavigateToBackupSettings)
            SettingsItem("节假日管理", "自定义节假日规则", onNavigateToHolidaySettings)
            SettingsItem("关于", "版本信息与使用帮助", onNavigateToAbout)
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        trailingContent = { Icon(Icons.Default.ArrowForward, contentDescription = null) },
        modifier = Modifier.clickable(onClick = onClick)
    )
    HorizontalDivider()
}
