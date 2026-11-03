package com.example.nhsentranet.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhsentranet.model.UserSession
import com.example.nhsentranet.ui.theme.*

@Composable
fun AzureSettingsView(
    session: UserSession,
    isDarkTheme: Boolean = false,
    onToggleDarkTheme: () -> Unit = {},
    onLogout: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, WebDivider),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Account & App Settings", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Signed in as: ${session.fullName} (@${session.username})", fontSize = 13.sp, color = WebSlateIcon)
                Text("Role: ${session.userRole}", fontSize = 13.sp, color = WebSlateIcon)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Server: Connected to Azure App Service", fontSize = 13.sp, color = WebSlateIcon)

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = WebDivider)
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Dark Theme", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
                        Text("Switch to dark color palette", fontSize = 11.sp, color = WebSlateIcon)
                    }
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { onToggleDarkTheme() }
                    )
                }

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
