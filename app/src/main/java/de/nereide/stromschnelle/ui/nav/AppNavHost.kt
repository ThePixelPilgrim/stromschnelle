package de.nereide.stromschnelle.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import de.nereide.stromschnelle.ui.completed.CompletedScreen
import de.nereide.stromschnelle.ui.detail.TodoDetailScreen
import de.nereide.stromschnelle.ui.edit.TodoEditScreen
import de.nereide.stromschnelle.ui.list.TodoListScreen
import de.nereide.stromschnelle.ui.settings.SettingsScreen

/** Route definitions for the app's single-activity navigation graph. */
object Routes {
    const val LIST = "list"
    const val DETAIL = "detail/{id}"
    const val EDIT_NEW = "edit"
    const val EDIT_EXISTING = "edit/{id}"
    const val COMPLETED = "completed"
    const val SETTINGS = "settings"

    fun detail(id: Long) = "detail/$id"
    fun editExisting(id: Long) = "edit/$id"
}

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.LIST
    ) {
        composable(Routes.LIST) {
            TodoListScreen(
                onOpenTodo = { id -> navController.navigate(Routes.detail(id)) },
                onAddTodo = { navController.navigate(Routes.EDIT_NEW) },
                onOpenCompleted = { navController.navigate(Routes.COMPLETED) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: return@composable
            TodoDetailScreen(
                id = id,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Routes.editExisting(id)) }
            )
        }
        composable(Routes.EDIT_NEW) {
            TodoEditScreen(
                id = null,
                onDone = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.EDIT_EXISTING,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: return@composable
            TodoEditScreen(
                id = id,
                onDone = { navController.popBackStack() }
            )
        }
        composable(Routes.COMPLETED) {
            CompletedScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
