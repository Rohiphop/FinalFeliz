package com.example.finalfeliz.data

import kotlinx.coroutines.flow.Flow

class ProductRepository(
    private val dao: ProductDao
) {
    // Flow que emite tiempo real desde Room
    val productsFlow: Flow<List<Product>> = dao.getAll()

    suspend fun add(name: String, material: String, priceClp: Long, imageRes: Int?, desc: String?) {
        dao.insert(Product(name = name, material = material, priceClp = priceClp, imageRes = imageRes, description = desc))
    }

    suspend fun update(product: Product) = dao.update(product)
    suspend fun delete(product: Product) = dao.delete(product)
    suspend fun findById(id: Long) = dao.findById(id)
    suspend fun count(): Int = dao.count()
}
