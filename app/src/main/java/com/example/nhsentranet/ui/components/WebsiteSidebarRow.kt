package com.example.nhsentranet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhsentranet.ui.theme.WebActiveItemBg
import com.example.nhsentranet.ui.theme.WebCharcoalText
import com.example.nhsentranet.ui.theme.WebSlateIcon
import com.example.nhsentranet.ui.theme.WebTealBrand

@Composable
fun WebsiteSidebarRow(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val activeBg = MaterialTheme.colorScheme.surfaceVariant
    val activeBrand = MaterialTheme.colorScheme.primary
    val textDark = MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) activeBg else Color.Transparent)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight(0.65f)
                    .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                    .background(activeBrand)
            )
            Spacer(modifier = Modifier.width(11.dp))
        } else {
            Spacer(modifier = Modifier.width(16.dp))
        }

        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) activeBrand else WebSlateIcon,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) activeBrand else textDark
        )
    }
}
