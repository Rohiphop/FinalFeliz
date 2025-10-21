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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.finalfeliz.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    vm: UserViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val state by vm.state.collectAsState()

    // Carga inicial de usuarios (solo si es admin)
    LaunchedEffect(true) {
        vm.loadAdminData()
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
