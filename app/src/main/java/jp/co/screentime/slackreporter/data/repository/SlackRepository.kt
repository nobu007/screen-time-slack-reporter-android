package jp.co.screentime.slackreporter.data.repository

import jp.co.screentime.slackreporter.data.slack.SlackWebhookClient
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Slackリポジトリ
 */
@Singleton
class SlackRepository @Inject constructor(
    private val slackWebhookClient: SlackWebhookClient
) {
    /**
     * Slackへメッセージを送信
     *
     * @param webhookUrl Webhook URL
     * @param text 送信するテキスト
     * @return 送信結果
     */
    suspend fun sendMessage(webhookUrl: String, text: String): Result<Unit> {
        if (webhookUrl.isBlank()) {
            return Result.failure(IllegalArgumentException("Webhook URL is blank"))
        }
        if (text.isBlank()) {
            return Result.failure(IllegalArgumentException("Message text is blank"))
        }
        return slackWebhookClient.sendMessage(webhookUrl, text)
    }

    /**
     * テスト送信
     *
     * @param webhookUrl Webhook URL
     * @return 送信結果
     */
    suspend fun sendTestMessage(webhookUrl: String): Result<Unit> {
        return slackWebhookClient.sendTestMessage(webhookUrl)
    }
}
