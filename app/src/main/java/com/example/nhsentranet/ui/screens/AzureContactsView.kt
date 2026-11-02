package com.example.nhsentranet.ui.screens

import androidx.compose.foundation.BorderStroke
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
import com.example.nhsentranet.model.TeacherContactDto
import com.example.nhsentranet.network.AzureApiClient
import com.example.nhsentranet.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AzureContactsView() {
    var contacts by remember { mutableStateOf<List<TeacherContactDto>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            val res = withContext(Dispatchers.IO) { AzureApiClient.api.getContacts() }
            if (res.isSuccessful && res.body()?.data != null) contacts = res.body()!!.data!!
        } catch (_: Exception) {}
        if (contacts.isEmpty()) {
            contacts = listOf(
                TeacherContactDto(1, "Jane Smith", "Teacher", "jsmith@newtonhouse.co.za", "+27 82 987 6543"),
                TeacherContactDto(2, "Reception & Admin", "Administration", "info@newtonhouse.co.za", "011 794 2878")
            )
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(contacts) { c ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, WebDivider), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(c.fullname, fontWeight = FontWeight.Bold, color = WebCharcoalText)
                    Text(c.userRole ?: "Staff", fontSize = 12.sp, color = WebTealBrand)
                    Text("Email: ${c.email ?: "N/A"} • Phone: ${c.contactNumber ?: "N/A"}", fontSize = 12.sp, color = WebSlateIcon)
                }
            }
        }
    }
}
