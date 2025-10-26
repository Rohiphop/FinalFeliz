package com.example.finalfeliz.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
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
import com.example.finalfeliz.validation.isStrongPassword
import com.example.finalfeliz.validation.isValidName
import com.example.finalfeliz.viewmodel.UserEvent
import com.example.finalfeliz.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    vm: UserViewModel,
    onBack: () -> Unit
) {
    // Paleta coherente con la app
    val greenPrimary = Color(0xFF015709)
    val greenSecondary = Color(0xFF0A6E1A)
    val greenAccent = Color(0xFF2FD03B)
    val cardOverlay = Color(0xFF111111).copy(alpha = 0.55f)   // tarjeta semitransparente

    // Estado global
    val state by vm.state.collectAsState()

    // Snackbar
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Nombre
    var name by remember(state.userName) { mutableStateOf(state.userName.orEmpty()) }
    var nameDirty by remember { mutableStateOf(false) }
    val nameOk = isValidName(name)

    // Teléfono (+569 fijo, se editan 8 dígitos)
    var phoneDigits by remember(state.userPhone) {
        val raw = state.userPhone.orEmpty()
        val digits = if (raw.startsWith("+569") && raw.length >= 12) raw.substring(4) else raw
        mutableStateOf(digits.filter { it.isDigit() }.take(8))
    }
    var phoneDirty by remember { mutableStateOf(false) }
    val phoneOk = phoneDigits.length >= 8
    val fullPhone = if (phoneDigits.isBlank()) null else "+569$phoneDigits"

    // Contraseña
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var showOld by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    val newPassOk = newPass.isBlank() || isStrongPassword(newPass)

    // Escucha eventos del ViewModel para feedback
    LaunchedEffect(Unit) {
        vm.events.collect { ev ->
            when (ev) {
                is UserEvent.ProfileNameSaved -> scope.launch { snackbar.showSnackbar("Nombre actualizado") }
                is UserEvent.PhoneSaved      -> scope.launch { snackbar.showSnackbar("Teléfono actualizado") }
                is UserEvent.PasswordChanged -> {
                    scope.launch { snackbar.showSnackbar("Contraseña cambiada con éxito") }
                    oldPass = ""; newPass = ""
                }
                is UserEvent.ShowMessage     -> scope.launch { snackbar.showSnackbar(ev.msg) }
                else -> Unit
            }
        }
    }

    // Fondo
    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.fondo_cementerio),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().blur(16.dp),
            contentScale = ContentScale.Crop
        )
        // Velo oscuro con sutil degradado hacia el verde
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.50f),
                            Color(0xFF07230B).copy(alpha = 0.55f)
                        )
                    )
                )
        )

        // UI principal
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbar) },
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Mi perfil",
                            color = Color.White,
                            fontSize = 20.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
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
                Modifier
                    .fillMaxSize()
                    .padding(inner)
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Tarjeta central
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = cardOverlay,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth(0.96f)
                ) {
                    Column(
                        Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        // Encabezado de sección
                        Text(
                            "Datos personales",
                            style = MaterialTheme.typography.titleMedium,
                            color = greenAccent
                        )

                        // Correo (solo lectura)
                        OutlinedTextField(
                            value = state.userEmail ?: "",
                            onValueChange = {},
                            label = { Text("Correo") },
                            readOnly = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = greenPrimary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
                                focusedLabelColor = greenAccent,
                                cursorColor = greenPrimary,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Nombre editable
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it; if (!nameDirty) nameDirty = true },
                            label = { Text("Nombre completo") },
                            singleLine = true,
                            isError = nameDirty && !nameOk,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (nameOk) greenAccent else MaterialTheme.colorScheme.error,
                                unfocusedBorderColor = if (nameOk) greenPrimary else Color.Red.copy(alpha = 0.4f),
                                focusedLabelColor = greenAccent,
                                cursorColor = greenPrimary,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (nameDirty && !nameOk) {
                            Text(
                                "Ingrese un nombre válido",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Button(
                            onClick = { vm.saveProfileName(name.trim()) },
                            enabled = nameOk && !state.loading,
                            colors = ButtonDefaults.buttonColors(containerColor = greenPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Guardar nombre", color = Color.White) }

                        Divider(Modifier.padding(vertical = 4.dp), color = Color.White.copy(alpha = 0.15f))

                        // Teléfono
                        OutlinedTextField(
                            value = phoneDigits,
                            onValueChange = { raw ->
                                phoneDigits = raw.filter { it.isDigit() }.take(8)
                                if (!phoneDirty) phoneDirty = true
                            },
                            label = { Text("Teléfono") },
                            singleLine = true,
                            prefix = { Text("+569 ", color = Color.White) },
                            isError = phoneDirty && !phoneOk,
                            supportingText = {
                                if (phoneDirty && !phoneOk)
                                    Text("Debe tener al menos 8 dígitos.", color = MaterialTheme.colorScheme.error)
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (phoneOk) greenAccent else MaterialTheme.colorScheme.error,
                                unfocusedBorderColor = if (phoneOk) greenPrimary else Color.Red.copy(alpha = 0.4f),
                                focusedLabelColor = greenAccent,
                                cursorColor = greenPrimary,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = { vm.savePhone(fullPhone ?: "") },
                            enabled = phoneOk && !state.loading,
                            colors = ButtonDefaults.buttonColors(containerColor = greenSecondary),
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Guardar teléfono", color = Color.White) }

                        Divider(Modifier.padding(vertical = 4.dp), color = Color.White.copy(alpha = 0.15f))

                        // Sección de contraseña
                        Text(
                            "Cambiar contraseña",
                            style = MaterialTheme.typography.titleMedium,
                            color = greenAccent
                        )

                        // Contraseña actual
                        OutlinedTextField(
                            value = oldPass,
                            onValueChange = { oldPass = it },
                            label = { Text("Contraseña actual") },
                            singleLine = true,
                            visualTransformation = if (showOld) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showOld = !showOld }) {
                                    Icon(
                                        if (showOld) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = greenPrimary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
                                focusedLabelColor = greenAccent,
                                cursorColor = greenPrimary,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Nueva contraseña
                        OutlinedTextField(
                            value = newPass,
                            onValueChange = { newPass = it },
                            label = { Text("Nueva contraseña") },
                            singleLine = true,
                            visualTransformation = if (showNew) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showNew = !showNew }) {
                                    Icon(
                                        if (showNew) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                            },
                            supportingText = {
                                if (newPass.isNotBlank() && !newPassOk)
                                    Text("Mín. 8, con mayúscula, número y símbolo.", color = MaterialTheme.colorScheme.error)
                            },
                            isError = newPass.isNotBlank() && !newPassOk,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (newPassOk) greenAccent else MaterialTheme.colorScheme.error,
                                unfocusedBorderColor = if (newPassOk) greenPrimary else Color.Red.copy(alpha = 0.4f),
                                focusedLabelColor = greenAccent,
                                cursorColor = greenPrimary,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = { vm.changePassword(oldPass, newPass) },
                            enabled = oldPass.isNotBlank() && newPass.isNotBlank() && newPassOk && !state.loading,
                            colors = ButtonDefaults.buttonColors(containerColor = greenPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Guardar contraseña", color = Color.White) }

                        // Estado de carga y error global
                        if (state.loading) {
                            LinearProgressIndicator(
                                trackColor = Color.White.copy(alpha = 0.15f),
                                color = greenAccent,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        state.error?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
