package com.example.nhsentranet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.example.nhsentranet.model.UserSession
import com.example.nhsentranet.network.AzureApiClient
import com.example.nhsentranet.ui.screens.LoginScreen
import com.example.nhsentranet.ui.screens.WebsiteMatchedPortal
import com.example.nhsentranet.ui.theme.WebCharcoalText
import com.example.nhsentranet.ui.theme.NewtonHouseTheme
import com.example.nhsentranet.ui.theme.WebCharcoalText
import com.example.nhsentranet.ui.theme.WebPageBg
import com.example.nhsentranet.ui.theme.WebTealBrand

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }

            NewtonHouseTheme(isDarkTheme = isDarkTheme) {
                var session by remember { mutableStateOf<UserSession?>(null) }
                if (session == null) {
                    LoginScreen(onLoginSuccess = { session = it })
                } else {
                    WebsiteMatchedPortal(
                        session = session!!,
                        isDarkTheme = isDarkTheme,
                        onToggleDarkTheme = { isDarkTheme = !isDarkTheme },
                        onLogout = {
                            AzureApiClient.clearSession()
                            session = null
                        }
                    )
                }
            }
        }
    }
}
