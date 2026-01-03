package jp.co.screentime.slackreporter.domain.model

/**
 * アプリ設定
 *
 * @property webhookUrl Slack Webhook URL
 * @property sendEnabled 自動送信が有効かどうか
 * @property sendHour 送信時刻（時）
 * @property sendMinute 送信時刻（分）
 * @property excludedPackages 除外パッケージ一覧
 */
data class AppSettings(
    val webhookUrl: String = "",
    val sendEnabled: Boolean = false,
    val sendHour: Int = 21,
    val sendMinute: Int = 0,
    val excludedPackages: Set<String> = emptySet()
) {
    /**
     * Webhook URLが設定されているかどうか
     */
    val isWebhookConfigured: Boolean
        get() = webhookUrl.isNotBlank()

    /**
     * 送信可能かどうか（Webhook設定済み & 送信有効）
     */
    val canSend: Boolean
        get() = isWebhookConfigured && sendEnabled
}
