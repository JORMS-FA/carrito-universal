package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val title: String,
    val url: String,
    val brand: String,
    val imageUrl: String,
    val price: Double,
    val currency: String,
    val category: String,
    val priority: String, // "Alta", "Media", "Baja"
    val status: String, // "Por revisar", "Prioritario", "En espera", "Comprado", "Descartado"
    val reminderDate: Long?,
    val rating: Float, // Rated 0.0 - 5.0
    val summaryMd: String, // Generated Markdown
    val notes: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val sourceStore: String,
    val comparisonGroupId: String?,
    
    // Additional comparisons attributes
    val quality: Int = 3, // Rating 1 to 5
    val pros: String = "",
    val contras: String = "",
    val score: Int = 70, // 1 to 100 overall score

    // Price Tracker and Alerts (One UI 8.5 concept feature)
    val priceAlertEnabled: Boolean = false,
    val priceAlertThreshold: Double = 0.0,
    val priceHistory: String = "" // Formato "timestamp:price,timestamp:price"
)
