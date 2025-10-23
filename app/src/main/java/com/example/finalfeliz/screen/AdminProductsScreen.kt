package com.example.finalfeliz.screen

// Material 3 ExposedDropdown

// FlowRow es experimental
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.finalfeliz.R
import com.example.finalfeliz.data.Product
import com.example.finalfeliz.viewmodel.ProductVMFactory
import com.example.finalfeliz.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductsScreen(
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val vm: ProductViewModel = viewModel(factory = ProductVMFactory(ctx.applicationContext))
    val state by vm.state.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Product?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Gestionar Productos", color = MaterialTheme.colorScheme.onPrimary) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { editing = null; showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) { inner ->
        Column(Modifier.padding(inner).padding(16.dp)) {
            if (state.loading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.products, key = { it.id }) { p ->
                    ElevatedCard {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (p.imageRes != null) {
                                Image(
                                    painter = painterResource(id = p.imageRes),
                                    contentDescription = null,
                                    modifier = Modifier.size(72.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Column(Modifier.weight(1f)) {
                                Text(p.name, style = MaterialTheme.typography.titleMedium)
                                Text(p.material, style = MaterialTheme.typography.bodySmall)
                                Text(
                                    "$${"%,d".format(p.priceClp)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = { editing = p; showDialog = true }) {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                            IconButton(onClick = { vm.delete(p) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        ProductEditorDialog(
            initial = editing,
            onDismiss = { showDialog = false },
            onConfirm = { name, material, price, imageRes, desc ->
                if (editing == null) {
                    vm.add(name, material, price, imageRes, desc)
                } else {
                    vm.update(
                        editing!!.copy(
                            name = name,
                            material = material,
                            priceClp = price,
                            imageRes = imageRes,
                            description = desc
                        )
                    )
                }
                showDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ProductEditorDialog(
    initial: Product? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Long, Int?, String?) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var material by remember { mutableStateOf(initial?.material ?: "") }
    var priceText by remember { mutableStateOf(if (initial != null) initial.priceClp.toString() else "") }
    var description by remember { mutableStateOf(initial?.description ?: "") }

    // Mapa simple de imágenes disponibles en drawable
    val images = listOf(
        "Cementerio 1" to R.drawable.fondo_cementerio,
        "Cementerio 2" to R.drawable.fondo_cementerio2,
        "Memorial"     to R.drawable.bg_memorial,
        "Natural"      to R.drawable.natural,
        "Pulido"       to R.drawable.pulido
    )
    var imageIndex by remember {
        val idx = images.indexOfFirst { it.second == initial?.imageRes }
        mutableStateOf(if (idx >= 0) idx else 0)
    }

    // estado del dropdown
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Nuevo producto" else "Editar producto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = material,
                    onValueChange = { material = it },
                    label = { Text("Material") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Precio CLP (solo números)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // --- Selector de imagen con ExposedDropdownMenuBox (Material 3) ---
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = images[imageIndex].first,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Imagen") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier
                            .menuAnchor()   // <- necesario en M3
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        images.forEachIndexed { i, pair ->
                            DropdownMenuItem(
                                text = { Text(pair.first) },
                                onClick = {
                                    imageIndex = i
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Opcional: chips rápidos debajo
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    images.forEachIndexed { i, pair ->
                        FilterChip(
                            selected = imageIndex == i,
                            onClick = { imageIndex = i },
                            label = { Text(pair.first) }
                        )
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val price = priceText.toLongOrNull() ?: 0L
                onConfirm(
                    name.trim(),
                    material.trim(),
                    price,
                    images[imageIndex].second,
                    description.trim().ifBlank { null }
                )
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
