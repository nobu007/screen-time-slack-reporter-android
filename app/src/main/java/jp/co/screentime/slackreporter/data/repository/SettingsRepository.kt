package jp.co.screentime.slackreporter.data.repository

import jp.co.screentime.slackreporter.data.settings.PreferencesDataStore
import jp.co.screentime.slackreporter.domain.model.AppSettings
import jp.co.screentime.slackreporter.domain.model.SendResult
import jp.co.screentime.slackreporter.domain.model.SendStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 設定リポジトリ
 */
@Singleton
class SettingsRepository @Inject constructor(
    private val preferencesDataStore: PreferencesDataStore
) {
    /**
     * 設定のFlow
     */
    val settingsFlow: Flow<AppSettings> = preferencesDataStore.settingsFlow

    /**
     * 送信結果のFlow
     */
    val sendResultFlow: Flow<SendResult> = preferencesDataStore.sendResultFlow

    /**
     * 「対象外のみ表示」フラグのFlow
     */
    val showExcludedOnlyFlow: Flow<Boolean> = preferencesDataStore.showExcludedOnlyFlow

    /**
     * Webhook URLを保存
     */
    suspend fun setWebhookUrl(url: String) {
        preferencesDataStore.setWebhookUrl(url)
    }

    /**
     * 送信有効/無効を保存
     */
    suspend fun setSendEnabled(enabled: Boolean) {
        preferencesDataStore.setSendEnabled(enabled)
    }

    /**
     * 送信時刻を保存
     */
    suspend fun setSendTime(hour: Int, minute: Int) {
        preferencesDataStore.setSendTime(hour, minute)
    }

    /**
     * パッケージの除外状態を更新
     */
    suspend fun setExcluded(packageName: String, excluded: Boolean) {
        preferencesDataStore.setExcluded(packageName, excluded)
    }

    /**
     * 「対象外のみ表示」フラグを保存
     */
    suspend fun setShowExcludedOnly(showExcludedOnly: Boolean) {
        preferencesDataStore.setShowExcludedOnly(showExcludedOnly)
    }

    /**
     * 送信結果を更新
     */
    suspend fun updateSendResult(status: SendStatus, epochMillis: Long?, errorMessage: String?) {
        preferencesDataStore.updateSendResult(status, epochMillis, errorMessage)
    }
}
