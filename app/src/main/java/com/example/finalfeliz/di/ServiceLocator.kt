package com.example.finalfeliz.di

import android.content.Context
import com.example.finalfeliz.data.AppDatabase
import com.example.finalfeliz.data.ProductRepository

object ServiceLocator {
    @Volatile private var productRepository: ProductRepository? = null

    fun provideProductRepository(context: Context): ProductRepository =
        productRepository ?: synchronized(this) {
            val db = AppDatabase.get(context)      // usa tu AppDatabase.get(context)
            val repo = ProductRepository(db.productDao())
            productRepository = repo
            repo
        }
}
