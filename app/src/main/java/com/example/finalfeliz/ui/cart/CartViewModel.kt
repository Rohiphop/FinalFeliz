package com.example.finalfeliz.ui.cart

import androidx.lifecycle.ViewModel
import com.example.finalfeliz.data.cart.CartRepository
import com.example.finalfeliz.data.cart.InMemoryCartRepository
import com.example.finalfeliz.domain.model.CartItem
import com.example.finalfeliz.domain.model.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val itemCount: Int = 0,
    val subtotalClp: Long = 0L,
    val totalClp: Long = 0L // akí podría sumar delivery, descuentos, etc.
)

class CartViewModel(
    private val repo: CartRepository = InMemoryCartRepository()
) : ViewModel() {

    val uiState: StateFlow<CartUiState> =
        repo.items
            .map { list ->
                val subtotal = list.sumOf { it.lineTotal }
                CartUiState(
                    items = list,
                    itemCount = list.sumOf { it.quantity },
                    subtotalClp = subtotal,
                    totalClp = subtotal
                )
            }
            .stateIn(
                scope = CoroutineScope(Dispatchers.Default),
                started = SharingStarted.Eagerly,
                initialValue = CartUiState()
            )

    fun add(product: Product, qty: Int = 1) = repo.add(product, qty)
    fun inc(productId: String) {
        val item = uiState.value.items.firstOrNull { it.product.id == productId } ?: return
        repo.updateQuantity(productId, item.quantity + 1)
    }
    fun dec(productId: String) {
        val item = uiState.value.items.firstOrNull { it.product.id == productId } ?: return
        repo.updateQuantity(productId, item.quantity - 1)
    }
    fun remove(productId: String) = repo.remove(productId)
    fun clear() = repo.clear()
}
