package jp.co.screentime.slackreporter.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import jp.co.screentime.slackreporter.domain.model.AppSettings
import jp.co.screentime.slackreporter.domain.model.SendResult
import jp.co.screentime.slackreporter.domain.model.SendStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "screen_time_settings"
)

/**
 * DataStoreを使用した設定の永続化
 */
@Singleton
class PreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    /**
     * 設定のFlowを取得
     */
    val settingsFlow: Flow<AppSettings> = dataStore.data.map { prefs ->
        AppSettings(
            webhookUrl = prefs[PreferencesKeys.SLACK_WEBHOOK_URL] ?: "",
            sendEnabled = prefs[PreferencesKeys.SEND_ENABLED] ?: false,
            sendHour = prefs[PreferencesKeys.SEND_HOUR] ?: 21,
            sendMinute = prefs[PreferencesKeys.SEND_MINUTE] ?: 0,
            excludedPackages = prefs[PreferencesKeys.EXCLUDED_PACKAGES] ?: emptySet()
        )
    }

    /**
     * 送信結果のFlowを取得
     */
    val sendResultFlow: Flow<SendResult> = dataStore.data.map { prefs ->
        val statusString = prefs[PreferencesKeys.LAST_SEND_STATUS]
        val status = when (statusString) {
            "success" -> SendStatus.SUCCESS
            "failed" -> SendStatus.FAILED
            else -> SendStatus.NOT_SENT
        }
        SendResult(
            status = status,
            lastSentEpochMillis = prefs[PreferencesKeys.LAST_SEND_EPOCH_MILLIS],
            errorMessage = prefs[PreferencesKeys.LAST_SEND_ERROR]
        )
    }

    /**
     * 「対象外のみ表示」フラグのFlow
     */
    val showExcludedOnlyFlow: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PreferencesKeys.SHOW_EXCLUDED_ONLY] ?: false
    }

    /**
     * Webhook URLを保存
     */
    suspend fun setWebhookUrl(url: String) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.SLACK_WEBHOOK_URL] = url
        }
    }

    /**
     * 送信有効/無効を保存
     */
    suspend fun setSendEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.SEND_ENABLED] = enabled
        }
    }

    /**
     * 送信時刻を保存
     */
    suspend fun setSendTime(hour: Int, minute: Int) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.SEND_HOUR] = hour
            prefs[PreferencesKeys.SEND_MINUTE] = minute
        }
    }

    /**
     * パッケージの除外状態を更新
     */
    suspend fun setExcluded(packageName: String, excluded: Boolean) {
        dataStore.edit { prefs ->
            val current = prefs[PreferencesKeys.EXCLUDED_PACKAGES] ?: emptySet()
            prefs[PreferencesKeys.EXCLUDED_PACKAGES] = if (excluded) {
                current + packageName
            } else {
                current - packageName
            }
        }
    }

    /**
     * 「対象外のみ表示」フラグを保存
     */
    suspend fun setShowExcludedOnly(showExcludedOnly: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.SHOW_EXCLUDED_ONLY] = showExcludedOnly
        }
    }

    /**
     * 送信結果を更新
     */
    suspend fun updateSendResult(status: SendStatus, epochMillis: Long?, errorMessage: String?) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.LAST_SEND_STATUS] = when (status) {
                SendStatus.SUCCESS -> "success"
                SendStatus.FAILED -> "failed"
                SendStatus.NOT_SENT -> "not_sent"
            }
            if (epochMillis != null) {
                prefs[PreferencesKeys.LAST_SEND_EPOCH_MILLIS] = epochMillis
            }
            if (errorMessage != null) {
                prefs[PreferencesKeys.LAST_SEND_ERROR] = errorMessage
            } else {
                prefs.remove(PreferencesKeys.LAST_SEND_ERROR)
            }
        }
    }
}
