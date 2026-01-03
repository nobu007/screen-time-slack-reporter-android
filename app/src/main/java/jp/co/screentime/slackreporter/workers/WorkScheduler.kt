package jp.co.screentime.slackreporter.workers

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * WorkManagerのスケジュール管理
 */
@Singleton
class WorkScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    /**
     * 日次Workerをスケジュール（または更新）
     *
     * @param targetHour 送信時刻（時）
     * @param targetMinute 送信時刻（分）
     */
    fun scheduleOrUpdateDailyWorker(targetHour: Int, targetMinute: Int) {
        // 初回実行までの遅延を計算
        val initialDelay = calculateInitialDelay(targetHour, targetMinute)

        // 制約：ネットワーク接続が必要
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // 24時間ごとの定期実行
        val workRequest = PeriodicWorkRequestBuilder<DailySlackReportWorker>(
            repeatInterval = 24,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag(DailySlackReportWorker.WORK_NAME)
            .build()

        // Unique Periodic Workとして登録（既存があれば置き換え）
        workManager.enqueueUniquePeriodicWork(
            DailySlackReportWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    /**
     * 日次Workerをキャンセル
     */
    fun cancelDailyWorker() {
        workManager.cancelUniqueWork(DailySlackReportWorker.WORK_NAME)
    }

    /**
     * 初回実行までの遅延を計算
     *
     * @param targetHour 目標時刻（時）
     * @param targetMinute 目標時刻（分）
     * @return 遅延ミリ秒
     */
    private fun calculateInitialDelay(targetHour: Int, targetMinute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, targetMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // 既に今日の目標時刻を過ぎていたら翌日にする
        if (target.before(now)) {
            target.add(Calendar.DAY_OF_MONTH, 1)
        }

        return target.timeInMillis - now.timeInMillis
    }
}
