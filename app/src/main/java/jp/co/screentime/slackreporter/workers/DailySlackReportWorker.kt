package jp.co.screentime.slackreporter.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import jp.co.screentime.slackreporter.data.repository.SettingsRepository
import jp.co.screentime.slackreporter.data.repository.UsageRepository
import jp.co.screentime.slackreporter.domain.usecase.SendDailyReportUseCase
import jp.co.screentime.slackreporter.domain.model.SendStatus
import jp.co.screentime.slackreporter.platform.NotificationHelper
import kotlinx.coroutines.flow.first

/**
 * 日次Slackレポート送信Worker
 *
 * WorkManagerで毎日定時に実行される
 */
@HiltWorker
class DailySlackReportWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val settingsRepository: SettingsRepository,
    private val usageRepository: UsageRepository,
    private val sendDailyReportUseCase: SendDailyReportUseCase,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "daily_slack_report_worker"
    }

    override suspend fun doWork(): Result {
        // 設定を取得
        val settings = settingsRepository.settingsFlow.first()

        // 送信が無効なら終了
        if (!settings.sendEnabled) {
            return Result.success()
        }

        // Webhook URLが未設定なら終了
        if (!settings.isWebhookConfigured) {
            return Result.success()
        }

        // Usage Accessが無効なら失敗（通知を表示）
        if (!usageRepository.isUsageAccessGranted()) {
            notificationHelper.showSlackSendFailureNotification(
                "使用状況へのアクセス権限が無効です。設定から権限を許可してください。"
            )
            return Result.failure()
        }

        // レポートを送信
        val sendResult = sendDailyReportUseCase()

        return when (sendResult.status) {
            SendStatus.SUCCESS -> Result.success()
            SendStatus.FAILED -> {
                val errorMessage = sendResult.errorMessage ?: "不明なエラー"
                // ネットワークエラーの可能性があるためリトライ
                if (runAttemptCount < 3) {
                    Result.retry()
                } else {
                    // 最終失敗時のみ通知を表示
                    notificationHelper.showSlackSendFailureNotification(errorMessage)
                    Result.failure()
                }
            }
            SendStatus.NOT_SENT -> Result.success()
        }
    }
}

