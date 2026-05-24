package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.ShoppingViewModel
import kotlinx.coroutines.launch
import com.example.util.MarkdownExporter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkdownDocumentScreen(
    productId: Int,
    viewModel: ShoppingViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var markdownContent by remember { mutableStateOf("") }
    var productTitle by remember { mutableStateOf("Ficha Técnica") }

    LaunchedEffect(productId) {
        val product = viewModel.getProductById(productId)
        if (product != null) {
            productTitle = "Ficha_${product.title.replace(" ", "_")}.md"
            markdownContent = MarkdownExporter.generateMarkdownContent(product)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Doc", 
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = productTitle, 
                            fontFamily = FontFamily.Monospace, 
                            fontSize = 15.sp, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("markdown_back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    // Copy RAW markdown string to system clipboard
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Carrito Universal Markdown", markdownContent)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copiado al portapapeles!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("markdown_copy_button")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copiar Código")
                    }

                    // Share File (.md) trigger
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                val p = viewModel.getProductById(productId)
                                if (p != null) {
                                    MarkdownExporter.exportAndShare(context, p)
                                }
                            }
                        },
                        modifier = Modifier.testTag("markdown_share_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir Ficha")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F1115)) // Beautiful obsidian slate pitch black document canvas
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Action Panel Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E222B))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("MD", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Text(
                        text = "Ficha generada en Markdown compatible con Obsidian, Notion, Logseq e iA Writer.",
                        fontSize = 11.sp,
                        color = Color(0xFF9EA3B0),
                        lineHeight = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Raw Text Block showing documentation content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF16181D))
                    .padding(18.dp)
            ) {
                Text(
                    text = markdownContent,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = Color(0xFFE2E4E9),
                    lineHeight = 18.sp
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
