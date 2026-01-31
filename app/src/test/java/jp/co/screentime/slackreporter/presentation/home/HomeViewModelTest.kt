package jp.co.screentime.slackreporter.presentation.home

import android.content.Context
import app.cash.turbine.test
import io.mockk.*
import jp.co.screentime.slackreporter.data.repository.SettingsRepository
import jp.co.screentime.slackreporter.data.repository.UsageRepository
import jp.co.screentime.slackreporter.domain.model.AppSettings
import jp.co.screentime.slackreporter.domain.model.AppUsage
import jp.co.screentime.slackreporter.domain.model.SendResult
import jp.co.screentime.slackreporter.domain.model.SendStatus
import jp.co.screentime.slackreporter.domain.usecase.GetTodayUsageUseCase
import jp.co.screentime.slackreporter.domain.usecase.SendDailyReportUseCase
import jp.co.screentime.slackreporter.platform.AppLabelResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var getTodayUsageUseCase: GetTodayUsageUseCase
    private lateinit var sendDailyReportUseCase: SendDailyReportUseCase
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var usageRepository: UsageRepository
    private lateinit var appLabelResolver: AppLabelResolver
    private lateinit var context: Context
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getTodayUsageUseCase = mockk()
        sendDailyReportUseCase = mockk()
        settingsRepository = mockk(relaxed = true)
        usageRepository = mockk(relaxed = true)
        appLabelResolver = mockk(relaxed = true)
        context = mockk(relaxed = true)
        
        every { settingsRepository.settingsFlow } returns flowOf(
            AppSettings(
                webhookUrl = "https://hooks.slack.com/services/xxx",
                sendEnabled = true,
                sendHour = 21,
                sendMinute = 0,
                excludedPackages = emptySet()
            )
        )
        every { settingsRepository.sendResultFlow } returns flowOf(
            SendResult(SendStatus.NOT_SENT, null, null)
        )
        every { usageRepository.isUsageAccessGranted() } returns true
        
        // Default mock behavior
        coEvery { getTodayUsageUseCase() } returns emptyList()
        every { appLabelResolver.getAppLabel(any()) } returns "App"
        every { appLabelResolver.getAppIcon(any()) } returns null
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `初期状態でUsageDataがロードされる`() = runTest {
        val mockUsage = listOf(
            AppUsage("com.youtube", 1800000L),
            AppUsage("com.chrome", 900000L)
        )
        coEvery { getTodayUsageUseCase() } returns mockUsage
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        viewModel.uiState.test {
            val state = awaitItem()
            if (state.topApps.isEmpty()) {
                 val loadedState = awaitItem()
                 assertEquals(2, loadedState.topApps.size)
                 assertEquals("com.youtube", loadedState.topApps[0].packageName)
            } else {
                 assertEquals(2, state.topApps.size)
                 assertEquals("com.youtube", state.topApps[0].packageName)
            }
        }
    }

    @Test
    fun `手動送信が成功する`() = runTest {
        coEvery { getTodayUsageUseCase() } returns emptyList()
        coEvery { sendDailyReportUseCase() } returns SendResult(
            status = SendStatus.SUCCESS,
            lastSentEpochMillis = 1234567890L,
            errorMessage = null
        )
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        viewModel.onClickSendNow()
        advanceUntilIdle()
        
        coVerify { sendDailyReportUseCase() }
        
        viewModel.uiState.test {
             val state = awaitItem()
             assertEquals(SendStatus.SUCCESS, state.sendStatus)
        }
    }
    
    private fun createViewModel() = HomeViewModel(
        context = context,
        getTodayUsageUseCase = getTodayUsageUseCase,
        sendDailyReportUseCase = sendDailyReportUseCase,
        settingsRepository = settingsRepository,
        usageRepository = usageRepository,
        appLabelResolver = appLabelResolver,
        ioDispatcher = testDispatcher
    )
}
