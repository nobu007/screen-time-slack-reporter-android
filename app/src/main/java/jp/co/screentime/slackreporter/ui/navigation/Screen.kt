package jp.co.screentime.slackreporter.ui.navigation

/**
 * ナビゲーション先の定義
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Settings : Screen("settings")
    data object Exclusions : Screen("exclusions")
}
