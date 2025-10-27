package com.example.finalfeliz.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.finalfeliz.R
import com.example.finalfeliz.data.User
import com.example.finalfeliz.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsersScreen(
    vm: UserViewModel,
    onBack: () -> Unit
) {
    val state by vm.state.collectAsState()

    // Carga inicial (y cada vez que entras)
    LaunchedEffect(Unit) { vm.refreshAdmin() }

    // Paleta/estilo consistente
    val overlay = Color.Black.copy(alpha = 0.40f)
    val panel   = Color(0xFF0B0B0B).copy(alpha = 0.55f)
    val border  = Color.White.copy(alpha = 0.18f)
    val green   = Color(0xFF1B5E20)
    val success = Color(0xFF2FD03B)
    val danger  = Color(0xFFB71C1C)

    // Filtro
    var query by remember { mutableStateOf("") }
    val filtered = remember(state.users, query) {
        val q = query.trim()
        if (q.isEmpty()) state.users
        else state.users.filter {
            it.name.contains(q, true) ||
                    it.email.contains(q, true) ||
                    (it.phone ?: "").contains(q, true)
        }
    }

    // Diálogos
    var editing by remember { mutableStateOf<User?>(null) }
    var toDelete by remember { mutableStateOf<User?>(null) }

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
                    title = { Text("Usuarios registrados", color = Color.White) },
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
                        IconButton(onClick = { vm.refreshAdmin() }) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Actualizar", tint = Color.White)
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Buscador en panel oscuro
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = panel),
                ) {
                    Column(Modifier.padding(14.dp)) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            label = { Text("Buscar por nombre, email o teléfono", color = Color.White) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = border,
                                unfocusedBorderColor = border,
                                cursorColor = Color.White,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Total: ${filtered.size}",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp
                        )
                    }
                }

                if (state.loading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                state.error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }

                // Lista
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filtered, key = { it.id }) { user ->
                        UserRowCard(
                            user = user,
                            panel = panel,
                            border = border,
                            green = green,
                            success = success,
                            danger = danger,
                            onEdit = { editing = user },
                            onDelete = { toDelete = user },
                            onToggleAdmin = { makeAdmin ->
                                vm.setAdmin(user.id, makeAdmin)
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialogo editar
    editing?.let { u ->
        EditUserDialog(
            initial = u,
            onDismiss = { editing = null },
            onSave = { updated -> vm.update(updated); editing = null }
        )
    }

    // Confirmación eliminar
    toDelete?.let { u ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Eliminar usuario") },
            text = { Text("¿Seguro que deseas eliminar a ${u.name}? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = { vm.delete(u.id); toDelete = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = danger)
                ) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun UserRowCard(
    user: User,
    panel: Color,
    border: Color,
    green: Color,
    success: Color,
    danger: Color,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleAdmin: (Boolean) -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = panel),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Banda superior sutil (degradado)
        Box(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(green.copy(alpha = 0.35f), Color.Transparent)
                    )
                )
        )

        Row(
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar/rol
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AdminPanelSettings,
                    contentDescription = null,
                    tint = if (user.isAdmin) success else Color.White.copy(alpha = 0.65f)
                )
            }
            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        user.name,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (user.isAdmin) {
                        Spacer(Modifier.width(8.dp))
                        AssistChip(
                            onClick = { },
                            label = { Text("Admin") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color.White.copy(alpha = 0.10f),
                                labelColor = success
                            )
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(user.email, color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                val phoneShown = user.phone?.ifBlank { "—" } ?: "—"
                Text(phoneShown, color = Color.White.copy(alpha = 0.65f), fontSize = 12.sp)
            }

            // Acciones compactas
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedIconButton(
                    onClick = onEdit,
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                    colors = IconButtonDefaults.outlinedIconButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = "Editar")
                }
                IconButton(onClick = onDelete, colors = IconButtonDefaults.iconButtonColors(contentColor = danger)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                }
            }
        }

        // Toggle Admin
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.End
        ) {
            if (user.isAdmin) {
                FilledTonalButton(
                    onClick = { onToggleAdmin(false) },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = danger, contentColor = Color.White
                    )
                ) { Text("Quitar Admin") }
            } else {
                FilledTonalButton(
                    onClick = { onToggleAdmin(true) },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = green, contentColor = Color.White
                    )
                ) { Text("Hacer Admin") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditUserDialog(
    initial: User,
    onDismiss: () -> Unit,
    onSave: (User) -> Unit
) {
    var name by remember { mutableStateOf(initial.name) }
    var email by remember { mutableStateOf(initial.email) }
    var phone by remember { mutableStateOf(initial.phone ?: "") }
    var isAdmin by remember { mutableStateOf(initial.isAdmin) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar usuario") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text("Email") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { s -> phone = s.filter { ch -> ch.isDigit() || ch == '+' || ch == ' ' } },
                    label = { Text("Teléfono (opcional)") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Administrador")
                    Spacer(Modifier.width(8.dp))
                    Switch(checked = isAdmin, onCheckedChange = { isAdmin = it })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val updated = initial.copy(
                    name = name.trim(),
                    email = email.trim(),
                    phone = phone.trim().ifBlank { "" }, // mantenemos contrato no-nulo en la UI
                    isAdmin = isAdmin
                )
                onSave(updated)
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
