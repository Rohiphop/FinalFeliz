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
import com.example.finalfeliz.data.Product
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

                    // VM compartido de usuario
                    // ViewModel compartido de usuario
                    val userVm: com.example.finalfeliz.viewmodel.UserViewModel =
                        viewModel(factory = com.example.finalfeliz.viewmodel.UserVMFactory(applicationContext))
                    val state by userVm.state.collectAsState()

                    // VM compartido de productos (una sola instancia para toda la app)
                    val productVm: ProductViewModel = viewModel(factory = ProductVMFactory(applicationContext))

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
                        // Registro: tras crear cuenta, decide destino según el tipo de usuario
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
                        // Home (usuarios normales). Si es admin, verá además el botón
                        // de acceso al panel de administración.
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
                                onGoAdmin   = { navController.navigate("admin") },
                                cartCount = cartUi.itemCount,
                                onOpenCart = { navController.navigate("cart") }
                            )
                        }

                        // ------------------------------------------------------------------
                        // Panel de administración (soloadmin)
                        // ------------------------------------------------------------------
                        composable("admin") {
                            AdminScreen(
                                vm = userVm,
                                productVm = productVm, // ← pásalo aquí
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
                        // Personalización de ataúdes
                        // ------------------------------------------------------------------
                        composable("customize") {
                            CustomizeCoffinScreen(
                                onBack = { navController.popBackStack() },
                                onSave = { dbProduct ->
                                    cartVm.add(dbProduct.toDomain())  // data.Product -> domain.Product
                                    navController.navigate("cart")
                                }
                            )
                        }



                        // ------------------------------------------------------------------
                        // Gestión de productos del catálogo (acceso desde Admin)
                        // ------------------------------------------------------------------
                        composable("admin_products") {
                            AdminProductsScreen(
                                productVm = productVm,
                                onBack = { navController.popBackStack() }
                            )
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
