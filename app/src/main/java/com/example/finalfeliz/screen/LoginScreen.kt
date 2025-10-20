package com.example.finalfeliz.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.finalfeliz.R
import com.example.finalfeliz.validation.isValidEmail
import com.example.finalfeliz.viewmodel.UserVMFactory
import com.example.finalfeliz.viewmodel.UserViewModel
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,      // ← aquí navegas a "catalog"
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

    // Si el login terminó bien después de intentar
    LaunchedEffect(state.loading, state.error, tried) {
        if (tried && !state.loading && state.error == null) {
            tried = false
            onLoginSuccess()
        }
    }

    // Colores para borde por error de campo o error de login
    @Composable
    fun fieldColors(showAsError: Boolean) = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = if (showAsError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = if (showAsError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
        cursorColor = MaterialTheme.colorScheme.primary
    )

    Box(Modifier.fillMaxSize()) {
        // Fondo elegante
        Image(
            painter = painterResource(id = R.drawable.fondo_cementerio),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().blur(20.dp)
        )
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)))

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Inicio de Sesión", fontSize = 26.sp, color = Color.White)

            Spacer(Modifier.height(24.dp))

            ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(0.92f)) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // EMAIL
                    val showEmailError =
                        (emailDirty && !emailOk) || (state.error != null && tried)
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; if (!emailDirty) emailDirty = true },
                        label = { Text("Correo electrónico") },
                        singleLine = true,
                        isError = showEmailError,
                        supportingText = {
                            if (showEmailError) Text("Correo o contraseña incorrectas")
                        },
                        colors = fieldColors(showEmailError),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    // PASSWORD
                    val showPassError =
                        (passDirty && !passOk) || (state.error != null && tried)
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; if (!passDirty) passDirty = true },
                        label = { Text("Contraseña") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        isError = showPassError,
                        supportingText = {
                            if (showPassError) Text("Correo o contraseña incorrectas")
                        },
                        colors = fieldColors(showPassError),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = {
                            tried = true
                            if (emailOk && passOk) {
                                vm.login(email.trim(), password)
                            }
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
