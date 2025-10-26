package com.example.finalfeliz.ui.cart

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border // CAMBIO: import para el borde
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.finalfeliz.R
import com.example.finalfeliz.core.clp
import com.example.finalfeliz.domain.model.CartItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    state: CartUiState,
    onInc: (String) -> Unit,
    onDec: (String) -> Unit,
    onRemove: (String) -> Unit,
    onCheckout: () -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        // fondo es imagen + blur + overlay oscuro :)
        Image(
            painter = painterResource(id = R.drawable.fondo_cementerio),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().blur(20.dp)
        )
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)))

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Tu carrito", color = Color.White) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            },
            bottomBar = {

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = Color.Black.copy(alpha = 0.25f)
                    )
                ) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal", fontWeight = FontWeight.Medium, color = Color.White)
                            Text(clp(state.subtotalClp), fontWeight = FontWeight.Medium, color = Color.White)
                        }
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = onCheckout,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = state.items.isNotEmpty()
                        ) {
                            Text("Confirmar pedido  (${clp(state.totalClp)})")
                        }
                    }
                }
            }
        ) { inner ->
            if (state.items.isEmpty()) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(inner),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Tu carrito está vacío :(", color = Color.White)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(inner)
                        .padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.items, key = { it.product.id }) { item ->
                        CartItemRow(
                            item = item,
                            onInc = { onInc(item.product.id) },
                            onDec = { onDec(item.product.id) },
                            onRemove = { onRemove(item.product.id) }
                        )
                    }
                }
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

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 0.1.dp,
                color = Color.White.copy(alpha = 0f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(5.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.Black.copy(alpha = 0.25f)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp) // 👈 sombra más sutil
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {

            if (item.product.imageUrl != null) {
                Image(
                    painter = rememberAsyncImagePainter(item.product.imageUrl),
                    contentDescription = item.product.name,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(item.product.name, fontWeight = FontWeight.SemiBold, color = Color.White)
                Text(clp(item.product.priceClp), color = Color.White.copy(alpha = 0.9f))
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalButton(onClick = onDec) { Text("-") }
                    Text(
                        text = item.quantity.toString(),
                        modifier = Modifier.padding(horizontal = 12.dp),
                        color = Color.White
                    )
                    FilledTonalButton(onClick = onInc) { Text("+") }
                    Spacer(Modifier.width(12.dp))
                    Text("= ${clp(item.lineTotal)}", color = Color.White)
                }
            }

            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.White)
            }
        }
    }
}
