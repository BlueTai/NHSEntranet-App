package com.example.nhsentranet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
fun GalleryScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(stringResource(R.string.media_gallery), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NewtonNavy)
        Spacer(modifier = Modifier.height(12.dp))

        SecondaryTabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Photos") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Videos") })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Podcasts") })
        }

        Spacer(modifier = Modifier.height(16.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(2), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            when (selectedTab) {
                0 -> {
                    items(4) {
                        Box(modifier = Modifier.height(120.dp).clip(RoundedCornerShape(8.dp)).background(NewtonBorder), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = NewtonTextMuted)
                        }
                    }
                }
                1 -> {
                    items(listOf("Production Trailer: Grease", "Inter-House Athletics Highlights")) { title ->
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column {
                                Box(modifier = Modifier.height(100.dp).fillMaxWidth().background(Color.Black), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.PlayCircleOutline, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                                }
                                Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
                            }
                        }
                    }
                }
                2 -> {
                    items(listOf("Student Wellness Ep 1", "Matric Advice Ep 2")) { title ->
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Headphones, contentDescription = null, tint = NewtonTealPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
