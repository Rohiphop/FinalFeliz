package com.example.finalfeliz.data.cart

import com.example.finalfeliz.domain.model.CartItem
import com.example.finalfeliz.domain.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class InMemoryCartRepository : CartRepository {
    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    override val items: StateFlow<List<CartItem>> = _items

    override fun add(product: Product, qty: Int) {
        val current = _items.value.toMutableList()
        val idx = current.indexOfFirst { it.product.id == product.id }
        if (idx >= 0) {
            val existing = current[idx]
            current[idx] = existing.copy(quantity = existing.quantity + qty)
        } else {
            current += CartItem(product, qty)
        }
        _items.value = current
    }

    override fun updateQuantity(productId: String, qty: Int) {
        if (qty <= 0) {
            remove(productId)
            return
        }
        _items.value = _items.value.map {
            if (it.product.id == productId) it.copy(quantity = qty) else it
        }
    }

    override fun remove(productId: String) {
        _items.value = _items.value.filterNot { it.product.id == productId }
    }

    override fun clear() {
        _items.value = emptyList()
    }
}
