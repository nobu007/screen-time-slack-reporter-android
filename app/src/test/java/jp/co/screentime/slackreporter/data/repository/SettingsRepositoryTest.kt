package jp.co.screentime.slackreporter.data.repository

import io.mockk.*
import jp.co.screentime.slackreporter.data.settings.PreferencesDataStore
import jp.co.screentime.slackreporter.domain.model.AppSettings
import jp.co.screentime.slackreporter.domain.model.SendResult
import jp.co.screentime.slackreporter.domain.model.SendStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SettingsRepositoryTest {

    private lateinit var preferencesDataStore: PreferencesDataStore
    private lateinit var repository: SettingsRepository

    private val testSettings = AppSettings(
        webhookUrl = "https://hooks.slack.com/services/xxx",
        sendEnabled = true,
        sendHour = 21,
        sendMinute = 0,
        excludedPackages = setOf("com.excluded.app")
    )

    private val testSendResult = SendResult(
        status = SendStatus.SUCCESS,
        lastSentEpochMillis = 1234567890L,
        errorMessage = null
    )

    @Before
    fun setup() {
        preferencesDataStore = mockk {
            every { settingsFlow } returns flowOf(testSettings)
            every { sendResultFlow } returns flowOf(testSendResult)
            every { showExcludedOnlyFlow } returns flowOf(false)
        }
        repository = SettingsRepository(preferencesDataStore)
    }

    @Test
    fun `settingsFlowがPreferencesDataStoreから正しく取得される`() = runTest {
        val settings = repository.settingsFlow.first()

        assertEquals(testSettings.webhookUrl, settings.webhookUrl)
        assertEquals(testSettings.sendEnabled, settings.sendEnabled)
        assertEquals(testSettings.sendHour, settings.sendHour)
        assertEquals(testSettings.sendMinute, settings.sendMinute)
        assertEquals(testSettings.excludedPackages, settings.excludedPackages)
    }

    @Test
    fun `sendResultFlowがPreferencesDataStoreから正しく取得される`() = runTest {
        val result = repository.sendResultFlow.first()

        assertEquals(testSendResult.status, result.status)
        assertEquals(testSendResult.lastSentEpochMillis, result.lastSentEpochMillis)
        assertEquals(testSendResult.errorMessage, result.errorMessage)
    }

    @Test
    fun `showExcludedOnlyFlowがPreferencesDataStoreから正しく取得される`() = runTest {
        val showExcludedOnly = repository.showExcludedOnlyFlow.first()

        assertFalse(showExcludedOnly)
    }

    @Test
    fun `setWebhookUrlがPreferencesDataStoreに委譲される`() = runTest {
        val testUrl = "https://hooks.slack.com/services/new"
        coEvery { preferencesDataStore.setWebhookUrl(any()) } just Runs

        repository.setWebhookUrl(testUrl)

        coVerify { preferencesDataStore.setWebhookUrl(testUrl) }
    }

    @Test
    fun `setSendEnabledがPreferencesDataStoreに委譲される`() = runTest {
        coEvery { preferencesDataStore.setSendEnabled(any()) } just Runs

        repository.setSendEnabled(true)

        coVerify { preferencesDataStore.setSendEnabled(true) }
    }

    @Test
    fun `setSendTimeがPreferencesDataStoreに委譲される`() = runTest {
        coEvery { preferencesDataStore.setSendTime(any(), any()) } just Runs

        repository.setSendTime(22, 30)

        coVerify { preferencesDataStore.setSendTime(22, 30) }
    }

    @Test
    fun `setExcludedがPreferencesDataStoreに委譲される`() = runTest {
        coEvery { preferencesDataStore.setExcluded(any(), any()) } just Runs

        repository.setExcluded("com.test.app", true)

        coVerify { preferencesDataStore.setExcluded("com.test.app", true) }
    }

    @Test
    fun `setShowExcludedOnlyがPreferencesDataStoreに委譲される`() = runTest {
        coEvery { preferencesDataStore.setShowExcludedOnly(any()) } just Runs

        repository.setShowExcludedOnly(true)

        coVerify { preferencesDataStore.setShowExcludedOnly(true) }
    }

    @Test
    fun `updateSendResultがPreferencesDataStoreに委譲される`() = runTest {
        coEvery { preferencesDataStore.updateSendResult(any(), any(), any()) } just Runs

        repository.updateSendResult(SendStatus.SUCCESS, 9876543210L, null)

        coVerify { preferencesDataStore.updateSendResult(SendStatus.SUCCESS, 9876543210L, null) }
    }

    @Test
    fun `updateSendResultでエラーメッセージが正しく渡される`() = runTest {
        coEvery { preferencesDataStore.updateSendResult(any(), any(), any()) } just Runs

        repository.updateSendResult(SendStatus.FAILED, null, "Network error")

        coVerify { preferencesDataStore.updateSendResult(SendStatus.FAILED, null, "Network error") }
    }
}
