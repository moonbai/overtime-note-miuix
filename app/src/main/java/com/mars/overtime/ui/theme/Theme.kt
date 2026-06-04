package com.mars.overtime.ui.theme

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.View
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.mars.overtime.OvertimeApplication
import com.mars.overtime.util.BackupManager
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.rememberThemeController

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

enum class AccentColor {
    BLUE, PURPLE, TEAL, ORANGE, PINK, RED, GREEN, YELLOW
}

enum class FontScale {
    SMALL, NORMAL, LARGE, EXTRA_LARGE
}

enum class RadiusLevel {
    SMALL, MEDIUM, LARGE, EXTRA_LARGE
}

enum class BottomBarStyle {
    ICON_AND_TEXT, ICON_ONLY, TEXT_ONLY
}

@Composable
fun OvertimeTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current

    val themeMode by ThemeManager.themeMode.collectAsState()
    val accentColor by ThemeManager.accentColor.collectAsState()
    val dynamicColor by ThemeManager.dynamicColor.collectAsState()
    val fontScale by ThemeManager.fontScale.collectAsState()
    val radiusLevel by ThemeManager.radiusLevel.collectAsState()
    val bottomBarStyle by ThemeManager.bottomBarStyle.collectAsState()
    val quickReportMode by ThemeManager.quickReportMode.collectAsState()

    LaunchedEffect(Unit) {
        try {
            val db = OvertimeApplication.database
            val configDao = db.configDao()
            val themeModeConfig = configDao.getConfig("theme_mode")
            val accentColorConfig = configDao.getConfig("accent_color")
            val dynamicColorConfig = configDao.getConfig("dynamic_color")
            val fontScaleConfig = configDao.getConfig("font_scale")
            val radiusLevelConfig = configDao.getConfig("radius_level")
            val bottomBarStyleConfig = configDao.getConfig("bottom_bar_style")
            val quickReportModeConfig = configDao.getConfig("quick_report_mode")

            themeModeConfig?.value?.let { value ->
                ThemeManager.updateThemeMode(ThemeMode.valueOf(value))
            }
            accentColorConfig?.value?.let { value ->
                ThemeManager.updateAccentColor(AccentColor.valueOf(value))
            }
            dynamicColorConfig?.value?.let { value ->
                ThemeManager.updateDynamicColor(value.toBoolean())
            }
            fontScaleConfig?.value?.let { value ->
                ThemeManager.updateFontScale(FontScale.valueOf(value))
            }
            radiusLevelConfig?.value?.let { value ->
                ThemeManager.updateRadiusLevel(RadiusLevel.valueOf(value))
            }
            bottomBarStyleConfig?.value?.let { value ->
                ThemeManager.updateBottomBarStyle(BottomBarStyle.valueOf(value))
            }
            quickReportModeConfig?.value?.let { value ->
                ThemeManager.updateQuickReportMode(value.toBoolean())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val colorSchemeMode = when (themeMode) {
        ThemeMode.SYSTEM -> ColorSchemeMode.System
        ThemeMode.LIGHT -> ColorSchemeMode.Light
        ThemeMode.DARK -> ColorSchemeMode.Dark
    }

    val themeController = rememberThemeController(colorSchemeMode)

    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )

            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }

            WindowCompat.setDecorFitsSystemWindows(window, false)

            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MiuixTheme(
        controller = themeController,
        content = content
    )
}

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

suspend fun saveThemeMode(context: Context, mode: ThemeMode) {
    try {
        ThemeManager.updateThemeMode(mode)
        val db = OvertimeApplication.database
        val configDao = db.configDao()
        configDao.saveConfig(com.mars.overtime.database.AppConfig("theme_mode", mode.name))
        triggerAutoBackup(context)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

suspend fun saveAccentColor(context: Context, color: AccentColor) {
    try {
        ThemeManager.updateAccentColor(color)
        val db = OvertimeApplication.database
        val configDao = db.configDao()
        configDao.saveConfig(com.mars.overtime.database.AppConfig("accent_color", color.name))
        triggerAutoBackup(context)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

suspend fun saveDynamicColor(context: Context, enabled: Boolean) {
    try {
        ThemeManager.updateDynamicColor(enabled)
        val db = OvertimeApplication.database
        val configDao = db.configDao()
        configDao.saveConfig(com.mars.overtime.database.AppConfig("dynamic_color", enabled.toString()))
        triggerAutoBackup(context)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

suspend fun saveFontScale(context: Context, scale: FontScale) {
    try {
        ThemeManager.updateFontScale(scale)
        val db = OvertimeApplication.database
        val configDao = db.configDao()
        configDao.saveConfig(com.mars.overtime.database.AppConfig("font_scale", scale.name))
        triggerAutoBackup(context)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

suspend fun saveRadiusLevel(context: Context, level: RadiusLevel) {
    try {
        ThemeManager.updateRadiusLevel(level)
        val db = OvertimeApplication.database
        val configDao = db.configDao()
        configDao.saveConfig(com.mars.overtime.database.AppConfig("radius_level", level.name))
        triggerAutoBackup(context)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

suspend fun saveBottomBarStyle(context: Context, style: BottomBarStyle) {
    try {
        ThemeManager.updateBottomBarStyle(style)
        val db = OvertimeApplication.database
        val configDao = db.configDao()
        configDao.saveConfig(com.mars.overtime.database.AppConfig("bottom_bar_style", style.name))
        triggerAutoBackup(context)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

suspend fun saveQuickReportMode(context: Context, enabled: Boolean) {
    try {
        ThemeManager.updateQuickReportMode(enabled)
        val db = OvertimeApplication.database
        val configDao = db.configDao()
        configDao.saveConfig(com.mars.overtime.database.AppConfig("quick_report_mode", enabled.toString()))
        triggerAutoBackup(context)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
