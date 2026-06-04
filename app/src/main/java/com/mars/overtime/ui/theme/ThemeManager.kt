package com.mars.overtime.ui.theme

import androidx.compose.runtime.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ThemeManager {
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _accentColor = MutableStateFlow(AccentColor.ORANGE)
    val accentColor: StateFlow<AccentColor> = _accentColor.asStateFlow()

    private val _dynamicColor = MutableStateFlow(false)
    val dynamicColor: StateFlow<Boolean> = _dynamicColor.asStateFlow()

    private val _fontScale = MutableStateFlow(FontScale.NORMAL)
    val fontScale: StateFlow<FontScale> = _fontScale.asStateFlow()

    private val _radiusLevel = MutableStateFlow(RadiusLevel.MEDIUM)
    val radiusLevel: StateFlow<RadiusLevel> = _radiusLevel.asStateFlow()

    private val _bottomBarStyle = MutableStateFlow(BottomBarStyle.ICON_AND_TEXT)
    val bottomBarStyle: StateFlow<BottomBarStyle> = _bottomBarStyle.asStateFlow()

    private val _quickReportMode = MutableStateFlow(false)
    val quickReportMode: StateFlow<Boolean> = _quickReportMode.asStateFlow()

    fun updateThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }

    fun updateAccentColor(color: AccentColor) {
        _accentColor.value = color
    }

    fun updateDynamicColor(enabled: Boolean) {
        _dynamicColor.value = enabled
    }

    fun updateFontScale(scale: FontScale) {
        _fontScale.value = scale
    }

    fun updateRadiusLevel(level: RadiusLevel) {
        _radiusLevel.value = level
    }

    fun updateBottomBarStyle(style: BottomBarStyle) {
        _bottomBarStyle.value = style
    }

    fun updateQuickReportMode(enabled: Boolean) {
        _quickReportMode.value = enabled
    }

    fun getAccentColorName(color: AccentColor): String {
        return when (color) {
            AccentColor.BLUE -> "蓝色"
            AccentColor.PURPLE -> "紫色"
            AccentColor.TEAL -> "青色"
            AccentColor.ORANGE -> "橙色"
            AccentColor.PINK -> "粉色"
            AccentColor.RED -> "红色"
            AccentColor.GREEN -> "绿色"
            AccentColor.YELLOW -> "黄色"
        }
    }

    fun getFontScaleName(scale: FontScale): String {
        return when (scale) {
            FontScale.SMALL -> "小"
            FontScale.NORMAL -> "标准"
            FontScale.LARGE -> "大"
            FontScale.EXTRA_LARGE -> "特大"
        }
    }

    fun getRadiusLevelName(level: RadiusLevel): String {
        return when (level) {
            RadiusLevel.SMALL -> "紧凑"
            RadiusLevel.MEDIUM -> "标准"
            RadiusLevel.LARGE -> "圆润"
            RadiusLevel.EXTRA_LARGE -> "极圆"
        }
    }

    fun getBottomBarStyleName(style: BottomBarStyle): String {
        return when (style) {
            BottomBarStyle.ICON_AND_TEXT -> "图标+文字"
            BottomBarStyle.ICON_ONLY -> "仅图标"
            BottomBarStyle.TEXT_ONLY -> "仅文字"
        }
    }
}
