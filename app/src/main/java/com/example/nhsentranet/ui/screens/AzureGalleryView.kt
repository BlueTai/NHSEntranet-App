package com.example.nhsentranet.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhsentranet.model.MediaDto
import com.example.nhsentranet.network.AzureApiClient
import com.example.nhsentranet.ui.theme.WebCharcoalText
import com.example.nhsentranet.ui.theme.WebDivider
import com.example.nhsentranet.ui.theme.WebSlateIcon
import com.example.nhsentranet.ui.theme.WebTealBrand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AzureGalleryView() {
    var mediaList by remember { mutableStateOf<List<MediaDto>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            val res = withContext(Dispatchers.IO) { AzureApiClient.api.getMedia() }
            if (res.isSuccessful && res.body()?.data != null) mediaList = res.body()!!.data!!
        } catch (_: Exception) {}
        if (mediaList.isEmpty()) {
            mediaList = listOf(
                MediaDto(1, "/upload/gallery/21638e01-f735-438a-8225-1aadefb63165_.jpg", "Image", "Campus Highlights"),
                MediaDto(2, "/upload/gallery/82476dff-6e54-49b3-b17f-07fd92505d5c_.jpg", "Image", "Sports & Athletics")
            )
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(mediaList) { m ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, WebDivider), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Image, contentDescription = null, tint = WebTealBrand, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(m.description ?: "School Media", fontWeight = FontWeight.Bold, color = WebCharcoalText)
                        Text("Type: ${m.mediaType} • Path: ${m.fileURL}", fontSize = 11.sp, color = WebSlateIcon)
                    }
                }
            }
        }
    }
}
