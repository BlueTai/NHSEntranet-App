package com.example.nhsentranet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.example.nhsentranet.model.UserSession
import com.example.nhsentranet.network.AzureApiClient
import com.example.nhsentranet.ui.components.WebsiteMatchedPortal
import com.example.nhsentranet.ui.screens.LoginScreen
import com.example.nhsentranet.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = WebTealBrand,
                    secondary = WebCharcoalText,
                    background = WebPageBg,
                    surface = Color.White
                )
            ) {
                var session by remember { mutableStateOf<UserSession?>(null) }
                if (session == null) {
                    LoginScreen(onLoginSuccess = { session = it })
                } else {
                    WebsiteMatchedPortal(
                        session = session!!,
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
