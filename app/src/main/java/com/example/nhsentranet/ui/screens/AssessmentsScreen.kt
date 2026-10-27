package com.example.nhsentranet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhsentranet.R
import com.example.nhsentranet.data.NewtonFirebaseRepository
import com.example.nhsentranet.model.UserRoles
import com.example.nhsentranet.model.UserSession
import com.example.nhsentranet.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AssessmentsScreen(session: UserSession, studentId: Int) {
    val scope = rememberCoroutineScope()
    var markValue by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf<String?>(null) }
    val isHod = session.role == UserRoles.HeadOfDepartment || session.role == UserRoles.Admin

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(stringResource(R.string.marks_moderation), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NewtonNavy)
        Spacer(modifier = Modifier.height(12.dp))

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
                            val res = NewtonFirebaseRepository.getInstance().uploadMark(studentId, m, isHod)
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
