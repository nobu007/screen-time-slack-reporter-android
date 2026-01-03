package jp.co.screentime.slackreporter.presentation.settings

/**
 * 設定画面のUI状態
 */
data class SettingsUiState(
    val isLoading: Boolean = true,
    val webhookUrl: String = "",
    val webhookUrlMasked: String = "",
    val sendEnabled: Boolean = false,
    val sendHour: Int = 21,
    val sendMinute: Int = 0,
    val isTesting: Boolean = false,
    val testResult: TestResult? = null,
    val isSaved: Boolean = false
) {
    /**
     * Webhook URLが設定されているか
     */
    val isWebhookConfigured: Boolean
        get() = webhookUrl.isNotBlank()

    /**
     * フォーマットされた送信時刻
     */
    val formattedSendTime: String
        get() = String.format("%02d:%02d", sendHour, sendMinute)
}

/**
 * テスト送信結果
 */
sealed class TestResult {
    data object Success : TestResult()
    data class Failure(val message: String) : TestResult()
}
