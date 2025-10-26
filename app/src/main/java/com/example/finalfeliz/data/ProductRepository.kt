package com.example.finalfeliz.data

import kotlinx.coroutines.flow.Flow

class ProductRepository(
    private val dao: ProductDao
) {
    // 🔹 Flow reactivo de todos los productos (Room ya emite cambios en tiempo real)
    val productsFlow: Flow<List<Product>> = dao.getAll()

    // 🔹 Inserta o reemplaza un producto nuevo (más flexible para el AdminScreen)
    suspend fun add(
        name: String,
        material: String,
        priceClp: Long,
        imageRes: Int?,
        desc: String?
    ) {
        val product = Product(
            name = name.trim(),
            material = material.trim(),
            priceClp = priceClp,
            imageRes = imageRes,
            description = desc?.takeIf { it.isNotBlank() }?.trim()
        )
        dao.insert(product)
    }

    // 🔹 Inserta un objeto Product ya armado (por ejemplo al editar)
    suspend fun save(product: Product) = dao.insert(product)

    // 🔹 Actualiza solo campos existentes
    suspend fun update(product: Product) = dao.update(product)

    // 🔹 Elimina producto
    suspend fun delete(product: Product) = dao.delete(product)

    // 🔹 Busca por ID
    suspend fun findById(id: Long) = dao.findById(id)

    // 🔹 Cuenta total de registros
    suspend fun count(): Int = dao.count()

    // 🔹 (Opcional) Sembrado inicial para desarrollo o primera carga
    suspend fun ensureSeeded() {
        if (dao.count() == 0) {
            dao.insertAll(
                listOf(
                    Product(
                        name = "Clásico Roble",
                        material = "Roble",
                        priceClp = 349000,
                        description = "Madera de roble con terminación mate."
                    ),
                    Product(
                        name = "Elegance Blanco",
                        material = "MDF",
                        priceClp = 459000,
                        description = "Acabado blanco perla, interior acolchado."
                    ),
                    Product(
                        name = "Verde Esperanza",
                        material = "Madera natural",
                        priceClp = 399000,
                        description = "Tono verde sobrio con detalles metálicos."
                    )
                )
            )
        }
    }
}
