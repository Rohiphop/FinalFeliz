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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FinalFelizTheme {
                Surface(color = MaterialTheme.colorScheme.background) {

                    // Controlador de navegación principal
                    val navController = rememberNavController()

                    // ViewModel compartido entre TODAS las pantallas
                    val userVm: UserViewModel = viewModel(factory = UserVMFactory(applicationContext))
                    val state by userVm.state.collectAsState()

                    // Nombre a mostrar en Home:
                    // - Si es admin => "Admin"
                    // - Si no, el nombre del usuario o "Usuario" por defecto
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
                        // Login: decide Home o Admin según el usuario autenticado
                        // - Se inyecta el MISMO UserViewModel compartido
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
                        // - Se inyecta el MISMO UserViewModel compartido
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
                                userName = displayName,          // <-- mostrará "Admin" si corresponde
                                isAdmin = state.isAdmin,
                                onLogoutClick = {
                                    userVm.logout()
                                    navController.navigate("welcome") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                },
                                onGoCatalog = { navController.navigate("catalog") },
                                onGoCustomize = { navController.navigate("customize") },
                                onGoAdmin = { navController.navigate("admin") }
                            )
                        }

                        // ------------------------------------------------------------------
                        // Panel de administración (solo debería usarse si es admin)
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
                        // Personalización de ataúdes
                        // ------------------------------------------------------------------
                        composable("customize") {
                            CustomizeCoffinScreen(
                                onBack = { navController.popBackStack() },
                                onSave = { /* acción posterior si la necesitas */ }
                            )
                        }

                        // ------------------------------------------------------------------
                        // Gestión de productos del catálogo (acceso desde Admin)
                        // ------------------------------------------------------------------
                        composable("admin_products") {
                            AdminProductsScreen(onBack = { navController.popBackStack() })
                        }

                        // ------------------------------------------------------------------
                        // Catálogo
                        // ------------------------------------------------------------------
                        composable("catalog") {
                            CatalogScreen(
                                userName  = displayName,                 // coherente con Home
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
                    }
                }
            }
        }
    }
}
