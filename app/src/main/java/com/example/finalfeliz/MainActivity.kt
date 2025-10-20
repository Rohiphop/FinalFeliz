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
import com.example.finalfeliz.screen.CatalogScreen
import com.example.finalfeliz.screen.CustomizeCoffinScreen
import com.example.finalfeliz.screen.HomeScreen
import com.example.finalfeliz.screen.LoginScreen
import com.example.finalfeliz.screen.RegisterScreen
import com.example.finalfeliz.screen.WelcomeScreen
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

                    NavHost(navController = navController, startDestination = "welcome") {

                        // 🕊 Pantalla de bienvenida
                        composable("welcome") {
                            WelcomeScreen(
                                onGoLogin = { navController.navigate("login") },
                                onGoRegister = { navController.navigate("register") }
                            )
                        }

                        // 🔐 Login → Home
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = {
                                    navController.navigate("home") {
                                        popUpTo("welcome") { inclusive = true }
                                    }
                                },
                                onBackToWelcome = { navController.popBackStack() }
                            )
                        }

                        // 📝 Registro → Home
                        composable("register") {
                            RegisterScreen(
                                onBack = { navController.popBackStack() },
                                onRegisterSuccess = {
                                    navController.navigate("home") {
                                        popUpTo("welcome") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 🏠 Home → Catálogo
                        composable("home") {
                            val state by userVm.state.collectAsState()
                            HomeScreen(
                                userName = state.userName ?: "Usuario",
                                onLogoutClick = {
                                    userVm.logout()
                                    navController.navigate("welcome") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                },
                                onGoCatalog = { navController.navigate("catalog") },
                                onGoCustomize = { navController.navigate("customize") }
                            )
                        }
                        composable("customize") {
                            CustomizeCoffinScreen(
                                onBack = { navController.popBackStack() },
                                onSave = { config ->
                                    // TODO: Guarda en Room o pásalo al carrito
                                    // y si quieres, navega al catálogo o a un detalle:
                                    // navController.navigate("catalog")
                                }
                            )
                        }

                        //  Catalogo
                        composable("catalog") {
                            val state by userVm.state.collectAsState()
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
