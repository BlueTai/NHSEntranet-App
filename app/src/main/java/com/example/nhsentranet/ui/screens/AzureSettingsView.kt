package com.example.nhsentranet.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhsentranet.model.UserSession
import com.example.nhsentranet.ui.theme.NewtonRed
import com.example.nhsentranet.ui.theme.WebCharcoalText
import com.example.nhsentranet.ui.theme.WebDivider
import com.example.nhsentranet.ui.theme.WebSlateIcon

@Composable
fun AzureSettingsView(session: UserSession, onLogout: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, WebDivider), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Account Settings", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = WebCharcoalText)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Signed in as: ${session.fullName} (@${session.username})", fontSize = 13.sp, color = WebSlateIcon)
                Text("Role: ${session.userRole}", fontSize = 13.sp, color = WebSlateIcon)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Server: Connected to Azure App Service", fontSize = 13.sp, color = WebSlateIcon)
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(containerColor = NewtonRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign Out", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
