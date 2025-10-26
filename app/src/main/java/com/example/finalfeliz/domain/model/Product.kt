package com.example.finalfeliz.domain.model

data class Product(
    val id: String,
    val name: String,
    val priceClp: Long, // CLP sin decimales
    val imageUrl: String? = null
)

///producto que se ve en el carrito