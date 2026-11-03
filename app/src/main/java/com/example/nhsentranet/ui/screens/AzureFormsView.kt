package com.example.nhsentranet.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhsentranet.model.ConsentCurrentDto
import com.example.nhsentranet.model.FormFieldDto
import com.example.nhsentranet.model.LinkedChildDto
import com.example.nhsentranet.model.LocalConsentFormDto
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
    var activeForm by remember { mutableStateOf<LocalConsentFormDto?>(null) }
    var currentFormId by remember { mutableIntStateOf(0) }

    var formInputs by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var statusMsg by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    fun loadConsents() {
        scope.launch {
            isLoading = true
            try {
                val res = withContext(Dispatchers.IO) { AzureApiClient.api.getStudentConsents(selectedChild.studentId) }
                if (res.isSuccessful && res.body()?.data != null) consents = res.body()!!.data!!
            } catch (_: Exception) {}
            isLoading = false
        }
    }

    LaunchedEffect(selectedChild.studentId) { loadConsents() }

    val consentFormDefinitions = mapOf(
        "Indemnity" to LocalConsentFormDto(
            title = "Aftercare Indemnity",
            description = "Required for learners enrolled in the Aftercare Programme.",
            consentCode = "Indemnity",
            fields = listOf(
                FormFieldDto("MedicalConsent", "I authorise emergency medical treatment", "checkbox", true),
                FormFieldDto("RiskIndemnity", "I accept the risks of aftercare activities", "checkbox", true),
                FormFieldDto("Rules", "I agree to the rules and supervision policy", "checkbox", true)
            )
        ),
        "Medical" to LocalConsentFormDto(
            title = "Medical Information",
            description = "Please provide updated medical details for emergencies.",
            consentCode = "Medical",
            fields = listOf(
                FormFieldDto("MedicalAid", "Medical Aid Scheme", "text", false),
                FormFieldDto("MedicalNumber", "Medical Aid Number", "text", false),
                FormFieldDto("BloodType", "Blood Type", "text", true),
                FormFieldDto("Allergies", "Allergies", "textarea", false),
                FormFieldDto("Medication", "Permanent Medication", "textarea", false),
                FormFieldDto("OtherHealthInfo", "Other health info", "textarea", false)
            )
        ),
        "POPIA" to LocalConsentFormDto(
            title = "POPIA Consent",
            description = "Consent for processing of personal information.",
            consentCode = "POPIA",
            fields = listOf(
                FormFieldDto("POPIAAccepted", "I understand and accept the POPIA policy", "checkbox", true)
            )
        ),
        "Transport" to LocalConsentFormDto(
            title = "Transport Service Indemnity",
            description = "Permission for your child to travel using school transport.",
            consentCode = "Transport",
            fields = listOf(
                FormFieldDto("TransportAccepted", "I give permission to use school transport", "checkbox", true),
                FormFieldDto("CellFather", "Cell No. Father / Guardian", "text", false),
                FormFieldDto("CellMother", "Cell No. Mother / Guardian", "text", false)
            )
        ),
        "Trip" to LocalConsentFormDto(
            title = "Excursion Consent",
            description = "Consent for your child to participate in an upcoming excursion.",
            consentCode = "Trip",
            fields = listOf(
                FormFieldDto("ExcursionAccepted", "I give permission for my child to attend", "checkbox", true)
            )
        ),
        "Media" to LocalConsentFormDto(
            title = "Pupil Release Form",
            description = "Consent for use of photos, videos, and audio.",
            consentCode = "Media",
            fields = listOf(
                FormFieldDto("MediaConsent", "I consent to photos/videos being used", "checkbox", true),
                FormFieldDto("ParentName", "Printed Name of Parent/Legal Guardian", "text", true),
                FormFieldDto("ParentAddress", "Address of Parent/Legal Guardian", "textarea", false)
            )
        ),
        "CodeOfConduct" to LocalConsentFormDto(
            title = "School Policy Acknowledgement",
            description = "Acknowledge the school's code of conduct.",
            consentCode = "CodeOfConduct",
            fields = listOf(
                FormFieldDto("CodeAccepted", "I have read and agree to the Code of Conduct", "checkbox", true)
            )
        )
    )

    if (activeForm != null) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
            Text(activeForm!!.title, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = WebCharcoalText)
            Spacer(modifier = Modifier.height(4.dp))
            Text(activeForm!!.description, fontSize = 14.sp, color = WebSlateIcon)
            Spacer(modifier = Modifier.height(16.dp))

            activeForm!!.fields.forEach { field ->
                when (field.type) {
                    "checkbox" -> {
                        val isChecked = formInputs[field.fieldName] == "true"
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { chk ->
                                    val newMap = formInputs.toMutableMap()
                                    newMap[field.fieldName] = chk.toString()
                                    formInputs = newMap
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(field.label, fontSize = 14.sp, color = WebCharcoalText)
                        }
                    }
                    "textarea" -> {
                        OutlinedTextField(
                            value = formInputs[field.fieldName] ?: "",
                            onValueChange = { str ->
                                val newMap = formInputs.toMutableMap()
                                newMap[field.fieldName] = str
                                formInputs = newMap
                            },
                            label = { Text(field.label + if (field.isRequired) " *" else "") },
                            modifier = Modifier.fillMaxWidth().height(100.dp).padding(vertical = 4.dp)
                        )
                    }
                    else -> {
                        OutlinedTextField(
                            value = formInputs[field.fieldName] ?: "",
                            onValueChange = { str ->
                                val newMap = formInputs.toMutableMap()
                                newMap[field.fieldName] = str
                                formInputs = newMap
                            },
                            label = { Text(field.label + if (field.isRequired) " *" else "") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            singleLine = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { activeForm = null }, modifier = Modifier.weight(1f)) {
                    Text("Cancel", color = WebCharcoalText)
                }
                Button(
                    onClick = {
                        val missingRequired = activeForm!!.fields.any { it.isRequired && (formInputs[it.fieldName].isNullOrBlank() || formInputs[it.fieldName] == "false") }
                        if (missingRequired) {
                            statusMsg = "Please complete all required fields."
                        } else {
                            val notesPayload = formInputs.map { "${it.key}=${it.value}" }.joinToString("; ")
                            scope.launch {
                                try {
                                    val res = withContext(Dispatchers.IO) {
                                        AzureApiClient.api.signConsent(SignConsentRequest(selectedChild.studentId, currentFormId, notesPayload))
                                    }
                                    if (res.isSuccessful) {
                                        statusMsg = "Form successfully signed and submitted."
                                        activeForm = null
                                        loadConsents()
                                    } else {
                                        statusMsg = "Error HTTP ${res.code()}: ${res.body()?.message ?: ""}"
                                    }
                                } catch (e: Exception) {
                                    statusMsg = "Error: ${e.localizedMessage}"
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WebTealBrand),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Agree & Sign", fontWeight = FontWeight.Bold)
                }
            }

            if (statusMsg != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(statusMsg!!, color = NewtonRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (isLoading) item { LinearProgressIndicator(color = WebTealBrand, modifier = Modifier.fillMaxWidth()) }
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
                                    val localDef = consentFormDefinitions[form.consentCode]
                                    if (localDef != null) {
                                        formInputs = emptyMap()
                                        statusMsg = null
                                        currentFormId = form.consentTypeID
                                        activeForm = localDef
                                    } else {
                                        scope.launch {
                                            try {
                                                val res = withContext(Dispatchers.IO) {
                                                    AzureApiClient.api.signConsent(SignConsentRequest(selectedChild.studentId, form.consentTypeID, "Signed via Mobile App"))
                                                }
                                                if (res.isSuccessful) {
                                                    statusMsg = "Signed '${form.consentName}' in Azure SQL!"
                                                    loadConsents()
                                                }
                                            } catch (_: Exception) {}
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = WebTealBrand)
                            ) { Text("Fill Form", fontSize = 12.sp) }
                        }
                    }
                }
            }
        }
    }
}
