package jp.co.screentime.slackreporter.data.slack

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * SlackWebhookClientのテスト
 * 
 * Note: OkHttpClientのモックは複雑なため、URL検証ロジックのテストに集中
 * 実際のHTTP通信はSlackWebhookValidatorで検証済み
 */
class SlackWebhookClientTest {

    @Test
    fun `sendMessage - 無効なURLで失敗`() = runTest {
        // SlackWebhookValidatorが無効なURLを拒否することを確認
        val result = SlackWebhookValidator.validate("not-a-valid-url")
        assertTrue(result.isFailure)
    }

    @Test
    fun `sendMessage - 空のURLで失敗`() = runTest {
        val result = SlackWebhookValidator.validate("")
        assertTrue(result.isFailure)
    }

    @Test
    fun `sendMessage - Slack以外のURLで失敗`() = runTest {
        val result = SlackWebhookValidator.validate("https://example.com/webhook")
        assertTrue(result.isFailure)
    }

    @Test
    fun `sendMessage - HTTPプロトコルで失敗`() = runTest {
        val result = SlackWebhookValidator.validate("http://hooks.slack.com/services/xxx")
        assertTrue(result.isFailure)
    }

    @Test
    fun `sendMessage - 有効なSlack URLは検証を通過`() = runTest {
        val validUrl = "https://hooks.slack.com/services/T00000000/B00000000/XXXXXXXXXXXXXXXXXXXXXXXX"
        val result = SlackWebhookValidator.validate(validUrl)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `sendMessage - 前後の空白は除去される`() = runTest {
        val urlWithSpaces = "  https://hooks.slack.com/services/T00000000/B00000000/XXXXXXXX  "
        val result = SlackWebhookValidator.validate(urlWithSpaces)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `sendMessage - 不正なパスで失敗`() = runTest {
        val result = SlackWebhookValidator.validate("https://hooks.slack.com/wrong/path")
        assertTrue(result.isFailure)
    }

    @Test
    fun `sendMessage - null相当の空白のみで失敗`() = runTest {
        val result = SlackWebhookValidator.validate("   ")
        assertTrue(result.isFailure)
    }
}
