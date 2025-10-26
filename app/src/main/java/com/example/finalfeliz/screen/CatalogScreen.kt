package com.example.finalfeliz.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.finalfeliz.R
import com.example.finalfeliz.data.Product
import com.example.finalfeliz.viewmodel.ProductVMFactory
import com.example.finalfeliz.viewmodel.ProductViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    userName: String,
    userEmail: String,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onAddToCart: (com.example.finalfeliz.data.Product) -> Unit = {},
    onOpenCart: () -> Unit,
    cartCount: Int
) {
    // Paleta estilo Perfil/Register
    val green = Color(0xFF1B5E20)
    val panel = Color(0xFF0B0B0B).copy(alpha = 0.55f)
    val overlay = Color.Black.copy(alpha = 0.40f)
    val borderIdle = Color.White.copy(alpha = 0.22f)

    // VM productos (Room)
    val ctx = LocalContext.current
    val pvm: ProductViewModel = viewModel(factory = ProductVMFactory(ctx.applicationContext))
    val pState by pvm.state.collectAsState()

    // ---- Parpadeo verde del ícono del carrito por 2s cada vez que se agrega algo ----
    var flashCart by remember { mutableStateOf(false) }
    LaunchedEffect(flashCart) {
        if (flashCart) {
            delay(2000)
            flashCart = false
        }
    }
    val cartTint by animateColorAsState(
        targetValue = if (flashCart) Color(0xFF2FD03B) /* verde claro */ else Color.White,
        label = "cartTint"
    )
    // -------------------------------------------------------------------------------

    Box(Modifier.fillMaxSize()) {
        // Fondo con blur + overlay
        Image(
            painter = painterResource(id = R.drawable.fondo_cementerio),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().blur(16.dp)
        )
        Box(Modifier.fillMaxSize().background(overlay))

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Catálogo", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        // Reemplaza el TextButton por un ícono con badge y tint animado
                        BadgedBox(
                            badge = { if (cartCount > 0) Badge { Text("$cartCount") } }
                        ) {
                            IconButton(onClick = onOpenCart) {
                                Icon(
                                    imageVector = Icons.Filled.ShoppingCart,
                                    contentDescription = "Carrito",
                                    tint = cartTint
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        ) { inner ->
            Column(
                modifier = Modifier
                    .padding(inner)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxSize()
            ) {
                // Tarjeta usuario
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = Color.White)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                userName,
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                userEmail,
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        OutlinedButton(
                            onClick = onLogout,
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                width = 1.dp,
                                brush = Brush.linearGradient(listOf(borderIdle, borderIdle))
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Cerrar sesión")
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    "Honramos cada despedida con elegancia y respeto.",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(12.dp))

                // Loading / error
                if (pState.loading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                }
                pState.error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                }

                // Grid de productos
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 180.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(pState.products, key = { it.id }) { product ->
                        CatalogItemCard(
                            product = product,
                            onAdd = {
                                onAddToCart(product) // tu callback hacia afuera
                                flashCart = true     // activa parpadeo verde del ícono de carrito
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogItemCard(
    product: Product,
    green: Color,
    panel: Color,
    onAdd: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = panel),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Imagen con degradado inferior
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Color.White.copy(alpha = 0.06f))
            ) {
                product.imageRes?.let {
                    Image(
                        painter = painterResource(id = it),
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xAA000000))
                            )
                        )
                )
            }

            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    product.name,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    product.material,
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val priceLabel = "$ ${"%,d".format(product.priceClp)}"
                    Text(
                        priceLabel,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )

                    // ---- Botón redondo SOLO con carrito; se vuelve verde claro 2s ----
                    var pressed by rememberSaveable(product.id) { mutableStateOf(false) }
                    val lightGreen = Color(0xFF2FD03B)

                    val bg by animateColorAsState(
                        targetValue = if (pressed) lightGreen else green,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "bgCart"
                    )

                    LaunchedEffect(pressed) {
                        if (pressed) {
                            delay(2000) // ~2 segundos
                            pressed = false
                        }
                    }

                    Button(
                        onClick = {
                            if (!pressed) {
                                onAdd()
                                pressed = true
                            }
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = bg,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.size(42.dp) // tamaño compacto, sin texto
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AddShoppingCart,
                            contentDescription = "Agregar al carrito",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
