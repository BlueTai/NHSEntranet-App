package com.example.nhsentranet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhsentranet.R
import com.example.nhsentranet.data.NewtonFirebaseRepository
import com.example.nhsentranet.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AdminScreen() {
    val scope = rememberCoroutineScope()
    var msg by remember { mutableStateOf<String?>(null) }
    var studentName by remember { mutableStateOf("") }
    var selectedGrade by remember { mutableStateOf("Grade 10A") }

    var teacherMsg by remember { mutableStateOf<String?>(null) }
    var teacherName by remember { mutableStateOf("") }
    var teacherGrade by remember { mutableStateOf("Grade 10A") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text(stringResource(R.string.admin_console), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NewtonNavy)
        Spacer(modifier = Modifier.height(12.dp))

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
                                val res = NewtonFirebaseRepository.getInstance().assignClassTeacher(teacherGrade, teacherName)
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
