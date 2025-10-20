package com.example.finalfeliz.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
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
import com.example.finalfeliz.validation.isStrongPassword
import com.example.finalfeliz.validation.isValidEmail
import com.example.finalfeliz.validation.isValidName
import com.example.finalfeliz.viewmodel.UserVMFactory
import com.example.finalfeliz.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val context = LocalContext.current
    val vm: UserViewModel = viewModel(factory = UserVMFactory(context.applicationContext))
    val state by vm.state.collectAsState()

    // valores + "dirty" (si el usuario tocó el campo)
    var name by remember { mutableStateOf("") }
    var nameDirty by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("") }
    var emailDirty by remember { mutableStateOf(false) }

    var pass by remember { mutableStateOf("") }
    var passDirty by remember { mutableStateOf(false) }

    var confirm by remember { mutableStateOf("") }
    var confirmDirty by remember { mutableStateOf(false) }

    var triedSubmit by remember { mutableStateOf(false) }

    // validaciones
    val nameOk = isValidName(name)
    val emailOk = isValidEmail(email)
    val passOk = isStrongPassword(pass)
    val confirmOk = confirm.isNotBlank() && confirm == pass
    val allOk = nameOk && emailOk && passOk && confirmOk

    // colores para el borde/indicadores según estado
    @Composable
    fun fieldColors(valid: Boolean, show: Boolean) = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = when {
            !show -> MaterialTheme.colorScheme.primary
            valid -> Color(0xFF2E7D32) // verde
            else  -> MaterialTheme.colorScheme.error
        },
        unfocusedBorderColor = when {
            !show -> MaterialTheme.colorScheme.outline
            valid -> Color(0xFF2E7D32)
            else  -> MaterialTheme.colorScheme.error
        },
        cursorColor = MaterialTheme.colorScheme.primary
    )

    // Mostrar feedback si el usuario tocó el campo o si intentó registrar
    fun shouldShow(dirty: Boolean) = dirty || triedSubmit

    // navega cuando el registro termina bien
    LaunchedEffect(state.loading, state.error, triedSubmit) {
        if (triedSubmit && !state.loading && state.error == null && allOk) {
            triedSubmit = false
            onRegisterSuccess()
        }
    }

    Box(Modifier.fillMaxSize()) {
        // fondo estilo Welcome
        Image(
            painter = painterResource(id = R.drawable.fondo_cementerio),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().blur(20.dp)
        )
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)))

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
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(0.92f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        // NOMBRE
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                if (!nameDirty) nameDirty = true
                            },
                            label = { Text("Nombre completo") },
                            singleLine = true,
                            isError = shouldShow(nameDirty) && !nameOk,
                            trailingIcon = {
                                if (shouldShow(nameDirty)) {
                                    if (nameOk)
                                        Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF2E7D32))
                                    else
                                        Icon(Icons.Filled.Error, null, tint = MaterialTheme.colorScheme.error)
                                }
                            },
                            supportingText = {
                                if (shouldShow(nameDirty) && !nameOk)
                                    Text("Solo letras y espacios (mínimo 2).")
                            },
                            colors = fieldColors(nameOk, shouldShow(nameDirty)),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(12.dp))

                        // EMAIL
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                if (!emailDirty) emailDirty = true
                            },
                            label = { Text("Correo electrónico") },
                            singleLine = true,
                            isError = shouldShow(emailDirty) && !emailOk,
                            trailingIcon = {
                                if (shouldShow(emailDirty)) {
                                    if (emailOk)
                                        Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF2E7D32))
                                    else
                                        Icon(Icons.Filled.Error, null, tint = MaterialTheme.colorScheme.error)
                                }
                            },
                            supportingText = {
                                if (shouldShow(emailDirty) && !emailOk)
                                    Text("Debe contener @ y un punto después (ej: nombre@dominio.com).")
                            },
                            colors = fieldColors(emailOk, shouldShow(emailDirty)),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(12.dp))

                        // PASSWORD
                        OutlinedTextField(
                            value = pass,
                            onValueChange = {
                                pass = it
                                if (!passDirty) passDirty = true
                            },
                            label = { Text("Contraseña") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            isError = shouldShow(passDirty) && !passOk,
                            trailingIcon = {
                                if (shouldShow(passDirty)) {
                                    if (passOk)
                                        Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF2E7D32))
                                    else
                                        Icon(Icons.Filled.Error, null, tint = MaterialTheme.colorScheme.error)
                                }
                            },
                            supportingText = {
                                if (shouldShow(passDirty) && !passOk)
                                    Text("Mín. 8, 1 mayúscula, 1 minúscula, 1 número y 1 caracter especial.")
                            },
                            colors = fieldColors(passOk, shouldShow(passDirty)),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(12.dp))

                        // CONFIRM
                        OutlinedTextField(
                            value = confirm,
                            onValueChange = {
                                confirm = it
                                if (!confirmDirty) confirmDirty = true
                            },
                            label = { Text("Confirmar contraseña") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            isError = shouldShow(confirmDirty) && !confirmOk,
                            trailingIcon = {
                                if (shouldShow(confirmDirty)) {
                                    if (confirmOk)
                                        Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF2E7D32))
                                    else
                                        Icon(Icons.Filled.Error, null, tint = MaterialTheme.colorScheme.error)
                                }
                            },
                            supportingText = {
                                if (shouldShow(confirmDirty) && !confirmOk)
                                    Text("Debe coincidir con la contraseña.")
                            },
                            colors = fieldColors(confirmOk, shouldShow(confirmDirty)),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(20.dp))

                        // Botón SOLO si esta bien
                        Button(
                            onClick = {
                                triedSubmit = true
                                if (allOk) vm.register(name.trim(), email.trim(), pass)
                            },
                            enabled = allOk && !state.loading,
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
    }
}
