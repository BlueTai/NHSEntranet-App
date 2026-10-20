package com.example.nhsentranet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhsentranet.PortalSection
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
    sections: List<PortalSection>,
    onNavigate: (String) -> Unit
) {
    val role = session.role
    val isParent = role.equals("Parent", ignoreCase = true)
    val isTeacher = role.equals("Teacher", ignoreCase = true)
    val isHead = role.equals("Head of Academics", ignoreCase = true)
    val isAdmin = role.equals("Admin", ignoreCase = true) || role.equals("Administrator", ignoreCase = true)
    val isStudent = role.equals("Student", ignoreCase = true)

    // Parent Child-Switcher State
    var selectedChild by remember { mutableStateOf("John Doe (Grade 10)") }
    var showChildDropdown by remember { mutableStateOf(false) }
    val childrenList = listOf("John Doe (Grade 10)", "Jane Doe (Grade 8)")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // -----------------------------------------------------
        // DYNAMIC ROLE-SPECIFIC BANNER
        // -----------------------------------------------------
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0B2240)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Welcome, ${session.username}",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = when {
                                isStudent -> "Student Portal • Grade 10A"
                                isParent -> "Parent Portal • Linked Accounts"
                                isTeacher -> "Teacher Portal • Mathematics Department"
                                isHead -> "Academic Leadership Console"
                                isAdmin -> "Administrator Control Center"
                                else -> "Role: $role"
                            },
                            color = Color(0xFFCBD5E1),
                            fontSize = 13.sp
                        )
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

                Spacer(modifier = Modifier.height(12.dp))

                // Role Quick-Status Callouts
                when {
                    isStudent -> {
                        Surface(
                            color = Color.White.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Term 1 Average: 81.6% • 7 Enrolled Subjects", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                    isParent -> {
                        Box {
                            OutlinedButton(
                                onClick = { showChildDropdown = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Viewing: $selectedChild")
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = showChildDropdown,
                                onDismissRequest = { showChildDropdown = false }
                            ) {
                                childrenList.forEach { child ->
                                    DropdownMenuItem(
                                        text = { Text(child) },
                                        onClick = {
                                            selectedChild = child
                                            showChildDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    isTeacher -> {
                        Surface(
                            color = Color.White.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("3 Assessments Pending Mark Entry", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                    isHead -> {
                        Surface(
                            color = Color.White.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFFA7F3D0), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("1 Batch Awaiting Moderation (Grade 10 Maths)", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                    isAdmin -> {
                        Surface(
                            color = Color.White.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF6EE7B7), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("System Operational • 14 Pending Approvals", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Role-Filtered Grid
        val visibleSections = sections.filter { section ->
            section.allowedRoles.any { r -> r.equals(role, ignoreCase = true) }
        }

        Text(
            text = "Your Workspace Sections",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color(0xFF1E293B)
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(visibleSections) { section ->
                val view = getRoleSectionView(section, role)
                DashboardTile(view) { onNavigate(section.route) }
            }
        }
    }
}

fun getRoleSectionView(section: PortalSection, role: String): RoleSectionView {
    val isParent = role.equals("Parent", ignoreCase = true)
    val isTeacher = role.equals("Teacher", ignoreCase = true)
    val isHead = role.equals("Head of Academics", ignoreCase = true)
    val isAdmin = role.equals("Admin", ignoreCase = true) || role.equals("Administrator", ignoreCase = true)
    val isStudent = role.equals("Student", ignoreCase = true)

    return when (section.route) {
        "/Academic" -> when {
            isTeacher -> RoleSectionView(section.route, "Capture Assessment Marks", "Input & submit student test scores for moderation", section.icon, "Action Needed")
            isHead -> RoleSectionView(section.route, "Approve & Moderate Marks", "1 batch pending approval (Grade 10 Maths)", section.icon, "1 Pending")
            isParent -> RoleSectionView(section.route, "Child Academic Reports", "View term marks & download PDF report card", section.icon, "Term 1")
            isStudent -> RoleSectionView(section.route, "My Marks & Grades", "View recent assessment scores and subject averages", section.icon, "Avg: 81.6%")
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
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = view.icon, contentDescription = null, tint = Color(0xFF0B2240), modifier = Modifier.size(24.dp))
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

            Spacer(modifier = Modifier.height(12.dp))
            Text(view.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(4.dp))
            Text(view.subtitle, fontSize = 11.sp, color = Color(0xFF64748B), lineHeight = 15.sp)
        }
    }
}
