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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhsentranet.PortalSection
import com.example.nhsentranet.UserSession

@Composable
fun RoleBasedDashboard(
    session: UserSession,
    sections: List<PortalSection>,
    onNavigate: (String) -> Unit
) {
    // Parent Child-Switcher State
    var selectedChild by remember { mutableStateOf("John Doe (Grade 10)") }
    var showChildDropdown by remember { mutableStateOf(false) }
    val childrenList = listOf("John Doe (Grade 10)", "Jane Doe (Grade 8)")

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Dynamic Role Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0B2240)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Welcome, ${session.username}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Role: ${session.role}", color = Color(0xFFCBD5E1), fontSize = 14.sp)

                // -----------------------------------------------------
                // PARENT CHILD-SWITCHER (Section 3 Requirement)
                // -----------------------------------------------------
                if (session.role.equals("Parent", ignoreCase = true)) {
                    Spacer(modifier = Modifier.height(16.dp))
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
                                        // TODO: Trigger API refresh for the selected child's marks/forms
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Role-Filtered Grid (Section 2 Requirement)
        val visibleSections = sections.filter { section ->
            section.allowedRoles.any { role -> role.equals(session.role, ignoreCase = true) }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(visibleSections) { section ->
                DashboardTile(section) { onNavigate(section.route) }
            }
        }
    }
}

@Composable
fun DashboardTile(section: PortalSection, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(imageVector = section.icon, contentDescription = null, tint = Color(0xFF0B2240), modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(section.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E293B))
            Text(section.subtitle, fontSize = 12.sp, color = Color(0xFF64748B))
        }
    }
}
