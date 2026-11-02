package com.example.nhsentranet.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhsentranet.model.LocationDto
import com.example.nhsentranet.network.AzureApiClient
import com.example.nhsentranet.ui.theme.WebCharcoalText
import com.example.nhsentranet.ui.theme.WebDivider
import com.example.nhsentranet.ui.theme.WebSlateIcon
import com.example.nhsentranet.ui.theme.WebTealBrand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AzureMapsView() {
    val context = LocalContext.current
    var locations by remember { mutableStateOf<List<LocationDto>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            val res = withContext(Dispatchers.IO) { AzureApiClient.api.getLocations() }
            if (res.isSuccessful && res.body()?.data != null) locations = res.body()!!.data!!
        } catch (_: Exception) {}
        if (locations.isEmpty()) {
            locations = listOf(
                LocationDto(1, "Main Parking", "CampusPoint", "https://maps.google.com/?q=-26.123,28.456"),
                LocationDto(2, "Drop-off/Pick-up Zone", "CampusPoint", "https://maps.google.com/?q=-26.124,28.457"),
                LocationDto(3, "Sports Fields", "CampusPoint", "https://maps.google.com/?q=-26.125,28.458"),
                LocationDto(4, "Prep School", "CampusPoint", "https://maps.google.com/?q=-26.126,28.459")
            )
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(locations) { loc ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, WebDivider),
                modifier = Modifier.fillMaxWidth().clickable {
                    if (!loc.mapURL.isNullOrBlank()) {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(loc.mapURL)))
                    }
                }
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = WebTealBrand)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(loc.name, fontWeight = FontWeight.Bold, color = WebCharcoalText)
                        Text(loc.description ?: loc.locationType ?: "Campus Facility", fontSize = 12.sp, color = WebSlateIcon)
                    }
                }
            }
        }
    }
}
