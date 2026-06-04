package com.mars.overtime.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Help
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.content.Context
import com.mars.overtime.OvertimeApplication
import com.mars.overtime.database.AppConfig
import com.mars.overtime.database.OvertimeRecord
import com.mars.overtime.database.OvertimeType
import com.mars.overtime.push.PushManager
import com.mars.overtime.util.BackupManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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
fun PushSettingsPage(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val db = OvertimeApplication.database
    val configDao = db.configDao()

    val scope = rememberCoroutineScope()

    val allConfigs by configDao.getAllConfigs().collectAsState(initial = emptyList())

    var pushEnabled by remember { mutableStateOf(allConfigs.find { it.key == "push_enabled" }?.value?.toBoolean() ?: false) }

    // 渠道配置
    var dingtalkUrl by remember { mutableStateOf(allConfigs.find { it.key == "push_dingtalk" }?.value ?: "") }
    var dingtalkSecret by remember { mutableStateOf(allConfigs.find { it.key == "push_dingtalk_secret" }?.value ?: "") }
    var feishuUrl by remember { mutableStateOf(allConfigs.find { it.key == "push_feishu" }?.value ?: "") }
    var feishuSecret by remember { mutableStateOf(allConfigs.find { it.key == "push_feishu_secret" }?.value ?: "") }
    var wecomUrl by remember { mutableStateOf(allConfigs.find { it.key == "push_wecom" }?.value ?: "") }
    var wecomSecret by remember { mutableStateOf(allConfigs.find { it.key == "push_wecom_secret" }?.value ?: "") }
    var wxPusherUrl by remember { mutableStateOf(allConfigs.find { it.key == "push_wxpusher" }?.value ?: "") }
    var wxPusherToken by remember { mutableStateOf(allConfigs.find { it.key == "push_wxpusher_token" }?.value ?: "") }
    var telegramUrl by remember { mutableStateOf(allConfigs.find { it.key == "push_telegram" }?.value ?: "") }
    var telegramChatId by remember { mutableStateOf(allConfigs.find { it.key == "push_telegram_chatid" }?.value ?: "") }
    var discordUrl by remember { mutableStateOf(allConfigs.find { it.key == "push_discord" }?.value ?: "") }
    var discordUsername by remember { mutableStateOf(allConfigs.find { it.key == "push_discord_username" }?.value ?: "") }
    var customUrl by remember { mutableStateOf(allConfigs.find { it.key == "push_custom" }?.value ?: "") }
    var customHeaders by remember { mutableStateOf(allConfigs.find { it.key == "push_custom_headers" }?.value ?: "") }

    // 下拉选择
    val channels = listOf("钉钉", "飞书", "企业微信", "WxPusher", "Telegram", "Discord", "自定义推送")
    var selectedChannel by remember { mutableStateOf(channels.first()) }
    var channelExpanded by remember { mutableStateOf(false) }
    
    // 渠道与配置键的映射
    val channelConfigMap = mapOf(
        "钉钉" to "dingtalk",
        "飞书" to "feishu",
        "企业微信" to "wecom",
        "WxPusher" to "wxpusher",
        "Telegram" to "telegram",
        "Discord" to "discord",
        "自定义推送" to "custom"
    )

    var pushResult by remember { mutableStateOf("") }
    var showResultDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var helpContent by remember { mutableStateOf("") }

    LaunchedEffect(allConfigs) {
        pushEnabled = allConfigs.find { it.key == "push_enabled" }?.value?.toBoolean() ?: false
        dingtalkUrl = allConfigs.find { it.key == "push_dingtalk" }?.value ?: ""
        dingtalkSecret = allConfigs.find { it.key == "push_dingtalk_secret" }?.value ?: ""
        feishuUrl = allConfigs.find { it.key == "push_feishu" }?.value ?: ""
        feishuSecret = allConfigs.find { it.key == "push_feishu_secret" }?.value ?: ""
        wecomUrl = allConfigs.find { it.key == "push_wecom" }?.value ?: ""
        wecomSecret = allConfigs.find { it.key == "push_wecom_secret" }?.value ?: ""
        wxPusherUrl = allConfigs.find { it.key == "push_wxpusher" }?.value ?: ""
        wxPusherToken = allConfigs.find { it.key == "push_wxpusher_token" }?.value ?: ""
        telegramUrl = allConfigs.find { it.key == "push_telegram" }?.value ?: ""
        telegramChatId = allConfigs.find { it.key == "push_telegram_chatid" }?.value ?: ""
        discordUrl = allConfigs.find { it.key == "push_discord" }?.value ?: ""
        discordUsername = allConfigs.find { it.key == "push_discord_username" }?.value ?: ""
        customUrl = allConfigs.find { it.key == "push_custom" }?.value ?: ""
        customHeaders = allConfigs.find { it.key == "push_custom_headers" }?.value ?: ""
        
        // 加载保存的渠道
        val savedChannelKey = allConfigs.find { it.key == "push_channel" }?.value
        selectedChannel = channelConfigMap.entries.find { it.value == savedChannelKey }?.key ?: channels.first()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("推送设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        helpContent = """
                            各推送渠道说明：

                            1. 钉钉机器人：在钉钉群中添加自定义机器人，获取Webhook地址。支持加签密钥验证。

                            2. 飞书机器人：在飞书群中添加自定义机器人，获取Webhook地址。支持签名密钥验证。

                            3. 企业微信：在企业微信群中添加群机器人，获取Webhook地址。支持加签密钥验证。

                            4. WxPusher：访问 https://wxpusher.zjiecode.com/ 获取。可选填写App Token。

                            5. Telegram：创建Bot并获取Token，填入格式：https://api.telegram.org/bot{token}/sendMessage。需填写Chat ID。

                            6. Discord：在Discord服务器中创建Webhook，获取地址。可选填写用户名。

                            7. 自定义推送：填写您自己的API接口地址，将以POST方式发送JSON数据。可选填写自定义请求头。
                        """.trimIndent()
                        showHelpDialog = true
                    }) {
                        Icon(Icons.Default.Help, contentDescription = "帮助")
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
            // 1. 总开关
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("启用推送", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "开启后记录加班时会自动推送通知",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = pushEnabled,
                        onCheckedChange = { enabled ->
                            pushEnabled = enabled
                            scope.launch {
                                configDao.saveConfig(AppConfig("push_enabled", enabled.toString()))
                                triggerAutoBackup(context)
                            }
                        }
                    )
                }
            }

            if (pushEnabled) {
                Spacer(modifier = Modifier.height(24.dp))

                // 2. 下拉选择渠道
                Text("选择渠道", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = channelExpanded,
                    onExpandedChange = { channelExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedChannel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("推送渠道") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(channelExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = channelExpanded,
                        onDismissRequest = { channelExpanded = false }
                    ) {
                        channels.forEach { channel ->
                            DropdownMenuItem(
                                text = { Text(channel) },
                                onClick = {
                                    selectedChannel = channel
                                    channelExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 3. 渠道配置信息
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "${selectedChannel}配置",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        when (selectedChannel) {
                            "钉钉" -> {
                                OutlinedTextField(
                                    value = dingtalkUrl,
                                    onValueChange = { dingtalkUrl = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Webhook URL") },
                                    placeholder = { Text("https://oapi.dingtalk.com/robot/send?access_token=xxx") },
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = dingtalkSecret,
                                    onValueChange = { dingtalkSecret = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("加签密钥（可选）") },
                                    placeholder = { Text("SEC...") },
                                    singleLine = true
                                )
                            }
                            "飞书" -> {
                                OutlinedTextField(
                                    value = feishuUrl,
                                    onValueChange = { feishuUrl = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Webhook URL") },
                                    placeholder = { Text("https://open.feishu.cn/open-apis/bot/v2/hook/xxx") },
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = feishuSecret,
                                    onValueChange = { feishuSecret = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("签名密钥（可选）") },
                                    placeholder = { Text("签名密钥") },
                                    singleLine = true
                                )
                            }
                            "企业微信" -> {
                                OutlinedTextField(
                                    value = wecomUrl,
                                    onValueChange = { wecomUrl = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Webhook URL") },
                                    placeholder = { Text("https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=xxx") },
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = wecomSecret,
                                    onValueChange = { wecomSecret = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("加签密钥（可选）") },
                                    placeholder = { Text("加签密钥") },
                                    singleLine = true
                                )
                            }
                            "WxPusher" -> {
                                OutlinedTextField(
                                    value = wxPusherUrl,
                                    onValueChange = { wxPusherUrl = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Webhook URL") },
                                    placeholder = { Text("https://wxpusher.zjiecode.com/api/v1/send/xxx") },
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = wxPusherToken,
                                    onValueChange = { wxPusherToken = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("App Token（可选）") },
                                    placeholder = { Text("AT_xxx") },
                                    singleLine = true
                                )
                            }
                            "Telegram" -> {
                                OutlinedTextField(
                                    value = telegramUrl,
                                    onValueChange = { telegramUrl = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Bot API URL") },
                                    placeholder = { Text("https://api.telegram.org/bot{token}/sendMessage") },
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = telegramChatId,
                                    onValueChange = { telegramChatId = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Chat ID（必填）") },
                                    placeholder = { Text("123456789") },
                                    singleLine = true
                                )
                            }
                            "Discord" -> {
                                OutlinedTextField(
                                    value = discordUrl,
                                    onValueChange = { discordUrl = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Webhook URL") },
                                    placeholder = { Text("https://discord.com/api/webhooks/xxx/xxx") },
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = discordUsername,
                                    onValueChange = { discordUsername = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("用户名（可选）") },
                                    placeholder = { Text("自定义推送用户名") },
                                    singleLine = true
                                )
                            }
                            "自定义推送" -> {
                                OutlinedTextField(
                                    value = customUrl,
                                    onValueChange = { customUrl = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("API URL") },
                                    placeholder = { Text("https://your-server.com/api/push") },
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = customHeaders,
                                    onValueChange = { customHeaders = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("请求头（可选）") },
                                    placeholder = { Text("Authorization: Bearer xxx\nContent-Type: application/json") },
                                    minLines = 3,
                                    maxLines = 5
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 4. 测试按钮
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val testRecord = OvertimeRecord(
                                id = 0,
                                date = dateFormat.format(Date()),
                                startTime = "18:00",
                                endTime = "21:00",
                                duration = 3.0,
                                type = OvertimeType.WORKDAY,
                                money = 100.0,
                                remark = "测试推送",
                                createTime = System.currentTimeMillis()
                            )

                            val result = when (selectedChannel) {
                                "钉钉" -> {
                                    if (dingtalkUrl.isBlank()) {
                                        "请先填写 Webhook URL"
                                    } else {
                                        val success = PushManager.sendDingTalk(dingtalkUrl, dingtalkSecret, testRecord)
                                        "钉钉推送: ${if (success) "成功" else "失败"}"
                                    }
                                }
                                "飞书" -> {
                                    if (feishuUrl.isBlank()) {
                                        "请先填写 Webhook URL"
                                    } else {
                                        val success = PushManager.sendFeishu(feishuUrl, feishuSecret, testRecord)
                                        "飞书推送: ${if (success) "成功" else "失败"}"
                                    }
                                }
                                "企业微信" -> {
                                    if (wecomUrl.isBlank()) {
                                        "请先填写 Webhook URL"
                                    } else {
                                        val success = PushManager.sendWeCom(wecomUrl, wecomSecret, testRecord)
                                        "企业微信推送: ${if (success) "成功" else "失败"}"
                                    }
                                }
                                "WxPusher" -> {
                                    if (wxPusherUrl.isBlank()) {
                                        "请先填写 Webhook URL"
                                    } else {
                                        val success = PushManager.sendWxPusher(wxPusherUrl, testRecord)
                                        "WxPusher推送: ${if (success) "成功" else "失败"}"
                                    }
                                }
                                "Telegram" -> {
                                    if (telegramUrl.isBlank()) {
                                        "请先填写 Bot API URL"
                                    } else if (telegramChatId.isBlank()) {
                                        "请先填写 Chat ID"
                                    } else {
                                        val success = PushManager.sendTelegram(telegramUrl, telegramChatId, testRecord)
                                        "Telegram推送: ${if (success) "成功" else "失败"}"
                                    }
                                }
                                "Discord" -> {
                                    if (discordUrl.isBlank()) {
                                        "请先填写 Webhook URL"
                                    } else {
                                        val success = PushManager.sendDiscord(discordUrl, discordUsername, testRecord)
                                        "Discord推送: ${if (success) "成功" else "失败"}"
                                    }
                                }
                                "自定义推送" -> {
                                    if (customUrl.isBlank()) {
                                        "请先填写 API URL"
                                    } else {
                                        val success = PushManager.sendCustom(customUrl, customHeaders, testRecord)
                                        "自定义推送: ${if (success) "成功" else "失败"}"
                                    }
                                }
                                else -> "未知渠道"
                            }

                            pushResult = result
                            showResultDialog = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("测试推送", style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 5. 保存按钮
                Button(
                    onClick = {
                        scope.launch {
                            configDao.saveConfigs(
                                listOf(
                                    AppConfig("push_enabled", pushEnabled.toString()),
                                    AppConfig("push_channel", channelConfigMap[selectedChannel] ?: "dingtalk"),
                                    AppConfig("push_dingtalk", dingtalkUrl),
                                    AppConfig("push_dingtalk_secret", dingtalkSecret),
                                    AppConfig("push_feishu", feishuUrl),
                                    AppConfig("push_feishu_secret", feishuSecret),
                                    AppConfig("push_wecom", wecomUrl),
                                    AppConfig("push_wecom_secret", wecomSecret),
                                    AppConfig("push_wxpusher", wxPusherUrl),
                                    AppConfig("push_wxpusher_token", wxPusherToken),
                                    AppConfig("push_telegram", telegramUrl),
                                    AppConfig("push_telegram_chatid", telegramChatId),
                                    AppConfig("push_discord", discordUrl),
                                    AppConfig("push_discord_username", discordUsername),
                                    AppConfig("push_custom", customUrl),
                                    AppConfig("push_custom_headers", customHeaders)
                                )
                            )
                            triggerAutoBackup(context)
                            pushResult = "配置已保存"
                            showResultDialog = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("保存配置", style = MaterialTheme.typography.titleMedium)
                }
            } else {
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "推送功能已关闭",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showResultDialog) {
        AlertDialog(
            onDismissRequest = { showResultDialog = false },
            title = { Text("推送结果") },
            text = { Text(pushResult) },
            confirmButton = {
                TextButton(onClick = { showResultDialog = false }) { Text("确定") }
            }
        )
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("帮助说明") },
            text = { Text(helpContent) },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) { Text("确定") }
            }
        )
    }
}
