package jp.co.screentime.slackreporter.data.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 暗号化SharedPreferencesを使用したセキュアなストレージ
 *
 * Webhook URLなどの秘匿情報を安全に保存する
 */
@Singleton
class EncryptedPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val encryptedPrefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Webhook URLを取得
     */
    fun getWebhookUrl(): String {
        return encryptedPrefs.getString(KEY_WEBHOOK_URL, "") ?: ""
    }

    /**
     * Webhook URLを保存
     */
    fun setWebhookUrl(url: String) {
        encryptedPrefs.edit().putString(KEY_WEBHOOK_URL, url).apply()
    }

    /**
     * 移行済みフラグを取得
     */
    fun isMigrated(): Boolean {
        return encryptedPrefs.getBoolean(KEY_MIGRATION_DONE, false)
    }

    /**
     * 移行済みフラグを設定
     */
    fun setMigrated() {
        encryptedPrefs.edit().putBoolean(KEY_MIGRATION_DONE, true).apply()
    }

    companion object {
        private const val ENCRYPTED_PREFS_FILE = "encrypted_settings"
        private const val KEY_WEBHOOK_URL = "webhook_url"
        private const val KEY_MIGRATION_DONE = "migration_done"
    }
}
