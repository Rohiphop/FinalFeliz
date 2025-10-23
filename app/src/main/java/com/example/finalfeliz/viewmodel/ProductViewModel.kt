package com.example.finalfeliz.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.finalfeliz.data.AppDatabase
import com.example.finalfeliz.data.Product
import com.example.finalfeliz.data.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// -----------------------------------------------------------
// Representa el estado visual del catálogo o panel de productos
// -----------------------------------------------------------
data class ProductState(
    val loading: Boolean = true,          // indica si los productos están cargando
    val error: String? = null,            // mensaje de error si ocurre alguna excepción
    val products: List<Product> = emptyList() // lista actual de productos en memoria
)

// -----------------------------------------------------------
// ViewModel responsable de manejar los productos
// -----------------------------------------------------------
class ProductViewModel(
    private val repo: ProductRepository   // capa de datos que maneja operaciones en Room
) : ViewModel() {

    // Estado observable por la UI
    private val _state = MutableStateFlow(ProductState())
    val state: StateFlow<ProductState> = _state

    init {
        // Suscripción continua al flujo de productos almacenados en la base de datos
        // Esto permite actualizaciones reactivas en tiempo real cuando se agregan o eliminan productos
        viewModelScope.launch {
            repo.productsFlow
                .catch { e ->
                    // Si ocurre un error en el flujo (por ejemplo, DB inaccesible)
                    _state.value = _state.value.copy(
                        loading = false,
                        error = e.message
                    )
                }
                .collectLatest { list ->
                    // Cuando el flujo emite una nueva lista de productos
                    _state.value = _state.value.copy(
                        loading = false,
                        error = null,
                        products = list
                    )
                }
        }
    }

    // -----------------------------------------------------------
    // Operaciones CRUD sobre los productos
    // -----------------------------------------------------------

    /** Agrega un nuevo producto a la base de datos. */
    fun add(
        name: String,
        material: String,
        priceClp: Long,
        imageRes: Int?,
        desc: String?
    ) = viewModelScope.launch {
        repo.add(name, material, priceClp, imageRes, desc)
    }

    /** Actualiza un producto existente. */
    fun update(p: Product) = viewModelScope.launch {
        repo.update(p)
    }

    /** Elimina un producto de la base de datos. */
    fun delete(p: Product) = viewModelScope.launch {
        repo.delete(p)
    }
}

// -----------------------------------------------------------
// Factory personalizada para construir el ProductViewModel
// -----------------------------------------------------------
class ProductVMFactory(private val appContext: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        // 1. Se obtiene la instancia de la base de datos Room
        val db = AppDatabase.get(appContext)

        // 2. Se crea el repositorio que usa el DAO de productos
        val repo = ProductRepository(db.productDao())

        // 3. Se retorna el ViewModel configurado con su repositorio
        return ProductViewModel(repo) as T
    }
}
