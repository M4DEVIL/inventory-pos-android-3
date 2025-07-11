package com.example.inventorypos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Room.databaseBuilder(
        application, AppDatabase::class.java, "inventory-pos-db"
    ).build()
    private val productDao = db.productDao()
    private val transactionDao = db.transactionDao()

    private val _cashRegistry = MutableStateFlow(0.0)
    val cashRegistry: StateFlow<Double> = _cashRegistry

    fun addProduct(
        barcode: String, name: String, buyingPrice: Double, sellingPrice: Double?, margin: Double?, stock: Int
    ) {
        viewModelScope.launch {
            val finalSellingPrice = sellingPrice ?: (buyingPrice * (1 + (margin ?: 0.0) / 100))
            val finalMargin = margin ?: ((sellingPrice ?: finalSellingPrice - buyingPrice) / buyingPrice * 100)
            productDao.insert(Product(barcode, name, buyingPrice, finalSellingPrice, finalMargin, stock))
        }
    }

    fun sellProduct(barcode: String, quantity: Int) {
        viewModelScope.launch {
            val product = productDao.getByBarcode(barcode) ?: return@launch
            if (product.stock >= quantity) {
                val totalPrice = product.sellingPrice * quantity
                transactionDao.insert(Transaction(barcode = barcode, quantity = quantity, totalPrice = totalPrice, timestamp = LocalDateTime.now()))
                productDao.update(product.copy(stock = product.stock - quantity))
                _cashRegistry.value += totalPrice
            }
        }
    }

    fun refundTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionDao.update(transaction.copy(isRefunded = true))
            val product = productDao.getByBarcode(transaction.barcode)
            product?.let {
                productDao.update(it.copy(stock = it.stock + transaction.quantity))
                _cashRegistry.value -= transaction.totalPrice
            }
        }
    }

    fun getDailyProfit(date: LocalDate): Double {
        return 0.0
    }

    fun setInitialCashRegistry(amount: Double) {
        _cashRegistry.value = amount
    }

    fun scanBarcode(scanner: GmsBarcodeScanner, onResult: (String) -> Unit) {
        scanner.startScan()
            .addOnSuccessListener { barcode ->
                barcode.rawValue?.let { onResult(it) }
            }
    }
}