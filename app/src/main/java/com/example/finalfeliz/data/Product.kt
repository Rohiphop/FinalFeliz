package com.example.finalfeliz.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val material: String,
    val priceClp: Long,
    val imageRes: Int? = null,
    val description: String? = null
)
