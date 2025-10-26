package com.example.finalfeliz.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import com.example.finalfeliz.R
import com.example.finalfeliz.data.Product
import com.example.finalfeliz.viewmodel.ProductViewModel
import com.example.finalfeliz.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    vm: UserViewModel,
    productVm: ProductViewModel,   // VM de productos
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val state by vm.state.collectAsState()

    // Estado de productos (para la sección de catálogo)
    val productState by productVm.state.collectAsState()
    val products = productState.products

    // ✅ OBTÉN EL CONTEXTO AQUÍ (fuera del lambda)
    val ctx = LocalContext.current

    LaunchedEffect(true) {
        vm.loadAdminData()
        // productVm.seedIfNeeded() // opcional en desarrollo
    }

    Box(Modifier.fillMaxSize()) {
        // Fondo elegante
        Image(
            painter = painterResource(id = R.drawable.fondo_cementerio),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().blur(16.dp)
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Panel de Administración", color = Color.White) },
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
                    .fillMaxSize()
                    .padding(inner)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Resumen de usuarios
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = Color.White.copy(alpha = 0.9f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Usuarios Registrados",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B1B1B)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${state.userCount} usuarios en total",
                            fontSize = 15.sp,
                            color = Color.DarkGray
                        )
                    }
                }

                // Lista de usuarios
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(state.users) { user ->
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = Color.White.copy(alpha = 0.85f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = user.name,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = user.email,
                                        fontSize = 13.sp,
                                        color = Color.Gray
                                    )
                                }

                                if (user.isAdmin) {
                                    FilledTonalButton(
                                        onClick = { vm.setAdmin(user.id, false) },
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = Color(0xFFB71C1C),
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Icon(
                                            Icons.Filled.AdminPanelSettings,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text("Quitar Admin")
                                    }
                                } else {
                                    FilledTonalButton(
                                        onClick = { vm.setAdmin(user.id, true) },
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = Color(0xFF1B5E20),
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Icon(
                                            Icons.Filled.AdminPanelSettings,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text("Hacer Admin")
                                    }
                                }
                            }
                        }
                    }
                }

                // ======== Gestión de Catálogo ========
                ProductAdminSection(
                    products = products,
                    onAdd = { name, material, priceText, imageResName, description ->
                        val price = priceText.filter { it.isDigit() }.toLongOrNull() ?: 0L

                        val imageResId = imageResName?.takeIf { it.isNotBlank() }?.let { resName ->
                            val id = ctx.resources.getIdentifier(resName, "drawable", ctx.packageName)
                            id.takeIf { it != 0 }
                        }

                        if (name.isNotBlank() && material.isNotBlank() && price > 0) {
                            productVm.add(name, material, price, imageResId, description)
                        }
                    },
                    onDelete = { productVm.delete(it) }
                )

                // Botón salir
                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF265818)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Logout, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Cerrar sesión", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun ProductAdminSection(
    products: List<Product>,
    onAdd: (name: String, material: String, priceText: String, imageResName: String?, description: String?) -> Unit,
    onDelete: (Product) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var material by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }
    var imageResName by rememberSaveable { mutableStateOf("") } // ej: "madera_maciza"
    var description by rememberSaveable { mutableStateOf("") }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        )
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Gestión de Catálogo",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B1B1B)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del producto") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = material,
                onValueChange = { material = it },
                label = { Text("Material") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Precio (CLP)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = imageResName,
                onValueChange = { imageResName = it },
                label = { Text("Drawable (opcional, ej: madera_maciza)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción (opcional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        onAdd(
                            name,
                            material,
                            price,
                            imageResName.ifBlank { null },
                            description.ifBlank { null }
                        )
                        // limpiar campos
                        name = ""
                        material = ""
                        price = ""
                        imageResName = ""
                        description = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))
                ) {
                    Text("Agregar al catálogo", color = Color.White)
                }
            }

            Divider(Modifier.padding(vertical = 8.dp))

            Text("Productos actuales: ${products.size}", color = Color.DarkGray, fontSize = 14.sp)

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.heightIn(max = 360.dp)
            ) {
                items(products) { p ->
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = Color.White.copy(alpha = 0.85f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(p.name, fontWeight = FontWeight.SemiBold, color = Color.Black)
                                Text("${p.priceClp} CLP", fontSize = 13.sp, color = Color(0xFF1B5E20))
                                Text("Material: ${p.material}", fontSize = 12.sp, color = Color.Gray)
                                p.description?.let {
                                    Text(it, fontSize = 12.sp, color = Color.DarkGray, maxLines = 2)
                                }
                            }
                            TextButton(
                                onClick = { onDelete(p) },
                                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFB71C1C))
                            ) {
                                Text("Eliminar")
                            }
                        }
                    }
                }
            }
        }
    }
}
