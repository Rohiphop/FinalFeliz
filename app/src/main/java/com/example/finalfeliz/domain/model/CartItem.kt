package com.example.finalfeliz.domain.model

data class CartItem(
    val product: Product,
    val quantity: Int
) {
    val lineTotal: Long get() = product.priceClp * quantity
}
