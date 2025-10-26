package com.example.finalfeliz.ui.cart

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.example.finalfeliz.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartIconWithBadge(count: Int, onClick: () -> Unit) {
    BadgedBox(
        badge = {
            if (count > 0) {
                Badge { Text(count.toString()) }
            }
        }
    ) {
        IconButton(onClick = onClick) {
            Icon(
                painter = painterResource(id = R.drawable.ic_cart), // agrega un vector asset
                contentDescription = "Carrito"
            )
        }
    }
}
