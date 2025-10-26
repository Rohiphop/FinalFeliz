package com.example.finalfeliz.ui.components

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun AddToCartButton(onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text("Agregar al carrito")
    }
}
