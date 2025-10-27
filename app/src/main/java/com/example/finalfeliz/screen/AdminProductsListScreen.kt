package com.example.finalfeliz.screen

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.finalfeliz.R
import com.example.finalfeliz.data.Product
import com.example.finalfeliz.viewmodel.ProductViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductsListScreen(
    productVm: ProductViewModel,
    onBack: () -> Unit
) {
    val state by productVm.state.collectAsState()

    // paleta consistente con el resto
    val green = Color(0xFF1B5E20)
    val panel = Color(0xFF0B0B0B).copy(alpha = 0.55f)
    val overlay = Color.Black.copy(alpha = 0.40f)

    val clp = NumberFormat.getNumberInstance(Locale("es", "CL"))

    Box(Modifier.fillMaxSize()) {
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
                    title = { Text("Productos actuales", color = Color.White) },
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
                Modifier
                    .fillMaxSize()
                    .padding(inner)
                    .padding(16.dp)
            ) {
                if (state.loading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                }
                state.error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                }

                Text(
                    "Total: ${state.products.size}",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.products, key = { it.id }) { p ->
                        ProductRowCard(
                            product = p,
                            priceLabel = "$ ${clp.format(p.priceClp)}",
                            priceColor = green,
                            panelColor = panel,
                            onDelete = { productVm.delete(p) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductRowCard(
    product: Product,
    priceLabel: String,
    priceColor: Color,
    panelColor: Color,
    onDelete: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = panelColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.SemiBold, color = Color.White)
                Spacer(Modifier.height(2.dp))
                Text(priceLabel, fontSize = 13.sp, color = priceColor)
                Spacer(Modifier.height(2.dp))
                Text("Material: ${product.material}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
                product.description?.let {
                    Spacer(Modifier.height(2.dp))
                    Text(it, fontSize = 12.sp, color = Color.White.copy(alpha = 0.80f), maxLines = 2)
                }
            }
            TextButton(
                onClick = onDelete,
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFB71C1C))
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Eliminar")
            }
        }
    }
}
