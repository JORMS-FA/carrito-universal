package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import coil.compose.AsyncImage
import com.example.data.local.ProductEntity
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityLow
import com.example.ui.theme.PriorityMedium
import com.example.ui.viewmodel.ShoppingViewModel
import com.example.util.MarkdownExporter
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: Int,
    viewModel: ShoppingViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToMarkdown: (Int) -> Unit
) {
    val context = LocalContext.current
    var productState by remember { mutableStateOf<ProductEntity?>(null) }
    var activeTab by remember { mutableStateOf("Ficha IA") } // "Ficha IA", "Especificaciones", "Ajustes"
    
    // Quick schedule pop-up trigger
    var showReminderDialog by remember { mutableStateOf(false) }

    // Reload product periodically on edit
    LaunchedEffect(productId) {
        productState = viewModel.getProductById(productId)
    }

    val product = productState

    if (product == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(product.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("detail_back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.deleteProduct(product)
                            Toast.makeText(context, "Producto eliminado", Toast.LENGTH_SHORT).show()
                            onNavigateBack()
                        },
                        modifier = Modifier.testTag("detail_delete_button")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar deseo", tint = PriorityHigh)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            // Elegant M3 floating footer target link
            Surface(
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            try {
                                val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse(product.url))
                                context.startActivity(urlIntent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "No se pudo abrir el enlace", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(52.dp)
                            .testTag("detail_buy_now_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Launch, contentDescription = "Tienda")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ir a Tienda", fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            MarkdownExporter.exportAndShare(context, product)
                        },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .weight(0.8f)
                            .height(52.dp)
                            .testTag("detail_export_md_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Share, contentDescription = "Exportar")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Compartir", fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Hero Large Image Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Dim Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))
                            )
                        )
                )

                // Metadata Float Badge
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = product.category,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.85f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = product.sourceStore,
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Tabs Selector inside product card (Samsung style)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 24.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Ficha IA", "Especificaciones", "Precios", "Ajustes").forEach { tabName ->
                    val isActive = activeTab == tabName
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isActive) MaterialTheme.colorScheme.surface else Color.Transparent)
                            .clickable { activeTab = tabName }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tabName,
                            fontSize = 13.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Active Tab Rendering Layout
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                when (activeTab) {
                    "Ficha IA" -> {
                        // Display Gemini Generated Markdown Document View card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onNavigateToMarkdown(product.id) }
                                .testTag("detail_markdown_shortcut_card"),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "IA",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Análisis de Inteligencia Artificial",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = "Ver completo",
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Generamos un reporte con estructura organizada en Markdown conteniendo ventajas, desventajas, notas del analista, y calificación de compra para Obsidian, Notion o lectura futura.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.background)
                                        .padding(14.dp)
                                ) {
                                    Text(
                                        text = if (product.summaryMd.length > 250) product.summaryMd.take(250) + "..." else product.summaryMd,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                                        maxLines = 10,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    "Especificaciones" -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp)
                            ) {
                                Text(
                                    text = "Características del Artículo (Bento Grid)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                // Bento Specs inside the panel Card!
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    BentoSpecCard(
                                        modifier = Modifier.weight(1f),
                                        label = "MARCA",
                                        valText = product.brand,
                                        emoji = "🏷️",
                                        containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f)
                                    )
                                    BentoSpecCard(
                                        modifier = Modifier.weight(1.2f),
                                        label = "TIENDA",
                                        valText = product.sourceStore,
                                        emoji = "🏪",
                                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    BentoSpecCard(
                                        modifier = Modifier.weight(1.2f),
                                        label = "PRECIO",
                                        valText = "${product.currency} ${product.price}",
                                        emoji = "💵",
                                        containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)
                                    )
                                    BentoSpecCard(
                                        modifier = Modifier.weight(1f),
                                        label = "CALIDAD",
                                        valText = "★".repeat(product.quality) + "☆".repeat(5 - product.quality),
                                        emoji = "✨",
                                        containerColor = PriorityMedium.copy(alpha = 0.08f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    BentoSpecCard(
                                        modifier = Modifier.weight(1f),
                                        label = "PUNTUACIÓN",
                                        valText = "${product.score} / 100",
                                        emoji = "🤖",
                                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                    )
                                    BentoSpecCard(
                                        modifier = Modifier.weight(1.1f),
                                        label = "VALORACIÓN",
                                        valText = "${product.rating} ★",
                                        emoji = "⭐",
                                        containerColor = PriorityLow.copy(alpha = 0.08f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "Ventajas",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PriorityLow
                                )
                                Column(
                                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (product.pros.isNotBlank()) {
                                        product.pros.split(",").forEach { pro ->
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Check, contentDescription = "Pro", tint = PriorityLow, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(pro.trim(), fontSize = 13.sp)
                                            }
                                        }
                                    } else {
                                        Text("Sin ventajas registradas", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Text(
                                    text = "Desventajas",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PriorityHigh
                                )
                                Column(
                                    modifier = Modifier.padding(top = 4.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (product.contras.isNotBlank()) {
                                        product.contras.split(",").forEach { contra ->
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Close, contentDescription = "Contra", tint = PriorityHigh, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(contra.trim(), fontSize = 13.sp)
                                            }
                                        }
                                    } else {
                                        Text("Sin desventajas registradas", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }

                    "Precios" -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Bento Card 1: Main statistics & badge
                            val isLowPrice = product.status == "Precio bajo detectado"
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isLowPrice) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                                ),
                                border = if (isLowPrice) BorderStroke(1.5.dp, MaterialTheme.colorScheme.error) else BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "PRECIO ACTUAL",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                letterSpacing = 0.5.sp
                                            )
                                            Text(
                                                text = "${product.currency}${product.price}",
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isLowPrice) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
                                            )
                                        }
                                        
                                        // Status Pills badge
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (isLowPrice) MaterialTheme.colorScheme.error
                                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                                )
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = if (isLowPrice) "¡OFERTA DETECTADA!" else "Rastreando Precio",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isLowPrice) Color.White else MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    
                                    if (product.priceAlertEnabled) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.NotificationsActive,
                                                contentDescription = "Alert",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "Alerta activa para precios menores de: ",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "${product.currency}${product.priceAlertThreshold}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                        }
                                    }
                                }
                            }

                            // Bento Card 2: Price History Interactive Line Graph
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Text(
                                        text = "Gráfico de Tendencia",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )
                                    
                                    val parsedHistory = remember(product.priceHistory, product.price) {
                                        val list = mutableListOf<Pair<Long, Double>>()
                                        if (product.priceHistory.isNotBlank()) {
                                            product.priceHistory.split(",").forEach { part ->
                                                val sub = part.split(":")
                                                if (sub.size == 2) {
                                                    val t = sub[0].toLongOrNull()
                                                    val p = sub[1].toDoubleOrNull()
                                                    if (t != null && p != null) {
                                                        list.add(t to p)
                                                    }
                                                }
                                            }
                                        }
                                        if (list.isEmpty()) {
                                            list.add(product.createdAt to product.price)
                                        }
                                        list.sortedBy { it.first }
                                    }

                                    val primaryColor = MaterialTheme.colorScheme.primary

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp)
                                            .padding(vertical = 10.dp)
                                    ) {
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            val width = size.width
                                            val height = size.height
                                            
                                            val minPrice = (parsedHistory.map { it.second }.minOrNull() ?: 0.0) * 0.95
                                            val maxPrice = (parsedHistory.map { it.second }.maxOrNull() ?: 100.0) * 1.05
                                            val priceRange = if (maxPrice == minPrice) 1.0 else (maxPrice - minPrice)
                                            
                                            val pointsCount = parsedHistory.size
                                            val stepX = if (pointsCount > 1) width / (pointsCount - 1) else width
                                            
                                            val path = androidx.compose.ui.graphics.Path()
                                            val fillPath = androidx.compose.ui.graphics.Path()
                                            
                                            parsedHistory.forEachIndexed { idx, item ->
                                                val x = idx * stepX
                                                val ratio = (item.second - minPrice) / priceRange
                                                val y = height - (ratio.toFloat() * height * 0.75f + height * 0.12f)
                                                
                                                if (idx == 0) {
                                                    path.moveTo(x, y)
                                                    fillPath.moveTo(x, height)
                                                    fillPath.lineTo(x, y)
                                                } else {
                                                    path.lineTo(x, y)
                                                    fillPath.lineTo(x, y)
                                                }
                                                
                                                if (idx == pointsCount - 1) {
                                                    fillPath.lineTo(width, height)
                                                    fillPath.close()
                                                }
                                            }
                                            
                                            // Draw area fill
                                            if (pointsCount > 0) {
                                                drawPath(
                                                    path = fillPath,
                                                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                                        colors = listOf(primaryColor.copy(alpha = 0.2f), Color.Transparent)
                                                    )
                                                )
                                                
                                                // Draw line
                                                drawPath(
                                                    path = path,
                                                    color = primaryColor,
                                                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                                                        width = 4.dp.toPx(),
                                                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                                                    )
                                                )
                                            }
                                            
                                            // Draw points
                                            parsedHistory.forEachIndexed { idx, item ->
                                                val x = idx * stepX
                                                val ratio = (item.second - minPrice) / priceRange
                                                val y = height - (ratio.toFloat() * height * 0.75f + height * 0.12f)
                                                
                                                drawCircle(
                                                    color = primaryColor,
                                                    radius = 5.dp.toPx(),
                                                    center = androidx.compose.ui.geometry.Offset(x, y)
                                                )
                                                drawCircle(
                                                    color = Color.White,
                                                    radius = 2.dp.toPx(),
                                                    center = androidx.compose.ui.geometry.Offset(x, y)
                                                )
                                            }
                                        }
                                    }

                                    // Print points timestamps & prices
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Historial de Cambios:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    parsedHistory.forEach { p ->
                                        val sdf = remember { java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()) }
                                        val dateStr = sdf.format(java.util.Date(p.first))
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = dateStr, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                                            Text(text = "${product.currency}${p.second}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                                        }
                                    }
                                }
                            }

                            // Bento Card 3: Interactive Alert Config & QA Simulation Sliders
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Text(
                                        text = "Ajustar Alerta y Simulación",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )

                                    var alertEnabledState by remember { mutableStateOf(product.priceAlertEnabled) }
                                    var alertThresholdInput by remember { mutableStateOf(product.priceAlertThreshold.toString()) }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "Habilitar Alerta de Precio", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                        Switch(
                                            checked = alertEnabledState,
                                            onCheckedChange = {
                                                alertEnabledState = it
                                                val updated = product.copy(
                                                    priceAlertEnabled = it,
                                                    updatedAt = System.currentTimeMillis()
                                                )
                                                viewModel.updateProduct(updated)
                                                productState = updated
                                            }
                                        )
                                    }

                                    if (alertEnabledState) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedTextField(
                                                value = alertThresholdInput,
                                                onValueChange = { alertThresholdInput = it },
                                                label = { Text("Valor de la Alerta") },
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.weight(1f),
                                                singleLine = true
                                            )
                                            Button(
                                                onClick = {
                                                    alertThresholdInput.toDoubleOrNull()?.let { threshold ->
                                                        val updated = product.copy(
                                                            priceAlertThreshold = threshold,
                                                            updatedAt = System.currentTimeMillis()
                                                        )
                                                        viewModel.updateProduct(updated)
                                                        productState = updated
                                                        Toast.makeText(context, "Umbral de alerta actualizado a ${product.currency}$threshold", Toast.LENGTH_SHORT).show()
                                                    } ?: run {
                                                        Toast.makeText(context, "Ingrese un número válido", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.height(56.dp)
                                            ) {
                                                Text("Guardar")
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Science,
                                            contentDescription = "Simulate",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Simulador de Cambios para QA / Demo",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Text(
                                        text = "Haz clic en rebajas ficticias para probar instantáneamente la detección del sistema y alertas locales:",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf(-10, -25, -50).forEach { discountPercent ->
                                            val factor = 1.0 + (discountPercent / 100.0)
                                            val simPrice = (product.price * factor)
                                            val simPriceRounded = Math.round(simPrice * 100.0) / 100.0
                                            
                                            Card(
                                                modifier = Modifier.weight(1f).height(42.dp),
                                                onClick = {
                                                    viewModel.simulateNewPrice(product, simPriceRounded)
                                                    val targetVal = product.copy(
                                                        price = simPriceRounded,
                                                        priceHistory = product.priceHistory + ",${System.currentTimeMillis()}:$simPriceRounded",
                                                        status = if (alertEnabledState && simPriceRounded < (alertThresholdInput.toDoubleOrNull() ?: 0.0)) "Precio bajo detectado" else "Por revisar"
                                                    )
                                                    productState = targetVal
                                                    Toast.makeText(context, "Precio simulado a ${product.currency}$simPriceRounded (${discountPercent}%)", Toast.LENGTH_SHORT).show()
                                                },
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                                                )
                                            ) {
                                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "$discountPercent%",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            }
                                        }
                                        
                                        Card(
                                            modifier = Modifier.weight(1f).height(42.dp),
                                            onClick = {
                                                val originalPriceRounded = product.price * 1.2
                                                val simPriceRounded = Math.round(originalPriceRounded * 100.0) / 100.0
                                                viewModel.simulateNewPrice(product, simPriceRounded)
                                                val targetVal = product.copy(
                                                    price = simPriceRounded,
                                                    priceHistory = product.priceHistory + ",${System.currentTimeMillis()}:$simPriceRounded",
                                                    status = "Por revisar"
                                                )
                                                productState = targetVal
                                                Toast.makeText(context, "Sube precio a ${product.currency}$simPriceRounded (+20%)", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                                            )
                                        ) {
                                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "+20%",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "Ajustes" -> {
                        // Decision Config Panel
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "Configuración del Deseo",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                // Edit Status Dropdown Simulated Pills
                                Text("Estado de Adquisición", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 6.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 14.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("Por revisar", "Prioritario", "En espera", "Comprado").forEach { stat ->
                                        val selected = product.status == stat
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(
                                                    if (selected) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
                                                )
                                                .clickable {
                                                    val u = product.copy(status = stat, updatedAt = System.currentTimeMillis())
                                                    viewModel.updateProduct(u)
                                                    productState = u
                                                }
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = stat,
                                                fontSize = 11.sp,
                                                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                // Edit Priority Dropdown Simulated pills
                                Text("Prioridad de Decisión", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 6.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 14.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("Alta", "Media", "Baja").forEach { prio ->
                                        val selected = product.priority == prio
                                        val color = when (prio) {
                                            "Alta" -> PriorityHigh
                                            "Media" -> PriorityMedium
                                            else -> PriorityLow
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(
                                                    if (selected) color
                                                    else color.copy(alpha = 0.08f)
                                                )
                                                .clickable {
                                                    val u = product.copy(priority = prio, updatedAt = System.currentTimeMillis())
                                                    viewModel.updateProduct(u)
                                                    productState = u
                                                }
                                                .padding(horizontal = 12.dp, vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = prio,
                                                fontSize = 11.sp,
                                                color = if (selected) Color.White else color,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                // Reminders schedule triggers
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.background)
                                        .clickable { showReminderDialog = true }
                                        .padding(14.dp)
                                        .testTag("detail_schedule_alarm_trigger")
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.NotificationsActive, contentDescription = "Alarm", tint = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text("Recordatorio de Compra", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                Text(
                                                    text = if (product.reminderDate != null) {
                                                        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(product.reminderDate))
                                                    } else "Desactivado",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Icon(Icons.Default.ChevronRight, contentDescription = "Configurar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Notes input
                                var liveNotes by remember { mutableStateOf(product.notes) }
                                val notesChanged = liveNotes != product.notes
                                OutlinedTextField(
                                    value = liveNotes,
                                    onValueChange = { liveNotes = it },
                                    label = { Text("Editar Notas Personales") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    maxLines = 4
                                )
                                if (notesChanged) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            val u = product.copy(notes = liveNotes, updatedAt = System.currentTimeMillis())
                                            viewModel.updateProduct(u)
                                            productState = u
                                            Toast.makeText(context, "Notas actualizadas", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.align(Alignment.End),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Guardar Notas")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Snooze & scheduler reminder Quick dialog list
    if (showReminderDialog) {
        AlertDialog(
            onDismissRequest = { showReminderDialog = false },
            title = { Text("Suscitar Alarma de Compra") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Te notificaremos mediante alarmas del sistema para decidir si adquieres este artículo o mantienes tus ahorros.")
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    TextButton(
                        onClick = {
                            viewModel.snoozeReminder(product, 1) // 1 Hour
                            Toast.makeText(context, "Listo, alarma en 1 hora", Toast.LENGTH_SHORT).show()
                            showReminderDialog = false
                            onNavigateBack()
                        },
                        modifier = Modifier.fillMaxWidth().testTag("alarm_snooze_1h")
                    ) {
                        Text("En 1 hora (Evaluación Rápida)", textAlign = TextAlign.Start, modifier = Modifier.fillMaxWidth())
                    }
                    TextButton(
                        onClick = {
                            viewModel.snoozeReminder(product, 24) // 1 Day
                            Toast.makeText(context, "Listo, alarma para mañana", Toast.LENGTH_SHORT).show()
                            showReminderDialog = false
                            onNavigateBack()
                        },
                        modifier = Modifier.fillMaxWidth().testTag("alarm_snooze_24h")
                    ) {
                        Text("Mañana por la mañana", textAlign = TextAlign.Start, modifier = Modifier.fillMaxWidth())
                    }
                    TextButton(
                        onClick = {
                            viewModel.snoozeReminder(product, 72) // 3 Days
                            Toast.makeText(context, "Listo, alarma en 3 días", Toast.LENGTH_SHORT).show()
                            showReminderDialog = false
                            onNavigateBack()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Siguiente fin de semana (3 días)", textAlign = TextAlign.Start, modifier = Modifier.fillMaxWidth())
                    }
                    if (product.reminderDate != null) {
                        TextButton(
                            onClick = {
                                viewModel.clearReminder(product)
                                Toast.makeText(context, "Recordatorio desactivado", Toast.LENGTH_SHORT).show()
                                showReminderDialog = false
                                onNavigateBack()
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = PriorityHigh),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Desactivar Alarma", textAlign = TextAlign.Start, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showReminderDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun SpecsRow(label: String, valText: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = valText,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun BentoSpecCard(
    modifier: Modifier = Modifier,
    label: String,
    valText: String,
    emoji: String,
    containerColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    letterSpacing = 0.5.sp
                )
                Text(text = emoji, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = valText.ifBlank { "N/A" },
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
