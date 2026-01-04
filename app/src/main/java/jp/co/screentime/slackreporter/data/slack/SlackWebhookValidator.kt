package jp.co.screentime.slackreporter.data.slack

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/**
 * Slack Incoming Webhook URLの検証
 */
object SlackWebhookValidator {
    private const val WEBHOOK_HOST = "hooks.slack.com"
    private const val WEBHOOK_PATH_PREFIX = "/services/"

    fun validate(webhookUrl: String): Result<HttpUrl> {
        val normalizedUrl = webhookUrl.trim()
        val parsedUrl = normalizedUrl.toHttpUrlOrNull()
            ?: return Result.failure(IllegalArgumentException("Webhook URLが不正です"))

        if (parsedUrl.scheme != "https") {
            return Result.failure(IllegalArgumentException("Webhook URLが不正です"))
        }

        if (parsedUrl.host != WEBHOOK_HOST) {
            return Result.failure(IllegalArgumentException("Webhook URLが不正です"))
        }

        if (!parsedUrl.encodedPath.startsWith(WEBHOOK_PATH_PREFIX)) {
            return Result.failure(IllegalArgumentException("Webhook URLが不正です"))
        }

        return Result.success(parsedUrl)
    }
}
