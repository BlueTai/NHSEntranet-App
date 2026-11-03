package com.example.nhsentranet.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhsentranet.model.DashboardStatsDto
import com.example.nhsentranet.model.DashboardTimetableDto
import com.example.nhsentranet.model.LinkedChildDto
import com.example.nhsentranet.model.UpcomingAssessmentDto
import com.example.nhsentranet.model.UserSession
import com.example.nhsentranet.network.AzureApiClient
import com.example.nhsentranet.ui.screens.mainSidebarItems
import com.example.nhsentranet.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AzureDashboardView(
    session: UserSession,
    selectedChild: LinkedChildDto,
    onSelectChild: (LinkedChildDto) -> Unit,
    onNavigate: (String) -> Unit
) {
    var stats by remember { mutableStateOf<DashboardStatsDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(session.userId, selectedChild.studentId) {
        isLoading = true
        try {
            val res = withContext(Dispatchers.IO) { AzureApiClient.api.getDashboardStats() }
            if (res.isSuccessful && res.body()?.data != null) {
                stats = res.body()!!.data
            }
        } catch (_: Exception) {}
        if (stats == null) {
            stats = DashboardStatsDto(
                fullName = session.fullName,
                userRole = session.userRole,
                totalUsers = 48, students = 30, parents = 25, pendingApprovals = 3,
                attendanceRate = 96.4, classesToday = 5, pendingMarking = 3,
                upcomingAssessments = listOf(UpcomingAssessmentDto("Mathematics Test 1", "Mathematics", "2026-11-10")),
                timetable = listOf(
                    DashboardTimetableDto("08:00 - 08:50", "Mathematics", "Jane Smith"),
                    DashboardTimetableDto("09:00 - 09:50", "Physical Sciences", "Dr. Adams")
                )
            )
        }
        isLoading = false
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. WELCOME BANNER & CHILD SELECTOR ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, WebDivider),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp).fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Welcome back, ${session.fullName}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Role: ${session.userRole}", fontSize = 12.sp, color = WebTealBrand, fontWeight = FontWeight.Medium)
                        }

                        if (session.linkedChildren.isNotEmpty()) {
                            Surface(
                                color = WebActiveItemBg,
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, WebTealBrand)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Outlined.School, contentDescription = null, tint = WebTealBrand, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        selectedChild.fullName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WebTealBrand
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isLoading) {
            item { LinearProgressIndicator(color = WebTealBrand, modifier = Modifier.fillMaxWidth()) }
        }

        // --- 2. KPI METRICS CARDS GRID ---
        item {
            val s = stats
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    MetricKpiCard(
                        title = "Attendance Rate",
                        value = "${s?.attendanceRate ?: 96.4}%",
                        subtitle = "Target: 90.0%",
                        badgeText = "Excellent",
                        badgeColor = NewtonGreen,
                        icon = Icons.Outlined.CheckCircle,
                        modifier = Modifier.weight(1f)
                    )
                    MetricKpiCard(
                        title = "Academic Average",
                        value = "81.5%",
                        subtitle = "Term 1 Aggregate",
                        badgeText = "+3.2%",
                        badgeColor = WebTealBrand,
                        icon = Icons.AutoMirrored.Outlined.TrendingUp,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    MetricKpiCard(
                        title = "Total Students",
                        value = "${s?.students ?: 42}",
                        subtitle = "Enrolled Pupil Roster",
                        badgeText = "Active",
                        badgeColor = WebTealBrand,
                        icon = Icons.Outlined.PeopleOutline,
                        modifier = Modifier.weight(1f)
                    )
                    MetricKpiCard(
                        title = "Pending Consents",
                        value = "2 Forms",
                        subtitle = "Action Required",
                        badgeText = "Pending",
                        badgeColor = NewtonAmber,
                        icon = Icons.Outlined.AssignmentLate,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("/Forms") }
                    )
                }
            }
        }

        // --- 3. GRAPH 1: WEEKLY ATTENDANCE TREND (CANVAS BAR CHART) ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, WebDivider),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp).fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Weekly Attendance Trend", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = WebCharcoalText)
                            Text("Mon – Fri Attendance Percentages", fontSize = 12.sp, color = WebSlateIcon)
                        }
                        Surface(color = WebActiveItemBg, shape = RoundedCornerShape(8.dp)) {
                            Text("Avg 97%", color = WebTealBrand, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Canvas Bar Chart
                    val dayData = listOf("Mon" to 0.98f, "Tue" to 0.95f, "Wed" to 1.0f, "Thu" to 0.94f, "Fri" to 0.97f)
                    val barColor = WebTealBrand

                    Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height
                            val bottomLabelSpace = 30.dp.toPx()
                            val topPadding = 20.dp.toPx()
                            val chartHeight = canvasHeight - bottomLabelSpace - topPadding

                            // Target 90% Line
                            val targetY = topPadding + chartHeight * (1f - 0.90f)
                            drawLine(
                                color = Color.LightGray,
                                start = Offset(0f, targetY),
                                end = Offset(canvasWidth, targetY),
                                strokeWidth = 2f
                            )

                            // Bars
                            val barWidth = 28.dp.toPx()
                            val numBars = dayData.size
                            val spacing = (canvasWidth - (barWidth * numBars)) / (numBars + 1)

                            dayData.forEachIndexed { index, pair ->
                                val x = spacing + index * (barWidth + spacing)
                                val barHeight = chartHeight * pair.second
                                val y = topPadding + (chartHeight - barHeight)

                                drawRoundRect(
                                    color = barColor,
                                    topLeft = Offset(x, y),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = CornerRadius(8f, 8f)
                                )
                            }
                        }

                        // Labels overlay below chart
                        Row(
                            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            dayData.forEach { pair ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${(pair.second * 100).toInt()}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = WebTealBrand)
                                    Text(pair.first, fontSize = 11.sp, color = WebSlateIcon)
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 4. GRAPH 2: ACADEMIC PERFORMANCE LINE GRAPH (CANVAS) ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, WebDivider),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp).fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Term Mark Progression", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = WebCharcoalText)
                            Text("Overall Average per Term", fontSize = 12.sp, color = WebSlateIcon)
                        }
                        Text("Current: 81.5%", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = WebTealBrand)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val termPoints = listOf("Term 1" to 76f, "Term 2" to 78.5f, "Term 3" to 80f, "Term 4" to 81.5f)

                    Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height - 30.dp.toPx()
                            val padX = 30.dp.toPx()
                            val usableWidth = w - (padX * 2)

                            val minVal = 60f
                            val maxVal = 100f

                            val points = termPoints.mapIndexed { i, pair ->
                                val x = padX + i * (usableWidth / (termPoints.size - 1))
                                val y = h - ((pair.second - minVal) / (maxVal - minVal)) * h
                                Offset(x, y)
                            }

                            // Draw gradient background path
                            val fillPath = Path().apply {
                                moveTo(points.first().x, h)
                                points.forEach { lineTo(it.x, it.y) }
                                lineTo(points.last().x, h)
                                close()
                            }

                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(WebTealBrand.copy(alpha = 0.25f), Color.Transparent)
                                )
                            )

                            // Draw line path
                            val strokePath = Path().apply {
                                moveTo(points.first().x, points.first().y)
                                points.forEach { lineTo(it.x, it.y) }
                            }

                            drawPath(
                                path = strokePath,
                                color = WebTealBrand,
                                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                            )

                            // Draw data dots
                            points.forEach { pt ->
                                drawCircle(color = Color.White, radius = 6.dp.toPx(), center = pt)
                                drawCircle(color = WebTealBrand, radius = 4.dp.toPx(), center = pt)
                            }
                        }

                        // Labels below line graph
                        Row(
                            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            termPoints.forEach { pair ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${pair.second}%", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = WebTealBrand)
                                    Text(pair.first, fontSize = 11.sp, color = WebSlateIcon)
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 5. SUBJECT MARKS BREAKDOWN ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, WebDivider),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp).fillMaxWidth()) {
                    Text("Subject Performance Overview", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = WebCharcoalText)
                    Spacer(modifier = Modifier.height(14.dp))

                    val subjects = listOf(
                        Triple("Mathematics", "85%", WebTealBrand),
                        Triple("Physical Sciences", "78%", Color(0xFF2563EB)),
                        Triple("English Home Lang", "88%", NewtonGreen),
                        Triple("Life Sciences", "82%", NewtonAmber)
                    )

                    subjects.forEach { (sub, mark, col) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(col))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(sub, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = WebCharcoalText)
                            }
                            Surface(color = col.copy(alpha = 0.12f), shape = RoundedCornerShape(6.dp)) {
                                Text(mark, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = col, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                            }
                        }
                        HorizontalDivider(color = WebDivider.copy(alpha = 0.5f))
                    }
                }
            }
        }

        // --- 6. UPCOMING ASSESSMENTS & SCHEDULE ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, WebDivider),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp).fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Upcoming Assessments", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = WebCharcoalText)
                        TextButton(onClick = { onNavigate("/Calendar") }) {
                            Text("View All", color = WebTealBrand, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val upcoming = listOf(
                        Triple("Mathematics Term Test", "10 Nov 2026", "100 Marks"),
                        Triple("Physical Sciences Practical", "14 Nov 2026", "50 Marks")
                    )

                    upcoming.forEach { (test, date, marks) ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = WebPageBg),
                            border = BorderStroke(1.dp, WebDivider),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Event, contentDescription = null, tint = WebTealBrand, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(test, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WebCharcoalText)
                                        Text("Due: $date", fontSize = 11.sp, color = WebSlateIcon)
                                    }
                                }
                                Surface(color = WebActiveItemBg, shape = RoundedCornerShape(6.dp)) {
                                    Text(marks, color = WebTealBrand, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 7. QUICK NAVIGATION TILES ---
        item {
            Text("Quick Navigation", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = WebCharcoalText)
        }

        val quickLinks = mainSidebarItems.filter { it.roles.contains(session.userRole) && it.route != "/Dashboard" }
        items(quickLinks) { item ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, WebDivider),
                modifier = Modifier.fillMaxWidth().clickable { onNavigate(item.route) }
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(WebActiveItemBg), contentAlignment = Alignment.Center) {
                        Icon(item.icon, contentDescription = null, tint = WebTealBrand)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(item.label, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = WebCharcoalText)
                }
            }
        }
    }
}

@Composable
fun MetricKpiCard(
    title: String,
    value: String,
    subtitle: String,
    badgeText: String,
    badgeColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, WebDivider),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(badgeColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(18.dp))
                }
                Surface(color = badgeColor.copy(alpha = 0.12f), shape = RoundedCornerShape(10.dp)) {
                    Text(badgeText, color = badgeColor, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = WebCharcoalText)
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = WebTealBrand)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 10.sp, color = WebSlateIcon)
        }
    }
}
