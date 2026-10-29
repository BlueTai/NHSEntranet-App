package com.example.nhsentranet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayCircleOutline
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
import com.example.nhsentranet.data.NewtonFirebaseRepository
import com.example.nhsentranet.model.MediaDto
import com.example.nhsentranet.ui.theme.*

@Composable
fun GalleryScreen() {
    var mediaList by remember { mutableStateOf<List<MediaDto>>(emptyList()) }
    LaunchedEffect(Unit) {
        mediaList = NewtonFirebaseRepository.getInstance().getMedia()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(stringResource(R.string.media_gallery), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NewtonNavy)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(mediaList) { m ->
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (m.type == "Photo") Icons.Default.Image else Icons.Default.PlayCircleOutline, contentDescription = null, tint = NewtonTealPrimary, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(m.title, fontWeight = FontWeight.Bold, color = NewtonNavy)
                            Text("Source: Azure Blob Storage • ${m.type}", fontSize = 11.sp, color = NewtonTextMuted)
                        }
                    }
                }
            }
        }
    }
}
