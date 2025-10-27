package com.example.finalfeliz.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.example.finalfeliz.screen.HomeScreen
import com.example.finalfeliz.screen.CatalogScreen
import com.example.finalfeliz.ui.cart.CartScreen
import com.example.finalfeliz.ui.cart.CartViewModel
import com.example.finalfeliz.domain.mappers.toDomain

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Catalog : Screen("catalog")
    data object Cart : Screen("cart")
}

@Composable
fun AppNavHost(nav: NavHostController, userName: String, isAdmin: Boolean, onLogout: () -> Unit) {
    val vm: CartViewModel = viewModel()
    val state = vm.uiState.collectAsStateWithLifecycle()

    NavHost(navController = nav, startDestination = Screen.Home.route) {

        // HOME (portada)
        composable(Screen.Home.route) {
            HomeScreen(
                userName = userName,
                isAdmin = isAdmin,
                onLogoutClick = onLogout,
                onGoCatalog = { nav.navigate(Screen.Catalog.route) },
                onGoCustomize = { /* nav.navigate(Screen.Customize.route) */ },
                onGoAdmin = { /* nav.navigate(Screen.Admin.route) */ },
                cartCount = state.value.itemCount,
                onOpenCart = { nav.navigate(Screen.Cart.route) }
            )
        }

        // CATÁLOGO (usa Room internamente)
        composable(Screen.Catalog.route) {
            CatalogScreen(
                userName = userName,
                userEmail = "", // si tienes SessionManager, pasa el email real
                onBack = { nav.popBackStack() },
                onLogout = onLogout,
                onAddToCart = { dbProduct -> vm.add(dbProduct.toDomain()) }, // mapper a dominio
                onOpenCart = { nav.navigate(Screen.Cart.route) },
                cartCount = state.value.itemCount
            )
        }

        // CARRITO (visual, sin pago)
        composable(Screen.Cart.route) {
            CartScreen(
                state = state.value,
                onInc = vm::inc,
                onDec = vm::dec,
                onRemove = vm::remove,
                onCheckout = { /* vacío */ },
                onBack = { nav.popBackStack() },
                onGoHome = { nav.navigate("home") },      // 👈 nuevo
                onGoCatalog = { nav.navigate("catalog") }
            )
        }
    }
}
