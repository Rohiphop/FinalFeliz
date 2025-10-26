package com.example.finalfeliz.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.finalfeliz.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userName: String,
    isAdmin: Boolean = false,         // Indica si el usuario actual es administrador
    onLogoutClick: () -> Unit,        // Acción al cerrar sesión
    onGoCatalog: () -> Unit,          // Navegar al catálogo de productos
    onGoCustomize: () -> Unit,        // Navegar a la personalización de ataúdes
    onGoAdmin: () -> Unit = {},       // Navegar al panel de administración (solo admins)
    // 🔽 NUEVOS:
    cartCount: Int,
    onOpenCart: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Bienvenido, $userName",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    // 🛒 Carrito con badge
                    BadgedBox(
                        badge = { if (cartCount > 0) Badge { Text("$cartCount") } }
                    ) {
                        IconButton(onClick = onOpenCart) {
                            Icon(
                                imageVector = Icons.Filled.ShoppingCart,
                                contentDescription = "Carrito",
                                tint = Color.White
                            )
                        }
                    }
                    TextButton(onClick = onLogoutClick) {
                        Text("Salir", color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2C2C2C)
                )
            )
        }
    ) { innerPadding ->
        // Contenedor principal con fondo e imagen difuminada
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Fondo con efecto blur
            Image(
                painter = painterResource(id = R.drawable.fondo_cementerio2),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(12.dp)
            )

            // Contenido centrado
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Título principal
                Text(
                    text = "Explora Nuestros \n\n Servicios",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Subtítulo descriptivo
                Text(
                    text = "Encuentra diseños sobrios y elegantes\no personaliza el tuyo con respeto y distinción.",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE0E0E0),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Botón para ver el catálogo
                Button(
                    onClick = onGoCatalog,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF015709)
                    )
                ) {
                    Text(
                        "Ver Catálogo",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Botón para personalizar ataúdes
                Button(
                    onClick = onGoCustomize,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF064C01)
                    )
                ) {
                    Text(
                        "Personalizar Ataúd",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }

                // Sección visible solo para administradores
                if (isAdmin) {
                    Spacer(modifier = Modifier.height(30.dp))

                    OutlinedButton(
                        onClick = onGoAdmin,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(50.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.5.dp,
                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                listOf(Color(0xFFB8860B), Color(0xFFE8B923))
                            )
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AdminPanelSettings,
                            contentDescription = null,
                            tint = Color(0xFFE8B923)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Panel de Administración",
                            color = Color(0xFFE8B923),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
