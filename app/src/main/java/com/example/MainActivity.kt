package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ShoppingViewModel

class MainActivity : ComponentActivity() {
    private fun extractSharedUrl(intent: Intent?): String? {
        if (intent?.action != Intent.ACTION_SEND || intent.type != "text/plain") return null
        return intent.getStringExtra(Intent.EXTRA_TEXT)
            ?.split(Regex("\\s+"))
            ?.firstOrNull { it.startsWith("http://") || it.startsWith("https://") }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var sharedUrl by remember { mutableStateOf(extractSharedUrl(intent)) }
            val shoppingViewModel: ShoppingViewModel = viewModel()
            val themeMode by shoppingViewModel.themeMode.collectAsState()
            val themeColor by shoppingViewModel.themeColor.collectAsState()

            MyApplicationTheme(themeMode = themeMode, themeColor = themeColor) {
                val userSession by shoppingViewModel.currentUser.collectAsState()
                
                val navController = rememberNavController()

                // Check for session recovery on startup
                val startDestination = if (userSession != null) "main" else "splash"

                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {
                    // 1. Onboarding Screen
                    composable("splash") {
                        OnboardingScreen(
                            onNavigateToLogin = {
                                navController.navigate("login") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        )
                    }

                    // 2. Supabase Login Screen
                    composable("login") {
                        LoginScreen(
                            viewModel = shoppingViewModel,
                            onLoginSuccess = {
                                shoppingViewModel.checkActiveSession()
                                navController.navigate("main") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }

                    // 3. Core Multi-tab One UI Layout Container
                    composable("main") {
                        // Monitor logouts
                        if (userSession == null) {
                            LaunchedEffect(Unit) {
                                navController.navigate("login") {
                                    popUpTo("main") { inclusive = true }
                                }
                            }
                        }

                        MainFrameScreen(
                            viewModel = shoppingViewModel,
                            onNavigateToAddProduct = {
                                navController.navigate("add_product")
                            },
                            onNavigateToProductDetail = { productId ->
                                navController.navigate("product_detail/$productId")
                            },
                            onNavigateToMarkdownView = { productId ->
                                navController.navigate("markdown_view/$productId")
                            },
                            onOpenSettings = {
                                navController.navigate("main_settings")
                            }
                        )
                    }

                    composable("main_settings") {
                        ProfileSettingsScreen(
                            viewModel = shoppingViewModel,
                            onLogout = {
                                shoppingViewModel.checkActiveSession()
                                navController.navigate("login") {
                                    popUpTo("main") { inclusive = true }
                                }
                            }
                        )
                    }

                    // 4. Link extraction / manual product form
                    composable("add_product") {
                        AddProductScreen(
                            viewModel = shoppingViewModel,
                            initialUrl = sharedUrl,
                            onNavigateBack = {
                                sharedUrl = null
                                navController.popBackStack()
                            }
                        )
                    }

                    // 5. Rich item detailed evaluation tabs
                    composable(
                        route = "product_detail/{productId}",
                        arguments = listOf(navArgument("productId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val productId = backStackEntry.arguments?.getInt("productId") ?: 0
                        ProductDetailScreen(
                            productId = productId,
                            viewModel = shoppingViewModel,
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onNavigateToMarkdown = { id ->
                                navController.navigate("markdown_view/$id")
                            }
                        )
                    }

                    // 6. Obsidian Markdown viewer full specifications screen
                    composable(
                        route = "markdown_view/{productId}",
                        arguments = listOf(navArgument("productId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val productId = backStackEntry.arguments?.getInt("productId") ?: 0
                        MarkdownDocumentScreen(
                            productId = productId,
                            viewModel = shoppingViewModel,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }

                LaunchedEffect(sharedUrl, userSession) {
                    if (sharedUrl != null && userSession != null) {
                        navController.navigate("add_product")
                    }
                }
            }
        }
    }
}
