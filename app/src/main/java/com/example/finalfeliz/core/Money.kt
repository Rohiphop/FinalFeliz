package com.example.finalfeliz.core

import java.text.NumberFormat
import java.util.Locale

// Función para formatear CLP sin decimales, ejemplo: $149.990
fun clp(amount: Long): String {
    val nf = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
    return nf.format(amount)
}
