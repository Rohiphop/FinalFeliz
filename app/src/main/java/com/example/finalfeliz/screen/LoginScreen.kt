package com.example.finalfeliz.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.finalfeliz.R
import com.example.finalfeliz.validation.isValidEmail
import com.example.finalfeliz.viewmodel.UserEvent
import com.example.finalfeliz.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    vm: UserViewModel,
    onLoginSuccess: () -> Unit,
    onAdminLogin: () -> Unit,
    onBackToWelcome: () -> Unit
) {
    val state by vm.state.collectAsState()
    val scope = rememberCoroutineScope()

    // —— Paleta “estética perfil” ——
    val green = Color(0xFF1B5E20)
    val panel = Color(0xFF0B0B0B).copy(alpha = 0.55f)      // tarjeta oscura translúcida
    val overlay = Color.Black.copy(alpha = 0.40f)
    val borderIdle = Color.White.copy(alpha = 0.28f)

    // Campos
    var email by remember { mutableStateOf("") }
    var emailDirty by remember { mutableStateOf(false) }

    var password by remember { mutableStateOf("") }
    var passDirty by remember { mutableStateOf(false) }
    var showPass by remember { mutableStateOf(false) }

    var tried by remember { mutableStateOf(false) }

    // Validaciones
    val emailOk = isValidEmail(email)
    val passOk = password.isNotBlank()
    val canSubmit = emailOk && passOk && !state.loading

    // Eventos del VM
    LaunchedEffect(Unit) {
        vm.events.collect { ev ->
            when (ev) {
                is UserEvent.LoginSuccess ->
                    if (state.isAdmin) onAdminLogin() else onLoginSuccess()
                else -> Unit
            }
        }
    }

    // Colores de TextField sobre panel oscuro
    @Composable
    fun darkFieldColors(valid: Boolean, dirty: Boolean) = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedBorderColor = if (!dirty) borderIdle else if (valid) green else MaterialTheme.colorScheme.error,
        unfocusedBorderColor = borderIdle,
        cursorColor = Color.White,
        focusedLabelColor = Color.White.copy(alpha = 0.9f),
        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
        focusedTrailingIconColor = Color.White,
        unfocusedTrailingIconColor = Color.White,
        focusedPlaceholderColor = Color.White.copy(alpha = 0.6f),
        unfocusedPlaceholderColor = Color.White.copy(alpha = 0.6f),
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent
    )

    Box(Modifier.fillMaxSize()) {
        // Fondo y overlay
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
                    title = { Text("Inicio de Sesión", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBackToWelcome) {
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
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Panel oscuro translúcido como en Perfil/Register
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = panel),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // EMAIL
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; if (!emailDirty) emailDirty = true },
                            label = { Text("Correo electrónico") },
                            singleLine = true,
                            isError = tried && state.error != null,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            colors = darkFieldColors(emailOk, emailDirty),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(12.dp))

                        // PASSWORD con ojito 👁️
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; if (!passDirty) passDirty = true },
                            label = { Text("Contraseña") },
                            singleLine = true,
                            visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showPass = !showPass }) {
                                    Icon(
                                        imageVector = if (showPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = if (showPass) "Ocultar contraseña" else "Mostrar contraseña",
                                        tint = Color.White
                                    )
                                }
                            },
                            isError = tried && state.error != null,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            colors = darkFieldColors(passOk, passDirty),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (state.error != null && tried) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = state.error ?: "",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(Modifier.height(18.dp))

                        // BOTÓN VERDE coherente con toda la app
                        Button(
                            onClick = {
                                tried = true
                                scope.launch {
                                    if (emailOk && passOk) {
                                        vm.login(email.trim(), password)
                                    }
                                }
                            },
                            enabled = canSubmit,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = green,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (state.loading) {
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Ingresando…")
                            } else {
                                Text("Iniciar sesión")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                TextButton(onClick = onBackToWelcome) {
                    Text("Volver a inicio", color = Color.White.copy(alpha = 0.9f))
                }
            }
        }
    }
}
