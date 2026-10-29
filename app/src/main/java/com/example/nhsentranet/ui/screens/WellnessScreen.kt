package com.example.nhsentranet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhsentranet.data.NewtonFirebaseRepository
import com.example.nhsentranet.model.IncidentDto
import com.example.nhsentranet.model.UserSession
import com.example.nhsentranet.ui.theme.*

@Composable
fun WellnessScreen(session: UserSession) {
    var incidents by remember { mutableStateOf<List<IncidentDto>>(emptyList()) }
    LaunchedEffect(Unit) {
        incidents = NewtonFirebaseRepository.getInstance().getOpenIncidents()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Student Wellness & Open Incidents", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NewtonNavy)
            Spacer(modifier = Modifier.height(4.dp))
        }
        items(incidents) { inc ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(inc.studentName, fontWeight = FontWeight.Bold, color = NewtonNavy)
                        Text("Severity: ${inc.severity}", color = NewtonAmber, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(inc.description, fontSize = 13.sp, color = NewtonTextDark)
                    Text("Category: ${inc.category} | Logged: ${inc.occurredAt}", fontSize = 11.sp, color = NewtonTextMuted)
                }
            }
        }
    }
}
