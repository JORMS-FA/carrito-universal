package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ProductEntity
import com.example.ui.viewmodel.ExtractionUiState
import com.example.ui.viewmodel.ShoppingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    viewModel: ShoppingViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val extractionState by viewModel.extractionState.collectAsState()

    var urlInput by remember { mutableStateOf("") }
    
    // Form fields (shown after successful extraction/fallback)
    var title by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var priceInput by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("$") }
    var category by remember { mutableStateOf("Otros") }
    var sourceStore by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(4.0f) }
    var quality by remember { mutableStateOf(4) }
    var score by remember { mutableStateOf(80) }
    var pros by remember { mutableStateOf("") }
    var contras by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Media") }
    var status by remember { mutableStateOf("Por revisar") }
    var notes by remember { mutableStateOf("") }
    var summaryMd by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var priceAlertEnabled by remember { mutableStateOf(false) }
    var priceAlertThresholdInput by remember { mutableStateOf("") }

    val categories = listOf("Otros", "Tecnología", "Hogar", "Ropa", "Deportes", "Belleza", "Libros", "Zapatos", "Café")
    val priorities = listOf("Baja", "Media", "Alta")
    val statuses = listOf("Por revisar", "Prioritario", "En espera", "Comprado", "Descartado")

    var showForm by remember { mutableStateOf(false) }

    // React to Gemini api results
    LaunchedEffect(extractionState) {
        when (val state = extractionState) {
            is ExtractionUiState.Success -> {
                val p = state.product
                title = p.title
                brand = p.brand
                priceInput = p.price.toString()
                currency = p.currency
                category = p.category
                sourceStore = p.sourceStore
                description = p.notes
                rating = p.rating
                quality = p.quality
                score = p.score
                pros = p.pros
                contras = p.contras
                priority = p.priority
                status = p.status
                notes = p.notes
                summaryMd = p.summaryMd
                imageUrl = p.imageUrl
                showForm = true
                Toast.makeText(context, "Análisis de Gemini completado con éxito!", Toast.LENGTH_SHORT).show()
                viewModel.clearExtractionState()
            }
            is ExtractionUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.clearExtractionState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Añadir Deseo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("add_back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Paste Link Box
            if (!showForm) {
                Text(
                    text = "Analizar Enlace",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Pega el enlace de algún producto que desees de Amazon, AliExpress, Mercado Libre u otra tienda. Gemini completará sus datos automáticamente.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text("Enlace del producto (URL)") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .testTag("add_url_input"),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                val clipText = clipboardManager.getText()?.text
                                if (clipText != null) {
                                    urlInput = clipText
                                }
                            }
                        ) {
                            Icon(Icons.Default.ContentPaste, contentDescription = "Pegar")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Action Buttons
                if (extractionState is ExtractionUiState.Loading) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Gemini está analizando la tienda...",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Buscando especificaciones, precios y pros/contras.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            if (urlInput.isNotBlank()) {
                                viewModel.extractAndAnalyzeLink(urlInput)
                            } else {
                                Toast.makeText(context, "Pega un enlace para empezar", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("add_analyze_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "Analizar")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Análisis Inteligente por IA", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            urlInput = "https://www.amazon.com/example/product"
                            showForm = true
                        },
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("add_manual_button")
                    ) {
                        Text("Crear de Forma Manual", fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Editable Extraction Form Map (Visible after analysis)
            AnimatedVisibility(
                visible = showForm,
                enter = fadeIn() + expandVertically()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Headline Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Resultados Detectados\nPuedes ajustar cualquier parámetro antes de archivar:",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Form Fields
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Nombre del Producto") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_title_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = priceInput,
                            onValueChange = { priceInput = it },
                            label = { Text("Precio") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("form_price_input")
                        )
                        OutlinedTextField(
                            value = currency,
                            onValueChange = { currency = it },
                            label = { Text("Moneda") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(0.8f)
                                .testTag("form_currency_input")
                        )
                    }

                    // Price Alert Options Block (One UI 8.5 Style)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        tint = MaterialTheme.colorScheme.primary,
                                        contentDescription = "Alertas"
                                    )
                                    Column {
                                        Text(
                                            text = "Activar Alerta de Precio",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Text(
                                            text = "Notificarme si baja de un umbral",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Switch(
                                    checked = priceAlertEnabled,
                                    onCheckedChange = { priceAlertEnabled = it }
                                )
                            }
                            
                            AnimatedVisibility(
                                visible = priceAlertEnabled,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(modifier = Modifier.padding(top = 12.dp)) {
                                    OutlinedTextField(
                                        value = priceAlertThresholdInput,
                                        onValueChange = { priceAlertThresholdInput = it },
                                        label = { Text("Notificar si el precio baja de...") },
                                        placeholder = { Text("Ej: " + (priceInput.toDoubleOrNull()?.let { it * 0.9 } ?: 100.0).toString()) },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = brand,
                            onValueChange = { brand = it },
                            label = { Text("Marca") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("form_brand_input")
                        )
                        OutlinedTextField(
                            value = sourceStore,
                            onValueChange = { sourceStore = it },
                            label = { Text("Vendedor / Tienda") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("form_store_input")
                        )
                    }

                    // Category scroll list drop select representation
                    Column {
                        Text(
                            text = "Categoría",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .fillMaxWidth()
                        ) {
                            categories.take(5).forEach { cat ->
                                val active = category == cat
                                FilterChip(
                                    selected = active,
                                    onClick = { category = cat },
                                    label = { Text(cat) }
                                )
                            }
                        }
                    }

                    // Priority selection
                    Column {
                        Text(
                            text = "Prioridad de Compra",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            priorities.forEach { prio ->
                                val active = priority == prio
                                FilterChip(
                                    selected = active,
                                    onClick = { priority = prio },
                                    label = { Text(prio) }
                                )
                            }
                        }
                    }

                    // Status selection
                    Column {
                        Text(
                            text = "Estado del Artículo",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            statuses.take(3).forEach { stat ->
                                val active = status == stat
                                FilterChip(
                                    selected = active,
                                    onClick = { status = stat },
                                    label = { Text(stat) }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = pros,
                        onValueChange = { pros = it },
                        label = { Text("Ventajas principales (Separadas por comas)") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = contras,
                        onValueChange = { contras = it },
                        label = { Text("Desventajas principales (Separadas por comas)") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descripción Corta") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notas Personales") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                Toast.makeText(context, "El nombre de producto es obligatorio", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            
                            val pDouble = priceInput.toDoubleOrNull() ?: 0.0
                            val thresholdDouble = priceAlertThresholdInput.toDoubleOrNull() ?: 0.0

                            val entity = ProductEntity(
                                userId = "", // Assigned dynamically in ViewModel save
                                title = title,
                                url = urlInput.ifBlank { "https://manual-input.com" },
                                brand = brand.ifBlank { "Desconocido" },
                                imageUrl = imageUrl.ifBlank { "https://images.unsplash.com/photo-1523275335684-37898b6baf30" },
                                price = pDouble,
                                currency = currency.ifBlank { "$" },
                                category = category,
                                priority = priority,
                                status = status,
                                reminderDate = null,
                                rating = rating,
                                summaryMd = summaryMd,
                                notes = notes,
                                sourceStore = sourceStore.ifBlank { "Manual" },
                                comparisonGroupId = null,
                                quality = quality,
                                pros = pros,
                                contras = contras,
                                score = score,
                                priceAlertEnabled = priceAlertEnabled,
                                priceAlertThreshold = thresholdDouble,
                                priceHistory = "${System.currentTimeMillis()}:$pDouble"
                            )

                            viewModel.saveProduct(entity) {
                                Toast.makeText(context, "Listo! Deseo guardado correctamente.", Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            }
                        },
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("form_submit_button")
                    ) {
                        Text("Registrar Deseo", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
