package com.example.finalfeliz.data.cart

import com.example.finalfeliz.domain.model.CartItem
import com.example.finalfeliz.domain.model.Product
import kotlinx.coroutines.flow.StateFlow

interface CartRepository {
    val items: StateFlow<List<CartItem>>

    fun add(product: Product, qty: Int = 1)
    fun updateQuantity(productId: String, qty: Int)
    fun remove(productId: String)
    fun clear()
}
