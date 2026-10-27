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
import com.example.nhsentranet.ui.theme.NewtonBorder
import com.example.nhsentranet.ui.theme.NewtonNavy
import com.example.nhsentranet.ui.theme.NewtonSlateBg
import com.example.nhsentranet.ui.theme.NewtonTealPrimary
import com.example.nhsentranet.ui.theme.NewtonTextDark
import kotlinx.coroutines.launch

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
    val messages = remember { mutableStateListOf(ChatMessage("Hi ${session.fullName}! Ask me for marks, fees, notices, or forms.", false)) }

    fun handleSend(query: String) {
        val q = query.trim()
        if (q.isEmpty()) return
        messages.add(ChatMessage(q, true))
        input = ""
        val lower = q.lowercase()
        val reply = when {
            lower.contains("mark") -> ChatMessage("Opening Academic Centre.", false, "academic", "Open Academic")
            lower.contains("fee") || lower.contains("proof") -> ChatMessage("Viewing Invoices.", false, "invoices", "View Invoices")
            lower.contains("map") -> ChatMessage("Opening Campus Maps.", false, "maps", "Campus Maps")
            else -> ChatMessage("I can help you navigate.", false, "dashboard", "Dashboard")
        }
        messages.add(reply)
        onSpeak(reply.text)
        scope.launch { listState.animateScrollToItem(messages.size - 1) }
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
                Text(
                    text = stringResource(R.string.newton_navigator),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
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
                IconButton(onClick = { handleSend(input) }) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = NewtonNavy)
                }
            }
        }
    }
}
