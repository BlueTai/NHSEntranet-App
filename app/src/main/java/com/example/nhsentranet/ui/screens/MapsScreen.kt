package com.example.nhsentranet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhsentranet.R
import com.example.nhsentranet.ui.theme.*

@Composable
fun MapsScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text(stringResource(R.string.campus_maps), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NewtonNavy)
        Spacer(modifier = Modifier.height(12.dp))

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = NewtonTealPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("118 CR Swart Drive, Boskruin, Randburg", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text("Campus Zones", fontWeight = FontWeight.Bold, color = NewtonNavy)
                Spacer(modifier = Modifier.height(8.dp))

                val zones = listOf(
                    "Parking & Drop-off/Pick-up Zones",
                    "Preparatory Campus",
                    "High School / College Campus",
                    "Sports Fields",
                    "Aftercare Centre",
                    "Main School Hall"
                )
                zones.forEach { zone ->
                    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(NewtonAmber))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(zone, fontSize = 13.sp, color = NewtonTextDark)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { /* Open Google Maps */ }, colors = ButtonDefaults.buttonColors(containerColor = NewtonNavy), modifier = Modifier.fillMaxWidth()) {
                    Text("Open in Google Maps")
                }
            }
        }
    }
}
