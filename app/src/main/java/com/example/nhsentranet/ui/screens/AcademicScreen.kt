package com.example.nhsentranet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhsentranet.R
import com.example.nhsentranet.model.UserRoles
import com.example.nhsentranet.model.UserSession
import com.example.nhsentranet.ui.theme.*
import com.example.nhsentranet.ui.viewmodels.AcademicViewModel

@Composable
fun AcademicScreen(
    session: UserSession,
    studentId: Int,
    academicViewModel: AcademicViewModel
) {
    if (session.role == UserRoles.Teacher || session.role == UserRoles.HeadOfDepartment) {
        TeacherAcademicView(session)
    } else {
        StudentAcademicView(session = session, studentId = studentId, academicViewModel = academicViewModel)
    }
}

@Composable
fun TeacherAcademicView(session: UserSession) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Teacher Academic Portal", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NewtonNavy)
        Spacer(modifier = Modifier.height(12.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Assigned Classes", fontWeight = FontWeight.Bold, color = NewtonNavy)
                Spacer(modifier = Modifier.height(8.dp))
                Text("• Grade 10A Mathematics (24 Students)", fontSize = 13.sp, color = NewtonTextDark)
                Text("• Grade 10B Mathematics (22 Students)", fontSize = 13.sp, color = NewtonTextDark)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = NewtonTealPrimary)) {
                    Text("View Class Assessments")
                }
            }
        }
    }
}

@Composable
fun StudentAcademicView(
    session: UserSession,
    studentId: Int,
    academicViewModel: AcademicViewModel
) {
    val marks by academicViewModel.marks.collectAsState()
    val selectedTab by academicViewModel.selectedTab.collectAsState()
    val subjectSelection = remember { mutableStateListOf("Mathematics", "Physical Sciences", "Information Technology") }

    LaunchedEffect(studentId, session.role) {
        val isParentOrStudent = session.role == UserRoles.Parent || session.role == UserRoles.Student
        academicViewModel.loadMarks(studentId, isParentOrStudent)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(stringResource(R.string.academic_centre), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NewtonNavy)
        Spacer(modifier = Modifier.height(12.dp))

        SecondaryTabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { academicViewModel.selectTab(0) }, text = { Text("Marks") })
            Tab(selected = selectedTab == 1, onClick = { academicViewModel.selectTab(1) }, text = { Text("Report Cards") })
            Tab(selected = selectedTab == 2, onClick = { academicViewModel.selectTab(2) }, text = { Text("Subject Selection") })
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
                                if (session.role == UserRoles.Parent) {
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
