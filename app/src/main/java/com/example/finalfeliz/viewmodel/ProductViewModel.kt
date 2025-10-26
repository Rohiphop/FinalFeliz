package com.example.finalfeliz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.finalfeliz.data.Product
import com.example.finalfeliz.data.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class ProductState(
    val loading: Boolean = true,
    val error: String? = null,
    val products: List<Product> = emptyList()
)

class ProductViewModel(
    private val repo: ProductRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProductState())
    val state: StateFlow<ProductState> = _state

    init {
        viewModelScope.launch {
            repo.productsFlow
                .catch { e ->
                    _state.value = _state.value.copy(
                        loading = false,
                        error = e.message ?: "Error al cargar productos"
                    )
                }
                .collectLatest { list ->
                    _state.value = _state.value.copy(
                        loading = false,
                        error = null,
                        products = list
                    )
                }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    // --------- CRUD ---------

    fun add(
        name: String,
        material: String,
        priceClp: Long,
        imageRes: Int?,
        desc: String?
    ) = viewModelScope.launch {
        try {
            repo.add(name, material, priceClp, imageRes, desc)
        } catch (e: Exception) {
            _state.value = _state.value.copy(error = e.message ?: "Error al agregar producto")
        }
    }

    fun update(p: Product) = viewModelScope.launch {
        try {
            repo.update(p)
        } catch (e: Exception) {
            _state.value = _state.value.copy(error = e.message ?: "Error al actualizar producto")
        }
    }

    fun delete(p: Product) = viewModelScope.launch {
        try {
            repo.delete(p)
        } catch (e: Exception) {
            _state.value = _state.value.copy(error = e.message ?: "Error al eliminar producto")
        }
    }

    // --------- Utilidades ---------

    fun seedIfNeeded() = viewModelScope.launch {
        try {
            repo.ensureSeeded()
        } catch (e: Exception) {
            _state.value = _state.value.copy(error = e.message ?: "Error al precargar productos")
        }
    }

    suspend fun findById(id: Long): Product? = try {
        repo.findById(id)
    } catch (_: Exception) {
        null
    }
}
