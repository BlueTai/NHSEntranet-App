package com.example.nhsentranet.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhsentranet.R
import com.example.nhsentranet.ui.theme.WebCharcoalText
import com.example.nhsentranet.ui.theme.WebHeaderCyan
import com.example.nhsentranet.ui.theme.WebTealBrand

@Composable
fun NewtonHouseBrandHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(WebHeaderCyan)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Newton House Logo image resource
        Image(
            painter = painterResource(id = R.drawable.newton_house_logo),
            contentDescription = "Newton House Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = "Newton House",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = WebCharcoalText
            )
            Text(
                text = "S T U D E N T   I N T R A N E T",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = WebTealBrand,
                letterSpacing = 1.2.sp
            )
        }
    }
}
