package com.example.finalfeliz.screen

// ✅ usa el Product de DATA (Room) para guardar y luego mapear a dominio en el NavHost
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.finalfeliz.R
import kotlin.math.roundToInt
import com.example.finalfeliz.data.Product as DbProduct

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizeCoffinScreen(
    onBack: () -> Unit,
    onSave: (DbProduct) -> Unit = {} // ← ahora devuelve un data.Product listo para carrito
) {
    // ----------- Estado del formulario -----------
    var material by remember { mutableStateOf(MaterialOption.Nogal) }
    var color by remember { mutableStateOf(ColorOption.Caoba) }
    var size by remember { mutableStateOf(SizeOption.Estándar) }
    var finish by remember { mutableStateOf(FinishOption.Mate) }
    var engraving by remember { mutableStateOf("") }
    var premiumHandles by remember { mutableStateOf(false) }
    var paddedInterior by remember { mutableStateOf(true) }

    // ----------- Cálculo de precio -----------
    val basePrice = 500_000.0 // CLP base
    val materialFactor = when (material) {
        MaterialOption.Nogal -> 1.00
        MaterialOption.Roble -> 1.10
        MaterialOption.Ebano -> 1.35
        MaterialOption.BlancoLacado -> 1.15
    }
    val sizeFactor = when (size) {
        SizeOption.Compacto -> 0.90
        SizeOption.Estándar -> 1.00
        SizeOption.Extendido -> 1.20
    }
    val finishFactor = when (finish) {
        FinishOption.Mate -> 1.00
        FinishOption.Satinado -> 1.05
        FinishOption.Brillante -> 1.10
    }
    val extras = (if (premiumHandles) 90_000.0 else 0.0) + (if (paddedInterior) 130_000.0 else 0.0)
    val engravingCost = if (engraving.isNotBlank()) 50_000.0 else 0.0

    val estimatedPriceClp = ((basePrice * materialFactor * sizeFactor * finishFactor) + extras + engravingCost)
        .roundToInt()
        .toLong()

    // ----------- UI -----------
    Box(Modifier.fillMaxSize()) {
        // Fondo
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
                    title = { Text("Personalizar Ataúd", color = Color.White) },
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
                    .padding(inner)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // Vista previa / resumen
                ElevatedCard(shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Vista previa",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = buildPreviewTitle(material, color),
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Tamaño: ${size.label}  •  Terminación: ${finish.label}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (engraving.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Grabado: “$engraving”",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        val extrasList = buildList {
                            if (premiumHandles) add("Manillas premium")
                            if (paddedInterior) add("Interior acolchado")
                        }.joinToString(" • ")
                        if (extrasList.isNotEmpty()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                extrasList,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Estimado: $${"%,d".format(estimatedPriceClp)}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Sección: Material
                SectionCard(title = "Material") {
                    SingleChoiceChips(
                        options = MaterialOption.entries,
                        selected = material,
                        onSelect = { material = it }
                    )
                }

                // Sección: Color
                SectionCard(title = "Color") {
                    SingleChoiceChips(
                        options = ColorOption.entries,
                        selected = color,
                        onSelect = { color = it }
                    )
                }

                // Sección: Tamaño
                SectionCard(title = "Tamaño") {
                    SingleChoiceChips(
                        options = SizeOption.entries,
                        selected = size,
                        onSelect = { size = it }
                    )
                }

                // Sección: Terminación
                SectionCard(title = "Terminación") {
                    SingleChoiceChips(
                        options = FinishOption.entries,
                        selected = finish,
                        onSelect = { finish = it }
                    )
                }

                // sección: Grabado
                SectionCard(title = "Grabado (opcional)") {
                    OutlinedTextField(
                        value = engraving,
                        onValueChange = { engraving = it.take(40) }, // 40 caracteres max
                        placeholder = { Text("Ej: En memoria de...") },
                        singleLine = true,
                        supportingText = { Text("${engraving.length}/40") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Sección: Extras
                SectionCard(title = "Extras") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Manillas premium")
                        Switch(checked = premiumHandles, onCheckedChange = { premiumHandles = it })
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Interior acolchado")
                        Switch(checked = paddedInterior, onCheckedChange = { paddedInterior = it })
                    }
                }

                // Acciones
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            // reset
                            material = MaterialOption.Nogal
                            color = ColorOption.Caoba
                            size = SizeOption.Estándar
                            finish = FinishOption.Mate
                            engraving = ""
                            premiumHandles = false
                            paddedInterior = true
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Reset")
                    }

                    Button(
                        onClick = {
                            // Construimos un data.Product (DbProduct) para mandarlo al carrito
                            val name = "Ataúd ${material.label} • ${color.label}"
                            val desc = buildString {
                                append("Material: ${material.label}")
                                append(" | Color: ${color.label}")
                                append(" | Tamaño: ${size.label}")
                                append(" | Terminación: ${finish.label}")
                                if (engraving.isNotBlank()) append(" | Grabado: $engraving")
                                append(" | Manillas premium: ${if (premiumHandles) "Sí" else "No"}")
                                append(" | Interior acolchado: ${if (paddedInterior) "Sí" else "No"}")
                            }

                            onSave(
                                DbProduct(
                                    id = 0, // Room puede autogenerar si luego persistes
                                    name = name,
                                    material = material.label,
                                    priceClp = estimatedPriceClp,
                                    imageRes = null,
                                    description = desc
                                )
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Guardar")
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ------- Modelos y helpers -------

data class CoffinConfig( // (ya no se usa en onSave, lo dejo por si lo referenciabas en otra parte)
    val material: MaterialOption,
    val color: ColorOption,
    val size: SizeOption,
    val finish: FinishOption,
    val engraving: String,
    val premiumHandles: Boolean,
    val paddedInterior: Boolean,
    val estimatedPrice: Int
)

enum class MaterialOption(val label: String) {
    Nogal("Nogal"),
    Roble("Roble"),
    Ebano("Ébano"),
    BlancoLacado("Blanco lacado");
}

enum class ColorOption(val label: String) {
    Caoba("Caoba"),
    Chocolate("Chocolate"),
    Negro("Negro"),
    Blanco("Blanco"),
    Marfil("Marfil");
}

enum class SizeOption(val label: String) {
    Compacto("Compacto"),
    Estándar("Estándar"),
    Extendido("Extendido");
}

enum class FinishOption(val label: String) {
    Mate("Mate"),
    Satinado("Satinado"),
    Brillante("Brillante");
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T : Enum<T>> SingleChoiceChips(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 3
    ) {
        options.forEach { opt ->
            FilterChip(
                selected = opt == selected,
                onClick = { onSelect(opt) },
                label = { Text(opt.label()) }
            )
        }
    }
}

// Helpers para etiquetas
private fun <T : Enum<T>> T.label(): String {
    return when (this) {
        is MaterialOption -> this.label
        is ColorOption -> this.label
        is SizeOption -> this.label
        is FinishOption -> this.label
        else -> name
    }
}

// Para el título de la vista previa
private fun buildPreviewTitle(material: MaterialOption, color: ColorOption): String {
    val mat = material.label
    val col = color.label
    return "Modelo $mat • $col"
}
