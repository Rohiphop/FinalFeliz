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
// Screens
import com.example.finalfeliz.screen.*   // HomeScreen, CatalogScreen, AdminScreen, etc.
import com.example.finalfeliz.ui.theme.FinalFelizTheme
// Carrito
import com.example.finalfeliz.ui.cart.CartScreen
import com.example.finalfeliz.ui.cart.CartViewModel
// Mapper de Product (data) -> Product (domain) para el carrito
import com.example.finalfeliz.domain.mappers.toDomain
import com.example.finalfeliz.data.Product
import com.example.finalfeliz.domain.mappers.toDomain


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FinalFelizTheme {
                Surface(color = MaterialTheme.colorScheme.background) {

                    // Controlador de navegación principal
                    val navController = rememberNavController()

                    // ViewModel compartido de usuario
                    val userVm: com.example.finalfeliz.viewmodel.UserViewModel =
                        viewModel(factory = com.example.finalfeliz.viewmodel.UserVMFactory(applicationContext))
                    val state by userVm.state.collectAsState()

                    // Carrito en memoria (compartido)
                    val cartVm: CartViewModel = viewModel()
                    val cartUi by cartVm.uiState.collectAsState()

                    // Nombre a mostrar en Home
                    val displayName = if (state.isAdmin) "Admin" else state.userName ?: "Usuario"

                    NavHost(navController = navController, startDestination = "welcome") {

                        // ------------------------------------------------------------------
                        // Bienvenida
                        // ------------------------------------------------------------------
                        composable("welcome") {
                            WelcomeScreen(
                                onGoLogin = { navController.navigate("login") },
                                onGoRegister = { navController.navigate("register") }
                            )
                        }

                        // ------------------------------------------------------------------
                        // Login
                        // ------------------------------------------------------------------
                        composable("login") {
                            LoginScreen(
                                vm = userVm,
                                onLoginSuccess = {
                                    navController.navigate("home") {
                                        popUpTo("welcome") { inclusive = true }
                                    }
                                },
                                onAdminLogin = {
                                    navController.navigate("admin") {
                                        popUpTo("welcome") { inclusive = true }
                                    }
                                },
                                onBackToWelcome = { navController.popBackStack() }
                            )
                        }

                        // ------------------------------------------------------------------
                        // Registro
                        // ------------------------------------------------------------------
                        composable("register") {
                            RegisterScreen(
                                vm = userVm,
                                onBack = { navController.popBackStack() },
                                onRegisterSuccess = {
                                    if (state.isAdmin) {
                                        navController.navigate("admin") {
                                            popUpTo("welcome") { inclusive = true }
                                        }
                                    } else {
                                        navController.navigate("home") {
                                            popUpTo("welcome") { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }

                        // ------------------------------------------------------------------
                        // Home (portada principal)
                        // ------------------------------------------------------------------
                        composable("home") {
                            HomeScreen(
                                userName = displayName,
                                isAdmin = state.isAdmin,
                                onLogoutClick = {
                                    userVm.logout()
                                    navController.navigate("welcome") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                },
                                onGoCatalog = { navController.navigate("catalog") },
                                onGoCustomize = { navController.navigate("customize") },
                                onGoAdmin = { navController.navigate("admin") },

                                // ⭐️ Estos dos eran los que faltaban:
                                cartCount = cartUi.itemCount,
                                onOpenCart = { navController.navigate("cart") }
                            )
                        }

                        // ------------------------------------------------------------------
                        // Panel Admin
                        // ------------------------------------------------------------------
                        composable("admin") {
                            AdminScreen(
                                vm = userVm,
                                onBack = { navController.popBackStack() },
                                onLogout = {
                                    userVm.logout()
                                    navController.navigate("welcome") {
                                        popUpTo("admin") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // ------------------------------------------------------------------
                        // Personalización
                        // ------------------------------------------------------------------
                        composable("customize") {
                            CustomizeCoffinScreen(
                                onBack = { navController.popBackStack() },
                                onSave = { dbProduct ->
                                    cartVm.add(dbProduct.toDomain())  // data.Product -> domain.Product
                                    navController.navigate("cart")    // opcional: mostrar carrito al guardar
                                }
                            )
                        }



                        // ------------------------------------------------------------------
                        // Gestión de productos (Admin)
                        // ------------------------------------------------------------------
                        composable("admin_products") {
                            AdminProductsScreen(onBack = { navController.popBackStack() })
                        }

                        // ------------------------------------------------------------------
                        // Catálogo
                        // ------------------------------------------------------------------
                        composable("catalog") {
                            CatalogScreen(
                                userName  = displayName,
                                userEmail = state.userEmail ?: "—",
                                onBack = { navController.popBackStack() },
                                onLogout = {
                                    userVm.logout()
                                    navController.navigate("welcome") {
                                        popUpTo("catalog") { inclusive = true }
                                    }
                                },
                                onAddToCart = { p -> cartVm.add(p.toDomain()) }, // agrega al carrito
                                onOpenCart = { navController.navigate("cart") },  // abre carrito
                                cartCount = cartUi.itemCount                      // contador en AppBar
                            )
                        }

                        // ------------------------------------------------------------------
                        // Carrito (visual, sin pago)
                        // ------------------------------------------------------------------
                        composable("cart") {
                            CartScreen(
                                state = cartUi,
                                onInc = cartVm::inc,
                                onDec = cartVm::dec,
                                onRemove = cartVm::remove,
                                onCheckout = { /* vacío: no hay pago */ }
                            )
                        }
                    }
                }
            }
        }
    }
}
