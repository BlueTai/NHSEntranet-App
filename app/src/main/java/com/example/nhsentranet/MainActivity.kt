package com.example.nhsentranet

import com.example.nhsentranet.ui.screens.RoleBasedDashboard
import com.example.nhsentranet.ui.screens.AcademicWorkflowScreen
import com.example.nhsentranet.ui.screens.FormsScreen

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.Locale
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

// =========================================================
// 1. EXACT NEWTON HOUSE COLOUR PALETTE
// =========================================================
val NewtonNavy = Color(0xFF0B2240)
val NewtonBlue = Color(0xFF2563EB)
val NewtonSlateBg = Color(0xFFF8FAFC)
val NewtonBorder = Color(0xFFE2E8F0)
val NewtonChipBg = Color(0xFFE2E8F0)
val NewtonTextDark = Color(0xFF1E293B)
val NewtonTextMuted = Color(0xFF64748B)
val NewtonRedMic = Color(0xFFEF4444)
val NewtonAmberFocus = Color(0xFFF59E0B)

// =========================================================
// 2. API CONFIGURATION & DATA MODELS
// =========================================================
object ApiConfig {
    // 10.0.2.2 points the Android Emulator to your PC's localhost.
    // Match the port to your NewtonHouse backend or ApiBaseUrl (7322).
    const val BACKEND_BASE_URL = "https://10.0.2.2:7322"
    const val GEMINI_FALLBACK_KEY = "YOUR_GEMINI_API_KEY_HERE"
}

data class UserSession(
    val username: String,
    val role: String, // "Student", "Parent", or "Administrator"
    val token: String? = null
)

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val suggestedRoute: String? = null,
    val actionLabel: String? = null
)

data class PortalSection(
    val route: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val allowedRoles: List<String> = listOf("Student", "Parent", "Teacher", "Head of Academics", "Admin", "Administrator"),
    val adminOnly: Boolean = false
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable persistent cookies (Matches ASP.NET CookieForwardHandler)
        CookieHandler.setDefault(CookieManager(null, CookiePolicy.ACCEPT_ALL))
        // Allow local self-signed ASP.NET Core dev certificates (https://localhost:7322)
        trustAllLocalCerts()

        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = NewtonNavy,
                    secondary = NewtonBlue,
                    background = NewtonSlateBg,
                    surface = Color.White
                )
            ) {
                NewtonHouseRoot()
            }
        }
    }
}

// =========================================================
// 3. AUTHENTICATION STATE ROUTER
// =========================================================
@Composable
fun NewtonHouseRoot() {
    var currentSession by remember { mutableStateOf<UserSession?>(null) }

    if (currentSession == null) {
        LoginScreen(
            onLoginSuccess = { session ->
                currentSession = session
            }
        )
    } else {
        NewtonHousePortal(
            session = currentSession!!,
            onLogout = { currentSession = null }
        )
    }
}

// =========================================================
// 4. LOGIN SCREEN (Connects to ASP.NET API)
// =========================================================
@Composable
fun LoginScreen(onLoginSuccess: (UserSession) -> Unit) {
    val coroutineScope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf("Student") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val roles = listOf("Student", "Parent", "Teacher", "Head of Academics", "Admin", "Administrator")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NewtonNavy)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(20.dp, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // School Crest / Icon Header
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(NewtonNavy),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Newton House Crest",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Newton House",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = NewtonNavy
                )
                Text(
                    text = "Student & Parent Intranet Portal",
                    fontSize = 13.sp,
                    color = NewtonTextMuted
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Role Selector Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    roles.forEach { role ->
                        val isSelected = selectedRole == role
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedRole = role },
                            label = { Text(role, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NewtonNavy,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it; errorMessage = null },
                    label = { Text("Username or Email") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = NewtonNavy) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NewtonBlue)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = NewtonNavy) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle password visibility"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NewtonBlue)
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = NewtonRedMic,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (username.isBlank() || password.isBlank()) {
                            errorMessage = "Please enter both your username and password."
                            return@Button
                        }
                        isLoading = true
                        coroutineScope.launch {
                            val result = authenticateWithApi(username.trim(), password, selectedRole)
                            isLoading = false
                            if (result != null) {
                                onLoginSuccess(result)
                            } else {
                                errorMessage = "Invalid credentials. Please try again."
                            }
                        }
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = NewtonNavy),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text("Sign In to Portal", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// =========================================================
// 5. MAIN PORTAL SCAFFOLD (Post-Login)
// =========================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewtonHousePortal(session: UserSession, onLogout: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var currentRoute by remember { mutableStateOf("/Dashboard") }
    var isChatOpen by remember { mutableStateOf(false) }

    // Text-to-Speech Setup
    var ttsEnabled by remember { mutableStateOf(true) }
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(context) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsEngine?.language = Locale.US
            }
        }
        ttsEngine = tts
        onDispose { tts.shutdown() }
    }

    fun speakText(text: String) {
        if (!ttsEnabled) return
        val cleanText = text.replace(Regex("[*_#`]"), "")
        ttsEngine?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    val allSections = listOf(
        PortalSection("/Academic", "Academic Centre", "Marks, Assessments & Reports", Icons.Default.School, listOf("Student", "Parent", "Teacher", "Head of Academics", "Admin", "Administrator")),
        PortalSection("/Notices", "Notice Board", "Events, Circulars & Alerts", Icons.Default.Campaign, listOf("Student", "Parent", "Teacher", "Head of Academics", "Admin", "Administrator")),
        PortalSection("/Forms", "Digital Forms", "Consent, Medical & POPIA", Icons.Default.Description, listOf("Student", "Parent", "Teacher", "Admin", "Administrator")),
        PortalSection("/Maps", "Campus Maps", "Parking & Drop-off Zones", Icons.Default.Map, listOf("Student", "Parent", "Teacher", "Head of Academics", "Admin", "Administrator")),
        PortalSection("/Contacts", "Contacts Directory", "Teacher Emails & Support", Icons.Default.ContactMail, listOf("Student", "Parent", "Teacher", "Head of Academics", "Admin", "Administrator")),
        PortalSection("/Gallery", "Media Gallery", "Photos, Videos & Podcasts", Icons.Default.PhotoLibrary, listOf("Student", "Parent", "Teacher", "Head of Academics", "Admin", "Administrator")),
        PortalSection("/Admin", "Admin Console", "Manage Users, Notices & Forms", Icons.Default.AdminPanelSettings, listOf("Head of Academics", "Admin", "Administrator"))
    )

    val visibleSections = allSections.filter { section -> section.allowedRoles.any { role -> role.equals(session.role, ignoreCase = true) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Newton House",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "${session.role} Portal • ${session.username}",
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    if (currentRoute != "/Dashboard") {
                        IconButton(onClick = { currentRoute = "/Dashboard" }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { currentRoute = "/Notices" }) {
                        BadgedBox(badge = { Badge(containerColor = NewtonAmberFocus) { Text("3") } }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
                        }
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sign Out", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NewtonNavy)
            )
        },
        containerColor = NewtonSlateBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentRoute) {
                "/Dashboard" -> RoleBasedDashboard(
                    session = session,
                    sections = allSections,
                    onNavigate = { route -> currentRoute = route }
                )
                else -> SectionDetailScreen(
                    route = currentRoute,
                    sections = visibleSections,
                    session = session,
                    onBack = { currentRoute = "/Dashboard" }
                )
            }

            // Floating Chatbot Window & Launcher
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                AnimatedVisibility(
                    visible = isChatOpen,
                    enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut()
                ) {
                    ChatbotWindow(
                        ttsEnabled = ttsEnabled,
                        onToggleTts = {
                            ttsEnabled = !ttsEnabled
                            if (!ttsEnabled) ttsEngine?.stop()
                        },
                        onClose = { isChatOpen = false },
                        onNavigate = { route ->
                            currentRoute = route
                            isChatOpen = false
                        },
                        onSendMessage = { query, onResult ->
                            coroutineScope.launch {
                                val response = queryBackendOrGemini(query, session)
                                onResult(response)
                                speakText(response.text)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                FloatingActionButton(
                    onClick = { isChatOpen = !isChatOpen },
                    containerColor = NewtonNavy,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = if (isChatOpen) Icons.Default.Close else Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "Open Intranet Navigator Chatbot"
                    )
                }
            }
        }
    }
}

// =========================================================
// 6. DASHBOARD & DETAIL SCREENS
// =========================================================
@Composable
fun SectionDetailScreen(route: String, sections: List<PortalSection>, session: UserSession, onBack: () -> Unit) {
    val section = sections.find { it.route == route }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NewtonBorder, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = section?.icon ?: Icons.Default.Info,
                        contentDescription = null,
                        tint = NewtonNavy,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = section?.title ?: route,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = NewtonTextDark
                        )
                        Text(
                            text = section?.subtitle ?: "",
                            fontSize = 13.sp,
                            color = NewtonTextMuted
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = NewtonBorder)

                when (route) {
                    "/Academic" -> {
                        AcademicWorkflowScreen(role = session.role)
                    }
                    "/Forms" -> {
                        FormsScreen(isAdmin = session.role.equals("Admin", ignoreCase = true) || session.role.equals("Administrator", ignoreCase = true))
                    }
                    "/Notices" -> {
                        Text("Latest Circulars & Alerts", fontWeight = FontWeight.Bold, color = NewtonNavy)
                        Spacer(modifier = Modifier.height(8.dp))
                        NoticeRow("Winter Uniform Transition", "Effective from next Monday.")
                        NoticeRow("Parent-Teacher Consultations", "Bookings open on the portal.")
                    }
                    "/Admin" -> {
                        Text("Administrator Responsibilities", fontWeight = FontWeight.Bold, color = NewtonNavy)
                        Spacer(modifier = Modifier.height(8.dp))
                        NoticeRow("Publish Urgent Push Alert", "Broadcasts to all student & parent devices.")
                        NoticeRow("Review Digital Consent Forms", "14 POPIA & excursion forms pending approval.")
                        NoticeRow("Manage User Accounts", "Provision student, parent, and staff roles.")
                    }
                    else -> {
                        Text(
                            text = "Connected to ${ApiConfig.BACKEND_BASE_URL}$route",
                            color = NewtonTextDark,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = NewtonNavy),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Return to Dashboard")
                }
            }
        }
    }
}

@Composable
fun NoticeRow(title: String, desc: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(NewtonSlateBg, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(title, color = NewtonTextDark, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text(desc, color = NewtonTextMuted, fontSize = 13.sp)
    }
}

// =========================================================
// 7. CHATBOT WIDGET UI
// =========================================================
@Composable
fun ChatbotWindow(
    ttsEnabled: Boolean,
    onToggleTts: () -> Unit,
    onClose: () -> Unit,
    onNavigate: (String) -> Unit,
    onSendMessage: (String, (ChatMessage) -> Unit) -> Unit
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var inputText by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                text = "Hi! I can help you navigate to marks, notices, forms, maps, and staff contacts.",
                isUser = false
            )
        )
    }

    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    val speechIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
    }

    fun triggerSubmit(queryText: String) {
        val clean = queryText.trim()
        if (clean.isEmpty()) return
        messages.add(ChatMessage(text = clean, isUser = true))
        inputText = ""
        isSearching = true
        coroutineScope.launch { listState.animateScrollToItem(messages.size - 1) }

        onSendMessage(clean) { botReply ->
            isSearching = false
            messages.add(botReply)
            coroutineScope.launch { listState.animateScrollToItem(messages.size - 1) }
        }
    }

    DisposableEffect(Unit) {
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { isRecording = true }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { isRecording = false }
            override fun onError(error: Int) { isRecording = false }
            override fun onResults(results: Bundle?) {
                isRecording = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    triggerSubmit(matches[0])
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        onDispose { speechRecognizer.destroy() }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            speechRecognizer.startListening(speechIntent)
        }
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .width(350.dp)
            .height(480.dp)
            .shadow(16.dp, RoundedCornerShape(12.dp))
            .border(1.dp, NewtonBorder, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NewtonNavy)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Newton Navigator", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onToggleTts, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = if (ttsEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                            contentDescription = "Toggle Voice Output",
                            tint = if (ttsEnabled) Color.White else Color.White.copy(alpha = 0.45f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFFCBD5E1))
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(NewtonSlateBg)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { msg ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (msg.isUser) Alignment.End else Alignment.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .widthIn(max = 280.dp)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 12.dp,
                                        topEnd = 12.dp,
                                        bottomStart = if (msg.isUser) 12.dp else 4.dp,
                                        bottomEnd = if (msg.isUser) 4.dp else 12.dp
                                    )
                                )
                                .background(if (msg.isUser) NewtonNavy else Color.White)
                                .border(
                                    width = if (msg.isUser) 0.dp else 1.dp,
                                    color = if (msg.isUser) Color.Transparent else NewtonBorder,
                                    shape = RoundedCornerShape(
                                        topStart = 12.dp,
                                        topEnd = 12.dp,
                                        bottomStart = if (msg.isUser) 12.dp else 4.dp,
                                        bottomEnd = if (msg.isUser) 4.dp else 12.dp
                                    )
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Column {
                                Text(
                                    text = msg.text,
                                    color = if (msg.isUser) Color.White else NewtonTextDark,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                )

                                if (!msg.isUser && msg == messages.first()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        QuickChip("Report Cards") { triggerSubmit("Where are my marks?") }
                                        QuickChip("Notices") { triggerSubmit("School notices") }
                                        QuickChip("Forms") { triggerSubmit("Consent forms") }
                                        QuickChip("Maps") { triggerSubmit("Campus map") }
                                    }
                                }

                                if (msg.suggestedRoute != null && msg.actionLabel != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { onNavigate(msg.suggestedRoute) },
                                        colors = ButtonDefaults.buttonColors(containerColor = NewtonBlue),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text(msg.actionLabel, color = Color.White, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                if (isSearching) {
                    item {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp, 12.dp, 12.dp, 4.dp))
                                .background(Color.White)
                                .border(1.dp, NewtonBorder, RoundedCornerShape(12.dp, 12.dp, 12.dp, 4.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text("Searching sections...", color = NewtonTextMuted, fontSize = 14.sp)
                        }
                    }
                }
            }

            HorizontalDivider(color = NewtonBorder)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Use voice input",
                        tint = if (isRecording) NewtonRedMic else NewtonTextMuted
                    )
                }

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(if (isRecording) "Listening..." else "Ask or speak...", fontSize = 13.sp, color = NewtonTextMuted)
                    },
                    singleLine = true,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NewtonBlue,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { triggerSubmit(inputText) },
                    colors = ButtonDefaults.buttonColors(containerColor = NewtonNavy),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.height(42.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun QuickChip(label: String, onClick: () -> Unit) {
    Surface(
        color = NewtonChipBg,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            color = NewtonNavy,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

// =========================================================
// 8. ASP.NET CORE API INTEGRATION (Login + Chatbot)
// =========================================================

/**
 * Calls ASP.NET Core Login API (`https://10.0.2.2:7322/api/auth/login`).
 * Stores the authentication cookie via CookieManager for future RAG requests.
 * If the local server isn't running, falls back cleanly to offline session mode.
 */
suspend fun authenticateWithApi(username: String, password: String, selectedRole: String): UserSession? = withContext(Dispatchers.IO) {
    try {
        val url = URL("${ApiConfig.BACKEND_BASE_URL}/api/auth/login")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            doOutput = true
            connectTimeout = 3000
            readTimeout = 3000
        }

        val payload = JSONObject().apply {
            put("username", username)
            put("password", password)
            put("role", selectedRole)
        }

        conn.outputStream.use { it.write(payload.toString().toByteArray()) }

        if (conn.responseCode in 200..299) {
            val responseStr = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(responseStr)
            val token = json.optString("token").takeIf { it.isNotEmpty() }
            return@withContext UserSession(
                username = json.optString("username", username),
                role = json.optString("role", selectedRole),
                token = token
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    // Offline / Demo Fallback Mode
    if (username.isNotBlank() && password.isNotBlank()) {
        return@withContext UserSession(
            username = username,
            role = selectedRole
        )
    }

    return@withContext null
}

suspend fun queryBackendOrGemini(userMessage: String, session: UserSession): ChatMessage = withContext(Dispatchers.IO) {
    val lower = userMessage.lowercase()

    // 1. Local / Demo Offline RAG Fallback
    if (ApiConfig.GEMINI_FALLBACK_KEY.isBlank() || ApiConfig.GEMINI_FALLBACK_KEY.startsWith("YOUR_")) {
        return@withContext when {
            lower.contains("mark") || lower.contains("grade") || lower.contains("report") || lower.contains("academic") || lower.contains("math") ->
                ChatMessage(
                    text = "Your recent Mathematics mark is 85/100 and Physical Sciences is 78/100. View full reports in the Academic Centre.",
                    isUser = false,
                    suggestedRoute = "/Academic",
                    actionLabel = "Go to Academic Centre"
                )
            lower.contains("notice") || lower.contains("event") || lower.contains("alert") || lower.contains("meeting") ->
                ChatMessage(
                    text = "Notices, events, and school circulars are published on the Notice Board.",
                    isUser = false,
                    suggestedRoute = "/Notices",
                    actionLabel = "Notice Board"
                )
            lower.contains("form") || lower.contains("consent") || lower.contains("sign") || lower.contains("medical") || lower.contains("popia") ->
                ChatMessage(
                    text = "Consent, indemnity, excursion, and POPIA forms are available to complete online.",
                    isUser = false,
                    suggestedRoute = "/Forms",
                    actionLabel = "Digital Forms"
                )
            lower.contains("map") || lower.contains("parking") || lower.contains("campus") || lower.contains("drop-off") ->
                ChatMessage(
                    text = "View parking lots, drop-off areas, and facilities on our campus maps.",
                    isUser = false,
                    suggestedRoute = "/Maps",
                    actionLabel = "Campus Maps"
                )
            lower.contains("contact") || lower.contains("teacher") || lower.contains("email") || lower.contains("staff") ->
                ChatMessage(
                    text = "Reach out to teachers and student support via the Contacts directory.",
                    isUser = false,
                    suggestedRoute = "/Contacts",
                    actionLabel = "Contacts Directory"
                )
            lower.contains("gallery") || lower.contains("photo") || lower.contains("video") || lower.contains("podcast") ->
                ChatMessage(
                    text = "Photos, match highlights, and podcasts are stored in the Media Gallery.",
                    isUser = false,
                    suggestedRoute = "/Gallery",
                    actionLabel = "Media Gallery"
                )
            lower.contains("admin") ->
                if (session.role == "Administrator") {
                    ChatMessage(
                        text = "As an Administrator, you can manage push notifications, consent forms, and user accounts in the Admin Console.",
                        isUser = false,
                        suggestedRoute = "/Admin",
                        actionLabel = "Admin Console"
                    )
                } else {
                    ChatMessage(
                        text = "Admin features are restricted to Administrator accounts.",
                        isUser = false,
                        suggestedRoute = "/Dashboard",
                        actionLabel = "Dashboard"
                    )
                }
            else ->
                ChatMessage(
                    text = "I can help you navigate across the Newton House portal.",
                    isUser = false,
                    suggestedRoute = "/Dashboard",
                    actionLabel = "Dashboard"
                )
        }
    }

    // 2. Live Gemini 1.5 Flash Request with RAG Session Context
    try {
        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=${ApiConfig.GEMINI_FALLBACK_KEY}")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            doOutput = true
        }

        val systemPrompt = """
            You are the Newton House Student Intranet Navigator assistant.
            --- CURRENT USER SESSION ---
            Username: ${session.username}, Role: ${session.role}.
            Student Marks: Mathematics: 85/100, Physical Sciences: 78/100, English: 82/100.
            ---------------------------
            Routes: /Dashboard, /Academic, /Notices, /Forms, /Maps, /Contacts, /Gallery${if (session.role == "Administrator") ", /Admin" else ""}.
            Respond ONLY in valid JSON matching this schema:
            {"reply": "<helpful answer under 2 sentences>", "suggestedRoute": "<route or null>", "actionLabel": "<button label or null>"}
        """.trimIndent()

        val payload = JSONObject().apply {
            put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemPrompt))))
            put("contents", JSONArray().put(JSONObject().put("parts", JSONArray().put(JSONObject().put("text", userMessage)))))
            put("generationConfig", JSONObject().put("responseMimeType", "application/json").put("temperature", 0.2))
        }

        conn.outputStream.use { it.write(payload.toString().toByteArray()) }

        if (conn.responseCode == HttpURLConnection.HTTP_OK) {
            val responseStr = conn.inputStream.bufferedReader().use { it.readText() }
            val textJson = JSONObject(responseStr)
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")

            val parsed = JSONObject(textJson)
            return@withContext ChatMessage(
                text = parsed.optString("reply", "Here is what I found:"),
                isUser = false,
                suggestedRoute = parsed.optString("suggestedRoute").takeIf { it.isNotEmpty() && it != "null" },
                actionLabel = parsed.optString("actionLabel").takeIf { it.isNotEmpty() && it != "null" }
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return@withContext ChatMessage(
        text = "Navigation assistant is currently offline. Please use the main dashboard tiles.",
        isUser = false,
        suggestedRoute = "/Dashboard",
        actionLabel = "Dashboard"
    )
}

@android.annotation.SuppressLint("TrustAllX509TrustManager", "CustomX509TrustManager")
fun trustAllLocalCerts() {
    try {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) {}
        })
        val sc = SSLContext.getInstance("SSL")
        sc.init(null, trustAllCerts, SecureRandom())
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
        HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
