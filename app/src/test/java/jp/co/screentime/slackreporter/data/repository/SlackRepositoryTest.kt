package jp.co.screentime.slackreporter.data.repository

import io.mockk.*
import jp.co.screentime.slackreporter.data.slack.SlackWebhookClient
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class SlackRepositoryTest {

    private lateinit var slackWebhookClient: SlackWebhookClient
    private lateinit var repository: SlackRepository

    @Before
    fun setup() {
        slackWebhookClient = mockk()
        repository = SlackRepository(slackWebhookClient)
    }

    @Test
    fun `メッセージ送信が成功する`() = runTest {
        val webhookUrl = "https://hooks.slack.com/services/xxx"
        val message = "test message"
        
        coEvery { slackWebhookClient.sendMessage(webhookUrl, message) } returns Result.success(Unit)
        
        val result = repository.sendMessage(webhookUrl, message)
        
        assertTrue(result.isSuccess)
        coVerify { slackWebhookClient.sendMessage(webhookUrl, message) }
    }

    @Test
    fun `メッセージ送信が失敗する`() = runTest {
        val webhookUrl = "https://hooks.slack.com/services/xxx"
        val message = "test message"
        val error = Exception("Network error")
        
        coEvery { slackWebhookClient.sendMessage(webhookUrl, message) } returns Result.failure(error)
        
        val result = repository.sendMessage(webhookUrl, message)
        
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `空のWebhook URLでは送信しない`() = runTest {
        val result = repository.sendMessage("", "message")
        
        assertTrue(result.isFailure)
        coVerify(exactly = 0) { slackWebhookClient.sendMessage(any(), any()) }
    }

    @Test
    fun `空のメッセージでは送信しない`() = runTest {
        val result = repository.sendMessage("https://hooks.slack.com/services/xxx", "")
        
        assertTrue(result.isFailure)
        coVerify(exactly = 0) { slackWebhookClient.sendMessage(any(), any()) }
    }
}
