package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.ProductEntity
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityLow
import com.example.ui.theme.PriorityMedium
import com.example.ui.viewmodel.ShoppingViewModel
import androidx.compose.ui.res.stringResource
import com.example.R
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: ShoppingViewModel,
    onNavigateToAdd: () -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToCompare: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val products by viewModel.allUserProducts.collectAsState(initial = emptyList())
    val userSession by viewModel.currentUser.collectAsState()
    val uniqueCategories by viewModel.uniqueCategories.collectAsState(initial = emptyList())
    
    var selectedStatusFilter by remember { mutableStateOf("Todos") }
    var selectedPriorityFilter by remember { mutableStateOf("Todos") }
    var sortBy by remember { mutableStateOf("Fecha Reciente") } // "Fecha Reciente", "Precio Mayor", "Precio Menor", "Puntuación de IA"
    
    val statusFilters = listOf("Todos", "Por revisar", "Prioritario", "En espera", "Comprado", "Descartado")
    val priorityFilters = listOf("Todos", "High", "Medium", "Low")

    // Computed Stats
    val totalPendingValue = products.filter { it.status != "Comprado" && it.status != "Descartado" }.sumOf { it.price }
    val activeCurrency = products.firstOrNull()?.currency ?: "$"
    val priorityCount = products.filter { it.priority == "Alta" && it.status != "Comprado" }.size
    val totalProductsCount = products.size

    // Apply filtering and sorting
    val filteredProducts = products.filter { product ->
        val statusMatch = selectedStatusFilter == "Todos" || product.status.equals(selectedStatusFilter, ignoreCase = true)
        val priorityMatch = selectedPriorityFilter == "Todos" || when (selectedPriorityFilter) {
            "High" -> product.priority.equals("Alta", ignoreCase = true)
            "Medium" -> product.priority.equals("Media", ignoreCase = true)
            "Low" -> product.priority.equals("Baja", ignoreCase = true)
            else -> true
        }
        statusMatch && priorityMatch
    }.sortedWith { a, b ->
        when (sortBy) {
            "Precio Mayor" -> b.price.compareTo(a.price)
            "Precio Menor" -> a.price.compareTo(b.price)
            "Puntuación de IA" -> b.score.compareTo(a.score)
            else -> b.createdAt.compareTo(a.createdAt) // Default date
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(bottom = 16.dp, end = 8.dp)
                    .testTag("dashboard_fab_add_product")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir Producto",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // Samsung One UI HUGE Collapsible Header Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    AsyncImage(
                        model = userSession?.photoUrl ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
                        contentDescription = "Avatar de usuario",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onOpenSettings() },
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.dashboard_hello, userSession?.displayName?.substringBefore(" ") ?: "Comprador"),
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onOpenSettings() }
                    )
                }
                
                Text(
                    text = stringResource(R.string.dashboard_title),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = 0.sp
                )

                // Bento Layout Quick Stats
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left Bento Card: Presupuesto Requerido
                    Card(
                        modifier = Modifier
                            .weight(1.1f)
                            .height(115.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.dashboard_budget),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.5.sp
                            )
                            Column {
                                Text(
                                    text = String.format(Locale.getDefault(), "%s %,.0f", activeCurrency, totalPendingValue),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = stringResource(R.string.dashboard_budget_sub),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    // Right Bento Stack: Urgentes and Guardados
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(115.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // High Priority Items Subcard
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = stringResource(R.string.dashboard_urgents),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PriorityHigh,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = "$priorityCount",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(PriorityHigh.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "🔥", fontSize = 12.sp)
                                }
                            }
                        }

                        // Total Items Subcard
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = stringResource(R.string.dashboard_saved),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = "$totalProductsCount",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "🛍️", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Bento Comparison Shortcut Banner
                Card(
                    onClick = onNavigateToCompare,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE3F2FD)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFBBDEFB))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Compare,
                                    contentDescription = "Comparación",
                                    tint = Color(0xFF0377EB),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                val categoryText = if (uniqueCategories.isNotEmpty()) {
                                    stringResource(R.string.dashboard_compare_sub_count, uniqueCategories.size)
                                } else {
                                    stringResource(R.string.dashboard_compare_sub_empty)
                                }
                                Text(
                                    text = stringResource(R.string.dashboard_compare_title),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF0D47A1)
                                )
                                Text(
                                    text = categoryText,
                                    fontSize = 11.sp,
                                    color = Color(0xFF1976D2)
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Ir al Comparador",
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Tabs Filters Row (Status)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(statusFilters) { status ->
                    val isSelected = selectedStatusFilter == status
                    FilterChipTheme(
                        label = status,
                        isSelected = isSelected,
                        onClick = { selectedStatusFilter = status }
                    )
                }
            }

            // Sub Filters Row (Priorities & Sorters)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Priority pills selector (Compact)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    priorityFilters.forEach { priority ->
                        val isSelected = selectedPriorityFilter == priority
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                    else Color.Transparent
                                )
                                .clickable { selectedPriorityFilter = priority }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = priority,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Sorter Dropdown selector trigger
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        sortBy = when (sortBy) {
                            "Fecha Reciente" -> "Precio Mayor"
                            "Precio Mayor" -> "Precio Menor"
                            "Precio Menor" -> "Puntuación de IA"
                            else -> "Fecha Reciente"
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = "Ordenar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = sortBy,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Core List (Products)
            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = "Vacio",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.dashboard_empty_title),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.dashboard_empty_sub),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredProducts) { product ->
                        ProductItemCard(
                            product = product,
                            onClick = { onNavigateToDetail(product.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChipTheme(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun ProductItemCard(product: ProductEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Hero Thumbnail in premium container
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text Info Panel
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Category & Store Label
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "•",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = product.sourceStore,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Title
                Text(
                    text = product.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (product.status == "Precio bajo detectado") {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "🔥 ¡PRECIO BAJO DETECTADO!",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PriorityHigh
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Price Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (product.price > 0.0) String.format(Locale.getDefault(), "%s %.2f", product.currency, product.price) else "Gratis/Manual",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (product.summaryMd.isNotBlank()) {
                            // .MD format block badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "MD",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                            // AI Analyzer Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "🤖",
                                    fontSize = 9.sp
                                )
                            }
                        }

                        // Priority indicator dot/badge
                        val priorityColor = when (product.priority.lowercase()) {
                            "alta" -> PriorityHigh
                            "media" -> PriorityMedium
                            else -> PriorityLow
                        }
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(priorityColor.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = product.priority,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = priorityColor
                            )
                        }
                    }
                }
            }
            
            // Score Badge (One UI bubble)
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = product.score.toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "score",
                        fontSize = 8.sp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
