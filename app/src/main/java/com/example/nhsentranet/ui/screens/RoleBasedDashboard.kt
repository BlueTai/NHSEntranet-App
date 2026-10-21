package com.example.nhsentranet.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhsentranet.PortalTile
import com.example.nhsentranet.network.LinkedChildDto
import com.example.nhsentranet.network.UserSession

data class RoleSectionView(
    val route: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val badge: String? = null
)

@Composable
fun RoleBasedDashboard(
    session: UserSession,
    sections: List<PortalTile>,
    onNavigate: (String) -> Unit
) {
    val role = session.role
    val isParent = role.equals("Parent", ignoreCase = true)
    val isTeacher = role.equals("Teacher", ignoreCase = true)
    val isHead = role.equals("Head of Academics", ignoreCase = true) || role.equals("HeadOfDepartment", ignoreCase = true)
    val isAdmin = role.equals("Admin", ignoreCase = true) || role.equals("Administrator", ignoreCase = true)
    val isStudent = role.equals("Student", ignoreCase = true)

    // Parent Child-Switcher State
    var selectedChild by remember { mutableStateOf(session.linkedChildren.firstOrNull() ?: LinkedChildDto(1, "John Johnson", "Grade 10A")) }
    var showChildDropdown by remember { mutableStateOf(false) }

    val primaryNavy = Color(0xFF0B2240)
    val secondaryTeal = Color(0xFF0D6E7E)
    val lightBg = Color(0xFFF8FAFC)
    val cardBorderColor = Color(0xFFE2E8F0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // -----------------------------------------------------
        // 1. GRADIENT WELCOME BANNER
        // -----------------------------------------------------
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(primaryNavy, secondaryTeal)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.AccountCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Welcome, ${session.fullName.ifEmpty { session.username }}",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = when {
                                    isStudent -> "Student Portal • Grade 10A"
                                    isParent -> "Parent Portal • Linked Enrolments"
                                    isTeacher -> "Teacher Portal • Mathematics Dept"
                                    isHead -> "Academic Leadership Console"
                                    isAdmin -> "Administrator Control Center"
                                    else -> "Role: $role"
                                },
                                color = Color(0xFFBAE6FD),
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Role Badge
                    Surface(
                        color = when {
                            isAdmin -> Color(0xFFEF4444)
                            isHead -> Color(0xFF8B5CF6)
                            isTeacher -> Color(0xFF10B981)
                            isParent -> Color(0xFF2563EB)
                            else -> Color(0xFFF59E0B)
                        },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = role,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Parent Multi-Child Switcher
                if (isParent && session.linkedChildren.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Box {
                        OutlinedButton(
                            onClick = { showChildDropdown = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFBAE6FD)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Viewing: ${selectedChild.fullName} (${selectedChild.gradeName})", fontSize = 12.sp)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = showChildDropdown,
                            onDismissRequest = { showChildDropdown = false }
                        ) {
                            session.linkedChildren.forEach { child ->
                                DropdownMenuItem(
                                    text = { Text("${child.fullName} (${child.gradeName})") },
                                    onClick = {
                                        selectedChild = child
                                        showChildDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // -----------------------------------------------------
        // 2. DETAILED 4-KPI SUMMARY ROW
        // -----------------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KpiSummaryCard(
                modifier = Modifier.weight(1f),
                title = if (isParent || isStudent) "Average" else "Sessions",
                value = if (isParent || isStudent) "81.6%" else "5 Today",
                subtext = "Term 1",
                icon = Icons.Default.Star,
                accentColor = Color(0xFFF59E0B)
            )
            KpiSummaryCard(
                modifier = Modifier.weight(1f),
                title = "Attendance",
                value = "96.4%",
                subtext = "48/50 Days",
                icon = Icons.Default.CheckCircle,
                accentColor = Color(0xFF10B981)
            )
            KpiSummaryCard(
                modifier = Modifier.weight(1f),
                title = "Notices",
                value = "3 Alerts",
                subtext = "1 Urgent",
                icon = Icons.Default.Campaign,
                accentColor = Color(0xFF2563EB)
            )
            KpiSummaryCard(
                modifier = Modifier.weight(1f),
                title = if (isParent) "Fees Balance" else if (isTeacher) "Pending Marks" else if (isHead) "Moderation" else "Approvals",
                value = if (isParent) "R4,500" else if (isTeacher) "3 Tests" else if (isHead) "1 Batch" else "14 Forms",
                subtext = "Action Needed",
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                accentColor = Color(0xFFEF4444)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // -----------------------------------------------------
        // 3. ACADEMIC GRADE BREAKDOWN WIDGET
        // -----------------------------------------------------
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, cardBorderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.School, contentDescription = null, tint = secondaryTeal, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isParent) "${selectedChild.fullName}'s Performance Overview" else "Academic Performance Overview",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF1E293B)
                        )
                    }

                    Text(
                        text = "View Details",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2563EB),
                        modifier = Modifier.clickable { onNavigate("/Academic") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                SubjectGradeRow("Mathematics", "85%", 0.85f, Color(0xFF10B981))
                Spacer(modifier = Modifier.height(8.dp))
                SubjectGradeRow("Physical Sciences", "78%", 0.78f, Color(0xFF2563EB))
                Spacer(modifier = Modifier.height(8.dp))
                SubjectGradeRow("Information Technology", "88%", 0.88f, Color(0xFF0D6E7E))
                Spacer(modifier = Modifier.height(8.dp))
                SubjectGradeRow("English Home Language", "82%", 0.82f, Color(0xFFF59E0B))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // -----------------------------------------------------
        // 4. UPCOMING EVENTS & TIMETABLE WIDGET
        // -----------------------------------------------------
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, cardBorderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Schedule, contentDescription = null, tint = primaryNavy, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Upcoming Schedule & Key Dates", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                    }
                    Text(
                        text = "Notices",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2563EB),
                        modifier = Modifier.clickable { onNavigate("/Notices") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                EventScheduleRow("Term 4 Final Examination Timetable", "2026-10-20 • High School Hall", "Academic")
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF1F5F9))
                EventScheduleRow("POPIA & Medical Consent Forms Due", "2026-10-28 • Digital Submission", "Urgent")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // -----------------------------------------------------
        // 5. WORKSPACE SECTIONS GRID
        // -----------------------------------------------------
        Text(
            text = "Your Workspace Sections",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color(0xFF1E293B)
        )
        Spacer(modifier = Modifier.height(10.dp))

        val visibleSections = sections.filter { section ->
            section.allowedRoles.any { r -> r.equals(role, ignoreCase = true) }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            visibleSections.chunked(2).forEach { rowTiles ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowTiles.forEach { section ->
                        val view = getRoleSectionView(section, role)
                        Box(modifier = Modifier.weight(1f)) {
                            DashboardTile(view) { onNavigate(section.route) }
                        }
                    }
                    if (rowTiles.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun KpiSummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtext: String,
    icon: ImageVector,
    accentColor: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(title, fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtext, fontSize = 9.sp, color = Color(0xFF94A3B8))
        }
    }
}

@Composable
fun SubjectGradeRow(subject: String, percentageText: String, progressFraction: Float, progressColor: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(subject, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
            Text(percentageText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = progressColor)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progressFraction },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = progressColor,
            trackColor = Color(0xFFF1F5F9)
        )
    }
}

@Composable
fun EventScheduleRow(title: String, details: String, tag: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(2.dp))
            Text(details, fontSize = 11.sp, color = Color(0xFF64748B))
        }
        Surface(
            color = if (tag == "Urgent") Color(0xFFFEF2F2) else Color(0xFFEFF6FF),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = tag,
                color = if (tag == "Urgent") Color(0xFFEF4444) else Color(0xFF2563EB),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

fun getRoleSectionView(section: PortalTile, role: String): RoleSectionView {
    val isParent = role.equals("Parent", ignoreCase = true)
    val isTeacher = role.equals("Teacher", ignoreCase = true)
    val isHead = role.equals("Head of Academics", ignoreCase = true) || role.equals("HeadOfDepartment", ignoreCase = true)
    val isAdmin = role.equals("Admin", ignoreCase = true) || role.equals("Administrator", ignoreCase = true)
    val isStudent = role.equals("Student", ignoreCase = true)

    return when (section.route) {
        "/Academic" -> when {
            isTeacher -> RoleSectionView(section.route, "Capture Assessment Marks", "Input & submit test scores for moderation", section.icon, "Action Needed")
            isHead -> RoleSectionView(section.route, "Approve & Moderate Marks", "1 batch pending approval (Grade 10 Maths)", section.icon, "1 Pending")
            isParent -> RoleSectionView(section.route, "Child Academic Reports", "View term marks & download PDF report card", section.icon, "Term 1")
            isStudent -> RoleSectionView(section.route, "My Marks & Grades", "View assessment scores and subject averages", section.icon, "Avg: 81.6%")
            else -> RoleSectionView(section.route, "Academic Operations", "Overview of school marks, assessments & reports", section.icon)
        }
        "/Forms" -> when {
            isParent -> RoleSectionView(section.route, "Medical & Consent Forms", "Complete POPIA, medical updates & excursion consent", section.icon, "2 Required")
            isTeacher -> RoleSectionView(section.route, "Class & Trip Forms", "Manage excursion permission slips and class rosters", section.icon)
            isAdmin || isHead -> RoleSectionView(section.route, "Digital Forms & Google Forms", "Manage form templates, POPIA compliance & embeds", section.icon, "Admin")
            else -> RoleSectionView(section.route, section.title, section.subtitle, section.icon)
        }
        "/Admin" -> when {
            isHead -> RoleSectionView(section.route, "Academic Moderation Console", "Review subject performance and grade boundaries", section.icon, "Moderation")
            else -> RoleSectionView(section.route, "Admin Console", "User provisioning, system logs & notifications", section.icon, "System")
        }
        "/Notices" -> when {
            isAdmin || isHead -> RoleSectionView(section.route, "Notice & Push Broadcast", "Publish circulars and send instant push alerts", section.icon, "Publish")
            else -> RoleSectionView(section.route, "Notice Board", "School events, circulars, and official alerts", section.icon, "3 New")
        }
        "/Contacts" -> when {
            isStudent || isParent -> RoleSectionView(section.route, "Teacher Directory", "Email subject teachers & support staff", section.icon)
            else -> RoleSectionView(section.route, "Staff Directory", "Contact details for colleagues and admin", section.icon)
        }
        else -> RoleSectionView(section.route, section.title, section.subtitle, section.icon)
    }
}

@Composable
fun DashboardTile(view: RoleSectionView, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color(0xFFE5F4F7), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = view.icon, contentDescription = null, tint = Color(0xFF0D6E7E), modifier = Modifier.size(22.dp))
                }

                if (view.badge != null) {
                    Surface(
                        color = Color(0xFFEFF6FF),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = view.badge,
                            color = Color(0xFF2563EB),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(view.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(3.dp))
            Text(view.subtitle, fontSize = 11.sp, color = Color(0xFF64748B), lineHeight = 14.sp)
        }
    }
}
