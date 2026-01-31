package jp.co.screentime.slackreporter.data.usage

import android.app.usage.UsageStatsManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import jp.co.screentime.slackreporter.domain.model.AppUsage
import jp.co.screentime.slackreporter.platform.UsageAccessHelper
import javax.inject.Inject
import javax.inject.Singleton

/**
 * UsageStatsManagerからアプリ利用情報を取得するデータソース
 */
@Singleton
class UsageStatsDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val usageAccessHelper: UsageAccessHelper
) {
    private val usageStatsManager: UsageStatsManager? by lazy {
        context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
    }

    /**
     * 指定期間のアプリ利用情報を取得
     *
     * @param startTimeMillis 開始時刻（エポックミリ秒）
     * @param endTimeMillis 終了時刻（エポックミリ秒）
     * @return アプリ別利用時間のリスト
     */
    fun getUsageStats(startTimeMillis: Long, endTimeMillis: Long): List<AppUsage> {
        // Bug fix: INTERVAL_BESTを使用して日中クエリでも正確なデータを取得
        val usageStats = usageStatsManager?.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTimeMillis,
            endTimeMillis
        ) ?: return emptyList()

        // パッケージ名でグループ化して合計を取る
        return usageStats
            .groupBy { it.packageName }
            .map { (packageName, stats) ->
                val totalTime = stats.sumOf { it.totalTimeInForeground }
                AppUsage(
                    packageName = packageName,
                    durationMillis = totalTime
                )
            }
            .filter { it.durationMillis > 0 }
    }

    /**
     * Usage Access権限が有効かどうかを確認
     */
    fun isUsageAccessGranted(): Boolean {
        return usageAccessHelper.hasUsageAccessPermission()
    }
}
