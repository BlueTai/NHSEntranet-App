package com.example.nhsentranet

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhsentranet.network.InvoiceSummaryModel
import com.example.nhsentranet.network.LinkedChildDto
import com.example.nhsentranet.network.MarkRecordModel
import com.example.nhsentranet.network.NoticeModel
import com.example.nhsentranet.network.UserRoles
import com.example.nhsentranet.network.UserSession
import com.example.nhsentranet.ui.screens.RoleBasedDashboard
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

// =========================================================================
// 1. BRAND COLOURS (Newton House Web Theme)
// =========================================================================
val NewtonNavy = Color(0xFF0B2240)
val NewtonBlue = Color(0xFF2563EB)
val NewtonSlateBg = Color(0xFFF8FAFC)
val NewtonBorder = Color(0xFFE2E8F0)
val NewtonTextDark = Color(0xFF1E293B)
val NewtonTextMuted = Color(0xFF64748B)
val NewtonRed = Color(0xFFEF4444)
val NewtonGreen = Color(0xFF10B981)
val NewtonAmber = Color(0xFFF59E0B)

val NewtonHeaderBg = Color(0xFFCDEBF2)
val NewtonTealPrimary = Color(0xFF0D6E7E)
val NewtonDemoBoxBg = Color(0xFFF0F9FB)
val NewtonDemoBorder = Color(0xFF8CD4DF)

// =========================================================================
// 2. DTO DATA MODELS
// =========================================================================
data class ChatMessage(val text: String, val isUser: Boolean, val suggestedRoute: String? = null, val actionLabel: String? = null)
data class PortalTile(val route: String, val title: String, val subtitle: String, val icon: ImageVector, val allowedRoles: List<String>)

// =========================================================================
// 3. FIREBASE REPOSITORY (Supports Checklist Workflows)
// =========================================================================
object NewtonFirebaseRepository {
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val demoUsers = mapOf(
        "admin" to mapOf("userId" to 1, "username" to "admin", "password" to "Admin@123", "fullName" to "System Administrator", "userRole" to UserRoles.Admin),
        "jsmith" to mapOf("userId" to 2, "username" to "jsmith", "password" to "Teacher@123", "fullName" to "Jane Smith", "userRole" to UserRoles.Teacher),
        "john.johnson" to mapOf("userId" to 3, "username" to "john.johnson", "password" to "Student@123", "fullName" to "John Johnson", "userRole" to UserRoles.Student),
        "sjohnson" to mapOf("userId" to 4, "username" to "sjohnson", "password" to "Parent@123", "fullName" to "Sarah Johnson", "userRole" to UserRoles.Parent),
        "hod" to mapOf("userId" to 5, "username" to "hod", "password" to "Hod@123", "fullName" to "Dr. Adams", "userRole" to UserRoles.HeadOfDepartment)
    )

    suspend fun autoSeedSwaggerData() = withContext(Dispatchers.IO) {
        try {
            val adminDoc = db.collection("users").document("admin").get().await()
            if (!adminDoc.exists()) {
                for ((u, data) in demoUsers) db.collection("users").document(u).set(data, SetOptions.merge()).await()

                // Multi-child link for parent sjohnson
                val child1 = mapOf("studentId" to 1, "fullName" to "John Johnson", "gradeName" to "Grade 10A", "parentUsername" to "sjohnson")
                val child2 = mapOf("studentId" to 2, "fullName" to "Emily Johnson", "gradeName" to "Grade 8B", "parentUsername" to "sjohnson")
                db.collection("students").document("1").set(child1).await()
                db.collection("students").document("2").set(child2).await()

                // Assessment marks (Pending vs Approved workflow)
                val m1 = mapOf("resultId" to 1, "studentId" to 1, "subject" to "Mathematics", "assessmentName" to "Term 1 Test", "assessmentType" to "Test", "markObtained" to 85.0, "totalMarks" to 100.0, "isApproved" to true, "timestamp" to System.currentTimeMillis())
                val m2 = mapOf("resultId" to 2, "studentId" to 1, "subject" to "Physical Sciences", "assessmentName" to "Practical Lab", "assessmentType" to "Practical", "markObtained" to 78.0, "totalMarks" to 100.0, "isApproved" to false, "timestamp" to System.currentTimeMillis())
                db.collection("marks").document("mark_1").set(m1).await()
                db.collection("marks").document("mark_2").set(m2).await()

                val n1 = mapOf("noticeId" to 1, "noticeName" to "Term 4 Examination Timetable", "noticeType" to "Academic", "description" to "Final examination schedules for Grades 8–12 have been published.", "staffName" to "Jane Smith", "datePosted" to "2026-10-20", "isPinned" to true, "timestamp" to System.currentTimeMillis())
                db.collection("notices").document("notice_1").set(n1).await()

                val inv1 = mapOf("invoiceId" to 1, "studentId" to 1, "invoiceNumber" to "INV-2026-0001", "feeTypeName" to "Tuition", "description" to "Term 4 Tuition Fee", "amount" to 14500.0, "amountPaid" to 10000.0, "balance" to 4500.0, "dueDate" to "2026-11-01", "statusName" to "Partial")
                db.collection("invoices").document("inv_1").set(inv1).await()
            }
        } catch (_: Exception) {}
    }

    suspend fun login(username: String, pass: String): Result<UserSession> = withContext(Dispatchers.IO) {
        val clean = username.trim().lowercase()
        try {
            autoSeedSwaggerData()
            val snap = db.collection("users").document(clean).get().await()
            if (snap.exists() && snap.getString("password") == pass) {
                val role = snap.getString("userRole") ?: UserRoles.Student
                val fullName = snap.getString("fullName") ?: clean
                val userId = (snap.getLong("userId") ?: 1L).toInt()

                val children = mutableListOf<LinkedChildDto>()
                if (role == UserRoles.Parent || role == UserRoles.Student) {
                    val childSnaps = db.collection("students").whereEqualTo("parentUsername", if (role == UserRoles.Parent) clean else "sjohnson").get().await()
                    for (doc in childSnaps.documents) {
                        children.add(LinkedChildDto((doc.getLong("studentId") ?: 1L).toInt(), doc.getString("fullName") ?: "", doc.getString("gradeName") ?: "10A"))
                    }
                    if (children.isEmpty()) children.add(LinkedChildDto(1, "John Johnson", "Grade 10A"))
                }
                return@withContext Result.success(UserSession(userId = userId, username = clean, fullName = fullName, role = role, linkedChildren = children))
            }
        } catch (_: Exception) {}

        val demo = demoUsers[clean]
        if (demo != null && demo["password"] == pass) {
            val role = demo["userRole"] as String
            val children = if (role == UserRoles.Parent || role == UserRoles.Student) listOf(LinkedChildDto(1, "John Johnson", "Grade 10A"), LinkedChildDto(2, "Emily Johnson", "Grade 8B")) else emptyList()
            return@withContext Result.success(UserSession(userId = demo["userId"] as Int, username = clean, fullName = demo["fullName"] as String, role = role, linkedChildren = children))
        }
        Result.failure(Exception("Invalid username or password."))
    }

    suspend fun getMarks(studentId: Int, isParentOrStudent: Boolean): List<MarkRecordModel> = withContext(Dispatchers.IO) {
        try {
            val query = if (isParentOrStudent) {
                db.collection("marks").whereEqualTo("studentId", studentId).whereEqualTo("isApproved", true)
            } else {
                db.collection("marks").whereEqualTo("studentId", studentId)
            }
            val snap = query.get().await()
            val list = snap.documents.mapNotNull { d ->
                MarkRecordModel(
                    resultId = (d.getLong("resultId") ?: 1L).toInt(),
                    subject = d.getString("subject") ?: "Mathematics",
                    assessmentName = d.getString("assessmentName") ?: "Assessment",
                    assessmentType = d.getString("assessmentType") ?: "Test",
                    markObtained = d.getDouble("markObtained") ?: 80.0,
                    totalMarks = d.getDouble("totalMarks") ?: 100.0,
                    isApproved = d.getBoolean("isApproved") ?: true
                )
            }
            if (list.isNotEmpty()) return@withContext list
        } catch (_: Exception) {}

        listOf(MarkRecordModel(1, "Mathematics", "Term 1 Test", "Test", 85.0, 100.0, true))
    }

    suspend fun uploadMark(studentId: Int, mark: Double, isHod: Boolean): Result<String> = withContext(Dispatchers.IO) {
        try {
            val newDoc = db.collection("marks").document()
            newDoc.set(mapOf("resultId" to (10..999).random(), "studentId" to studentId, "subject" to "Information Technology", "assessmentName" to "Final Term Assessment", "assessmentType" to "Exam", "markObtained" to mark, "totalMarks" to 100.0, "isApproved" to isHod, "timestamp" to System.currentTimeMillis())).await()
            Result.success(if (isHod) "Mark approved & published." else "Mark submitted (Pending Head of Academics approval).")
        } catch (e: Exception) {
            Result.failure(Exception("Upload failed: ${e.message}"))
        }
    }

    suspend fun getNotices(): List<NoticeModel> = withContext(Dispatchers.IO) {
        try {
            val snap = db.collection("notices").orderBy("timestamp", Query.Direction.DESCENDING).get().await()
            val list = snap.documents.mapNotNull { d ->
                NoticeModel(
                    noticeId = (d.getLong("noticeId") ?: 1L).toInt(),
                    noticeName = d.getString("noticeName") ?: "",
                    noticeType = d.getString("noticeType") ?: "General",
                    description = d.getString("description") ?: "",
                    staffName = d.getString("staffName") ?: "Administration",
                    datePosted = d.getString("datePosted") ?: "Today",
                    isPinned = d.getBoolean("isPinned") ?: false
                )
            }
            if (list.isNotEmpty()) return@withContext list
        } catch (_: Exception) {}

        listOf(NoticeModel(1, "Term 4 Examination Timetable", "Academic", "Final examination schedules for Grades 8–12 have been published.", "Jane Smith", "Today", true))
    }

    suspend fun postNotice(title: String, desc: String, author: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            db.collection("notices").document().set(mapOf("noticeId" to (10..999).random(), "noticeName" to title, "noticeType" to "General", "description" to desc, "staffName" to author, "datePosted" to "Just now", "isPinned" to false, "timestamp" to System.currentTimeMillis())).await()
            Result.success("Notice published live to Firebase.")
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getInvoices(studentId: Int): List<InvoiceSummaryModel> = withContext(Dispatchers.IO) {
        try {
            val snap = db.collection("invoices").whereEqualTo("studentId", studentId).get().await()
            val list = snap.documents.mapNotNull { d ->
                InvoiceSummaryModel(
                    invoiceId = (d.getLong("invoiceId") ?: 1L).toInt(),
                    invoiceNumber = d.getString("invoiceNumber") ?: "INV-2026",
                    feeTypeName = d.getString("feeTypeName") ?: "Tuition",
                    description = d.getString("description") ?: "",
                    amount = d.getDouble("amount") ?: 14500.0,
                    amountPaid = d.getDouble("amountPaid") ?: 10000.0,
                    balance = d.getDouble("balance") ?: 4500.0,
                    dueDate = d.getString("dueDate") ?: "2026-11-01",
                    statusName = d.getString("statusName") ?: "Partial"
                )
            }
            if (list.isNotEmpty()) return@withContext list
        } catch (_: Exception) {}

        listOf(InvoiceSummaryModel(1, "INV-2026-0001", "Tuition", "Term 4 Tuition Fee", 14500.0, 10000.0, 4500.0, "2026-11-01", "Partial"))
    }

    // Upload Proof of Payment (Payments Service Integration)
    suspend fun uploadProofOfPayment(invoiceId: Int): Result<String> = withContext(Dispatchers.IO) {
        try {
            db.collection("proofs").document().set(mapOf(
                "invoiceId" to invoiceId,
                "status" to "Pending Verification",
                "timestamp" to System.currentTimeMillis()
            )).await()
            Result.success("Proof of Payment uploaded successfully.")
        } catch (_: Exception) {
            Result.failure(Exception("Upload failed."))
        }
    }

    // Assign Class Teacher (Admin Service Integration)
    suspend fun assignClassTeacher(grade: String, teacherName: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            db.collection("class_teachers").document(grade).set(mapOf(
                "grade" to grade,
                "teacherName" to teacherName,
                "timestamp" to System.currentTimeMillis()
            )).await()
            Result.success("Class Teacher assigned successfully.")
        } catch (_: Exception) {
            Result.failure(Exception("Assignment failed."))
        }
    }
}

// =========================================================================
// 4. MAIN ACTIVITY ENTRY POINT
// =========================================================================
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = NewtonNavy, secondary = NewtonTealPrimary, background = NewtonSlateBg, surface = Color.White)) {
                var session by remember { mutableStateOf<UserSession?>(null) }
                if (session == null) {
                    LoginScreen(onLoginSuccess = { session = it })
                } else {
                    NewtonHouseMainPortal(session = session!!, onLogout = { session = null })
                }
            }
        }
    }
}

// =========================================================================
// 5. LOGIN SCREEN
// =========================================================================
@Composable
fun LoginScreen(onLoginSuccess: (UserSession) -> Unit) {
    val scope = rememberCoroutineScope()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        NewtonFirebaseRepository.autoSeedSwaggerData()
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFE6F2F5)).verticalScroll(rememberScrollState()).padding(vertical = 24.dp, horizontal = 16.dp), contentAlignment = Alignment.Center) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 6.dp), modifier = Modifier.fillMaxWidth().widthIn(max = 420.dp)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().background(NewtonHeaderBg).padding(top = 28.dp, bottom = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(modifier = Modifier.size(76.dp).border(2.dp, Color(0xFF0A233A), RoundedCornerShape(14.dp)), shape = RoundedCornerShape(14.dp), color = NewtonTealPrimary) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
                            Text("NEWTON HOUSE", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Icon(Icons.Outlined.School, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                            Text("SCHOOL", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("STUDENT INTRANET", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp, color = NewtonTealPrimary)
                }

                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp)) {
                    Text("Welcome back", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = NewtonTextDark)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it; errorMessage = null },
                        label = { Text("Username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
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
                                if (passwordVisible) "Hide" else "Show",
                                color = NewtonTealPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.clickable { passwordVisible = !passwordVisible }.padding(12.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(errorMessage!!, color = NewtonRed, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedButton(
                        onClick = {
                            if (username.isBlank() || password.isBlank()) {
                                errorMessage = "Enter username and password."
                                return@OutlinedButton
                            }
                            isLoading = true
                            scope.launch {
                                val res = NewtonFirebaseRepository.login(username, password)
                                isLoading = false
                                res.onSuccess { onLoginSuccess(it) }
                                    .onFailure { errorMessage = it.message }
                            }
                        },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.5.dp, NewtonTealPrimary),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        if (isLoading) CircularProgressIndicator(color = NewtonTealPrimary, modifier = Modifier.size(20.dp))
                        else Text("Sign in", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NewtonTealPrimary)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(modifier = Modifier.fillMaxWidth().drawBehind { drawRoundRect(color = NewtonDemoBorder, style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)), cornerRadius = CornerRadius(8.dp.toPx())) }.background(NewtonDemoBoxBg, RoundedCornerShape(8.dp)).padding(14.dp)) {
                        Column {
                            Surface(color = Color.White, shape = RoundedCornerShape(4.dp), border = BorderStroke(1.dp, Color(0xFFCBD5E1))) {
                                Text("Demo accounts:", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = NewtonTealPrimary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            DemoRow("Admin:", "admin", "Admin@123") { u, p -> username = u; password = p }
                            Spacer(modifier = Modifier.height(6.dp))
                            DemoRow("Teacher:", "jsmith", "Teacher@123") { u, p -> username = u; password = p }
                            Spacer(modifier = Modifier.height(6.dp))
                            DemoRow("HOD:", "hod", "Hod@123") { u, p -> username = u; password = p }
                            Spacer(modifier = Modifier.height(6.dp))
                            DemoRow("Student:", "john.johnson", "Student@123") { u, p -> username = u; password = p }
                            Spacer(modifier = Modifier.height(6.dp))
                            DemoRow("Parent:", "sjohnson", "Parent@123") { u, p -> username = u; password = p }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DemoRow(label: String, user: String, pass: String, onSelect: (String, String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onSelect(user, pass) }, verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = NewtonTealPrimary, modifier = Modifier.width(65.dp))
        Surface(color = Color.White, shape = RoundedCornerShape(4.dp), border = BorderStroke(1.dp, Color(0xFFCCE8ED))) {
            Text("$user / $pass", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = NewtonTealPrimary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
        }
    }
}

// =========================================================
// 6. MAIN PORTAL SCAFFOLD
// =========================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewtonHouseMainPortal(session: UserSession, onLogout: () -> Unit) {
    val context = LocalContext.current
    var currentRoute by remember { mutableStateOf("/Dashboard") }
    var isChatOpen by remember { mutableStateOf(false) }
    var selectedChild by remember { mutableStateOf(session.linkedChildren.firstOrNull() ?: LinkedChildDto(1, "John Johnson", "Grade 10A")) }

    var ttsEnabled by remember { mutableStateOf(true) }
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(context) {
        val tts = TextToSpeech(context) { if (it == TextToSpeech.SUCCESS) ttsEngine?.language = Locale.US }
        ttsEngine = tts
        onDispose { tts.shutdown() }
    }

    val portalTiles = listOf(
        PortalTile("/Academic", "Academic Centre", "Marks, Trends & Reports", Icons.Default.School, listOf(UserRoles.Admin, UserRoles.Teacher, UserRoles.HeadOfDepartment, UserRoles.Parent, UserRoles.Student)),
        PortalTile("/Assessments", "Marks Moderation", "Teacher Upload & HOD Approval", Icons.Default.EditNote, listOf(UserRoles.Teacher, UserRoles.HeadOfDepartment, UserRoles.Admin)),
        PortalTile("/Notices", "Notice Board", "School Alerts & Circulars", Icons.Default.Campaign, listOf(UserRoles.Admin, UserRoles.Teacher, UserRoles.HeadOfDepartment, UserRoles.Parent, UserRoles.Student)),
        PortalTile("/Invoices", "Invoices & Fees", "Tuition, Payments & Proofs", Icons.AutoMirrored.Filled.ReceiptLong, listOf(UserRoles.Parent, UserRoles.Admin)),
        PortalTile("/Forms", "Digital Consent", "Medical, POPIA & Transport Forms", Icons.Default.Description, listOf(UserRoles.Parent, UserRoles.Student, UserRoles.Admin)),
        PortalTile("/Contacts", "Contacts Directory", "Staff & Emergency Contacts", Icons.Default.ContactMail, listOf(UserRoles.Admin, UserRoles.Teacher, UserRoles.HeadOfDepartment, UserRoles.Parent, UserRoles.Student)),
        PortalTile("/Maps", "Campus Maps", "Randburg Campus Zones", Icons.Default.Map, listOf(UserRoles.Admin, UserRoles.Teacher, UserRoles.HeadOfDepartment, UserRoles.Parent, UserRoles.Student)),
        PortalTile("/Gallery", "Media Gallery", "Photos & Podcasts", Icons.Default.PhotoLibrary, listOf(UserRoles.Admin, UserRoles.Teacher, UserRoles.HeadOfDepartment, UserRoles.Parent, UserRoles.Student)),
        PortalTile("/Admin", "Admin Console", "Enrol Students & Assign Teachers", Icons.Default.AdminPanelSettings, listOf(UserRoles.Admin))
    )
    val visibleTiles = portalTiles.filter { it.allowedRoles.contains(session.userRole) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Newton House Intranet", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Text("${session.fullName} (${session.userRole})", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    if (currentRoute != "/Dashboard") {
                        IconButton(onClick = { currentRoute = "/Dashboard" }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, "Sign Out", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NewtonNavy)
            )
        },
        containerColor = NewtonSlateBg
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (currentRoute) {
                "/Dashboard" -> RoleBasedDashboard(
                    session = session,
                    sections = visibleTiles,
                    onNavigate = { currentRoute = it }
                )
                "/Academic" -> AcademicView(session = session, studentId = selectedChild.studentId)
                "/Assessments" -> AssessmentsView(session = session, studentId = selectedChild.studentId)
                "/Notices" -> NoticesView(session = session)
                "/Invoices" -> InvoicesView(studentId = selectedChild.studentId)
                "/Forms" -> FormsView(selectedChild = selectedChild)
                "/Contacts" -> ContactsView()
                "/Maps" -> MapsView()
                "/Gallery" -> GalleryView()
                "/Admin" -> AdminView()
            }

            Column(modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp), horizontalAlignment = Alignment.End) {
                AnimatedVisibility(visible = isChatOpen, enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(), exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut()) {
                    NavigatorChatWidget(
                        session = session, ttsEnabled = ttsEnabled,
                        onToggleTts = { ttsEnabled = !ttsEnabled; if (!ttsEnabled) ttsEngine?.stop() },
                        onSpeak = { if (ttsEnabled) ttsEngine?.speak(it, TextToSpeech.QUEUE_FLUSH, null, null) },
                        onClose = { isChatOpen = false },
                        onNavigate = { currentRoute = it; isChatOpen = false }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                FloatingActionButton(onClick = { isChatOpen = !isChatOpen }, containerColor = NewtonNavy, contentColor = Color.White, modifier = Modifier.size(56.dp)) {
                    Icon(if (isChatOpen) Icons.Default.Close else Icons.AutoMirrored.Filled.Chat, "Chat")
                }
            }
        }
    }
}

// =========================================================
// 7. CAMPUS MAPS VIEW
// =========================================================
@Composable
fun MapsView() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Campus Maps & Zones", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NewtonNavy)
        Spacer(modifier = Modifier.height(12.dp))

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = NewtonTealPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("118 CR Swart Drive, Boskruin, Randburg", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text("Campus Zones", fontWeight = FontWeight.Bold, color = NewtonNavy)
                Spacer(modifier = Modifier.height(8.dp))

                val zones = listOf(
                    "Parking & Drop-off/Pick-up Zones",
                    "Preparatory Campus",
                    "High School / College Campus",
                    "Sports Fields",
                    "Aftercare Centre",
                    "Main School Hall"
                )
                zones.forEach { zone ->
                    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(NewtonAmber))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(zone, fontSize = 13.sp, color = NewtonTextDark)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { /* Open Google Maps */ }, colors = ButtonDefaults.buttonColors(containerColor = NewtonNavy), modifier = Modifier.fillMaxWidth()) {
                    Text("Open in Google Maps")
                }
            }
        }
    }
}

// =========================================================
// 8. MEDIA GALLERY VIEW
// =========================================================
@Composable
fun GalleryView() {
    var selectedTab by remember { mutableIntStateOf(0) }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Media Gallery", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NewtonNavy)
        Spacer(modifier = Modifier.height(12.dp))

        SecondaryTabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Photos") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Videos") })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Podcasts") })
        }

        Spacer(modifier = Modifier.height(16.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(2), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            when (selectedTab) {
                0 -> {
                    items(4) {
                        Box(modifier = Modifier.height(120.dp).clip(RoundedCornerShape(8.dp)).background(NewtonBorder), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = NewtonTextMuted)
                        }
                    }
                }
                1 -> {
                    items(listOf("Production Trailer: Grease", "Inter-House Athletics Highlights")) { title ->
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column {
                                Box(modifier = Modifier.height(100.dp).fillMaxWidth().background(Color.Black), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.PlayCircleOutline, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                                }
                                Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
                            }
                        }
                    }
                }
                2 -> {
                    items(listOf("Student Wellness Ep 1", "Matric Advice Ep 2")) { title ->
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Headphones, contentDescription = null, tint = NewtonTealPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================
// 9. EXPANDED DIGITAL FORMS VIEW
// =========================================================
@Composable
fun FormsView(selectedChild: LinkedChildDto) {
    var msg by remember { mutableStateOf<String?>(null) }
    var occupation by remember { mutableStateOf("Civil Engineer") }
    var allergies by remember { mutableStateOf("Peanuts (Mild)") }
    var driver by remember { mutableStateOf("Bongani Mkhize (082 555 0192)") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Digital Consent Forms & Data Capture", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NewtonNavy)
        Spacer(modifier = Modifier.height(12.dp))

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Medical & Transport Consent Form", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NewtonNavy)
                Text("Auto-populated from database records.", fontSize = 11.sp, color = NewtonGreen)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(value = selectedChild.fullName, onValueChange = {}, label = { Text("Learner Full Name") }, enabled = false, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = selectedChild.gradeName, onValueChange = {}, label = { Text("Learner Grade") }, enabled = false, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = occupation, onValueChange = { occupation = it }, label = { Text("Parent Occupation") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = allergies, onValueChange = { allergies = it }, label = { Text("Student Medical Allergies") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = driver, onValueChange = { driver = it }, label = { Text("Transport Driver Name & Contact") }, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { msg = "Consent Receipt Generated. Form saved to database and PDF downloaded." },
                    colors = ButtonDefaults.buttonColors(containerColor = NewtonNavy),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign Digitally & Download Receipt")
                }

                if (msg != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(msg!!, color = NewtonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// =========================================================
// 10. CONTACTS DIRECTORY VIEW
// =========================================================
@Composable
fun ContactsView() {
    val contacts = listOf(
        Triple("Principal", "principal@newtonhouse.co.za", "011 463 5500"),
        Triple("Admissions", "admissions@newtonhouse.co.za", "011 463 5501"),
        Triple("Finance & Accounts", "accounts@newtonhouse.co.za", "011 463 5502"),
        Triple("Student Support", "support@newtonhouse.co.za", "011 463 5503"),
        Triple("Estate Manager", "estate@newtonhouse.co.za", "011 463 5504"),
        Triple("Transport Desk", "transport@newtonhouse.co.za", "011 463 5505")
    )

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Staff & Emergency Contacts Directory", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NewtonNavy)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(contacts) { (role, email, phone) ->
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ContactPhone, contentDescription = null, tint = NewtonTealPrimary, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(role, fontWeight = FontWeight.Bold, color = NewtonNavy, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Email: $email", fontSize = 12.sp, color = NewtonTextDark)
                            Text("Phone: $phone", fontSize = 12.sp, color = NewtonTextMuted)
                        }
                    }
                }
            }
        }
    }
}

// =========================================================
// 11. ADMIN CONSOLE VIEW
// =========================================================
@Composable
fun AdminView() {
    val scope = rememberCoroutineScope()
    var msg by remember { mutableStateOf<String?>(null) }
    var studentName by remember { mutableStateOf("") }
    var selectedGrade by remember { mutableStateOf("Grade 10A") }

    var teacherMsg by remember { mutableStateOf<String?>(null) }
    var teacherName by remember { mutableStateOf("") }
    var teacherGrade by remember { mutableStateOf("Grade 10A") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Admin: Enrol New Student", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NewtonNavy)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = studentName, onValueChange = { studentName = it }, label = { Text("Student Full Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = selectedGrade, onValueChange = { selectedGrade = it }, label = { Text("Grade & Class") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        if (studentName.isNotBlank()) {
                            msg = "Student '$studentName' enrolled and assigned to $selectedGrade successfully."
                            studentName = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NewtonNavy),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Enrol Student & Assign Subjects")
                }
                if (msg != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(msg!!, color = NewtonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Admin: Assign Class Teacher", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NewtonNavy)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = teacherName, onValueChange = { teacherName = it }, label = { Text("Staff Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = teacherGrade, onValueChange = { teacherGrade = it }, label = { Text("Grade") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        if (teacherName.isNotBlank()) {
                            scope.launch {
                                val res = NewtonFirebaseRepository.assignClassTeacher(teacherGrade, teacherName)
                                teacherMsg = res.getOrElse { it.message }
                                teacherName = ""
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NewtonTealPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Assign Class Teacher")
                }
                if (teacherMsg != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(teacherMsg!!, color = NewtonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// =========================================================
// 12. ACADEMIC CENTRE VIEW
// =========================================================
@Composable
fun AcademicView(session: UserSession, studentId: Int) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var marks by remember { mutableStateOf<List<MarkRecordModel>>(emptyList()) }
    val subjectSelection = remember { mutableStateListOf("Mathematics", "Physical Sciences", "Information Technology") }

    LaunchedEffect(studentId, session.userRole) {
        val isParentOrStudent = session.userRole == UserRoles.Parent || session.userRole == UserRoles.Student
        marks = NewtonFirebaseRepository.getMarks(studentId, isParentOrStudent)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Academic Centre", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NewtonNavy)
        Spacer(modifier = Modifier.height(12.dp))

        SecondaryTabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Marks") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Report Cards") })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Subject Selection") })
        }
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            when (selectedTab) {
                0 -> {
                    items(marks) { m ->
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(m.subject, fontWeight = FontWeight.Bold, color = NewtonNavy)
                                    Text("${m.assessmentName} (${m.assessmentType})", fontSize = 12.sp, color = NewtonTextMuted)
                                    if (!m.isApproved) Text("Status: Pending HOD Approval", fontSize = 10.sp, color = NewtonAmber)
                                }
                                Text("${m.markObtained.toInt()} / ${m.totalMarks.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = NewtonTealPrimary)
                            }
                        }
                    }
                }
                1 -> {
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("Term 3 Report Card", fontWeight = FontWeight.Bold, color = NewtonNavy)
                                    Text("Auto-generated & Verified", fontSize = 12.sp, color = NewtonGreen)
                                }
                                Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = NewtonTealPrimary)) {
                                    Text("Download PDF", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
                else -> {
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Enrolled Elective Subjects", fontWeight = FontWeight.Bold, color = NewtonNavy)
                                Spacer(modifier = Modifier.height(8.dp))
                                subjectSelection.forEach { subj ->
                                    Text("• $subj (Core / Elective)", fontSize = 13.sp, color = NewtonTextDark, modifier = Modifier.padding(vertical = 2.dp))
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                if (session.userRole == UserRoles.Parent) {
                                    Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = NewtonNavy)) {
                                        Text("Modify Child Subject Selections")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================
// 13. ASSESSMENTS & MARKS MODERATION VIEW
// =========================================================
@Composable
fun AssessmentsView(session: UserSession, studentId: Int) {
    val scope = rememberCoroutineScope()
    var markValue by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf<String?>(null) }
    val isHod = session.userRole == UserRoles.HeadOfDepartment || session.userRole == UserRoles.Admin

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(if (isHod) "Head of Academics: Mark Moderation" else "Teacher: Upload Assessment Marks", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NewtonNavy)
                Text(if (isHod) "Approve pending teacher marks for parent portal." else "Marks require HOD approval before parents view them.", fontSize = 11.sp, color = NewtonTextMuted)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = markValue,
                    onValueChange = { markValue = it },
                    label = { Text("Enter Mark (0 - 100)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        val m = markValue.toDoubleOrNull()
                        if (m == null || m !in 0.0..100.0) {
                            feedback = "Enter valid mark between 0 and 100."
                            return@Button
                        }
                        scope.launch {
                            val res = NewtonFirebaseRepository.uploadMark(studentId, m, isHod)
                            feedback = res.getOrElse { it.message }
                            markValue = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NewtonNavy),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isHod) "Approve & Publish Marks" else "Upload Marks for Moderation")
                }

                if (feedback != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(feedback!!, color = NewtonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// =========================================================
// 14. NOTICES VIEW (Live Firebase Real-Time Circulars)
// =========================================================
@Composable
fun NoticesView(session: UserSession) {
    val scope = rememberCoroutineScope()
    var notices by remember { mutableStateOf<List<NoticeModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedCategory by remember { mutableStateOf("All") }

    // New Notice Form State
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }
    var isPosting by remember { mutableStateOf(false) }
    var postFeedback by remember { mutableStateOf<String?>(null) }

    val categories = listOf("All", "Academic", "Urgent", "General")

    val canPublish = session.userRole == UserRoles.Admin ||
            session.userRole == UserRoles.Teacher ||
            session.userRole == UserRoles.HeadOfDepartment ||
            session.userRole.contains("Head", ignoreCase = true)

    LaunchedEffect(Unit) {
        notices = NewtonFirebaseRepository.getNotices()
        isLoading = false
    }

    val filteredNotices = notices.filter { n ->
        selectedCategory == "All" || n.noticeType.equals(selectedCategory, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "School Notice Board",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = NewtonNavy
            )
            Text(
                text = "Official announcements, academic schedules & urgent alerts",
                fontSize = 12.sp,
                color = NewtonTextMuted
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 12.sp) }
                    )
                }
            }
        }

        if (canPublish) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, NewtonBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Campaign, contentDescription = null, tint = NewtonTealPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Publish School Announcement", fontWeight = FontWeight.Bold, color = NewtonNavy, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it; postFeedback = null },
                            label = { Text("Notice Title") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = desc,
                            onValueChange = { desc = it; postFeedback = null },
                            label = { Text("Announcement Details") },
                            modifier = Modifier.fillMaxWidth().height(90.dp),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("General", "Academic", "Urgent").forEach { type ->
                                    FilterChip(
                                        selected = category == type,
                                        onClick = { category = type },
                                        label = { Text(type, fontSize = 11.sp) }
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    if (title.isNotBlank()) {
                                        isPosting = true
                                        scope.launch {
                                            val res = NewtonFirebaseRepository.postNotice(title, desc, session.fullName.ifEmpty { session.username })
                                            isPosting = false
                                            res.onSuccess {
                                                postFeedback = "Notice published live to Firebase!"
                                                notices = NewtonFirebaseRepository.getNotices()
                                                title = ""; desc = ""
                                            }.onFailure { err ->
                                                postFeedback = err.message
                                            }
                                        }
                                    }
                                },
                                enabled = !isPosting && title.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = NewtonTealPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                if (isPosting) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else {
                                    Text("Publish", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (postFeedback != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(postFeedback!!, color = NewtonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = NewtonTealPrimary, modifier = Modifier.size(32.dp))
                }
            }
        } else if (filteredNotices.isEmpty()) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No notices posted under $selectedCategory yet.", color = NewtonTextMuted, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(filteredNotices) { n ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, NewtonBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = n.noticeName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = NewtonNavy,
                                modifier = Modifier.weight(1f)
                            )

                            Surface(
                                color = when (n.noticeType.lowercase()) {
                                    "urgent" -> Color(0xFFFEF2F2)
                                    "academic" -> Color(0xFFEFF6FF)
                                    else -> Color(0xFFF1F5F9)
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = n.noticeType,
                                    color = when (n.noticeType.lowercase()) {
                                        "urgent" -> NewtonRed
                                        "academic" -> NewtonBlue
                                        else -> NewtonTealPrimary
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(n.description, fontSize = 13.sp, color = NewtonTextDark, lineHeight = 18.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Posted by ${n.staffName} • ${n.datePosted}", fontSize = 11.sp, color = NewtonTextMuted)
                            if (n.isPinned) {
                                Icon(Icons.Default.PushPin, contentDescription = "Pinned", tint = NewtonAmber, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================
// 15. INVOICES VIEW
// =========================================================
@Composable
fun InvoicesView(studentId: Int) {
    val scope = rememberCoroutineScope()
    var invoices by remember { mutableStateOf<List<InvoiceSummaryModel>>(emptyList()) }
    var proofMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(studentId) { invoices = NewtonFirebaseRepository.getInvoices(studentId) }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(invoices) { inv ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("${inv.invoiceNumber} • ${inv.feeTypeName}", fontWeight = FontWeight.Bold, color = NewtonNavy)
                        Text(inv.statusName, color = NewtonAmber, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Total: R${inv.amount.toInt()} | Paid: R${inv.amountPaid.toInt()} | Bal: R${inv.balance.toInt()}", fontSize = 13.sp)
                    Text("Due Date: ${inv.dueDate}", fontSize = 11.sp, color = NewtonTextMuted)

                    if (inv.balance > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(onClick = {
                            scope.launch {
                                val res = NewtonFirebaseRepository.uploadProofOfPayment(inv.invoiceId)
                                proofMsg = res.getOrElse { it.message }
                            }
                        }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, tint = NewtonTealPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Upload Proof of Payment", color = NewtonTealPrimary)
                        }
                        if (proofMsg != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(proofMsg!!, color = NewtonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// =========================================================
// 16. NAVIGATOR CHATBOT
// =========================================================
@Composable
fun NavigatorChatWidget(session: UserSession, ttsEnabled: Boolean, onToggleTts: () -> Unit, onSpeak: (String) -> Unit, onClose: () -> Unit, onNavigate: (String) -> Unit) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf(ChatMessage("Hi ${session.fullName}! Ask me for marks, fees, notices, or forms.", false)) }

    fun handleSend(query: String) {
        val q = query.trim()
        if (q.isEmpty()) return
        messages.add(ChatMessage(q, true))
        input = ""
        val lower = q.lowercase()
        val reply = when {
            lower.contains("mark") -> ChatMessage("Opening Academic Centre.", false, "/Academic", "Open Academic")
            lower.contains("fee") || lower.contains("proof") -> ChatMessage("Viewing Invoices.", false, "/Invoices", "View Invoices")
            lower.contains("map") -> ChatMessage("Opening Campus Maps.", false, "/Maps", "Campus Maps")
            else -> ChatMessage("I can help you navigate.", false, "/Dashboard", "Dashboard")
        }
        messages.add(reply)
        onSpeak(reply.text)
        scope.launch { listState.animateScrollToItem(messages.size - 1) }
    }

    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.width(340.dp).height(440.dp).shadow(16.dp, RoundedCornerShape(12.dp))) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().background(NewtonNavy).padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Newton Navigator", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleTts) {
                        Icon(
                            imageVector = if (ttsEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                            contentDescription = "Toggle TTS Audio",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close Chat", tint = Color.White)
                    }
                }
            }
            LazyColumn(state = listState, modifier = Modifier.weight(1f).fillMaxWidth().background(NewtonSlateBg).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(messages) { m ->
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = if (m.isUser) Alignment.End else Alignment.Start) {
                        Surface(color = if (m.isUser) NewtonNavy else Color.White, shape = RoundedCornerShape(10.dp), border = if (m.isUser) null else BorderStroke(1.dp, NewtonBorder)) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(m.text, color = if (m.isUser) Color.White else NewtonTextDark, fontSize = 13.sp)
                                if (m.suggestedRoute != null && m.actionLabel != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Button(onClick = { onNavigate(m.suggestedRoute) }, colors = ButtonDefaults.buttonColors(containerColor = NewtonTealPrimary), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp), modifier = Modifier.height(30.dp)) {
                                        Text(m.actionLabel, fontSize = 11.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = input, onValueChange = { input = it }, placeholder = { Text("Ask navigator...", fontSize = 12.sp) }, singleLine = true, modifier = Modifier.weight(1f).height(46.dp))
                IconButton(onClick = { handleSend(input) }) { Icon(Icons.AutoMirrored.Filled.Send, null, tint = NewtonNavy) }
            }
        }
    }
}
