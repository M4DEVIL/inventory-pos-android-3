package com.example.inventorypos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InventoryPosApp()
        }
    }
}

@Composable
fun InventoryPosApp(viewModel: MainViewModel = viewModel()) {
    val cashRegistry by viewModel.cashRegistry.collectAsState()
    var barcode by remember { mutableStateOf("") }
    var productName by remember { mutableStateOf("") }
    var buyingPrice by remember { mutableStateOf("") }
    var sellingPrice by remember { mutableStateOf("") }
    var margin by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    val scanner = GmsBarcodeScanning.getClient(LocalContext.current)

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Inventory & POS App", style = MaterialTheme.typography.headlineMedium)

        Text("Add Product", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(value = barcode, onValueChange = { barcode = it }, label = { Text("Barcode") })
        Button(onClick = { viewModel.scanBarcode(scanner) { barcode = it } }) {
            Text("Scan Barcode")
        }
        OutlinedTextField(value = productName, onValueChange = { productName = it }, label = { Text("Name") })
        OutlinedTextField(value = buyingPrice, onValueChange = { buyingPrice = it }, label = { Text("Buying Price") })
        OutlinedTextField(value = sellingPrice, onValueChange = { sellingPrice = it }, label = { Text("Selling Price") })
        OutlinedTextField(value = margin, onValueChange = { margin = it }, label = { Text("Margin (%)") })
        OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Stock") })
        Button(onClick = {
            viewModel.addProduct(
                barcode,
                productName,
                buyingPrice.toDoubleOrNull() ?: 0.0,
                sellingPrice.toDoubleOrNull(),
                margin.toDoubleOrNull(),
                stock.toIntOrNull() ?: 0
            )
        }) {
            Text("Add Product")
        }

        Text("POS", style = MaterialTheme.typography.titleMedium)
        Text("Cash Registry: $$cashRegistry")
        Button(onClick = { viewModel.scanBarcode(scanner) { barcode = it; viewModel.sellProduct(barcode, 1) } }) {
            Text("Sell (Scan Barcode)")
        }

        Text("Daily Profit: $${viewModel.getDailyProfit(LocalDate.now())}")
    }
}