package com.example.nhsentranet.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhsentranet.model.StudentDto
import com.example.nhsentranet.network.AzureApiClient
import com.example.nhsentranet.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AzureStudentsListView() {
    var students by remember { mutableStateOf<List<StudentDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val res = withContext(Dispatchers.IO) { AzureApiClient.api.getStudents() }
            if (res.isSuccessful && res.body()?.data != null) students = res.body()!!.data!!
        } catch (_: Exception) {}
        if (students.isEmpty()) {
            students = listOf(
                StudentDto(studentID = 1, studentNumber = "ST00001", fullname = "John Johnson", gradeLevel = "10", classSymbol = "A", classTeacher = "Jane Smith"),
                StudentDto(studentID = 2, studentNumber = "ST00002", fullname = "Emily Johnson", gradeLevel = "8", classSymbol = "B", classTeacher = "Jane Smith")
            )
        }
        isLoading = false
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (isLoading) item { LinearProgressIndicator(color = WebTealBrand, modifier = Modifier.fillMaxWidth()) }
        items(students) { st ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, WebDivider), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(st.fullname ?: "Student", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = WebCharcoalText)
                        Text("Student Number: ${st.studentNumber ?: st.studentNum ?: "ST0000${st.studentID}"}", fontSize = 12.sp, color = WebSlateIcon)
                        if (!st.classTeacher.isNullOrBlank()) {
                            Text("Class Teacher: ${st.classTeacher}", fontSize = 11.sp, color = WebSlateIcon)
                        }
                    }
                    Surface(color = WebActiveItemBg, shape = RoundedCornerShape(6.dp)) {
                        Text("Grade ${st.gradeLevel ?: "10"}${st.classSymbol ?: "A"}", color = WebTealBrand, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }
        }
    }
}
