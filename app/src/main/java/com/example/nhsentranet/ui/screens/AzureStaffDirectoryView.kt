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
import com.example.nhsentranet.model.StaffDto
import com.example.nhsentranet.network.AzureApiClient
import com.example.nhsentranet.ui.theme.WebCharcoalText
import com.example.nhsentranet.ui.theme.WebDivider
import com.example.nhsentranet.ui.theme.WebSlateIcon
import com.example.nhsentranet.ui.theme.WebTealBrand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AzureStaffDirectoryView() {
    var staffList by remember { mutableStateOf<List<StaffDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val res = withContext(Dispatchers.IO) { AzureApiClient.api.getStaff() }
            if (res.isSuccessful && res.body()?.data != null) staffList = res.body()!!.data!!
        } catch (_: Exception) {}
        if (staffList.isEmpty()) {
            staffList = listOf(
                StaffDto(staffID = 1, fullname = "Jane Smith", email = "jsmith@newtonhouse.co.za", userRole = "Teacher", contactNumber = "011 794 2878"),
                StaffDto(staffID = 2, fullname = "Dr. Adams", email = "hod@newtonhouse.co.za", userRole = "HeadOfDepartment", contactNumber = "011 794 2879")
            )
        }
        isLoading = false
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (isLoading) item { LinearProgressIndicator(color = WebTealBrand, modifier = Modifier.fillMaxWidth()) }
        items(staffList) { s ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, WebDivider), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(s.fullname ?: "Staff Member", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = WebCharcoalText)
                    Text("Role: ${s.userRole ?: "Staff"}", fontSize = 12.sp, color = WebTealBrand)
                    Text("Email: ${s.email ?: "N/A"} • Tel: ${s.contactNumber ?: "N/A"}", fontSize = 12.sp, color = WebSlateIcon)
                }
            }
        }
    }
}
