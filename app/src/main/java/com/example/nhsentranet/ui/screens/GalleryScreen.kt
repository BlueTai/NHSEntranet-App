package com.example.nhsentranet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(stringResource(R.string.media_gallery), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NewtonNavy)
        }

        items(mediaList) { m ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(8.dp))
            ) {
                Column {
                    if (m.type == "Photo" || m.type == "Image") {
                        AsyncImage(
                            model = m.url,
                            contentDescription = m.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().height(180.dp).background(NewtonSlateBg)
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(140.dp).background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PlayCircleOutline, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                        }
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(m.title, fontWeight = FontWeight.Bold, color = NewtonNavy, fontSize = 16.sp)
                        Text("Type: ${m.type}", fontSize = 12.sp, color = NewtonTextMuted)
                    }
                }
            }
        }
    }
}
