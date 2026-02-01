package jp.co.screentime.slackreporter.data.repository

import io.mockk.*
import jp.co.screentime.slackreporter.data.settings.PreferencesDataStore
import jp.co.screentime.slackreporter.domain.model.AppSettings
import jp.co.screentime.slackreporter.domain.model.SendResult
import jp.co.screentime.slackreporter.domain.model.SendStatus
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SettingsRepositoryTest {

    private lateinit var preferencesDataStore: PreferencesDataStore
    private lateinit var repository: SettingsRepository

    @Before
    fun setup() {
        preferencesDataStore = mockk(relaxed = true)
        
        // Mock flows
        every { preferencesDataStore.settingsFlow } returns flowOf(
            AppSettings(
                webhookUrl = "https://hooks.slack.com/services/xxx",
                sendEnabled = true,
                sendHour = 21,
                sendMinute = 0,
                excludedPackages = emptySet()
            )
        )
        every { preferencesDataStore.sendResultFlow } returns flowOf(
            SendResult(SendStatus.NOT_SENT, null, null)
        )
        every { preferencesDataStore.showExcludedOnlyFlow } returns flowOf(false)
        
        repository = SettingsRepository(preferencesDataStore)
    }

    @Test
    fun `setWebhookUrl - PreferencesDataStoreに委譲される`() = runTest {
        val url = "https://hooks.slack.com/services/T00000000/B00000000/XXXXXXXX"
        
        repository.setWebhookUrl(url)
        
        coVerify { preferencesDataStore.setWebhookUrl(url) }
    }

    @Test
    fun `setSendEnabled - trueを設定`() = runTest {
        repository.setSendEnabled(true)
        
        coVerify { preferencesDataStore.setSendEnabled(true) }
    }

    @Test
    fun `setSendEnabled - falseを設定`() = runTest {
        repository.setSendEnabled(false)
        
        coVerify { preferencesDataStore.setSendEnabled(false) }
    }

    @Test
    fun `setSendTime - 有効な時刻を設定`() = runTest {
        repository.setSendTime(21, 30)
        
        coVerify { preferencesDataStore.setSendTime(21, 30) }
    }

    @Test
    fun `setSendTime - 0時0分を設定`() = runTest {
        repository.setSendTime(0, 0)
        
        coVerify { preferencesDataStore.setSendTime(0, 0) }
    }

    @Test
    fun `setSendTime - 23時59分を設定`() = runTest {
        repository.setSendTime(23, 59)
        
        coVerify { preferencesDataStore.setSendTime(23, 59) }
    }

    @Test
    fun `setExcluded - パッケージを除外`() = runTest {
        val packageName = "com.example.app"
        
        repository.setExcluded(packageName, true)
        
        coVerify { preferencesDataStore.setExcluded(packageName, true) }
    }

    @Test
    fun `setExcluded - パッケージの除外を解除`() = runTest {
        val packageName = "com.example.app"
        
        repository.setExcluded(packageName, false)
        
        coVerify { preferencesDataStore.setExcluded(packageName, false) }
    }

    @Test
    fun `setShowExcludedOnly - trueを設定`() = runTest {
        repository.setShowExcludedOnly(true)
        
        coVerify { preferencesDataStore.setShowExcludedOnly(true) }
    }

    @Test
    fun `setShowExcludedOnly - falseを設定`() = runTest {
        repository.setShowExcludedOnly(false)
        
        coVerify { preferencesDataStore.setShowExcludedOnly(false) }
    }

    @Test
    fun `updateSendResult - 成功ステータスを更新`() = runTest {
        val epochMillis = System.currentTimeMillis()
        
        repository.updateSendResult(SendStatus.SUCCESS, epochMillis, null)
        
        coVerify { preferencesDataStore.updateSendResult(SendStatus.SUCCESS, epochMillis, null) }
    }

    @Test
    fun `updateSendResult - 失敗ステータスをエラーメッセージ付きで更新`() = runTest {
        val epochMillis = System.currentTimeMillis()
        val errorMessage = "Network error"
        
        repository.updateSendResult(SendStatus.FAILED, epochMillis, errorMessage)
        
        coVerify { preferencesDataStore.updateSendResult(SendStatus.FAILED, epochMillis, errorMessage) }
    }

    @Test
    fun `updateSendResult - NOT_SENTステータスを更新`() = runTest {
        repository.updateSendResult(SendStatus.NOT_SENT, null, null)
        
        coVerify { preferencesDataStore.updateSendResult(SendStatus.NOT_SENT, null, null) }
    }

    @Test
    fun `settingsFlow - PreferencesDataStoreのFlowを返す`() {
        val flow = repository.settingsFlow
        
        assertNotNull(flow)
        assertEquals(preferencesDataStore.settingsFlow, flow)
    }

    @Test
    fun `sendResultFlow - PreferencesDataStoreのFlowを返す`() {
        val flow = repository.sendResultFlow
        
        assertNotNull(flow)
        assertEquals(preferencesDataStore.sendResultFlow, flow)
    }

    @Test
    fun `showExcludedOnlyFlow - PreferencesDataStoreのFlowを返す`() {
        val flow = repository.showExcludedOnlyFlow
        
        assertNotNull(flow)
        assertEquals(preferencesDataStore.showExcludedOnlyFlow, flow)
    }
}
