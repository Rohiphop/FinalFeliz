package com.example.finalfeliz.ui.cart

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.finalfeliz.core.clp
import com.example.finalfeliz.domain.model.CartItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    state: CartUiState,
    onInc: (String) -> Unit,
    onDec: (String) -> Unit,
    onRemove: (String) -> Unit,
    onCheckout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Tu carrito") })
        },
        bottomBar = {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Subtotal", fontWeight = FontWeight.Medium)
                    Text(clp(state.subtotalClp), fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onCheckout,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.items.isNotEmpty()
                ) { Text("Finalizar Compra  (${clp(state.totalClp)})") }
            }
        }
    ) { inner ->
        if (state.items.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(inner), contentAlignment = Alignment.Center) {
                Text("Tu carrito está vacío :(")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(inner),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.items, key = { it.product.id }) { item ->
                    CartItemRow(
                        item = item,
                        onInc = { onInc(item.product.id) },
                        onDec = { onDec(item.product.id) },
                        onRemove = { onRemove(item.product.id) }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun CartItemRow(
    item: CartItem,
    onInc: () -> Unit,
    onDec: () -> Unit,
    onRemove: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = rememberAsyncImagePainter(item.product.imageUrl),
                contentDescription = item.product.name,
                modifier = Modifier.size(64.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.product.name, fontWeight = FontWeight.SemiBold)
                Text(clp(item.product.priceClp))
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalButton(onClick = onDec) { Text("-") }
                    Text(
                        text = item.quantity.toString(),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    FilledTonalButton(onClick = onInc) { Text("+") }
                    Spacer(Modifier.width(12.dp))
                    Text("= ${clp(item.lineTotal)}")
                }
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
            }
        }
    }
}
