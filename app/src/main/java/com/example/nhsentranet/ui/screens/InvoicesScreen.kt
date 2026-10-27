package com.example.nhsentranet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhsentranet.R
import com.example.nhsentranet.ui.theme.*
import com.example.nhsentranet.ui.viewmodels.InvoicesViewModel

@Composable
fun InvoicesScreen(
    studentId: Int,
    invoicesViewModel: InvoicesViewModel
) {
    val invoices by invoicesViewModel.invoices.collectAsState()
    val proofMsg by invoicesViewModel.proofMsg.collectAsState()

    LaunchedEffect(studentId) {
        invoicesViewModel.loadInvoices(studentId)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(stringResource(R.string.invoices_and_fees), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NewtonNavy)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(invoices) { inv ->
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("${inv.invoiceNumber} • ${inv.feeTypeName}", fontWeight = FontWeight.Bold, color = NewtonNavy)
                            Text(inv.statusName, color = NewtonAmber, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Total: R${inv.amount.toInt()} | Paid: R${inv.amountPaid.toInt()} | Bal: R${inv.balance.toInt()}", fontSize = 13.sp)
                        Text("Due Date: ${inv.dueDate}", fontSize = 11.sp, color = NewtonTextMuted)

                        if (inv.balance > 0) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = { invoicesViewModel.uploadProofOfPayment(inv.invoiceId) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.UploadFile, contentDescription = null, tint = NewtonTealPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Upload Proof of Payment", color = NewtonTealPrimary)
                            }
                            if (proofMsg != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(proofMsg!!, color = NewtonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
