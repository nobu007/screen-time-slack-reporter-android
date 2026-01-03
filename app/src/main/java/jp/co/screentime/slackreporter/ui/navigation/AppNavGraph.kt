package jp.co.screentime.slackreporter.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import jp.co.screentime.slackreporter.ui.screens.exclusions.ExclusionsScreen
import jp.co.screentime.slackreporter.ui.screens.home.HomeScreen
import jp.co.screentime.slackreporter.ui.screens.settings.SettingsScreen

/**
 * アプリのナビゲーショングラフ
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onOpenUsageAccessSettings: () -> Unit,
    onOpenSlackDeveloperPage: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToExclusions = { navController.navigate(Screen.Exclusions.route) },
                onOpenUsageAccessSettings = onOpenUsageAccessSettings
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onOpenSlackDeveloperPage = onOpenSlackDeveloperPage
            )
        }

        composable(Screen.Exclusions.route) {
            ExclusionsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
