package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Compare
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.viewmodel.ShoppingViewModel

@Composable
fun MainFrameScreen(
    viewModel: ShoppingViewModel,
    onNavigateToAddProduct: () -> Unit,
    onNavigateToProductDetail: (Int) -> Unit,
    onNavigateToMarkdownView: (Int) -> Unit
) {
    var activeSubScreen by remember { mutableStateOf("deseos") } // "deseos", "comparar", "recordatorios", "ajustes"

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars).testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = activeSubScreen == "deseos",
                    onClick = { activeSubScreen = "deseos" },
                    icon = { 
                        Icon(
                            imageVector = if (activeSubScreen == "deseos") Icons.Default.ShoppingCart else Icons.Outlined.ShoppingCart, 
                            contentDescription = "Deseos"
                        ) 
                    },
                    label = { Text("Deseos", fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_item_deseos")
                )

                NavigationBarItem(
                    selected = activeSubScreen == "comparar",
                    onClick = { activeSubScreen = "comparar" },
                    icon = { 
                        Icon(
                            imageVector = if (activeSubScreen == "comparar") Icons.Default.Compare else Icons.Outlined.Compare, 
                            contentDescription = "Comparar"
                        ) 
                    },
                    label = { Text("Comparar", fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_item_comparar")
                )

                NavigationBarItem(
                    selected = activeSubScreen == "recordatorios",
                    onClick = { activeSubScreen = "recordatorios" },
                    icon = { 
                        Icon(
                            imageVector = if (activeSubScreen == "recordatorios") Icons.Default.Alarm else Icons.Outlined.Alarm, 
                            contentDescription = "Alertas"
                        ) 
                    },
                    label = { Text("Alertas", fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_item_alertas")
                )

                NavigationBarItem(
                    selected = activeSubScreen == "ajustes",
                    onClick = { activeSubScreen = "ajustes" },
                    icon = { 
                        Icon(
                            imageVector = if (activeSubScreen == "ajustes") Icons.Default.Person else Icons.Outlined.Person, 
                            contentDescription = "Ajustes"
                        ) 
                    },
                    label = { Text("Ajustes", fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_item_ajustes")
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (activeSubScreen) {
                "deseos" -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToAdd = onNavigateToAddProduct,
                    onNavigateToDetail = onNavigateToProductDetail,
                    onNavigateToCompare = { activeSubScreen = "comparar" }
                )
                "comparar" -> ComparadorScreen(
                    viewModel = viewModel,
                    onNavigateToProduct = onNavigateToProductDetail
                )
                "recordatorios" -> ReminderScreen(
                    viewModel = viewModel
                )
                "ajustes" -> ProfileSettingsScreen(
                    viewModel = viewModel,
                    onLogout = {
                        // Triggers onLogout callback indirectly which resets session check
                    }
                )
            }
        }
    }
}
