package jp.co.screentime.slackreporter.domain.usecase

import io.mockk.*
import jp.co.screentime.slackreporter.data.repository.SettingsRepository
import jp.co.screentime.slackreporter.data.repository.SlackRepository
import jp.co.screentime.slackreporter.data.slack.SlackMessageBuilder
import jp.co.screentime.slackreporter.domain.model.AppSettings
import jp.co.screentime.slackreporter.domain.model.AppUsage
import jp.co.screentime.slackreporter.domain.model.SendStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SendDailyReportUseCaseTest {

    private lateinit var getTodayUsageUseCase: GetTodayUsageUseCase
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var slackRepository: SlackRepository
    private lateinit var slackMessageBuilder: SlackMessageBuilder
    private lateinit var useCase: SendDailyReportUseCase

    @Before
    fun setup() {
        getTodayUsageUseCase = mockk()
        settingsRepository = mockk(relaxed = true)
        slackRepository = mockk()
        slackMessageBuilder = mockk()
        
        useCase = SendDailyReportUseCase(
            getTodayUsageUseCase,
            settingsRepository,
            slackRepository,
            slackMessageBuilder
        )
    }

    @Test
    fun `returns FAILED when webhook URL is not configured`() = runTest {
        val settings = AppSettings(
            webhookUrl = "",
            sendEnabled = false,
            sendHour = 21,
            sendMinute = 0,
            excludedPackages = emptySet()
        )
        every { settingsRepository.settingsFlow } returns flowOf(settings)

        val result = useCase()

        assertEquals(SendStatus.FAILED, result.status)
        assertEquals("Webhook URLが設定されていません", result.errorMessage)
    }

    @Test
    fun `returns SUCCESS when message is sent successfully`() = runTest {
        val webhookUrl = "https://hooks.slack.com/services/T00/B00/XXX"
        val settings = AppSettings(
            webhookUrl = webhookUrl,
            sendEnabled = true,
            sendHour = 21,
            sendMinute = 0,
            excludedPackages = emptySet()
        )
        val usageList = listOf(AppUsage("com.youtube", 3600000L))
        val message = "Test message"

        every { settingsRepository.settingsFlow } returns flowOf(settings)
        coEvery { getTodayUsageUseCase() } returns usageList
        every { slackMessageBuilder.build(usageList, any()) } returns message
        coEvery { slackRepository.sendMessage(webhookUrl, message) } returns Result.success(Unit)

        val result = useCase()

        assertEquals(SendStatus.SUCCESS, result.status)
        assertNotNull(result.lastSentEpochMillis)
        assertNull(result.errorMessage)
    }

    @Test
    fun `returns FAILED when Slack API returns error`() = runTest {
        val webhookUrl = "https://hooks.slack.com/services/T00/B00/XXX"
        val settings = AppSettings(
            webhookUrl = webhookUrl,
            sendEnabled = true,
            sendHour = 21,
            sendMinute = 0,
            excludedPackages = emptySet()
        )
        val usageList = listOf(AppUsage("com.youtube", 3600000L))
        val message = "Test message"
        val errorMessage = "Network error"

        every { settingsRepository.settingsFlow } returns flowOf(settings)
        coEvery { getTodayUsageUseCase() } returns usageList
        every { slackMessageBuilder.build(usageList, any()) } returns message
        coEvery { slackRepository.sendMessage(webhookUrl, message) } returns Result.failure(Exception(errorMessage))

        val result = useCase()

        assertEquals(SendStatus.FAILED, result.status)
        assertEquals(errorMessage, result.errorMessage)
    }

    @Test
    fun `filters excluded packages from usage list`() = runTest {
        val webhookUrl = "https://hooks.slack.com/services/T00/B00/XXX"
        val excludedPackage = "com.excluded"
        val settings = AppSettings(
            webhookUrl = webhookUrl,
            sendEnabled = true,
            sendHour = 21,
            sendMinute = 0,
            excludedPackages = setOf(excludedPackage)
        )
        val usageList = listOf(
            AppUsage("com.youtube", 3600000L),
            AppUsage(excludedPackage, 1800000L)
        )
        val message = "Test message"

        every { settingsRepository.settingsFlow } returns flowOf(settings)
        coEvery { getTodayUsageUseCase() } returns usageList
        every { slackMessageBuilder.build(match { it.size == 1 && it[0].packageName == "com.youtube" }, any()) } returns message
        coEvery { slackRepository.sendMessage(webhookUrl, message) } returns Result.success(Unit)

        val result = useCase()

        assertEquals(SendStatus.SUCCESS, result.status)
        verify { slackMessageBuilder.build(match { 
            it.size == 1 && it[0].packageName == "com.youtube" 
        }, any()) }
    }

    @Test
    fun `handles exception during send gracefully`() = runTest {
        val webhookUrl = "https://hooks.slack.com/services/T00/B00/XXX"
        val settings = AppSettings(
            webhookUrl = webhookUrl,
            sendEnabled = true,
            sendHour = 21,
            sendMinute = 0,
            excludedPackages = emptySet()
        )
        val usageList = listOf(AppUsage("com.youtube", 3600000L))
        val message = "Test message"

        every { settingsRepository.settingsFlow } returns flowOf(settings)
        coEvery { getTodayUsageUseCase() } returns usageList
        every { slackMessageBuilder.build(usageList, any()) } returns message
        coEvery { slackRepository.sendMessage(webhookUrl, message) } throws RuntimeException("Unexpected error")

        val result = useCase()

        assertEquals(SendStatus.FAILED, result.status)
        assertEquals("Unexpected error", result.errorMessage)
    }
}
