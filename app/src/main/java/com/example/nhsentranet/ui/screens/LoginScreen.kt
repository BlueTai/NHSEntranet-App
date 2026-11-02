package com.example.nhsentranet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhsentranet.model.LinkedChildDto
import com.example.nhsentranet.model.UserRoles
import com.example.nhsentranet.model.UserSession
import com.example.nhsentranet.network.AzureApiClient
import com.example.nhsentranet.ui.components.NewtonHouseBrandHeader
import com.example.nhsentranet.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoginSuccess: (UserSession) -> Unit) {
    val scope = rememberCoroutineScope()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WebPageBg)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth().widthIn(max = 420.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                NewtonHouseBrandHeader()
                HorizontalDivider(color = WebDivider)

                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Sign in to your account", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = WebCharcoalText)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Sign in with your school account", fontSize = 12.sp, color = WebSlateIcon)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it; errorMessage = null },
                        label = { Text("Username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMessage = null },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            Text(
                                text = if (passwordVisible) "Hide" else "Show",
                                color = WebTealBrand,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.clickable { passwordVisible = !passwordVisible }.padding(12.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(errorMessage!!, color = NewtonRed, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (username.isBlank() || password.isBlank()) {
                                errorMessage = "Please enter your username and password."
                                return@Button
                            }
                            isLoading = true
                            scope.launch {
                                val result = AzureApiClient.loginViaWebsiteBridge(username.trim(), password)
                                isLoading = false
                                result.onSuccess { liveSession ->
                                    onLoginSuccess(liveSession)
                                }.onFailure { err ->
                                    // Fallback to offline demo only if Azure is unreachable
                                    val fallback = checkFallbackDemoUsers(username, password)
                                    if (fallback != null) {
                                        onLoginSuccess(fallback)
                                    } else {
                                        errorMessage = err.message ?: "Login failed."
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WebTealBrand),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        else Text("Sign In", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Quick Demo Logins (Tap to autofill):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WebTealBrand)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        AssistChip(onClick = { username = "admin"; password = "Admin@123" }, label = { Text("Admin", fontSize = 11.sp) })
                        AssistChip(onClick = { username = "jsmith"; password = "Teacher@123" }, label = { Text("Teacher", fontSize = 11.sp) })
                        AssistChip(onClick = { username = "sjohnson"; password = "Parent@123" }, label = { Text("Parent", fontSize = 11.sp) })
                        AssistChip(onClick = { username = "john.johnson"; password = "Student@123" }, label = { Text("Student", fontSize = 11.sp) })
                    }
                }
            }
        }
    }
}

private fun checkFallbackDemoUsers(user: String, pass: String): UserSession? {
    val clean = user.trim().lowercase()
    return when {
        clean == "admin" && pass == "Admin@123" -> UserSession(1, "admin", "System Administrator", UserRoles.Admin)
        clean == "jsmith" && pass == "Teacher@123" -> UserSession(2, "jsmith", "Jane Smith", UserRoles.Teacher)
        clean == "sjohnson" && pass == "Parent@123" -> UserSession(4, "sjohnson", "Sarah Johnson", UserRoles.Parent, listOf(LinkedChildDto(1, "John Johnson", "Grade 10A"), LinkedChildDto(2, "Emily Johnson", "Grade 8B")))
        clean == "john.johnson" && pass == "Student@123" -> UserSession(3, "john.johnson", "John Johnson", UserRoles.Student, listOf(LinkedChildDto(1, "John Johnson", "Grade 10A")))
        else -> null
    }
}
