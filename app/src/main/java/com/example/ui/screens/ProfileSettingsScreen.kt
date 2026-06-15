package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.PriorityHigh
import com.example.ui.viewmodel.ShoppingViewModel
import androidx.compose.ui.res.stringResource
import com.example.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(viewModel: ShoppingViewModel, onLogout: () -> Unit) {
    val context = LocalContext.current
    val userSession by viewModel.currentUser.collectAsState()
    val geminiApiKey by viewModel.geminiApiKey.collectAsState()
    val language by viewModel.language.collectAsState()
    var showGeminiDialog by remember { mutableStateOf(false) }
    var editableGeminiApiKey by remember(geminiApiKey) { mutableStateOf(geminiApiKey) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title), fontWeight = FontWeight.Bold) },
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
        ) {
            // Profile Headline Card in One UI style
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = userSession?.photoUrl ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = userSession?.displayName ?: "Usuario",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                    Text(
                        text = if (userSession?.isGuest == true) stringResource(R.string.settings_profile_guest_subtitle) else userSession?.email ?: "correo@supabase.com",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (userSession?.isGuest == true) stringResource(R.string.settings_profile_guest_badge) else stringResource(R.string.settings_profile_synced_badge),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // System settings section list
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_section_config),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 2.dp)
                )

                // One UI 8.5 Themes Panel Card
                val activeThemeMode by viewModel.themeMode.collectAsState()
                val activeThemeColor by viewModel.themeColor.collectAsState()

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = "Tema",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                               )
                            }
                            Column {
                                Text(
                                    text = stringResource(R.string.settings_theme_card_title),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = stringResource(R.string.settings_theme_card_sub),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Theme Mode selection: System, Light, Dark
                        Text(
                            text = stringResource(R.string.settings_theme_mode),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "system" to stringResource(R.string.settings_theme_mode_default),
                                "light" to stringResource(R.string.settings_theme_mode_light),
                                "dark" to stringResource(R.string.settings_theme_mode_dark)
                            ).forEach { (mode, label) ->
                                val active = activeThemeMode == mode
                                Card(
                                    onClick = { viewModel.updateTheme(mode, activeThemeColor) },
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
                                    ),
                                    border = if (active) null else BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 12.sp,
                                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                            color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Color Accents selection (One UI 8.5 Accent list)
                        Text(
                            text = stringResource(R.string.settings_theme_accent),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(
                                "blue" to Color(0xFF0377EB),
                                "violet" to Color(0xFF673AB7),
                                "mint" to Color(0xFF00897B),
                                "peach" to Color(0xFFD84315),
                                "dynamic" to Color(0xFFFF9800)
                            ).forEach { (colorCode, colorVal) ->
                                val active = activeThemeColor == colorCode
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(if (colorCode == "dynamic") Color.Black.copy(alpha = 0.05f) else colorVal)
                                        .clickable {
                                            viewModel.updateTheme(activeThemeMode, colorCode)
                                            if (colorCode == "dynamic") {
                                                Toast.makeText(context, "Sincronizando paleta con Android Wallpaper", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        .border(
                                            width = if (active) 3.dp else 1.dp,
                                            color = if (active) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (colorCode == "dynamic") {
                                        Text(text = "🎨", fontSize = 12.sp)
                                    } else if (active) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                        
                        if (activeThemeColor == "dynamic") {
                            Text(
                                text = stringResource(R.string.settings_theme_dynamic_msg),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 10.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Info Item - Encryption key warning info
                SettingsRow(
                    icon = Icons.Default.Security,
                    title = stringResource(R.string.settings_sync_title),
                    description = if (userSession?.isGuest == true) stringResource(R.string.settings_sync_desc_guest) else stringResource(R.string.settings_sync_desc_user),
                    onClick = {
                        Toast.makeText(context, if (userSession?.isGuest == true) context.getString(R.string.settings_profile_guest_badge) else "Sesion Supabase activa", Toast.LENGTH_SHORT).show()
                    }
                )

                // Developer notes item
                SettingsRow(
                    icon = Icons.Default.Language,
                    title = stringResource(R.string.settings_lang_title),
                    description = if (language == "es") "Español" else "English",
                    onClick = {
                        val nextLang = if (language == "es") "en" else "es"
                        viewModel.updateLanguage(nextLang)
                        val toastMsg = if (nextLang == "es") "Idioma cambiado a Español" else "Language changed to English"
                        Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                    }
                )

                SettingsRow(
                    icon = Icons.Default.Info,
                    title = stringResource(R.string.settings_gemini_title),
                    description = if (geminiApiKey.isBlank()) stringResource(R.string.settings_gemini_desc_empty) else stringResource(R.string.settings_gemini_desc_configured),
                    onClick = {
                        showGeminiDialog = true
                    }
                )

                // About item
                SettingsRow(
                    icon = Icons.Default.Star,
                    title = stringResource(R.string.settings_about_title),
                    description = stringResource(R.string.settings_about_desc),
                    onClick = {
                        Toast.makeText(context, "Carrito Universal v1.0", Toast.LENGTH_SHORT).show()
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Log out action item
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                            viewModel.logout()
                            Toast.makeText(context, context.getString(R.string.settings_logout_toast), Toast.LENGTH_SHORT).show()
                            onLogout()
                        }
                        .testTag("settings_logout_row"),
                    colors = CardDefaults.cardColors(containerColor = PriorityHigh.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = PriorityHigh,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (userSession?.isGuest == true) stringResource(R.string.settings_logout_guest) else stringResource(R.string.settings_logout_user),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = PriorityHigh
                            )
                            Text(
                                text = stringResource(R.string.settings_logout_desc),
                                fontSize = 12.sp,
                                color = PriorityHigh.copy(alpha = 0.8f)
                              )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showGeminiDialog) {
        AlertDialog(
            onDismissRequest = { showGeminiDialog = false },
            title = { Text("Clave Gemini API") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Crea tu clave personal en Google AI Studio y pegala aqui. Se guarda solo en este dispositivo.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = editableGeminiApiKey,
                        onValueChange = { editableGeminiApiKey = it },
                        label = { Text("Gemini API key") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextButton(
                        onClick = {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://aistudio.google.com/app/apikey")
                                )
                            )
                        }
                    ) {
                        Text("Abrir Google AI Studio")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateGeminiApiKey(editableGeminiApiKey)
                        Toast.makeText(context, "Clave Gemini guardada", Toast.LENGTH_SHORT).show()
                        showGeminiDialog = false
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGeminiDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Ver",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}
