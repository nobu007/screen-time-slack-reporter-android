package jp.co.screentime.slackreporter.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import jp.co.screentime.slackreporter.ui.navigation.AppNavGraph
import jp.co.screentime.slackreporter.ui.theme.ScreenTimeSlackReporterTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ScreenTimeSlackReporterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    AppNavGraph(
                        navController = navController,
                        onOpenUsageAccessSettings = ::openUsageAccessSettings,
                        onOpenSlackDeveloperPage = ::openSlackDeveloperPage
                    )
                }
            }
        }
    }

    /**
     * 使用状況アクセス設定画面を開く
     */
    private fun openUsageAccessSettings() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        startActivity(intent)
    }

    /**
     * Slack開発者ページを開く
     */
    private fun openSlackDeveloperPage() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.slack.com/apps"))
        startActivity(intent)
    }
}
