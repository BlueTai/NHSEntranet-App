package com.example.nhsentranet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhsentranet.R
import com.example.nhsentranet.ui.theme.NewtonNavy
import com.example.nhsentranet.ui.theme.NewtonTealPrimary
import com.example.nhsentranet.ui.theme.NewtonTextDark
import com.example.nhsentranet.ui.theme.NewtonTextMuted

@Composable
fun ContactsScreen() {
    val contacts = listOf(
        Triple("Principal", "principal@newtonhouse.co.za", "011 463 5500"),
        Triple("Admissions", "admissions@newtonhouse.co.za", "011 463 5501"),
        Triple("Finance & Accounts", "accounts@newtonhouse.co.za", "011 463 5502"),
        Triple("Student Support", "support@newtonhouse.co.za", "011 463 5503"),
        Triple("Estate Manager", "estate@newtonhouse.co.za", "011 463 5504"),
        Triple("Transport Desk", "transport@newtonhouse.co.za", "011 463 5505")
    )

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(stringResource(R.string.contacts_directory), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NewtonNavy)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(contacts) { (role, email, phone) ->
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ContactPhone, contentDescription = null, tint = NewtonTealPrimary, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(role, fontWeight = FontWeight.Bold, color = NewtonNavy, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Email: $email", fontSize = 12.sp, color = NewtonTextDark)
                            Text("Phone: $phone", fontSize = 12.sp, color = NewtonTextMuted)
                        }
                    }
                }
            }
        }
    }
}
