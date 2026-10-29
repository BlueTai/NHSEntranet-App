package com.example.nhsentranet

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.nhsentranet.model.LinkedChildDto
import com.example.nhsentranet.model.PortalTile
import com.example.nhsentranet.model.UserRoles
import com.example.nhsentranet.ui.components.NavigatorChatWidget
import com.example.nhsentranet.ui.screens.*
import com.example.nhsentranet.ui.theme.NewtonHouseTheme
import com.example.nhsentranet.ui.theme.NewtonNavy
import com.example.nhsentranet.ui.theme.NewtonSlateBg
import com.example.nhsentranet.ui.viewmodels.*
import java.util.Locale

object Routes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val ACADEMIC = "academic"
    const val ASSESSMENTS = "assessments"
    const val NOTICES = "notices"
    const val INVOICES = "invoices"
    const val FORMS = "forms"
    const val TRANSPORT = "transport"
    const val WELLNESS = "wellness"
    const val CONTACTS = "contacts"
    const val MAPS = "maps"
    const val GALLERY = "gallery"
    const val ADMIN = "admin"
    const val SETTINGS = "settings"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NewtonHouseTheme {
                val authViewModel: AuthViewModel = viewModel()
                val session by authViewModel.session.collectAsState()
                var isRegistering by remember { mutableStateOf(false) }

                if (session == null) {
                    if (isRegistering) {
                        RegisterScreen(
                            onBackToLogin = { isRegistering = false },
                            onRegisterSuccess = { isRegistering = false }
                        )
                    } else {
                        LoginScreen(
                            authViewModel = authViewModel,
                            onLoginSuccess = {},
                            onNavigateToRegister = { isRegistering = true }
                        )
                    }
                } else {
                    NewtonHouseAppScaffold(
                        session = session!!,
                        onLogout = { authViewModel.logout() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewtonHouseAppScaffold(
    session: com.example.nhsentranet.model.UserSession,
    onLogout: () -> Unit,
    dashboardViewModel: DashboardViewModel = viewModel(),
    academicViewModel: AcademicViewModel = viewModel(),
    noticesViewModel: NoticesViewModel = viewModel(),
    invoicesViewModel: InvoicesViewModel = viewModel()
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Routes.DASHBOARD

    var isChatOpen by remember { mutableStateOf(false) }

    // Initialize selected child for parents
    val initialChild = session.linkedChildren.firstOrNull() ?: LinkedChildDto(1, "John Johnson", "Grade 10A")
    LaunchedEffect(session) {
        dashboardViewModel.initChild(initialChild)
    }
    val selectedChild by dashboardViewModel.selectedChild.collectAsState()
    val activeChild = selectedChild ?: initialChild

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

    val portalTiles = listOf(
        PortalTile("academic", stringResource(R.string.academic_centre), "Marks, Trends & Reports", Icons.Default.School, listOf(UserRoles.Admin, UserRoles.Teacher, UserRoles.HeadOfDepartment, UserRoles.Parent, UserRoles.Student)),
        PortalTile("assessments", stringResource(R.string.marks_moderation), "Teacher Upload & HOD Approval", Icons.Default.EditNote, listOf(UserRoles.Teacher, UserRoles.HeadOfDepartment, UserRoles.Admin)),
        PortalTile("notices", stringResource(R.string.notice_board), "School Alerts & Circulars", Icons.Default.Campaign, listOf(UserRoles.Admin, UserRoles.Teacher, UserRoles.HeadOfDepartment, UserRoles.Parent, UserRoles.Student)),
        PortalTile("invoices", stringResource(R.string.invoices_and_fees), "Tuition, Payments & Proofs", Icons.AutoMirrored.Filled.ReceiptLong, listOf(UserRoles.Parent, UserRoles.Admin)),
        PortalTile("forms", stringResource(R.string.digital_consent), "Medical, POPIA & Transport Forms", Icons.Default.Description, listOf(UserRoles.Parent, UserRoles.Student, UserRoles.Admin)),
        PortalTile("transport", "Bus Routes", "Live Transport Status", Icons.Default.DirectionsBus, listOf(UserRoles.Parent, UserRoles.Student, UserRoles.Admin)),
        PortalTile("wellness", "Wellness Center", "Student Support & Incidents", Icons.Default.HealthAndSafety, listOf(UserRoles.Admin, UserRoles.HeadOfDepartment, UserRoles.Teacher)),
        PortalTile("contacts", stringResource(R.string.contacts_directory), "Staff & Emergency Contacts", Icons.Default.ContactMail, listOf(UserRoles.Admin, UserRoles.Teacher, UserRoles.HeadOfDepartment, UserRoles.Parent, UserRoles.Student)),
        PortalTile("maps", stringResource(R.string.campus_maps), "Randburg Campus Zones", Icons.Default.Map, listOf(UserRoles.Admin, UserRoles.Teacher, UserRoles.HeadOfDepartment, UserRoles.Parent, UserRoles.Student)),
        PortalTile("gallery", stringResource(R.string.media_gallery), "Photos & Podcasts", Icons.Default.PhotoLibrary, listOf(UserRoles.Admin, UserRoles.Teacher, UserRoles.HeadOfDepartment, UserRoles.Parent, UserRoles.Student)),
        PortalTile("admin", stringResource(R.string.admin_console), "Enrol Students & Assign Teachers", Icons.Default.AdminPanelSettings, listOf(UserRoles.Admin))
    )
    val visibleTiles = portalTiles.filter { it.allowedRoles.contains(session.role) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.newton_house_logo),
                            contentDescription = "Newton House Logo",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(stringResource(R.string.school_title), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("${session.fullName} (${session.role})", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                        }
                    }
                },
                navigationIcon = {
                    if (currentRoute != Routes.DASHBOARD) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), tint = Color.White)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.SETTINGS) }) {
                        Icon(Icons.Default.Settings, "Settings", tint = Color.White)
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, stringResource(R.string.sign_out), tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NewtonNavy)
            )
        },
        containerColor = NewtonSlateBg
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Routes.DASHBOARD,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Routes.DASHBOARD) {
                    RoleBasedDashboardScreen(
                        session = session,
                        selectedChild = activeChild,
                        onSelectChild = { dashboardViewModel.selectChild(it) },
                        sections = visibleTiles,
                        onNavigate = { route -> navController.navigate(route) }
                    )
                }
                composable(Routes.ACADEMIC) {
                    AcademicScreen(
                        session = session,
                        studentId = activeChild.studentId,
                        academicViewModel = academicViewModel
                    )
                }
                composable(Routes.ASSESSMENTS) {
                    AssessmentsScreen(
                        session = session,
                        studentId = activeChild.studentId
                    )
                }
                composable(Routes.NOTICES) {
                    NoticesScreen(
                        session = session,
                        noticesViewModel = noticesViewModel
                    )
                }
                composable(Routes.INVOICES) {
                    InvoicesScreen(
                        studentId = activeChild.studentId,
                        invoicesViewModel = invoicesViewModel
                    )
                }
                composable(Routes.FORMS) {
                    FormsScreen(
                        isAdmin = session.role == UserRoles.Admin,
                        studentName = "${activeChild.fullName} (${activeChild.gradeName})"
                    )
                }
                composable(Routes.TRANSPORT) {
                    TransportScreen()
                }
                composable(Routes.WELLNESS) {
                    WellnessScreen(session = session)
                }
                composable(Routes.CONTACTS) {
                    ContactsScreen()
                }
                composable(Routes.MAPS) {
                    MapsScreen()
                }
                composable(Routes.GALLERY) {
                    GalleryScreen()
                }
                composable(Routes.ADMIN) {
                    AdminScreen()
                }
                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        session = session,
                        ttsEnabled = ttsEnabled,
                        onToggleTts = {
                            ttsEnabled = !ttsEnabled
                            if (!ttsEnabled) ttsEngine?.stop()
                        },
                        onLogout = onLogout
                    )
                }
            }

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
                    NavigatorChatWidget(
                        session = session,
                        ttsEnabled = ttsEnabled,
                        onToggleTts = {
                            ttsEnabled = !ttsEnabled
                            if (!ttsEnabled) ttsEngine?.stop()
                        },
                        onSpeak = {
                            if (ttsEnabled) ttsEngine?.speak(it, TextToSpeech.QUEUE_FLUSH, null, null)
                        },
                        onClose = { isChatOpen = false },
                        onNavigate = { route ->
                            navController.navigate(route)
                            isChatOpen = false
                        }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                FloatingActionButton(
                    onClick = { isChatOpen = !isChatOpen },
                    containerColor = NewtonNavy,
                    contentColor = Color.White,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(if (isChatOpen) Icons.Default.Close else Icons.AutoMirrored.Filled.Chat, "Chat")
                }
            }
        }
    }
}
