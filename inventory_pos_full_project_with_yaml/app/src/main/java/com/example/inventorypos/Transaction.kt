package com.example.inventorypos

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val barcode: String,
    val quantity: Int,
    val totalPrice: Double,
    val timestamp: LocalDateTime,
    val isRefunded: Boolean = false
)