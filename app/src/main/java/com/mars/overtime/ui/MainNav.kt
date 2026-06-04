package com.mars.overtime.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import com.mars.overtime.ui.screen.*
import com.mars.overtime.ui.theme.BottomBarStyle
import com.mars.overtime.ui.theme.ThemeManager

sealed class BottomNavScreen(val route: String) {
    object Statistics : BottomNavScreen("statistics")
    object Home : BottomNavScreen("home")
    object Settings : BottomNavScreen("settings")
}

@Composable
fun MainNav() {
    val navController = rememberNavController()
    var currentBottomNavScreen by remember { mutableStateOf<BottomNavScreen>(BottomNavScreen.Home) }
    val quickReportMode by ThemeManager.quickReportMode.collectAsState()
    val bottomBarStyle by ThemeManager.bottomBarStyle.collectAsState()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    
    val isMainScreen by remember {
        derivedStateOf {
            currentBackStackEntry?.destination?.route in listOf(
                BottomNavScreen.Statistics.route,
                BottomNavScreen.Home.route,
                BottomNavScreen.Settings.route
            )
        }
    }

    LaunchedEffect(Unit) {
        try {
            val db = com.mars.overtime.OvertimeApplication.database
            val configDao = db.configDao()
            val bottomBarStyleConfig = configDao.getConfig("bottom_bar_style")
            val quickReportModeConfig = configDao.getConfig("quick_report_mode")
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

    Scaffold(
        bottomBar = {
            if (isMainScreen) {
                BottomNavigationBar(
                    onNavigateToStatistics = {
                        if (currentBottomNavScreen != BottomNavScreen.Statistics) {
                            navController.navigate(BottomNavScreen.Statistics.route) {
                                popUpTo(BottomNavScreen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                            currentBottomNavScreen = BottomNavScreen.Statistics
                        }
                    },
                    onNavigateToHome = {
                        if (quickReportMode) {
                            navController.navigate("add_edit_record")
                        } else {
                            if (currentBottomNavScreen != BottomNavScreen.Home) {
                                navController.navigate(BottomNavScreen.Home.route) {
                                    popUpTo(BottomNavScreen.Home.route) { inclusive = true }
                                    launchSingleTop = true
                                }
                                currentBottomNavScreen = BottomNavScreen.Home
                            }
                        }
                    },
                    onNavigateToSettings = {
                        if (currentBottomNavScreen != BottomNavScreen.Settings) {
                            navController.navigate(BottomNavScreen.Settings.route) {
                                popUpTo(BottomNavScreen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                            currentBottomNavScreen = BottomNavScreen.Settings
                        }
                    },
                    currentScreen = currentBottomNavScreen,
                    quickReportMode = quickReportMode,
                    bottomBarStyle = bottomBarStyle
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = BottomNavScreen.Home.route,
            modifier = Modifier.padding(
                bottom = if (isMainScreen) paddingValues.calculateBottomPadding() else 0.dp
            )
        ) {
            composable(
                BottomNavScreen.Statistics.route,
                enterTransition = { fadeIn(animationSpec = tween(250)) },
                exitTransition = { fadeOut(animationSpec = tween(250)) }
            ) {
                currentBottomNavScreen = BottomNavScreen.Statistics
                StatisticsPage()
            }

            composable(
                BottomNavScreen.Home.route,
                enterTransition = { fadeIn(animationSpec = tween(250)) },
                exitTransition = { fadeOut(animationSpec = tween(250)) }
            ) {
                currentBottomNavScreen = BottomNavScreen.Home
                HomePage(
                    onNavigateToAddEdit = { recordId ->
                        if (recordId != null) {
                            navController.navigate("add_edit_record?recordId=$recordId")
                        } else {
                            navController.navigate("add_edit_record")
                        }
                    },
                    onNavigateToSettings = {
                        navController.navigate(BottomNavScreen.Settings.route) {
                            popUpTo(BottomNavScreen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                        currentBottomNavScreen = BottomNavScreen.Settings
                    }
                )
            }

            composable(
                BottomNavScreen.Settings.route,
                enterTransition = { fadeIn(animationSpec = tween(250)) },
                exitTransition = { fadeOut(animationSpec = tween(250)) }
            ) {
                currentBottomNavScreen = BottomNavScreen.Settings
                SettingsPage(
                    onNavigateToAppearanceSettings = {
                        navController.navigate("appearance_settings")
                    },
                    onNavigateToPushSettings = {
                        navController.navigate("push_settings")
                    },
                    onNavigateToSalarySettings = {
                        navController.navigate("salary_settings")
                    },
                    onNavigateToCalendarSettings = {
                        navController.navigate("calendar_settings")
                    },
                    onNavigateToBackupSettings = {
                        navController.navigate("backup_settings")
                    },
                    onNavigateToHolidaySettings = {
                        navController.navigate("holiday_settings")
                    },
                    onNavigateToAbout = {
                        navController.navigate("about")
                    }
                )
            }

            composable(
                "add_edit_record?recordId={recordId}",
                enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)) },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)) },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)) }
            ) { backStackEntry ->
                val recordId = backStackEntry.arguments?.getString("recordId")?.toLongOrNull()
                AddEditRecordPage(
                    recordId = recordId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                "appearance_settings",
                enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)) },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)) },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)) }
            ) {
                AppearanceSettingsPage(onNavigateBack = {
                    navController.popBackStack()
                })
            }

            composable(
                "push_settings",
                enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)) },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)) },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)) }
            ) {
                PushSettingsPage(onNavigateBack = {
                    navController.popBackStack()
                })
            }

            composable(
                "salary_settings",
                enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)) },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)) },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)) }
            ) {
                SalarySettingsPage(onNavigateBack = {
                    navController.popBackStack()
                })
            }

            composable(
                "calendar_settings",
                enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)) },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)) },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)) }
            ) {
                CalendarSettingsPage(onNavigateBack = {
                    navController.popBackStack()
                })
            }

            composable(
                "backup_settings",
                enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)) },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)) },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)) }
            ) {
                BackupSettingsPage(onNavigateBack = {
                    navController.popBackStack()
                })
            }

            composable(
                "holiday_settings",
                enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)) },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)) },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)) }
            ) {
                HolidaySettingsPage(onNavigateBack = {
                    navController.popBackStack()
                })
            }

            composable(
                "about",
                enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)) },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)) },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)) }
            ) {
                AboutPage(onNavigateBack = {
                    navController.popBackStack()
                })
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    onNavigateToStatistics: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToSettings: () -> Unit,
    currentScreen: BottomNavScreen,
    quickReportMode: Boolean = false,
    bottomBarStyle: BottomBarStyle = BottomBarStyle.ICON_AND_TEXT
) {
    if (quickReportMode) {
        NavigationBar(
            tonalElevation = 0.dp,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            NavigationBarItem(
                selected = currentScreen == BottomNavScreen.Statistics,
                onClick = onNavigateToStatistics,
                icon = {
                    if (bottomBarStyle != BottomBarStyle.TEXT_ONLY) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = if (bottomBarStyle == BottomBarStyle.ICON_ONLY) "统计" else null
                        )
                    }
                },
                label = if (bottomBarStyle != BottomBarStyle.ICON_ONLY) {
                    { Text("统计") }
                } else null
            )

            Box(
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                FilledIconButton(
                    onClick = onNavigateToHome,
                    modifier = Modifier.size(48.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "快速提报",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            NavigationBarItem(
                selected = currentScreen == BottomNavScreen.Settings,
                onClick = onNavigateToSettings,
                icon = {
                    if (bottomBarStyle != BottomBarStyle.TEXT_ONLY) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = if (bottomBarStyle == BottomBarStyle.ICON_ONLY) "设置" else null
                        )
                    }
                },
                label = if (bottomBarStyle != BottomBarStyle.ICON_ONLY) {
                    { Text("设置") }
                } else null
            )
        }
    } else {
        NavigationBar(
            tonalElevation = 0.dp,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            NavigationBarItem(
                selected = currentScreen == BottomNavScreen.Statistics,
                onClick = onNavigateToStatistics,
                icon = {
                    if (bottomBarStyle != BottomBarStyle.TEXT_ONLY) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = if (bottomBarStyle == BottomBarStyle.ICON_ONLY) "统计" else null
                        )
                    }
                },
                label = if (bottomBarStyle != BottomBarStyle.ICON_ONLY) {
                    { Text("统计") }
                } else null
            )

            NavigationBarItem(
                selected = currentScreen == BottomNavScreen.Home,
                onClick = onNavigateToHome,
                icon = {
                    if (bottomBarStyle != BottomBarStyle.TEXT_ONLY) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = if (bottomBarStyle == BottomBarStyle.ICON_ONLY) "首页" else null
                        )
                    }
                },
                label = if (bottomBarStyle != BottomBarStyle.ICON_ONLY) {
                    { Text("首页") }
                } else null
            )

            NavigationBarItem(
                selected = currentScreen == BottomNavScreen.Settings,
                onClick = onNavigateToSettings,
                icon = {
                    if (bottomBarStyle != BottomBarStyle.TEXT_ONLY) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = if (bottomBarStyle == BottomBarStyle.ICON_ONLY) "设置" else null
                        )
                    }
                },
                label = if (bottomBarStyle != BottomBarStyle.ICON_ONLY) {
                    { Text("设置") }
                } else null
            )
        }
    }
}
