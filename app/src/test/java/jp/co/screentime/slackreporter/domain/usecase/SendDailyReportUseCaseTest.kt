package jp.co.screentime.slackreporter.domain.usecase

import io.mockk.*
import jp.co.screentime.slackreporter.data.repository.SettingsRepository
import jp.co.screentime.slackreporter.data.repository.SlackRepository
import jp.co.screentime.slackreporter.data.slack.SlackMessageBuilder
import jp.co.screentime.slackreporter.domain.model.AppSettings
import jp.co.screentime.slackreporter.domain.model.AppUsage
import jp.co.screentime.slackreporter.domain.model.SendStatus
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class SendDailyReportUseCaseTest {

    @Test
    fun `returns FAILED when webhook is not configured`() = runTest {
        val settingsRepository = mockk<SettingsRepository>()
        val slackRepository = mockk<SlackRepository>()
        val slackMessageBuilder = mockk<SlackMessageBuilder>()
        val getTodayUsageUseCase = mockk<GetTodayUsageUseCase>()

        val settings = mockk<AppSettings> {
            every { isWebhookConfigured } returns false
        }
        every { settingsRepository.settingsFlow } returns flowOf(settings)

        val useCase = SendDailyReportUseCase(
            getTodayUsageUseCase,
            settingsRepository,
            slackRepository,
            slackMessageBuilder
        )

        val result = useCase()

        assertEquals(SendStatus.FAILED, result.status)
        assertNotNull(result.errorMessage)
    }

    @Test
    fun `returns SUCCESS when message is sent successfully`() = runTest {
        val settingsRepository = mockk<SettingsRepository>()
        val slackRepository = mockk<SlackRepository>()
        val slackMessageBuilder = mockk<SlackMessageBuilder>()
        val getTodayUsageUseCase = mockk<GetTodayUsageUseCase>()

        val settings = mockk<AppSettings> {
            every { isWebhookConfigured } returns true
            every { webhookUrl } returns "https://hooks.slack.com/services/xxx"
            every { excludedPackages } returns emptySet()
        }
        every { settingsRepository.settingsFlow } returns flowOf(settings)
        coEvery { getTodayUsageUseCase.invoke() } returns listOf(AppUsage("com.test", 60000L))
        every { slackMessageBuilder.build(any(), any()) } returns "test message"
        coEvery { slackRepository.sendMessage(any(), any()) } returns Result.success(Unit)
        coEvery { settingsRepository.updateSendResult(any(), any(), any()) } just Runs

        val useCase = SendDailyReportUseCase(
            getTodayUsageUseCase,
            settingsRepository,
            slackRepository,
            slackMessageBuilder
        )

        val result = useCase()

        assertEquals(SendStatus.SUCCESS, result.status)
        assertNotNull(result.lastSentEpochMillis)
    }

    @Test
    fun `filters excluded packages from report`() = runTest {
        val settingsRepository = mockk<SettingsRepository>()
        val slackRepository = mockk<SlackRepository>()
        val slackMessageBuilder = mockk<SlackMessageBuilder>()
        val getTodayUsageUseCase = mockk<GetTodayUsageUseCase>()

        val excludedPackage = "com.excluded"
        val settings = mockk<AppSettings> {
            every { isWebhookConfigured } returns true
            every { webhookUrl } returns "https://hooks.slack.com/services/xxx"
            every { excludedPackages } returns setOf(excludedPackage)
        }
        every { settingsRepository.settingsFlow } returns flowOf(settings)
        coEvery { getTodayUsageUseCase.invoke() } returns listOf(
            AppUsage("com.youtube", 1800000L),
            AppUsage(excludedPackage, 900000L)
        )

        val capturedUsage = slot<List<AppUsage>>()
        every { slackMessageBuilder.build(capture(capturedUsage), any()) } returns "message"
        coEvery { slackRepository.sendMessage(any(), any()) } returns Result.success(Unit)
        coEvery { settingsRepository.updateSendResult(any(), any(), any()) } just Runs

        val useCase = SendDailyReportUseCase(
            getTodayUsageUseCase,
            settingsRepository,
            slackRepository,
            slackMessageBuilder
        )

        useCase()

        assertEquals(1, capturedUsage.captured.size)
        assertEquals("com.youtube", capturedUsage.captured[0].packageName)
    }

    @Test
    fun `returns FAILED when slack send fails`() = runTest {
        val settingsRepository = mockk<SettingsRepository>()
        val slackRepository = mockk<SlackRepository>()
        val slackMessageBuilder = mockk<SlackMessageBuilder>()
        val getTodayUsageUseCase = mockk<GetTodayUsageUseCase>()

        val settings = mockk<AppSettings> {
            every { isWebhookConfigured } returns true
            every { webhookUrl } returns "https://hooks.slack.com/services/xxx"
            every { excludedPackages } returns emptySet()
        }
        every { settingsRepository.settingsFlow } returns flowOf(settings)
        coEvery { getTodayUsageUseCase.invoke() } returns listOf(AppUsage("com.test", 60000L))
        every { slackMessageBuilder.build(any(), any()) } returns "test message"
        coEvery { slackRepository.sendMessage(any(), any()) } returns Result.failure(Exception("Network error"))
        coEvery { settingsRepository.updateSendResult(any(), any(), any()) } just Runs

        val useCase = SendDailyReportUseCase(
            getTodayUsageUseCase,
            settingsRepository,
            slackRepository,
            slackMessageBuilder
        )

        val result = useCase()

        assertEquals(SendStatus.FAILED, result.status)
        assertNotNull(result.errorMessage)
    }
}
