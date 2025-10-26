package com.example.finalfeliz.domain.mappers

import com.example.finalfeliz.data.Product as DbProduct
import com.example.finalfeliz.domain.model.Product as DomainProduct

// Esta función transforma un "data.Product" (del catálogo)
// en un "domain.model.Product" (del carrito)
fun DbProduct.toDomain(): DomainProduct =
    DomainProduct(
        id = id.toString(),
        name = name,
        priceClp = priceClp.toLong(),
        imageUrl = null //el modelo usa imageRes (Int), no URL
    )
