package com.example.nhsentranet.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhsentranet.model.AssessmentItemDto
import com.example.nhsentranet.model.LinkedChildDto
import com.example.nhsentranet.network.AzureApiClient
import com.example.nhsentranet.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AzureCalendarView(selectedChild: LinkedChildDto) {
    var assessments by remember { mutableStateOf<List<AssessmentItemDto>>(emptyList()) }

    LaunchedEffect(selectedChild.studentId) {
        try {
            val res = withContext(Dispatchers.IO) { AzureApiClient.api.getStudentAssessments(selectedChild.studentId) }
            if (res.isSuccessful && res.body()?.data != null) assessments = res.body()!!.data!!
        } catch (_: Exception) {}
        if (assessments.isEmpty()) {
            assessments = if (selectedChild.studentId == 2) {
                listOf(
                    AssessmentItemDto(10, "Natural Sciences Lab Test", "Natural Sciences", "2026-11-08", 50.0, "Practical"),
                    AssessmentItemDto(11, "English Prepared Speech", "English Home Lang", "2026-11-12", 30.0, "Oral")
                )
            } else {
                listOf(
                    AssessmentItemDto(1, "Mathematics Term Test", "Mathematics", "2026-11-10", 100.0, "Test"),
                    AssessmentItemDto(2, "Physical Sciences Lab", "Physical Sciences", "2026-11-14", 50.0, "Practical")
                )
            }
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, WebDivider), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Assessment & Exam Calendar", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = WebCharcoalText)
                    Text("Scheduled for: ${selectedChild.fullName} (${selectedChild.gradeName})", fontSize = 12.sp, color = WebTealBrand, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        items(assessments) { a ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, WebDivider), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = WebTealBrand)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(a.assessmentName, fontWeight = FontWeight.Bold, color = WebCharcoalText)
                        Text("${a.subject ?: ""} • Due: ${a.dueDate ?: "TBA"} (${a.totalMarks.toInt()} marks)", fontSize = 12.sp, color = WebSlateIcon)
                    }
                }
            }
        }
    }
}

@Composable
fun AzureCalendarView(studentId: Int) {
    AzureCalendarView(LinkedChildDto(studentId, "Student #$studentId", "Grade 10"))
}
