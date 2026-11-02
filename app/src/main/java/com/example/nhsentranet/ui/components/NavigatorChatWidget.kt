package com.example.nhsentranet.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhsentranet.R
import com.example.nhsentranet.model.ChatMessage
import com.example.nhsentranet.model.UserSession
import com.example.nhsentranet.network.AzureApiClient
import com.example.nhsentranet.network.ChatRequest
import com.example.nhsentranet.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun NavigatorChatWidget(
    session: UserSession,
    ttsEnabled: Boolean,
    onToggleTts: () -> Unit,
    onSpeak: (String) -> Unit,
    onClose: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }

    val messages = remember {
        mutableStateListOf(
            ChatMessage("Hi ${session.fullName}! Ask me about marks, fees, timetable, transport, or forms.", false)
        )
    }

    fun handleSend(query: String) {
        val q = query.trim()
        if (q.isEmpty() || isThinking) return

        messages.add(ChatMessage(q, true))
        input = ""
        isThinking = true

        scope.launch {
            listState.animateScrollToItem(messages.size - 1)

            var aiReplyText = ""
            var routeTarget: String? = null
            var actionLabelText: String? = null

            // 1. Attempt Gemini AI request via Azure API backend (/api/Operations/navigator-chat)
            try {
                val res = withContext(Dispatchers.IO) {
                    AzureApiClient.api.sendChatMessage("Bearer ${session.token}", ChatRequest(q))
                }
                if (res.isSuccessful && res.body()?.data != null) {
                    val data = res.body()!!.data!!
                    aiReplyText = data.reply
                    routeTarget = data.route
                }
            } catch (_: Exception) {}

            // 2. Intelligent local fallback with deep links
            if (aiReplyText.isBlank()) {
                val lower = q.lowercase()
                when {
                    lower.contains("mark") || lower.contains("grade") || lower.contains("report") -> {
                        aiReplyText = "Opening the Academic Centre to view ${session.fullName}'s latest assessment marks."
                        routeTarget = "/Academic"
                        actionLabelText = "Open Academic Centre"
                    }
                    lower.contains("fee") || lower.contains("invoice") || lower.contains("pay") || lower.contains("proof") -> {
                        aiReplyText = "Navigating to Invoices & Fees statement."
                        routeTarget = "/Invoices"
                        actionLabelText = "View Fee Invoices"
                    }
                    lower.contains("form") || lower.contains("consent") || lower.contains("popia") || lower.contains("medical") -> {
                        aiReplyText = "Opening Digital Consent Forms to complete annual medical and indemnity consents."
                        routeTarget = "/Forms"
                        actionLabelText = "Open Consent Forms"
                    }
                    lower.contains("bus") || lower.contains("transport") || lower.contains("route") -> {
                        aiReplyText = "Viewing live transport bus routes and driver contacts."
                        routeTarget = "/Transport"
                        actionLabelText = "View Bus Routes"
                    }
                    lower.contains("map") || lower.contains("campus") || lower.contains("location") -> {
                        aiReplyText = "Opening Randburg campus maps and drop-off zone locations."
                        routeTarget = "/Maps"
                        actionLabelText = "Campus Maps"
                    }
                    lower.contains("notice") || lower.contains("alert") || lower.contains("exam") || lower.contains("circular") -> {
                        aiReplyText = "Opening School Notice Board for exam schedules and alerts."
                        routeTarget = "/Notices"
                        actionLabelText = "View Notices"
                    }
                    lower.contains("contact") || lower.contains("phone") || lower.contains("email") || lower.contains("teacher") -> {
                        aiReplyText = "Viewing Staff Directory & Emergency Contacts."
                        routeTarget = "/Contacts"
                        actionLabelText = "Staff Directory"
                    }
                    else -> {
                        aiReplyText = "I'm Newton Navigator AI. I can guide you to marks, fee statements, bus routes, or school notices."
                        routeTarget = "/Dashboard"
                        actionLabelText = "Go to Dashboard"
                    }
                }
            } else if (routeTarget != null) {
                actionLabelText = "Open Section"
            }

            isThinking = false
            val chatReply = ChatMessage(aiReplyText, false, routeTarget, actionLabelText)
            messages.add(chatReply)
            onSpeak(aiReplyText)
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .width(340.dp)
            .height(440.dp)
            .shadow(16.dp, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NewtonNavy)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.newton_navigator),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = Color(0xFF10B981),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Gemini AI",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleTts) {
                        Icon(
                            imageVector = if (ttsEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                            contentDescription = "Toggle Audio",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(NewtonSlateBg)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { m ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (m.isUser) Alignment.End else Alignment.Start
                    ) {
                        Surface(
                            color = if (m.isUser) NewtonNavy else Color.White,
                            shape = RoundedCornerShape(10.dp),
                            border = if (m.isUser) null else BorderStroke(1.dp, NewtonBorder)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(m.text, color = if (m.isUser) Color.White else NewtonTextDark, fontSize = 13.sp)
                                if (m.suggestedRoute != null && m.actionLabel != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Button(
                                        onClick = { onNavigate(m.suggestedRoute) },
                                        colors = ButtonDefaults.buttonColors(containerColor = NewtonTealPrimary),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text(m.actionLabel, fontSize = 11.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }

                if (isThinking) {
                    item {
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, NewtonBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    color = NewtonTealPrimary,
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Gemini AI is processing...", fontSize = 12.sp, color = NewtonTextMuted)
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("Ask navigator...", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                )
                IconButton(
                    onClick = { handleSend(input) },
                    enabled = !isThinking && input.isNotBlank()
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = NewtonNavy)
                }
            }
        }
    }
}
