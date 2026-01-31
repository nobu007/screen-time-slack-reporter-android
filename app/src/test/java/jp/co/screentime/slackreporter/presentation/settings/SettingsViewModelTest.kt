package jp.co.screentime.slackreporter.presentation.settings

import android.content.Context
import app.cash.turbine.test
import io.mockk.*
import jp.co.screentime.slackreporter.data.repository.SettingsRepository
import jp.co.screentime.slackreporter.data.repository.SlackRepository
import jp.co.screentime.slackreporter.domain.model.AppSettings
import jp.co.screentime.slackreporter.workers.WorkScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var context: Context
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var slackRepository: SlackRepository
    private lateinit var workScheduler: WorkScheduler
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        context = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)
        slackRepository = mockk(relaxed = true)
        workScheduler = mockk(relaxed = true)
        
        every { settingsRepository.settingsFlow } returns flowOf(
            AppSettings(
                webhookUrl = "https://hooks.slack.com/services/xxx",
                sendEnabled = true,
                sendHour = 21,
                sendMinute = 0,
                excludedPackages = emptySet()
            )
        )
        
        viewModel = SettingsViewModel(context, settingsRepository, slackRepository, workScheduler)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Webhook URL更新が成功する`() = runTest {
        val newUrl = "https://hooks.slack.com/services/NEW"
        
        coEvery { settingsRepository.setWebhookUrl(newUrl) } just Runs
        
        viewModel.onWebhookUrlChanged(newUrl)
        viewModel.onSaveSettings()
        advanceUntilIdle()
        
        coVerify { settingsRepository.setWebhookUrl(newUrl) }
    }

    @Test
    fun `送信時刻更新が成功する`() = runTest {
        val hour = 22
        val minute = 30
        
        coEvery { settingsRepository.setSendTime(hour, minute) } just Runs
        
        viewModel.onSendTimeChanged(hour, minute)
        viewModel.onSaveSettings()
        advanceUntilIdle()
        
        coVerify { settingsRepository.setSendTime(hour, minute) }
    }

    @Test
    fun `自動送信フラグ切り替えが成功する`() = runTest {
        coEvery { settingsRepository.setSendEnabled(false) } just Runs
        
        viewModel.onSendEnabledChanged(false)
        viewModel.onSaveSettings()
        advanceUntilIdle()
        
        coVerify { settingsRepository.setSendEnabled(false) }
    }

    @Test
    fun `設定がUIStateとして取得できる`() = runTest {
        // Initial load happens in init block
        advanceUntilIdle()
        
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("https://hooks.slack.com/services/xxx", state.webhookUrl)
            assertEquals(21, state.sendHour)
            assertTrue(state.sendEnabled)
        }
    }
}
