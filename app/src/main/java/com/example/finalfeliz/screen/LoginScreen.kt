package com.example.finalfeliz.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.finalfeliz.R
import com.example.finalfeliz.validation.isValidEmail
import com.example.finalfeliz.viewmodel.UserVMFactory
import com.example.finalfeliz.viewmodel.UserViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onBackToWelcome: () -> Unit
) {
    val context = LocalContext.current
    val vm: UserViewModel = viewModel(factory = UserVMFactory(context.applicationContext))
    val state by vm.state.collectAsState()

    var email by remember { mutableStateOf("") }
    var emailDirty by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var passDirty by remember { mutableStateOf(false) }
    var tried by remember { mutableStateOf(false) }

    val emailOk = isValidEmail(email)
    val passOk = password.isNotBlank()
    val canSubmit = emailOk && passOk && !state.loading

    // Shake + limpieza
    val scope = rememberCoroutineScope()
    var shakeTrigger by remember { mutableStateOf(0) }
    val shake = remember { Animatable(0f) }

    LaunchedEffect(shakeTrigger) {
        if (shakeTrigger > 0) {
            shake.snapTo(0f)
            shake.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 500
                    -12f at 50; 10f at 100; -8f at 150; 6f at 200; -4f at 250; 2f at 300; 0f at 500
                }
            )
        }
    }

    fun shouldShow(dirty: Boolean) = dirty || tried

    // Control explícito del mensaje/estado de error global de auth
    var authErrorShown by remember { mutableStateOf(false) }
    var resetColors by remember { mutableStateOf(false) }

    // Reacción al resultado del login
    LaunchedEffect(state.loading, state.error, tried) {
        if (tried && !state.loading) {
            if (state.error == null && emailOk && passOk) {
                // éxito
                tried = false
                onLoginSuccess()
            } else if (state.error != null) {
                // ERROR: mostrar cruces y mensaje en ambos campos
                authErrorShown = true
                scope.launch {
                    shakeTrigger++
                    delay(500)
                    // limpiar y quitar mensaje
                    email = ""
                    password = ""
                    emailDirty = false
                    passDirty = false
                    tried = false
                    resetColors = true
                    authErrorShown = false
                    delay(600)
                    resetColors = false
                }
            }
        }
    }

    // Helpers visuales (manejan error global + reset)
    fun isFieldError(valid: Boolean, show: Boolean, global: Boolean, reset: Boolean): Boolean {
        if (reset) return false
        if (global) return true              // fuerza rojo por error de auth
        return show && !valid
    }

    @Composable
    fun fieldColors(valid: Boolean, show: Boolean, global: Boolean, reset: Boolean) =
        OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = when {
                reset -> MaterialTheme.colorScheme.primary
                global -> MaterialTheme.colorScheme.error
                valid  -> Color(0xFF2E7D32)
                show   -> MaterialTheme.colorScheme.error
                else   -> MaterialTheme.colorScheme.primary
            },
            unfocusedBorderColor = when {
                reset -> MaterialTheme.colorScheme.outline
                global -> MaterialTheme.colorScheme.error
                valid  -> Color(0xFF2E7D32)
                show   -> MaterialTheme.colorScheme.error
                else   -> MaterialTheme.colorScheme.outline
            },
            cursorColor = MaterialTheme.colorScheme.primary
        )

    @Composable
    fun trailingIcon(valid: Boolean, show: Boolean, global: Boolean, reset: Boolean) {
        if (reset) return
        when {
            global -> Icon(Icons.Filled.Error, null, tint = MaterialTheme.colorScheme.error)
            show && valid -> Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF2E7D32))
            show && !valid -> Icon(Icons.Filled.Error, null, tint = MaterialTheme.colorScheme.error)
        }
    }

    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo_cementerio),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().blur(20.dp)
        )
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Inicio de Sesión", fontSize = 26.sp, color = Color.White)
            Spacer(Modifier.height(24.dp))

            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .graphicsLayer { translationX = shake.value }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // EMAIL
                    val showEmail = shouldShow(emailDirty)
                    val emailIsError = isFieldError(emailOk, showEmail, authErrorShown, resetColors)

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            if (!emailDirty) emailDirty = true
                            resetColors = false
                            // al tipear se apaga el mensaje global
                            authErrorShown = false
                        },
                        label = { Text("Correo electrónico") },
                        singleLine = true,
                        isError = emailIsError,
                        trailingIcon = { trailingIcon(emailOk, showEmail, authErrorShown, resetColors) },
                        supportingText = {
                            when {
                                authErrorShown && !resetColors ->
                                    Text("Correo o contraseña incorrectas", color = MaterialTheme.colorScheme.error)
                                showEmail && !emailOk && !resetColors ->
                                    Text("Correo inválido (ej: nombre@dominio.com)")
                            }
                        },
                        colors = fieldColors(emailOk, showEmail, authErrorShown, resetColors),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    // PASSWORD
                    val showPass = shouldShow(passDirty)
                    val passIsError = isFieldError(passOk, showPass, authErrorShown, resetColors)

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            if (!passDirty) passDirty = true
                            resetColors = false
                            authErrorShown = false
                        },
                        label = { Text("Contraseña") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        isError = passIsError,
                        trailingIcon = { trailingIcon(passOk, showPass, authErrorShown, resetColors) },
                        supportingText = {
                            when {
                                authErrorShown && !resetColors ->
                                    Text("Correo o contraseña incorrectas", color = MaterialTheme.colorScheme.error)
                                showPass && !passOk && !resetColors ->
                                    Text("Ingresa tu contraseña")
                            }
                        },
                        colors = fieldColors(passOk, showPass, authErrorShown, resetColors),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = {
                            tried = true
                            if (emailOk && passOk) vm.login(email.trim(), password)
                        },
                        enabled = canSubmit,
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

                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = onBackToWelcome) { Text("Volver a inicio") }
                }
            }
        }
    }
}
