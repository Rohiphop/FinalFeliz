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
                    val userVm: UserViewModel = viewModel(factory = UserVMFactory(applicationContext))
                    val state by userVm.state.collectAsState()

                    // VM compartido de productos (una sola instancia para toda la app)
                    val productVm: ProductViewModel = viewModel(factory = ProductVMFactory(applicationContext))

                    val displayName = if (state.isAdmin) "Admin" else state.userName ?: "Usuario"

                    NavHost(navController = navController, startDestination = "welcome") {

                        composable("welcome") {
                            WelcomeScreen(
                                onGoLogin = { navController.navigate("login") },
                                onGoRegister = { navController.navigate("register") }
                            )
                        }

                        composable("login") {
                            LoginScreen(
                                vm = userVm,
                                onLoginSuccess = {
                                    navController.navigate("home") {
                                        popUpTo("welcome") { inclusive = true }
                                    }
                                },
                                // ⬇️ Antes llevaba a "admin"; ahora también a "home"
                                onAdminLogin = {
                                    navController.navigate("home") {
                                        popUpTo("welcome") { inclusive = true }
                                    }
                                },
                                onBackToWelcome = { navController.popBackStack() }
                            )
                        }


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
                                onGoProfile = { navController.navigate("profile") }
                            )
                        }

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

                        composable("customize") {
                            CustomizeCoffinScreen(
                                onBack = { navController.popBackStack() },
                                onSave = { /* acción posterior si la necesitas */ }
                            )
                        }

                        // Gestión de productos: usar el productVm compartido
                        composable("admin_products") {
                            AdminProductsScreen(
                                productVm = productVm,          // ← pásalo aquí también
                                onBack = { navController.popBackStack() }
                            )
                        }

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
