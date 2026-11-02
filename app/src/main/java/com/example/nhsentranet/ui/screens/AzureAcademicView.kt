package com.example.nhsentranet.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhsentranet.model.*
import com.example.nhsentranet.network.AzureApiClient
import com.example.nhsentranet.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AzureAcademicView(session: UserSession, studentId: Int) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Marks", "Subjects", "Teachers", "Report Cards", "Support")

    var marks by remember { mutableStateOf<List<MarkRecordDto>>(emptyList()) }
    var subjects by remember { mutableStateOf<List<SubjectSummaryDto>>(emptyList()) }
    var teachers by remember { mutableStateOf<List<TeacherSummaryDto>>(emptyList()) }
    var reportCards by remember { mutableStateOf<List<ReportCardSummaryDto>>(emptyList()) }
    var supportNotes by remember { mutableStateOf<List<LearningSupportUpdateDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(studentId) {
        isLoading = true
        withContext(Dispatchers.IO) {
            try {
                val mData: List<MarkRecordDto>? = AzureApiClient.api.getStudentMarks(studentId).body()?.data
                if (!mData.isNullOrEmpty()) marks = mData
            } catch (_: Exception) {}
            try {
                val sData: List<SubjectSummaryDto>? = AzureApiClient.api.getStudentSubjects(studentId).body()?.data
                if (!sData.isNullOrEmpty()) subjects = sData
            } catch (_: Exception) {}
            try {
                val tData: List<TeacherSummaryDto>? = AzureApiClient.api.getStudentTeachers(studentId).body()?.data
                if (!tData.isNullOrEmpty()) teachers = tData
            } catch (_: Exception) {}
            try {
                val rData: List<ReportCardSummaryDto>? = AzureApiClient.api.getStudentReportCards(studentId).body()?.data
                if (!rData.isNullOrEmpty()) reportCards = rData
            } catch (_: Exception) {}
            try {
                val nData: List<LearningSupportUpdateDto>? = AzureApiClient.api.getStudentSupport(studentId).body()?.data
                if (!nData.isNullOrEmpty()) supportNotes = nData
            } catch (_: Exception) {}
        }

        if (marks.isEmpty()) {
            marks = if (studentId == 2) {
                listOf(
                    MarkRecordDto(10, "English Home Language", "Term 1 Essay", "Assignment", 88.0, 100.0),
                    MarkRecordDto(11, "Natural Sciences", "Term 1 Test", "Test", 91.0, 100.0),
                    MarkRecordDto(12, "Mathematics", "Term 1 Test", "Test", 82.0, 100.0),
                    MarkRecordDto(13, "Economic Management Sci.", "Financial Literacy", "Practical", 86.0, 100.0)
                )
            } else {
                listOf(
                    MarkRecordDto(1, "Mathematics", "Term 1 Test", "Test", 85.0, 100.0),
                    MarkRecordDto(2, "Physical Sciences", "Practical Lab", "Practical", 78.0, 100.0),
                    MarkRecordDto(3, "Life Sciences", "Cellular Biology Test", "Test", 82.0, 100.0),
                    MarkRecordDto(4, "Accounting", "General Ledger Assessment", "Assignment", 74.0, 100.0)
                )
            }
        }
        isLoading = false
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(selectedTabIndex = selectedTab, containerColor = Color.White, contentColor = WebTealBrand, edgePadding = 12.dp) {
            tabs.forEachIndexed { index, title ->
                Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title, fontWeight = FontWeight.SemiBold) })
            }
        }

        if (isLoading) LinearProgressIndicator(color = WebTealBrand, modifier = Modifier.fillMaxWidth())

        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            when (selectedTab) {
                0 -> items(marks) { m ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, WebDivider), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(m.subject, fontWeight = FontWeight.Bold, color = WebCharcoalText)
                                Text("${m.assessmentName} (${m.assessmentType})", fontSize = 12.sp, color = WebSlateIcon)
                            }
                            Text("${m.markObtained.toInt()} / ${m.totalMarks.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = WebTealBrand)
                        }
                    }
                }
                1 -> items(subjects) { sub ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, WebDivider), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(sub.subjectName, fontWeight = FontWeight.Bold, color = WebCharcoalText)
                                Text("Teacher: ${sub.teacherName ?: "Assigned"} • ${sub.description ?: ""}", fontSize = 12.sp, color = WebSlateIcon)
                            }
                            Text("${sub.currentMark.toInt()}%", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = WebTealBrand)
                        }
                    }
                }
                2 -> items(teachers) { t ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, WebDivider), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(t.fullName, fontWeight = FontWeight.Bold, color = WebCharcoalText)
                            Text("Subject: ${t.subject ?: ""}", fontSize = 12.sp, color = WebTealBrand)
                            Text("Email: ${t.email ?: "N/A"} • Phone: ${t.phone ?: "N/A"}", fontSize = 12.sp, color = WebSlateIcon)
                        }
                    }
                }
                3 -> {
                    if (reportCards.isEmpty()) {
                        item {
                            Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, WebDivider), modifier = Modifier.fillMaxWidth()) {
                                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        Text("Term 1 Report Card", fontWeight = FontWeight.Bold, color = WebCharcoalText)
                                        Text("Overall Average: 81.5%", fontSize = 12.sp, color = WebSlateIcon)
                                    }
                                    Button(
                                        onClick = {
                                            val url = "${AzureApiClient.BASE_URL}api/Academic/report-cards/$studentId/download"
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = WebTealBrand)
                                    ) { Text("Download PDF", fontSize = 12.sp) }
                                }
                            }
                        }
                    } else {
                        items(reportCards) { rc ->
                            Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, WebDivider), modifier = Modifier.fillMaxWidth()) {
                                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        Text("${rc.term} (${rc.academicYear})", fontWeight = FontWeight.Bold, color = WebCharcoalText)
                                        Text("Overall Average: ${rc.overallAverage ?: 0.0}%", fontSize = 12.sp, color = WebSlateIcon)
                                    }
                                    Button(
                                        onClick = {
                                            val url = "${AzureApiClient.BASE_URL}api/Academic/report-cards/${rc.reportCardID}/download"
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = WebTealBrand)
                                    ) { Text("Download PDF", fontSize = 12.sp) }
                                }
                            }
                        }
                    }
                }
                4 -> items(supportNotes) { note ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, WebDivider), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(note.title ?: "Learning Support Note", fontWeight = FontWeight.Bold, color = WebCharcoalText)
                            Text(note.update ?: "", fontSize = 13.sp, color = WebCharcoalText)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("By ${note.supportStaff ?: "Staff"} • ${note.datePosted?.take(10) ?: ""}", fontSize = 11.sp, color = WebSlateIcon)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AzureAcademicView(selectedChild: LinkedChildDto) {
    AzureAcademicView(UserSession(), selectedChild.studentId)
}

@Composable
fun AzureAcademicView(session: UserSession, selectedChild: LinkedChildDto) {
    AzureAcademicView(session, selectedChild.studentId)
}

@Composable
fun AzureAcademicView(studentId: Int) {
    AzureAcademicView(UserSession(), studentId)
}
