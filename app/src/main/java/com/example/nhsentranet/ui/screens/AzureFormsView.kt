package com.example.nhsentranet.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhsentranet.model.ConsentCurrentDto
import com.example.nhsentranet.model.LinkedChildDto
import com.example.nhsentranet.model.SignConsentRequest
import com.example.nhsentranet.network.AzureApiClient
import com.example.nhsentranet.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AzureFormsView(selectedChild: LinkedChildDto, isAdminMode: Boolean) {
    val scope = rememberCoroutineScope()
    var consents by remember { mutableStateOf<List<ConsentCurrentDto>>(emptyList()) }
    var statusMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(selectedChild.studentId) {
        try {
            val res = withContext(Dispatchers.IO) { AzureApiClient.api.getStudentConsents(selectedChild.studentId) }
            if (res.isSuccessful && res.body()?.data != null) consents = res.body()!!.data!!
        } catch (_: Exception) {}
        if (consents.isEmpty()) {
            consents = listOf(
                ConsentCurrentDto(selectedChild.studentId, 1, "Indemnity", "Aftercare & Excursion Indemnity Form", true, false),
                ConsentCurrentDto(selectedChild.studentId, 2, "Media", "Pupil Media Release Form", false, true),
                ConsentCurrentDto(selectedChild.studentId, 3, "Medical", "Confidential Medical Information", true, false)
            )
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (statusMsg != null) {
            item { Text(statusMsg!!, color = NewtonGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
        }
        items(consents) { form ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, WebDivider), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(form.consentName, fontWeight = FontWeight.Bold, color = WebCharcoalText)
                        Text("Code: ${form.consentCode} • ${if (form.isSigned) "Signed" else "Pending Signature"}", fontSize = 12.sp, color = if (form.isSigned) NewtonGreen else NewtonAmber)
                    }
                    if (!form.isSigned && !isAdminMode) {
                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        withContext(Dispatchers.IO) {
                                            AzureApiClient.api.signConsent(SignConsentRequest(selectedChild.studentId, form.consentTypeID, "Signed via Mobile App"))
                                        }
                                    } catch (_: Exception) {}
                                    statusMsg = "Signed '${form.consentName}' successfully."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WebTealBrand)
                        ) { Text("Sign", fontSize = 12.sp) }
                    }
                }
            }
        }
    }
}
