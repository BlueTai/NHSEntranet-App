package com.example.nhsentranet.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhsentranet.model.UserSession
import com.example.nhsentranet.ui.theme.*

@Composable
fun SettingsScreen(
    session: UserSession,
    ttsEnabled: Boolean,
    onToggleTts: () -> Unit,
    onLogout: () -> Unit
) {
    var pushNotifications by remember { mutableStateOf(true) }
    var biometricLock by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var feedbackMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("App Settings & Preferences", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NewtonNavy)
        Text("Manage profile, notifications, security & AI navigator options", fontSize = 12.sp, color = NewtonTextMuted)
        Spacer(modifier = Modifier.height(14.dp))

        // -----------------------------------------------------
        // 1. PROFILE SUMMARY CARD
        // -----------------------------------------------------
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, NewtonBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = NewtonTealPrimary,
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = session.fullName.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(session.fullName.ifEmpty { session.username }, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NewtonNavy)
                    Text("Username: ${session.username} • Role: ${session.role}", fontSize = 12.sp, color = NewtonTextMuted)
                }

                Surface(
                    color = Color(0xFFEFF6FF),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = session.role,
                        color = NewtonBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // -----------------------------------------------------
        // 2. PREFERENCES & NOTIFICATION SETTINGS
        // -----------------------------------------------------
        Text("App Preferences", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NewtonTextDark)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, NewtonBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                SettingsSwitchRow(
                    title = "Newton Navigator Voice (TTS)",
                    subtitle = "Read AI assistant answers aloud using speech synthesis",
                    icon = Icons.AutoMirrored.Outlined.VolumeUp,
                    checked = ttsEnabled,
                    onCheckedChange = { onToggleTts() }
                )
                HorizontalDivider(color = Color(0xFFF1F5F9))
                SettingsSwitchRow(
                    title = "Push Notifications (FCM)",
                    subtitle = "Receive real-time circulars, marks & fee alerts",
                    icon = Icons.Outlined.Notifications,
                    checked = pushNotifications,
                    onCheckedChange = { pushNotifications = it }
                )
                HorizontalDivider(color = Color(0xFFF1F5F9))
                SettingsSwitchRow(
                    title = "Biometric Lock",
                    subtitle = "Require fingerprint/PIN to open portal",
                    icon = Icons.Outlined.Fingerprint,
                    checked = biometricLock,
                    onCheckedChange = { biometricLock = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // -----------------------------------------------------
        // 3. ACCOUNT SECURITY & PRIVACY
        // -----------------------------------------------------
        Text("Security & Support", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NewtonTextDark)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, NewtonBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                SettingsActionRow(
                    title = "Change Account Password",
                    subtitle = "Update your Newton House intranet credentials",
                    icon = Icons.Outlined.Lock,
                    onClick = { showChangePasswordDialog = true }
                )
                HorizontalDivider(color = Color(0xFFF1F5F9))
                SettingsActionRow(
                    title = "Clear Cache & Re-Sync Database",
                    subtitle = "Force fresh re-sync from Firebase Cloud Firestore",
                    icon = Icons.Outlined.Sync,
                    onClick = { feedbackMsg = "Database cache cleared & re-synced successfully." }
                )
                HorizontalDivider(color = Color(0xFFF1F5F9))
                SettingsActionRow(
                    title = "Helpdesk & Technical Support",
                    subtitle = "Email support@newtonhouse.co.za",
                    icon = Icons.AutoMirrored.Outlined.HelpOutline,
                    onClick = { feedbackMsg = "Contacting support at support@newtonhouse.co.za" }
                )
            }
        }

        if (feedbackMsg != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                color = Color(0xFFE5F4F7),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = feedbackMsg!!,
                    color = NewtonTealPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // -----------------------------------------------------
        // 4. SIGN OUT BUTTON
        // -----------------------------------------------------
        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = NewtonRed),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign Out of Portal", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Newton House Student Intranet • Version 1.0.0 (Build 2026.1)",
            fontSize = 11.sp,
            color = NewtonTextMuted,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }

    // Change Password Dialog
    if (showChangePasswordDialog) {
        var oldPass by remember { mutableStateOf("") }
        var newPass by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            title = { Text("Change Password", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = oldPass,
                        onValueChange = { oldPass = it },
                        label = { Text("Current Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = { Text("New Password (min 8 chars)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPass.length >= 8) {
                            feedbackMsg = "Password updated successfully."
                            showChangePasswordDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NewtonNavy)
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = NewtonNavy, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NewtonTextDark)
                Text(subtitle, fontSize = 11.sp, color = NewtonTextMuted)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingsActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = NewtonTealPrimary, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NewtonTextDark)
                Text(subtitle, fontSize = 11.sp, color = NewtonTextMuted)
            }
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = NewtonTextMuted, modifier = Modifier.size(20.dp))
    }
}
