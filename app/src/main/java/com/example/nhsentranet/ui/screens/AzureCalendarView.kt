package com.example.nhsentranet.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.nhsentranet.model.CreateEventDto
import com.example.nhsentranet.model.EventDto
import com.example.nhsentranet.model.LinkedChildDto
import com.example.nhsentranet.model.UserRoles
import com.example.nhsentranet.model.UserSession
import com.example.nhsentranet.network.AzureApiClient
import com.example.nhsentranet.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AzureCalendarView(session: UserSession = UserSession()) {
    val scope = rememberCoroutineScope()
    var events by remember { mutableStateOf<List<EventDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var evTitle by remember { mutableStateOf("") }
    var evLocation by remember { mutableStateOf("") }
    var evDate by remember { mutableStateOf("2026-11-15") }

    fun loadEvents() {
        scope.launch {
            isLoading = true
            try {
                val res = withContext(Dispatchers.IO) { AzureApiClient.api.getCalendarEvents() }
                if (res.isSuccessful && res.body()?.data != null) events = res.body()!!.data!!
            } catch (_: Exception) {}
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { loadEvents() }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (session.userRole in listOf(UserRoles.Admin, UserRoles.HeadOfDepartment, UserRoles.Teacher)) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, WebDivider)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Create Calendar Event", fontWeight = FontWeight.Bold, color = WebCharcoalText)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = evTitle, onValueChange = { evTitle = it }, label = { Text("Event Title") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = evDate, onValueChange = { evDate = it }, label = { Text("Date (YYYY-MM-DD)") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = evLocation, onValueChange = { evLocation = it }, label = { Text("Location") }, modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                if (evTitle.isNotBlank()) {
                                    scope.launch {
                                        try {
                                            withContext(Dispatchers.IO) {
                                                AzureApiClient.api.createCalendarEvent(
                                                    CreateEventDto(
                                                        title = evTitle,
                                                        eventDate = "${evDate.trim()}T09:00:00Z",
                                                        location = evLocation.ifBlank { "Main Campus" }
                                                    )
                                                )
                                            }
                                        } catch (_: Exception) {}
                                        evTitle = ""; evLocation = ""
                                        loadEvents()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WebTealBrand)
                        ) { Text("Add Event") }
                    }
                }
            }
        }

        if (isLoading) item { LinearProgressIndicator(color = WebTealBrand, modifier = Modifier.fillMaxWidth()) }
        items(events) { ev ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, WebDivider), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = WebTealBrand)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(ev.title, fontWeight = FontWeight.Bold, color = WebCharcoalText)
                        Text("${ev.eventType ?: "Event"} • ${ev.eventDate.take(10)} ${if (!ev.location.isNullOrBlank()) "@ ${ev.location}" else ""}", fontSize = 12.sp, color = WebTealBrand)
                        if (!ev.subtitle.isNullOrBlank()) {
                            Text(ev.subtitle, fontSize = 12.sp, color = WebSlateIcon)
                        }
                        if (!ev.createdByStaffName.isNullOrBlank()) {
                            Text("Organised by ${ev.createdByStaffName}", fontSize = 11.sp, color = WebSlateIcon)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AzureCalendarView(selectedChild: LinkedChildDto) {
    AzureCalendarView(UserSession())
}

@Composable
fun AzureCalendarView(studentId: Int) {
    AzureCalendarView(UserSession())
}
