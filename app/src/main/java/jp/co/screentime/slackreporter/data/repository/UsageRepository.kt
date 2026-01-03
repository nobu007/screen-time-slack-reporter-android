package jp.co.screentime.slackreporter.data.repository

import jp.co.screentime.slackreporter.data.usage.UsageStatsDataSource
import jp.co.screentime.slackreporter.domain.model.AppUsage
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 使用状況リポジトリ
 */
@Singleton
class UsageRepository @Inject constructor(
    private val usageStatsDataSource: UsageStatsDataSource
) {
    /**
     * 指定期間の利用状況を取得
     *
     * @param fromMillis 開始時刻（エポックミリ秒）
     * @param toMillis 終了時刻（エポックミリ秒）
     * @return アプリ別利用時間のリスト
     */
    fun getUsage(fromMillis: Long, toMillis: Long): List<AppUsage> {
        return usageStatsDataSource.getUsageStats(fromMillis, toMillis)
    }

    /**
     * Usage Access権限が有効かどうかを確認
     */
    fun isUsageAccessGranted(): Boolean {
        return usageStatsDataSource.isUsageAccessGranted()
    }
}
