package com.example.nhsentranet.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhsentranet.R
import com.example.nhsentranet.model.UserRoles
import com.example.nhsentranet.model.UserSession
import com.example.nhsentranet.ui.theme.*
import com.example.nhsentranet.ui.viewmodels.NoticesViewModel

@Composable
fun NoticesScreen(
    session: UserSession,
    noticesViewModel: NoticesViewModel
) {
    val notices by noticesViewModel.notices.collectAsState()
    val isLoading by noticesViewModel.isLoading.collectAsState()
    val selectedCategory by noticesViewModel.selectedCategory.collectAsState()
    val isPosting by noticesViewModel.isPosting.collectAsState()
    val postFeedback by noticesViewModel.postFeedback.collectAsState()

    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }

    val categories = listOf("All", "Academic", "Urgent", "General")

    val canPublish = session.role == UserRoles.Admin ||
            session.role == UserRoles.Teacher ||
            session.role == UserRoles.HeadOfDepartment ||
            session.role.contains("Head", ignoreCase = true)

    LaunchedEffect(Unit) {
        noticesViewModel.loadNotices()
    }

    val filteredNotices = notices.filter { n ->
        selectedCategory == "All" || n.noticeType.equals(selectedCategory, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.notice_board),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = NewtonNavy
            )
            Text(
                text = "Official announcements, academic schedules & urgent alerts",
                fontSize = 12.sp,
                color = NewtonTextMuted
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { noticesViewModel.selectCategory(cat) },
                        label = { Text(cat, fontSize = 12.sp) }
                    )
                }
            }
        }

        if (canPublish) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, NewtonBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Campaign, contentDescription = null, tint = NewtonTealPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Publish School Announcement", fontWeight = FontWeight.Bold, color = NewtonNavy, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it; noticesViewModel.clearFeedback() },
                            label = { Text("Notice Title") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = desc,
                            onValueChange = { desc = it; noticesViewModel.clearFeedback() },
                            label = { Text("Announcement Details") },
                            modifier = Modifier.fillMaxWidth().height(90.dp),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("General", "Academic", "Urgent").forEach { type ->
                                    FilterChip(
                                        selected = category == type,
                                        onClick = { category = type },
                                        label = { Text(type, fontSize = 11.sp) }
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    if (title.isNotBlank()) {
                                        noticesViewModel.postNotice(title, desc, session.fullName.ifEmpty { session.username })
                                        title = ""; desc = ""
                                    }
                                },
                                enabled = !isPosting && title.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = NewtonTealPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                if (isPosting) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else {
                                    Text(stringResource(R.string.publish), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (postFeedback != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(postFeedback!!, color = NewtonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = NewtonTealPrimary, modifier = Modifier.size(32.dp))
                }
            }
        } else if (filteredNotices.isEmpty()) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No notices posted under $selectedCategory yet.", color = NewtonTextMuted, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(filteredNotices) { n ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, NewtonBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = n.noticeName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = NewtonNavy,
                                modifier = Modifier.weight(1f)
                            )

                            Surface(
                                color = when (n.noticeType.lowercase()) {
                                    "urgent" -> Color(0xFFFEF2F2)
                                    "academic" -> Color(0xFFEFF6FF)
                                    else -> Color(0xFFF1F5F9)
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = n.noticeType,
                                    color = when (n.noticeType.lowercase()) {
                                        "urgent" -> NewtonRed
                                        "academic" -> NewtonBlue
                                        else -> NewtonTealPrimary
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(n.description, fontSize = 13.sp, color = NewtonTextDark, lineHeight = 18.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Posted by ${n.staffName} • ${n.datePosted}", fontSize = 11.sp, color = NewtonTextMuted)
                            if (n.isPinned) {
                                Icon(Icons.Default.PushPin, contentDescription = "Pinned", tint = NewtonAmber, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
