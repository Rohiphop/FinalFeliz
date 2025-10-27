@file:OptIn(ExperimentalLayoutApi::class)

package com.example.finalfeliz.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.finalfeliz.R
import com.example.finalfeliz.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductsScreen(
    productVm: ProductViewModel,
    onBack: () -> Unit,
    onGoList: () -> Unit
) {
    val ctx = LocalContext.current

    // Paleta consistente
    val green = Color(0xFF1B5E20)
    val panel = Color(0xFF0B0B0B).copy(alpha = 0.55f)
    val overlay = Color.Black.copy(alpha = 0.40f)
    val borderIdle = Color.White.copy(alpha = 0.22f)

    var name by rememberSaveable { mutableStateOf("") }
    var material by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }
    var imageResName by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }

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
                    title = { Text("Gestión de catálogo", color = Color.White) },
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
                        IconButton(onClick = onGoList) {
                            Icon(
                                imageVector = Icons.Filled.ListAlt,
                                contentDescription = "Ver productos",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            }
        ) { inner ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(inner)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Panel oscuro translúcido
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = panel),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Agregar producto",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        val fieldColors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = borderIdle,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.85f),
                            cursorColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedPlaceholderColor = Color.White.copy(alpha = 0.7f),
                            unfocusedPlaceholderColor = Color.White.copy(alpha = 0.5f)
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Nombre del producto") },
                            singleLine = true,
                            colors = fieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = material,
                            onValueChange = { material = it },
                            label = { Text("Material") },
                            singleLine = true,
                            colors = fieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Precio (CLP)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = fieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = imageResName,
                            onValueChange = { imageResName = it },
                            label = { Text("Drawable (opcional, ej: madera_maciza)") },
                            singleLine = true,
                            colors = fieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Descripción (opcional)") },
                            colors = fieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    val priceLong = price.toLongOrNull() ?: 0L
                                    val imageResId = imageResName
                                        .ifBlank { null }
                                        ?.let { res ->
                                            val id = ctx.resources.getIdentifier(res, "drawable", ctx.packageName)
                                            id.takeIf { it != 0 }
                                        }

                                    if (name.isNotBlank() && material.isNotBlank() && priceLong > 0) {
                                        productVm.add(
                                            name = name.trim(),
                                            material = material.trim(),
                                            priceClp = priceLong,
                                            imageRes = imageResId,
                                            desc = description.trim().ifBlank { null }
                                        )
                                        name = ""; material = ""; price = ""
                                        imageResName = ""; description = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = green,
                                    contentColor = Color.White
                                )
                            ) { Text("Agregar al catálogo") }
                        }
                    }
                }

                // Botón outline “Ver productos actuales” (alineado al centro)
                OutlinedButton(
                    onClick = onGoList,
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.dp,
                        brush = Brush.linearGradient(listOf(borderIdle, borderIdle))
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) { Text("Ver productos actuales") }
            }
        }
    }
}
