package com.example.nhsentranet.ui.screens

import android.view.ViewGroup
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import android.net.http.SslError
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Api
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.nhsentranet.network.SwaggerApiClient
import com.example.nhsentranet.network.SwaggerStatus
import com.example.nhsentranet.network.UserSession
import kotlinx.coroutines.launch

val NewtonHeaderBg = Color(0xFFCDEBF2)
val NewtonTealPrimary = Color(0xFF0D6E7E)
val NewtonTealBorder = Color(0xFF389BA8)
val NewtonAlertBg = Color(0xFFE5F4F7)
val NewtonDemoBoxBg = Color(0xFFF0F9FB)
val NewtonDemoBorder = Color(0xFF8CD4DF)
val NewtonTextDark = Color(0xFF1E293B)
val NewtonTextMuted = Color(0xFF64748B)
val NewtonFooterBg = Color(0xFFF8FAFC)

@Composable
fun LoginScreen(
    sessionExpired: Boolean = false,
    onLoginSuccess: (UserSession) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Swagger API Status State
    var swaggerStatus by remember { mutableStateOf<SwaggerStatus?>(null) }
    var showSwaggerExplorer by remember { mutableStateOf(false) }

    // Automatically ping /swagger/v1/swagger.json when Login screen opens
    LaunchedEffect(Unit) {
        swaggerStatus = SwaggerApiClient.verifySwaggerConnection()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE6F2F5))
            .verticalScroll(rememberScrollState())
            .padding(vertical = 24.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {

                // =========================================================
                // 1. HEADER BANNER
                // =========================================================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NewtonHeaderBg)
                        .padding(top = 28.dp, bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier
                            .size(76.dp)
                            .border(2.dp, Color(0xFF0A233A), RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF0D6E7E)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text("NEWTON HOUSE", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("RISE UP", fontSize = 6.sp, color = Color.White)
                            Icon(
                                imageVector = Icons.Outlined.School,
                                contentDescription = "Newton House Crest",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                            Text("SCHOOL", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "STUDENT INTRANET",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        color = NewtonTealPrimary
                    )
                }

                HorizontalDivider(color = Color(0xFFB8E0E8), thickness = 1.dp)

                // =========================================================
                // 2. MAIN LOGIN BODY
                // =========================================================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    Text(
                        text = "Welcome back",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = NewtonTextDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Login in to the Newton House Intranet.",
                        fontSize = 13.sp,
                        color = NewtonTextMuted
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Session Expired Alert (Only shown when actual session expiration occurs)
                    if (sessionExpired) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(NewtonAlertBg)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(72.dp)
                                    .background(NewtonTealPrimary)
                            )
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Schedule,
                                    contentDescription = null,
                                    tint = NewtonTealPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Your session has expired",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = NewtonTealPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "For your security you were logged out after 30 minutes of inactivity. Please sign in again.",
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        color = NewtonTealPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // Username Field
                    Text(
                        text = "Username",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = NewtonTextDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it; errorMessage = null },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NewtonTealPrimary,
                            unfocusedBorderColor = NewtonTealBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Field
                    Text(
                        text = "Password",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = NewtonTextDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMessage = null },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            Text(
                                text = if (passwordVisible) "Hide" else "Show",
                                color = NewtonTealPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier
                                    .clickable { passwordVisible = !passwordVisible }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NewtonTealPrimary,
                            unfocusedBorderColor = NewtonTealBorder
                        )
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = errorMessage!!,
                            color = Color(0xFFDC2626),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Sign In Button (Calls Swagger Login Endpoint)
                    OutlinedButton(
                        onClick = {
                            if (username.isBlank() || password.isBlank()) {
                                errorMessage = "Please enter both your username and password."
                                return@OutlinedButton
                            }

                            isLoading = true
                            errorMessage = null

                            coroutineScope.launch {
                                val result = SwaggerApiClient.login(username, password)
                                isLoading = false

                                result.onSuccess { session ->
                                    onLoginSuccess(session)
                                }.onFailure { err ->
                                    errorMessage = err.message
                                }
                            }
                        },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.5.dp, NewtonTealPrimary),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = NewtonTealPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = NewtonTealPrimary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = "Sign in",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = NewtonTealPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Demo Accounts Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                drawRoundRect(
                                    color = NewtonDemoBorder,
                                    style = Stroke(
                                        width = 1.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                    ),
                                    cornerRadius = CornerRadius(8.dp.toPx())
                                )
                            }
                            .background(NewtonDemoBoxBg, RoundedCornerShape(8.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                            ) {
                                Text(
                                    text = "Demo accounts:",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = NewtonTealPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            DemoRow("Admin:", "admin", "Admin@123") { u, p -> username = u; password = p }
                            Spacer(modifier = Modifier.height(6.dp))
                            DemoRow("Teacher:", "jsmith", "Teacher@123") { u, p -> username = u; password = p }
                            Spacer(modifier = Modifier.height(6.dp))
                            DemoRow("Student:", "john.johnson", "Student@123") { u, p -> username = u; password = p }
                            Spacer(modifier = Modifier.height(6.dp))
                            DemoRow("Parent:", "sjohnson", "Parent@123") { u, p -> username = u; password = p }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // =====================================================
                    // LIVE SWAGGER API CONNECTION BAR
                    // =====================================================
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFF1F5F9))
                            .clickable { showSwaggerExplorer = true }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (swaggerStatus?.isOnline == true) Color(0xFF10B981) else Color(0xFFF59E0B)
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (swaggerStatus?.isOnline == true)
                                    "Swagger API Connected (${swaggerStatus!!.endpointCount} routes)"
                                else
                                    "Swagger API: Connecting to ${SwaggerApiClient.BASE_URL}...",
                                fontSize = 11.sp,
                                color = NewtonTextMuted
                            )
                        }
                        Icon(
                            imageVector = Icons.Outlined.Api,
                            contentDescription = "Open Swagger UI",
                            tint = NewtonTealPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // 3. FOOTER
                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NewtonFooterBg)
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Forgotten your password? Please contact the school office.",
                        fontSize = 11.sp,
                        color = NewtonTextMuted
                    )
                }
            }
        }
    }

    // Modal to view the live Website Swagger UI inside the Android App
    if (showSwaggerExplorer) {
        SwaggerWebViewDialog(onDismiss = { showSwaggerExplorer = false })
    }
}

@Composable
private fun DemoRow(
    label: String,
    user: String,
    pass: String,
    onSelect: (String, String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(user, pass) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = NewtonTealPrimary,
            modifier = Modifier.width(65.dp)
        )
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, Color(0xFFCCE8ED))
        ) {
            Text(
                text = "$user / $pass",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = NewtonTealPrimary,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

/**
 * Renders website's live /swagger/index.html page directly inside the Android App
 * and bypasses localhost SSL warnings on the emulator.
 */
@Composable
fun SwaggerWebViewDialog(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NewtonTealPrimary)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Live Swagger API Explorer",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    TextButton(onClick = onDismiss) {
                        Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        WebView(context).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                allowFileAccess = true
                                allowContentAccess = true
                                mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            }
                            webViewClient = object : WebViewClient() {
                                override fun onReceivedSslError(
                                    view: WebView?,
                                    handler: SslErrorHandler?,
                                    error: SslError?
                                ) {
                                    // Allow localhost self-signed dev certificate in Emulator
                                    handler?.proceed()
                                }
                            }
                            loadUrl(SwaggerApiClient.SWAGGER_UI_URL)
                        }
                    }
                )
            }
        }
    }
}
