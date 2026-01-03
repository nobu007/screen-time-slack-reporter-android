package jp.co.screentime.slackreporter.data.settings

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

/**
 * DataStoreのキー定義
 */
object PreferencesKeys {
    /** Slack Webhook URL */
    val SLACK_WEBHOOK_URL = stringPreferencesKey("slack_webhook_url")

    /** 自動送信が有効かどうか */
    val SEND_ENABLED = booleanPreferencesKey("send_enabled")

    /** 送信時刻（時） */
    val SEND_HOUR = intPreferencesKey("send_hour")

    /** 送信時刻（分） */
    val SEND_MINUTE = intPreferencesKey("send_minute")

    /** 除外パッケージ一覧 */
    val EXCLUDED_PACKAGES = stringSetPreferencesKey("excluded_packages")

    /** 除外アプリ画面で「対象外のみ表示」かどうか */
    val SHOW_EXCLUDED_ONLY = booleanPreferencesKey("show_excluded_only")

    /** 最終送信時刻（エポックミリ秒） */
    val LAST_SEND_EPOCH_MILLIS = longPreferencesKey("last_send_epoch_millis")

    /** 最終送信ステータス（success/failed/not_sent） */
    val LAST_SEND_STATUS = stringPreferencesKey("last_send_status")

    /** 最終送信エラーメッセージ */
    val LAST_SEND_ERROR = stringPreferencesKey("last_send_error")
}
