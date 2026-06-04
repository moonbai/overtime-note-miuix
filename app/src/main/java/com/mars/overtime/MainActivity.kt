package com.mars.overtime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.mars.overtime.ui.MainNav
import com.mars.overtime.ui.theme.OvertimeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OvertimeApp()
        }
    }
}

@Composable
fun OvertimeApp() {
    OvertimeTheme {
        MainNav()
    }
}
