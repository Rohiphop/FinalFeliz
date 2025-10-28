package com.example.finalfeliz.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.finalfeliz.R
import com.example.finalfeliz.validation.isStrongPassword
import com.example.finalfeliz.validation.isValidEmail
import com.example.finalfeliz.validation.isValidName
import com.example.finalfeliz.viewmodel.UserViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    vm: UserViewModel,
    onBack: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val state by vm.state.collectAsState()
    val focus = LocalFocusManager.current

    // ——— Paleta para “estética perfil” ———
    val green = Color(0xFF1B5E20)
    val panel = Color(0xFF0B0B0B).copy(alpha = 0.55f)     // tarjeta oscura translúcida
    val overlay = Color.Black.copy(alpha = 0.40f)
    val borderIdle = Color.White.copy(alpha = 0.28f)
    val divider = Color.White.copy(alpha = 0.10f)

    // Campos (saveable)
    var name by rememberSaveable { mutableStateOf("") }
    var nameDirty by rememberSaveable { mutableStateOf(false) }

    var email by rememberSaveable { mutableStateOf("") }
    var emailDirty by rememberSaveable { mutableStateOf(false) }

    var phoneDigits by rememberSaveable { mutableStateOf("") }  // sólo dígitos tras +569
    var phoneDirty by rememberSaveable { mutableStateOf(false) }

    var pass by rememberSaveable { mutableStateOf("") }
    var passDirty by rememberSaveable { mutableStateOf(false) }
    var showPass by rememberSaveable { mutableStateOf(false) }

    var confirm by rememberSaveable { mutableStateOf("") }
    var confirmDirty by rememberSaveable { mutableStateOf(false) }
    var showConfirm by rememberSaveable { mutableStateOf(false) }

    var triedSubmit by rememberSaveable { mutableStateOf(false) }

    // Validaciones
    val nameOk = isValidName(name)
    val emailOk = isValidEmail(email)
    val phoneOk = phoneDigits.length >= 8
    val passOk = isStrongPassword(pass)
    val confirmOk = confirm.isNotBlank() && confirm == pass
    val allOk = nameOk && emailOk && phoneOk && passOk && confirmOk

    // Éxito
    var showSuccess by remember { mutableStateOf(false) }

    fun shouldShow(d: Boolean) = d || triedSubmit

    // Colores de TextField sobre panel oscuro
    @Composable
    fun darkFieldColors(valid: Boolean, show: Boolean) = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedBorderColor = when {
            !show -> borderIdle
            valid -> green
            else  -> MaterialTheme.colorScheme.error
        },
        unfocusedBorderColor = borderIdle,
        cursorColor = Color.White,
        focusedLabelColor = Color.White.copy(alpha = 0.9f),
        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
        focusedTrailingIconColor = Color.White,
        unfocusedTrailingIconColor = Color.White,
        focusedLeadingIconColor = Color.White,
        unfocusedLeadingIconColor = Color.White,
        focusedPlaceholderColor = Color.White.copy(alpha = 0.6f),
        unfocusedPlaceholderColor = Color.White.copy(alpha = 0.6f),
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent
    )

    // Cuando termina OK -> overlay + navegar
    LaunchedEffect(state.loading, state.error, triedSubmit) {
        if (triedSubmit && !state.loading && state.error == null && allOk) {
            triedSubmit = false
            showSuccess = true
        }
    }
    LaunchedEffect(showSuccess) {
        if (showSuccess) {
            delay(1200)
            onRegisterSuccess()
        }
    }

    Box(Modifier.fillMaxSize()) {
        // Fondo con blur + overlay oscuro (igual que Perfil)
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
                    title = { Text("Crear cuenta", color = Color.White) },
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
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Panel oscuro translúcido (como Perfil)
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
                        // Título de sección en verde
                        Text(
                            text = "Datos personales",
                            color = green,
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.height(10.dp))

                        // Nombre
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it; if (!nameDirty) nameDirty = true },
                            label = { Text("Nombre completo") },
                            singleLine = true,
                            isError = shouldShow(nameDirty) && !nameOk,
                            trailingIcon = {
                                if (shouldShow(nameDirty)) {
                                    if (nameOk) Icon(Icons.Filled.CheckCircle, null, tint = green)
                                    else Icon(Icons.Filled.Error, null, tint = MaterialTheme.colorScheme.error)
                                }
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
                            colors = darkFieldColors(nameOk, shouldShow(nameDirty)),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(10.dp))

                        // Email
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; if (!emailDirty) emailDirty = true },
                            label = { Text("Correo electrónico") },
                            singleLine = true,
                            isError = shouldShow(emailDirty) && !emailOk,
                            trailingIcon = {
                                if (shouldShow(emailDirty)) {
                                    if (emailOk) Icon(Icons.Filled.CheckCircle, null, tint = green)
                                    else Icon(Icons.Filled.Error, null, tint = MaterialTheme.colorScheme.error)
                                }
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
                            colors = darkFieldColors(emailOk, shouldShow(emailDirty)),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Divider(Modifier.padding(vertical = 14.dp), color = divider)

                        // Teléfono con prefijo +569
                        OutlinedTextField(
                            value = phoneDigits,
                            onValueChange = { raw ->
                                phoneDigits = raw.filter { it.isDigit() }.take(8)
                                if (!phoneDirty) phoneDirty = true
                            },
                            label = { Text("Teléfono") },
                            singleLine = true,
                            prefix = { Text("+569 ", color = Color.White) },
                            isError = shouldShow(phoneDirty) && !phoneOk,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
                            colors = darkFieldColors(phoneOk, shouldShow(phoneDirty)),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Divider(Modifier.padding(vertical = 14.dp), color = divider)

                        // Contraseña
                        OutlinedTextField(
                            value = pass,
                            onValueChange = { pass = it; if (!passDirty) passDirty = true },
                            label = { Text("Contraseña") },
                            singleLine = true,
                            visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                            isError = shouldShow(passDirty) && !passOk,
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (shouldShow(passDirty)) {
                                        if (passOk) Icon(Icons.Filled.CheckCircle, null, tint = green)
                                        else Icon(Icons.Filled.Error, null, tint = MaterialTheme.colorScheme.error)
                                        Spacer(Modifier.width(6.dp))
                                    }
                                    IconButton(onClick = { showPass = !showPass }) {
                                        Icon(
                                            imageVector = if (showPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                            contentDescription = if (showPass) "Ocultar contraseña" else "Mostrar contraseña",
                                            tint = Color.White
                                        )
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
                            colors = darkFieldColors(passOk, shouldShow(passDirty)),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(10.dp))

                        // Confirmación
                        OutlinedTextField(
                            value = confirm,
                            onValueChange = { confirm = it; if (!confirmDirty) confirmDirty = true },
                            label = { Text("Confirmar contraseña") },
                            singleLine = true,
                            visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                            isError = shouldShow(confirmDirty) && !confirmOk,
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (shouldShow(confirmDirty)) {
                                        if (confirmOk) Icon(Icons.Filled.CheckCircle, null, tint = green)
                                        else Icon(Icons.Filled.Error, null, tint = MaterialTheme.colorScheme.error)
                                        Spacer(Modifier.width(6.dp))
                                    }
                                    IconButton(onClick = { showConfirm = !showConfirm }) {
                                        Icon(
                                            imageVector = if (showConfirm) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                            contentDescription = if (showConfirm) "Ocultar" else "Mostrar",
                                            tint = Color.White
                                        )
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    triedSubmit = true
                                    if (allOk) {
                                        val fullPhone: String? =
                                            if (phoneDigits.isNotBlank()) "+569$phoneDigits" else null
                                        vm.register(
                                            name = name.trim(),
                                            email = email.trim(),
                                            password = pass,
                                            phone = fullPhone
                                        )
                                    }
                                }
                            ),
                            colors = darkFieldColors(confirmOk, shouldShow(confirmDirty)),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(18.dp))

                        // Botón verde
                        Button(
                            onClick = {
                                triedSubmit = true
                                if (allOk) {
                                    val fullPhone: String? =
                                        if (phoneDigits.isNotBlank()) "+569$phoneDigits" else null
                                    vm.register(
                                        name = name.trim(),
                                        email = email.trim(),
                                        password = pass,
                                        phone = fullPhone
                                    )
                                }
                            },
                            enabled = allOk && !state.loading,
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
                                Text("Creando…")
                            } else {
                                Text("Registrarme")
                            }
                        }

                        state.error?.let {
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        if (showSuccess) SuccessOverlay()
    }
}

@Composable
private fun SuccessOverlay() {
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(250, easing = LinearOutSlowInEasing),
        label = "overlayAlpha"
    )
    val pop = remember { Animatable(0.8f) }
    LaunchedEffect(Unit) { pop.animateTo(1f, spring(stiffness = Spring.StiffnessLow)) }
    val infinite = rememberInfiniteTransition(label = "pulse")
    val pulse by infinite.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E1B0E).copy(alpha = 0.75f))
            .graphicsLayer { this.alpha = alpha },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF2E7D32),
                modifier = Modifier
                    .size(96.dp)
                    .graphicsLayer {
                        val s = pop.value * pulse
                        scaleX = s; scaleY = s
                    }
            )
            Spacer(Modifier.height(12.dp))
            Text("Registro exitoso", color = Color.White, fontSize = 22.sp)
            Spacer(Modifier.height(4.dp))
            Text("Bienvenido a Final Feliz", color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp)
        }
    }
}
