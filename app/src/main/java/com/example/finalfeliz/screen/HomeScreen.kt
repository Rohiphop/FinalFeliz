package com.example.finalfeliz.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
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
    isAdmin: Boolean = false,
    onLogoutClick: () -> Unit,
    onGoCatalog: () -> Unit,
    onGoCustomize: () -> Unit,
    onGoAdmin: () -> Unit = {},
    onGoProfile: () -> Unit = {}     // 👈 navegación al perfil
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
                    //  Botón de PERFIL
                    Button(
                        onClick = onGoProfile,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = "Perfil",
                            tint = Color.White
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Perfil", color = Color.White, fontWeight = FontWeight.Medium)
                    }

                    //  Botón SALIR
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Fondo
            Image(
                painter = painterResource(id = R.drawable.fondo_cementerio2),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(12.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Explora Nuestros \n\n Servicios",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Encuentra diseños sobrios y elegantes\no personaliza el tuyo con respeto y distinción.",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFE0E0E0),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Botón Catálogo
                Button(
                    onClick = onGoCatalog,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF015709)
                    )
                ) {
                    Text("Ver Catálogo", color = Color.White, fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Botón Personalizar
                Button(
                    onClick = onGoCustomize,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF064C01)
                    )
                ) {
                    Text("Personalizar Ataúd", color = Color.White, fontSize = 18.sp)
                }

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
                            brush = Brush.horizontalGradient(
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
