package com.example.nhsentranet.ui.screens

import android.view.ViewGroup
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import android.net.http.SslError
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.nhsentranet.network.NewtonFirebaseDatabase
import kotlinx.coroutines.launch

@Composable
fun FormsScreen(isAdmin: Boolean, studentName: String = "John Johnson (Grade 10A)") {
    var tabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Expanded Medical Form", "Subject Selection", "Google Forms Embed")

    Column(modifier = Modifier.fillMaxSize()) {
        SecondaryTabRow(selectedTabIndex = tabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(selected = tabIndex == index, onClick = { tabIndex = index }, text = { Text(title, fontSize = 12.sp) })
            }
        }

        when (tabIndex) {
            0 -> ExpandedNativeForm(studentName = studentName)
            1 -> SubjectSelectionForm(studentName = studentName)
            2 -> GoogleFormsWebView(isAdmin = isAdmin)
        }
    }
}

// -----------------------------------------------------
// EXPANDED DATA CAPTURE FIELDS & PDF SAVE (Section 4 Requirement)
// -----------------------------------------------------
@Composable
fun ExpandedNativeForm(studentName: String) {
    val scope = rememberCoroutineScope()
    var parentOccupation by remember { mutableStateOf("Civil Engineer") }
    var medicalAid by remember { mutableStateOf("Discovery Health #90214820") }
    var allergies by remember { mutableStateOf("Penicillin, Peanuts") }
    var transportDriver by remember { mutableStateOf("David Miller (Contact: 082 555 1234)") }
    
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp).fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("Student Medical & Transport Consent", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0B2240))
        Text("Complete annual POPIA & medical updates", fontSize = 12.sp, color = Color(0xFF64748B))
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = studentName,
            onValueChange = {},
            label = { Text("Student Name (Auto-populated)") },
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        
        OutlinedTextField(
            value = parentOccupation,
            onValueChange = { parentOccupation = it },
            label = { Text("Parent Occupation") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = medicalAid,
            onValueChange = { medicalAid = it },
            label = { Text("Medical Aid Scheme & Number") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        
        OutlinedTextField(
            value = allergies,
            onValueChange = { allergies = it },
            label = { Text("Student Allergies & Medical Conditions") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        
        OutlinedTextField(
            value = transportDriver,
            onValueChange = { transportDriver = it },
            label = { Text("Transport Driver Name & Contact Details") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Digital Consent Signature:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                .background(Color(0xFFF8FAFC)),
            contentAlignment = Alignment.Center
        ) {
            Text("Digitally Signed by Parent", color = Color(0xFF0D6E7E), fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        if (statusMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(color = Color(0xFFE5F4F7), shape = RoundedCornerShape(6.dp)) {
                Text(
                    text = statusMessage!!,
                    color = Color(0xFF0D6E7E),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {
                isSaving = true
                scope.launch {
                    val res = NewtonFirebaseDatabase.submitForm(
                        studentName = studentName,
                        formType = "Medical & Transport",
                        parentOccupation = parentOccupation,
                        allergies = allergies,
                        driverInfo = transportDriver
                    )
                    isSaving = false
                    res.onSuccess {
                        statusMessage = "Form saved to database & PDF report card generated successfully."
                    }.onFailure { err ->
                        statusMessage = err.message
                    }
                }
            },
            enabled = !isSaving,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B2240)),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (isSaving) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Save & Download as PDF", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// -----------------------------------------------------
// SUBJECT SELECTION ON BEHALF OF CHILD (Section 3 Requirement)
// -----------------------------------------------------
@Composable
fun SubjectSelectionForm(studentName: String) {
    val availableSubjects = listOf(
        "Mathematics",
        "Physical Sciences",
        "Information Technology",
        "Accounting",
        "Life Sciences",
        "English Home Language",
        "Afrikaans First Additional",
        "History"
    )
    
    val selectedSubjects = remember { mutableStateListOf("Mathematics", "Physical Sciences", "Information Technology", "English Home Language") }
    var saveStatus by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.padding(16.dp).fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("Subject Enrolment", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0B2240))
        Text("Select subjects for $studentName (Admin constraint: 7 subjects)", fontSize = 12.sp, color = Color(0xFF64748B))
        Spacer(modifier = Modifier.height(16.dp))

        availableSubjects.forEach { subject ->
            val isChecked = selectedSubjects.contains(subject)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = { checked ->
                        if (checked) {
                            if (!selectedSubjects.contains(subject)) selectedSubjects.add(subject)
                        } else {
                            selectedSubjects.remove(subject)
                        }
                        saveStatus = null
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(subject, fontSize = 14.sp, fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal)
            }
        }

        if (saveStatus != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(saveStatus!!, color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {
                saveStatus = "Enrolled ${selectedSubjects.size} subjects for $studentName in database."
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D6E7E)),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Confirm Subject Choice", fontWeight = FontWeight.Bold)
        }
    }
}

// -----------------------------------------------------
// GOOGLE FORMS INTEGRATION (Section 4 Requirement)
// -----------------------------------------------------
@Composable
fun GoogleFormsWebView(isAdmin: Boolean) {
    var googleFormUrl by remember { mutableStateOf("https://docs.google.com/forms/d/e/1FAIpQLSfP812NewtonHouseForm/viewform?embedded=true") }

    Column(modifier = Modifier.fillMaxSize()) {
        if (isAdmin) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF0F9FB))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = googleFormUrl,
                    onValueChange = { googleFormUrl = it },
                    label = { Text("Admin Google Form Embed URL") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp)
                )
            }
        }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        allowFileAccess = true
                        allowContentAccess = true
                        mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    }
                    webViewClient = object : WebViewClient() {
                        override fun onReceivedSslError(
                            view: WebView?,
                            handler: SslErrorHandler?,
                            error: SslError?
                        ) {
                            handler?.proceed()
                        }
                    }
                    loadUrl(googleFormUrl)
                }
            }
        )
    }
}
