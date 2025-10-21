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
                    val navController = rememberNavController()
                    val userVm: UserViewModel = viewModel(factory = UserVMFactory(applicationContext))
                    val state by userVm.state.collectAsState()

                    NavHost(navController = navController, startDestination = "welcome") {

                        // 🕊 Pantalla de bienvenida
                        composable("welcome") {
                            WelcomeScreen(
                                onGoLogin = { navController.navigate("login") },
                                onGoRegister = { navController.navigate("register") }
                            )
                        }

                        // 🔐 Login → Home o Admin
                        composable("login") {
                            LoginScreen(
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

                        // 📝 Registro → Home o Admin
                        composable("register") {
                            RegisterScreen(
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

                        // 🏠 Home (usuarios normales)
                        composable("home") {
                            HomeScreen(
                                userName = state.userName ?: "Usuario",
                                isAdmin = state.isAdmin, // 👑 Nuevo
                                onLogoutClick = {
                                    userVm.logout()
                                    navController.navigate("welcome") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                },
                                onGoCatalog = { navController.navigate("catalog") },
                                onGoCustomize = { navController.navigate("customize") },
                                onGoAdmin = { navController.navigate("admin") } // 👑 Nuevo
                            )
                        }

                        // 🧰 Panel de Administración
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

                        // ⚙️ Personalización
                        composable("customize") {
                            CustomizeCoffinScreen(
                                onBack = { navController.popBackStack() },
                                onSave = { /* acciones posteriores */ }
                            )
                        }

                        // 📦 Catálogo
                        composable("catalog") {
                            CatalogScreen(
                                userName = state.userName ?: "Usuario",
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
