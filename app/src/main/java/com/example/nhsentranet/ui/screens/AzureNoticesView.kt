package com.example.nhsentranet.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhsentranet.model.CreateNoticeDto
import com.example.nhsentranet.model.NoticeDto
import com.example.nhsentranet.model.UserRoles
import com.example.nhsentranet.model.UserSession
import com.example.nhsentranet.network.AzureApiClient
import com.example.nhsentranet.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AzureNoticesView(session: UserSession) {
    val scope = rememberCoroutineScope()
    var notices by remember { mutableStateOf<List<NoticeDto>>(emptyList()) }
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    fun loadNotices() {
        scope.launch {
            try {
                val res = withContext(Dispatchers.IO) { AzureApiClient.api.getNotices() }
                if (res.isSuccessful && res.body()?.data != null) notices = res.body()!!.data!!
            } catch (_: Exception) {}
            if (notices.isEmpty()) {
                notices = listOf(NoticeDto(1, "Term 4 Examination Timetable", "Academic", "Final examination schedules for Grades 8–12 have been published.", "Jane Smith", "2026-10-29", true))
            }
        }
    }

    LaunchedEffect(Unit) { loadNotices() }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (session.userRole in listOf(UserRoles.Admin, UserRoles.Teacher, UserRoles.HeadOfDepartment)) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, WebDivider)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Post New Notice", fontWeight = FontWeight.Bold, color = WebCharcoalText)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Notice Title") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                if (title.isNotBlank() && desc.isNotBlank()) {
                                    scope.launch {
                                        try {
                                            withContext(Dispatchers.IO) { AzureApiClient.api.createNotice(CreateNoticeDto(title, "General", desc, false)) }
                                        } catch (_: Exception) {}
                                        title = ""; desc = ""
                                        loadNotices()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WebTealBrand)
                        ) { Text("Publish Notice") }
                    }
                }
            }
        }
        items(notices) { n ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, WebDivider), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(n.noticeName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = WebCharcoalText, modifier = Modifier.weight(1f))
                        if (session.userRole in listOf(UserRoles.Admin, UserRoles.Teacher)) {
                            IconButton(onClick = {
                                scope.launch {
                                    try { withContext(Dispatchers.IO) { AzureApiClient.api.deleteNotice(n.noticeID) } } catch (_: Exception) {}
                                    loadNotices()
                                }
                            }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = NewtonRed)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(n.description ?: "", fontSize = 13.sp, color = WebCharcoalText)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Posted by ${n.staffName ?: "Admin"} • ${n.datePosted.take(10)}", fontSize = 11.sp, color = WebSlateIcon)
                }
            }
        }
    }
}
