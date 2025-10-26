// app/src/main/java/com/example/finalfeliz/viewmodel/ProductVMFactory.kt
package com.example.finalfeliz.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.finalfeliz.di.ServiceLocator
import com.example.finalfeliz.data.ProductRepository

class ProductVMFactory(
    private val appContext: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repo: ProductRepository = ServiceLocator.provideProductRepository(appContext)
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            return ProductViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
