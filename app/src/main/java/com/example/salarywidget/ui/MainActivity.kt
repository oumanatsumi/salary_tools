package com.example.salarywidget.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.salarywidget.ui.screens.ConfigScreen
import com.example.salarywidget.ui.theme.SalaryWidgetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SalaryWidgetTheme {
                ConfigScreen()
            }
        }
    }
}
