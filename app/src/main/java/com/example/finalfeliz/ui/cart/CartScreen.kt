package com.example.finalfeliz.ui.cart

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.finalfeliz.R
import com.example.finalfeliz.core.clp
import com.example.finalfeliz.domain.model.CartItem
import androidx.compose.runtime.*
import androidx.activity.compose.BackHandler



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    state: CartUiState,
    onInc: (String) -> Unit,
    onDec: (String) -> Unit,
    onRemove: (String) -> Unit,
    onCheckout: () -> Unit,
    onBack: () -> Unit,
    onGoHome: () -> Unit,
    onGoCatalog: () -> Unit
) {
    var showConfirm by remember { mutableStateOf(false) }
    val darkOverlay = Color.Black.copy(alpha = 0.45f)
    val green = Color(0xFF015709)

    Box(Modifier.fillMaxSize()) {
        // Fondo con blur como el catálogo
        Image(
            painter = painterResource(id = R.drawable.fondo_cementerio),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().blur(20.dp)
        )
        Box(Modifier.fillMaxSize().background(darkOverlay))

        ////////////////////////////////////////////
        BackHandler(enabled = true) { onBack() }
        ////////////////////////////////////////////

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                //  botón de atrás y título
                CenterAlignedTopAppBar(
                    title = { Text("Tu Carrito", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            bottomBar = {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0xFF0B0B0B))
                            )
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal", color = Color.White, fontWeight = FontWeight.Medium)
                        Text(clp(state.subtotalClp), color = Color.White, fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {showConfirm = true},
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.items.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = green)
                    ) {
                        Text("Finalizar Compra (${clp(state.totalClp)})")
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
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    // Diálogo de confirmación de pedido (visual)
    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Pedido confirmado") },
            text = {
                Column {
                    Text("Gracias por tu preferencia.")
                    Spacer(Modifier.height(8.dp))
                    Text("Total: ${clp(state.totalClp)}", fontWeight = FontWeight.SemiBold)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirm = false
                        onCheckout()
                        onGoHome() //redirige al home
                    }
                ) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false
                onGoCatalog() //redirige al catalogo
                    }
                )
                {
                    Text("Seguir comprando")
                }
            }
        )
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White.copy(alpha = 0.12f))
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(item.product.imageUrl),
                contentDescription = item.product.name,
                modifier = Modifier.size(64.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.product.name, fontWeight = FontWeight.SemiBold, color = Color.White)
                Text(clp(item.product.priceClp), color = Color.White.copy(alpha = 0.9f))
                Spacer(Modifier.height(4.dp))
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
