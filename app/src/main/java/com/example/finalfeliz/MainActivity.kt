package com.example.finalfeliz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.finalfeliz.screen.*
import com.example.finalfeliz.ui.theme.FinalFelizTheme
import com.example.finalfeliz.ui.cart.CartScreen
import com.example.finalfeliz.ui.cart.CartViewModel
import com.example.finalfeliz.domain.mappers.toDomain
import com.example.finalfeliz.viewmodel.UserVMFactory
import com.example.finalfeliz.viewmodel.UserViewModel
import com.example.finalfeliz.viewmodel.ProductVMFactory
import com.example.finalfeliz.viewmodel.ProductViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FinalFelizTheme {
                Surface(color = MaterialTheme.colorScheme.background) {

                    val navController = rememberNavController()

                    // ViewModel de usuario
                    val userVm: UserViewModel = viewModel(factory = UserVMFactory(applicationContext))
                    val state by userVm.state.collectAsState()

                    // ViewModel de productos
                    val productVm: ProductViewModel = viewModel(factory = ProductVMFactory(applicationContext))

                    // ViewModel del carrito
                    val cartVm: CartViewModel = viewModel()
                    val cartUi by cartVm.uiState.collectAsState()

                    // Nombre mostrado en Home
                    val displayName = if (state.isAdmin) "Admin" else state.userName ?: "Usuario"

                    NavHost(navController = navController, startDestination = "welcome") {

                        // ------------------------------------------------------------
                        // Pantalla de bienvenida
                        // ------------------------------------------------------------
                        composable("welcome") {
                            WelcomeScreen(
                                onGoLogin = { navController.navigate("login") },
                                onGoRegister = { navController.navigate("register") }
                            )
                        }

                        // ------------------------------------------------------------
                        // Login
                        // ------------------------------------------------------------
                        composable("login") {
                            LoginScreen(
                                vm = userVm,
                                onLoginSuccess = {
                                    navController.navigate("home") {
                                        popUpTo("welcome") { inclusive = true }
                                    }
                                },
                                onAdminLogin = {
                                    navController.navigate("home") { // 👈 ahora el admin también va al home
                                        popUpTo("welcome") { inclusive = true }
                                    }
                                },
                                onBackToWelcome = { navController.popBackStack() }
                            )
                        }

                        // ------------------------------------------------------------
                        // Registro
                        // ------------------------------------------------------------
                        composable("register") {
                            RegisterScreen(
                                vm = userVm,
                                onBack = { navController.popBackStack() },
                                onRegisterSuccess = {
                                    navController.navigate("home") {
                                        popUpTo("welcome") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // ------------------------------------------------------------
                        // Home
                        // ------------------------------------------------------------
                        composable("home") {
                            HomeScreen(
                                userName = displayName,
                                isAdmin = state.isAdmin,
                                onLogoutClick = {
                                    cartVm.clear()
                                    userVm.logout()
                                    navController.navigate("welcome") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                },
                                onGoCatalog = { navController.navigate("catalog") },
                                onGoCustomize = { navController.navigate("customize") },
                                onGoAdmin = { navController.navigate("admin_hub") },
                                cartCount = cartUi.itemCount,
                                onOpenCart = { navController.navigate("cart") }
                            )
                        }

                        // ------------------------------------------------------------
                        // AdminHub (menú intermedio para elegir Usuarios o Catálogo)
                        // ------------------------------------------------------------
                        composable("admin_hub") {
                            AdminHubScreen(
                                onBack = { navController.popBackStack() },
                                onGoUsers = { navController.navigate("admin_users") },
                                onGoCatalog = { navController.navigate("admin_products") }
                            )
                        }

                        composable("admin_users") {
                            AdminUsersScreen(
                                vm = userVm,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // ------------------------------------------------------------
                        // Panel de administración (usuarios registrados)
                        // ------------------------------------------------------------
                        composable("admin") {
                            AdminScreen(
                                vm = userVm,
                                productVm = productVm,
                                onBack = { navController.popBackStack() },
                                onLogout = {
                                    cartVm.clear()
                                    userVm.logout()
                                    navController.navigate("welcome") {
                                        popUpTo("admin") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // ------------------------------------------------------------
                        // Gestión del catálogo (formulario)
                        // ------------------------------------------------------------
                        composable("admin_products") {
                            AdminProductsScreen(
                                productVm = productVm,
                                onBack = { navController.popBackStack() },
                                onGoList = { navController.navigate("admin_products_list") }
                            )
                        }

                        // ------------------------------------------------------------
                        // Lista de productos actuales
                        // ------------------------------------------------------------
                        composable("admin_products_list") {
                            AdminProductsListScreen(
                                productVm = productVm,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // ------------------------------------------------------------
                        // Personalización de ataúdes
                        // ------------------------------------------------------------
                        composable("customize") {
                            CustomizeCoffinScreen(
                                onBack = { navController.popBackStack() },
                                onSave = { dbProduct ->
                                    cartVm.add(dbProduct.toDomain())
                                    navController.navigate("cart")
                                }
                            )
                        }

                        // ------------------------------------------------------------
                        // Catálogo
                        // ------------------------------------------------------------
                        composable("catalog") {
                            CatalogScreen(
                                userName = displayName,
                                userEmail = state.userEmail ?: "—",
                                onBack = { navController.popBackStack() },
                                onLogout = {
                                    cartVm.clear()
                                    userVm.logout()
                                    navController.navigate("welcome") {
                                        popUpTo("catalog") { inclusive = true }
                                    }
                                },
                                onAddToCart = { p -> cartVm.add(p.toDomain()) },
                                onOpenCart = { navController.navigate("cart") },
                                cartCount = cartUi.itemCount
                            )
                        }

                        // ------------------------------------------------------------
                        // Carrito
                        // ------------------------------------------------------------
                        composable("cart") {
                            CartScreen(
                                state = cartUi,
                                onInc = cartVm::inc,
                                onDec = cartVm::dec,
                                onRemove = cartVm::remove,
                                onCheckout = { cartVm.clear() },
                                onBack = {
                                    val popped = navController.popBackStack("catalog", false)
                                    if (!popped) {
                                        navController.navigate("catalog") {
                                            launchSingleTop = true
                                            restoreState = true
                                            popUpTo("home") { inclusive = false }
                                        }
                                    }
                                },
                                onGoHome = {
                                    navController.navigate("home") {
                                        launchSingleTop = true
                                        restoreState = true
                                        popUpTo("home") { inclusive = false }
                                    }
                                },
                                onGoCatalog = {
                                    val poppedToCatalog = navController.popBackStack("catalog", false)
                                    if (!poppedToCatalog) {
                                        navController.navigate("catalog") {
                                            launchSingleTop = true
                                            restoreState = true
                                            popUpTo("home") { inclusive = false }
                                        }
                                    }
                                }
                            )
                        }

                        // ------------------------------------------------------------
                        // Perfil
                        // ------------------------------------------------------------
                        composable("profile") {
                            ProfileScreen(
                                vm = userVm,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
