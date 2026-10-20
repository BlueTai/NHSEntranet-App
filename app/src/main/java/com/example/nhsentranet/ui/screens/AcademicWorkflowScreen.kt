package com.example.nhsentranet.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

// -----------------------------------------------------
// LOADING SKELETON ANIMATION (Section 5 Requirement)
// -----------------------------------------------------
fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "")
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(animation = tween(1000)), label = ""
    )
    background(
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFE2E8F0), Color(0xFFF1F5F9), Color(0xFFE2E8F0)),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
        )
    )
}

@Composable
fun AcademicWorkflowScreen(role: String) {
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1500) // Simulate API load time
        isLoading = false
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (isLoading) {
            // Skeleton Loaders
            repeat(3) {
                Box(modifier = Modifier.fillMaxWidth().height(80.dp).padding(vertical = 8.dp).shimmerEffect().background(Color.Gray, RoundedCornerShape(8.dp)))
            }
        } else {
            when (role) {
                "Teacher" -> TeacherMarkUpload()
                "Head of Academics" -> HeadOfAcademicsApproval()
                "Parent", "Student" -> ParentReportDownload()
                else -> ParentReportDownload()
            }
        }
    }
}

// -----------------------------------------------------
// TEACHER: UPLOAD MARKS (Section 3 Requirement)
// -----------------------------------------------------
@Composable
fun TeacherMarkUpload() {
    var studentName by remember { mutableStateOf("Taahir Basson") }
    var mark by remember { mutableStateOf("") }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Capture Assessment Marks", fontWeight = FontWeight.Bold, color = Color(0xFF0B2240))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = studentName, onValueChange = {}, label = { Text("Student") }, enabled = false, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = mark, 
                onValueChange = { if (it.toIntOrNull() ?: 0 in 0..100) mark = it }, 
                label = { Text("Mark (0-100)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { /* TODO: POST /api/marks/upload */ }, modifier = Modifier.fillMaxWidth()) {
                Text("Submit for Moderation")
            }
        }
    }
}

// -----------------------------------------------------
// HEAD OF ACADEMICS: APPROVE MARKS (Section 3 Requirement)
// -----------------------------------------------------
@Composable
fun HeadOfAcademicsApproval() {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Grade 10A Mathematics", fontWeight = FontWeight.Bold)
                Text("Submitted by: Mr. Smith", color = Color.Gray)
            }
            Row {
                Button(onClick = { /* TODO: POST /api/marks/approve */ }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))) {
                    Text("Approve")
                }
            }
        }
    }
}

// -----------------------------------------------------
// PARENT: DOWNLOAD REPORT (Section 3 Requirement)
// -----------------------------------------------------
@Composable
fun ParentReportDownload() {
    Button(
        onClick = { /* TODO: Call API endpoint that returns PDF byte array, write to local storage, and open Intent */ },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Download Term 1 Report (PDF)")
    }
}
