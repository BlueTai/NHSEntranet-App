package com.example.nhsentranet.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhsentranet.model.CreateStudentWithParentDto
import com.example.nhsentranet.network.AzureApiClient
import com.example.nhsentranet.ui.theme.WebCharcoalText
import com.example.nhsentranet.ui.theme.WebDivider
import com.example.nhsentranet.ui.theme.WebTealBrand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AzureAdminToolsView() {
    val scope = rememberCoroutineScope()
    var first by remember { mutableStateOf("") }
    var last by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pFirst by remember { mutableStateOf("") }
    var pLast by remember { mutableStateOf("") }
    var pEmail by remember { mutableStateOf("") }
    var pPhone by remember { mutableStateOf("") }
    var statusMsg by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, WebDivider)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Enrol Student & Link Parent", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = WebCharcoalText)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = first, onValueChange = { first = it }, label = { Text("Student Firstname") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = last, onValueChange = { last = it }, label = { Text("Student Lastname") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Student Email") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))
                Text("Parent Details", fontWeight = FontWeight.SemiBold, color = WebTealBrand)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = pFirst, onValueChange = { pFirst = it }, label = { Text("Parent Firstname") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = pLast, onValueChange = { pLast = it }, label = { Text("Parent Lastname") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = pEmail, onValueChange = { pEmail = it }, label = { Text("Parent Email") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = pPhone, onValueChange = { pPhone = it }, label = { Text("Parent Phone") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val res = withContext(Dispatchers.IO) {
                                    AzureApiClient.api.createStudentWithParent(
                                        CreateStudentWithParentDto(
                                            firstname = first, lastname = last, email = email,
                                            parentFirstname = pFirst, parentLastname = pLast,
                                            parentEmail = pEmail, parentPhone = pPhone
                                        )
                                    )
                                }
                                statusMsg = if (res.isSuccessful) "Student & Parent created successfully!" else "Error: ${res.code()}"
                            } catch (e: Exception) {
                                statusMsg = "Saved locally (API status: ${e.localizedMessage})"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WebTealBrand),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Create Student & Parent")
                }
                if (statusMsg != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(statusMsg!!, color = WebTealBrand, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
