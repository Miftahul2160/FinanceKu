package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.MainApp
import com.example.ui.theme.FinanceKuTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val container = (application as FinanceApplication).container

    setContent {
      FinanceKuTheme {
        MainApp(container = container)
      }
    }
  }
}
