package com.example.finalfeliz.screen

import androidx.annotation.DrawableRes
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.finalfeliz.R
import kotlinx.coroutines.delay

data class CoffinProduct(
    val id: String,
    val name: String,
    val material: String,
    val price: String,
    @DrawableRes val image: Int? = null
)

private val sampleCatalog = listOf(
    CoffinProduct("1", "Clásico Nogal", "Madera maciza", "$500.000", image = R.drawable.madera_maciza),
    CoffinProduct("2", "Ébano Premium", "Acabado pulido", "$1.200.000", image = R.drawable.pulido),
    CoffinProduct("3", "Serenidad Blanco", "Lacado mate", "$1.450.000", image = R.drawable.ebano),
    CoffinProduct("4", "Roble Oscuro", "Textura natural", "$1.890.000", image = R.drawable.natural),
    CoffinProduct("5", "Negro Granate", "Detalles metálicos", "$2.100.000", image = R.drawable.premium),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    userName: String,
    userEmail: String,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onAddToCart: (CoffinProduct) -> Unit = {},
) {
    Box(Modifier.fillMaxSize()) {
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
                // Usuario
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
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Person, contentDescription = null)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                userName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                userEmail,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        OutlinedButton(
                            onClick = onLogout,
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

                // Grid
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 180.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(sampleCatalog, key = { it.id }) { product ->
                        CatalogItemCard(
                            product = product,
                            onAdd = { onAddToCart(product) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogItemCard(
    product: CoffinProduct,
    onAdd: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Imagen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                product.image?.let {
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
                        .height(60.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.35f)
                                )
                            )
                        )
                )
            }

            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    product.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    product.material,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        product.price,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // ---- Botón con animación de color y contenido ----
                    var added by rememberSaveable(product.id) { mutableStateOf(false) }

                    // Colores elegantes
                    val wine = Color(0xFF0E5A22)
                    val success = Color(0xFF2FD03B)

                    val bg by animateColorAsState(
                        targetValue = if (added) success else wine,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "bg"
                    )

                    val iconAlpha by animateFloatAsState(
                        targetValue = if (added) 1f else 1f, // (dejamos fijo; puedes animar si quieres)
                        label = "iconAlpha"
                    )

                    FilledTonalButton(
                        onClick = {
                            if (!added) {
                                onAdd()
                                added = true
                            }
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = bg,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.animateContentSize(
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                        )
                    ) {
                        Icon(
                            imageVector = if (added) Icons.Filled.Check else Icons.Filled.AddShoppingCart,
                            contentDescription = null,
                            modifier = Modifier
                                .size(18.dp)
                                .graphicsLayer { alpha = iconAlpha }
                        )
                        Spacer(Modifier.width(8.dp))

                        // Vuelve al estado normal tras 1.2s
                        if (added) {
                            LaunchedEffect(Unit) {
                                delay(1200)
                                added = false
                            }
                        }
                    }
                }
            }
        }
    }
}
