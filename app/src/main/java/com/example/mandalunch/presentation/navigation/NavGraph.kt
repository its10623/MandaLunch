package com.example.mandalunch.presentation.navigation

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mandalunch.presentation.ui.screen.FavoriteScreen
import com.example.mandalunch.presentation.ui.screen.HistoryScreen
import com.example.mandalunch.presentation.ui.screen.MandalartScreen
import com.example.mandalunch.presentation.ui.screen.MenuEditScreen
import com.example.mandalunch.presentation.ui.screen.MenuSelectScreen
import com.example.mandalunch.presentation.ui.screen.RestaurantScreen
import com.example.mandalunch.presentation.ui.screen.ResultScreen

object Routes {
    const val MANDALART = "mandalart"
    const val MENU_SELECT = "menu_select"
    const val MENU_EDIT = "menu_edit"
    const val RESULT = "result"
    const val RESTAURANT = "restaurant"
    const val HISTORY = "history"
    const val FAVORITES = "favorites"
    const val ARG_CATEGORY_ID = "categoryId"
    const val ARG_MENU_NAME = "menuName"
    const val ARG_RESTAURANT_MENU = "restaurantMenu"
    const val ARG_RESTAURANT_CATEGORY = "restaurantCategory"

    fun menuSelect(categoryId: Int): String = "$MENU_SELECT/$categoryId"
    fun menuEdit(categoryId: Int): String = "$MENU_EDIT/$categoryId"
    fun result(categoryId: Int, menuName: String): String =
        "$RESULT/$categoryId/${Uri.encode(menuName)}"
    fun restaurant(menuName: String, categoryName: String): String =
        "$RESTAURANT/${Uri.encode(menuName)}/${Uri.encode(categoryName)}"
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.MANDALART
    ) {
        composable(Routes.MANDALART) { backStackEntry ->
            val autoSpin by backStackEntry.savedStateHandle
                .getStateFlow("autoSpin", false)
                .collectAsStateWithLifecycle()
            MandalartScreen(
                onNavigateToMenuSelect = { categoryId ->
                    navController.navigate(Routes.menuSelect(categoryId))
                },
                onNavigateToMenuEdit = { categoryId ->
                    navController.navigate(Routes.menuEdit(categoryId))
                },
                onNavigateToHistory = {
                    navController.navigate(Routes.HISTORY)
                },
                onNavigateToFavorites = {
                    navController.navigate(Routes.FAVORITES)
                },
                autoSpin = autoSpin,
                onAutoSpinConsumed = {
                    backStackEntry.savedStateHandle.remove<Boolean>("autoSpin")
                }
            )
        }
        composable(
            route = "${Routes.MENU_EDIT}/{${Routes.ARG_CATEGORY_ID}}",
            arguments = listOf(
                navArgument(Routes.ARG_CATEGORY_ID) { type = NavType.IntType }
            )
        ) {
            MenuEditScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.HISTORY) {
            HistoryScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.FAVORITES) {
            FavoriteScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "${Routes.MENU_SELECT}/{${Routes.ARG_CATEGORY_ID}}",
            arguments = listOf(
                navArgument(Routes.ARG_CATEGORY_ID) { type = NavType.IntType }
            )
        ) {
            MenuSelectScreen(
                onBack = { navController.popBackStack() },
                onNavigateToResult = { categoryId, menuName ->
                    navController.navigate(Routes.result(categoryId, menuName))
                }
            )
        }
        composable(
            route = "${Routes.RESULT}/{${Routes.ARG_CATEGORY_ID}}/{${Routes.ARG_MENU_NAME}}",
            arguments = listOf(
                navArgument(Routes.ARG_CATEGORY_ID) { type = NavType.IntType },
                navArgument(Routes.ARG_MENU_NAME) { type = NavType.StringType }
            )
        ) {
            ResultScreen(
                onNavigateBackToMandalart = {
                    navController.popBackStack(Routes.MANDALART, inclusive = false)
                },
                onNavigateBackToMenuSelect = {
                    navController.popBackStack(
                        route = "${Routes.MENU_SELECT}/{${Routes.ARG_CATEGORY_ID}}",
                        inclusive = false
                    )
                },
                onNavigateToRestaurant = { menuName, categoryName ->
                    navController.navigate(Routes.restaurant(menuName, categoryName))
                }
            )
        }
        composable(
            route = "${Routes.RESTAURANT}/{${Routes.ARG_RESTAURANT_MENU}}/{${Routes.ARG_RESTAURANT_CATEGORY}}",
            arguments = listOf(
                navArgument(Routes.ARG_RESTAURANT_MENU) { type = NavType.StringType },
                navArgument(Routes.ARG_RESTAURANT_CATEGORY) { type = NavType.StringType }
            )
        ) {
            RestaurantScreen(
                onBack = { navController.popBackStack() },
                onRespin = {
                    navController.getBackStackEntry(Routes.MANDALART)
                        .savedStateHandle["autoSpin"] = true
                    navController.popBackStack(Routes.MANDALART, inclusive = false)
                }
            )
        }
    }
}
