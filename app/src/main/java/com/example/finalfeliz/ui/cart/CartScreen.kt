package com.example.finalfeliz.ui.cart

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.*
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    var showSuccess by remember { mutableStateOf(false) }
    var placing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // popup de éxito
    var purchaseItems by remember { mutableStateOf<List<CartItem>>(emptyList()) }
    var purchaseSubtotal by remember { mutableStateOf(0L) }
    val purchaseTotal by remember { derivedStateOf { purchaseSubtotal } }

    val darkOverlay = Color.Black.copy(alpha = 0.45f)
    val green = Color(0xFF015709)

    // Totales
    val subtotal = remember(state.items) { state.items.sumOf { it.lineTotal } }
    val total = subtotal

    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo_cementerio),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().blur(20.dp)
        )
        Box(Modifier.fillMaxSize().background(darkOverlay))

        BackHandler(enabled = true) { onBack() }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
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
                        Text(clp(subtotal), color = Color.White, fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { showConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.items.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = green)
                    ) {
                        Text("Finalizar Compra (${clp(total)})")
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

    // Confirmación antes de comprar
    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { if (!placing) showConfirm = false },
            title = { Text("Confirmar pedido", color = Color.White) },
            text = {
                Text(
                    "¿Deseas finalizar tu compra por un total de ${clp(total)}?",
                    color = Color.White.copy(alpha = 0.9f)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        placing = true
                        scope.launch {
                            // ANTES de vaciar
                            purchaseItems = state.items.toList()
                            purchaseSubtotal = state.items.sumOf { it.lineTotal }

                            onCheckout()       // limpia el carrito
                            delay(800)
                            placing = false
                            showConfirm = false
                            showSuccess = true
                        }
                    },
                    enabled = !placing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = green,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (placing) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp),
                            color = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Procesando…")
                    } else {
                        Text("Sí, finalizar compra")
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { if (!placing) showConfirm = false },
                    enabled = !placing,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Cancelar") }
            },
            containerColor = Color(0xFF0B0B0B).copy(alpha = 0.92f),
            tonalElevation = 8.dp,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // resumen
    if (showSuccess) {
        AlertDialog(
            onDismissRequest = { showSuccess = false },
            title = { Text("Pedido confirmado", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("¡Gracias por tu compra!", color = Color.White.copy(alpha = 0.95f))
                    Spacer(Modifier.height(8.dp))
                    Text("Resumen del pedido:", color = Color.White, fontWeight = FontWeight.Bold)

                    purchaseItems.forEach {
                        Text(
                            "• ${it.product.name} x${it.quantity} → ${clp(it.lineTotal)}",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = MaterialTheme.typography.bodySmall.fontSize
                        )
                    }

                    Spacer(Modifier.height(10.dp))
                    Divider(color = Color.White.copy(alpha = 0.3f))
                    Text("Subtotal: ${clp(purchaseSubtotal)}", color = Color.White)
                    Text("Total: ${clp(purchaseTotal)}", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccess = false
                        onGoHome()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = green,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Volver al inicio") }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showSuccess = false
                        onGoCatalog()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Seguir comprando") }
            },
            containerColor = Color(0xFF0B0B0B).copy(alpha = 0.92f),
            tonalElevation = 8.dp,
            shape = RoundedCornerShape(16.dp)
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
