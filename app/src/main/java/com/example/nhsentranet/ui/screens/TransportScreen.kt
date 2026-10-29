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
import com.example.nhsentranet.model.TransportRouteDto
import com.example.nhsentranet.ui.theme.*

@Composable
fun TransportScreen() {
    var routes by remember { mutableStateOf<List<TransportRouteDto>>(emptyList()) }
    LaunchedEffect(Unit) {
        routes = NewtonFirebaseRepository.getInstance().getTransportRoutes()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Live Transport Routes & Status", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NewtonNavy)
            Spacer(modifier = Modifier.height(4.dp))
        }
        items(routes) { route ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(route.routeName, fontWeight = FontWeight.Bold, color = NewtonNavy)
                        Text(
                            text = route.status,
                            color = if (route.status == "On Time") NewtonGreen else NewtonRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Driver: ${route.driverName} (${route.driverPhone})", fontSize = 13.sp, color = NewtonTextDark)
                    Text("ETA: ${route.eta}", fontSize = 12.sp, color = NewtonTextMuted)
                }
            }
        }
    }
}
