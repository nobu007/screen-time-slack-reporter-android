package jp.co.screentime.slackreporter.presentation.exclusions

import android.content.Context
import io.mockk.*
import jp.co.screentime.slackreporter.data.repository.SettingsRepository
import jp.co.screentime.slackreporter.domain.model.App
import jp.co.screentime.slackreporter.domain.model.AppSettings
import jp.co.screentime.slackreporter.domain.model.AppUsage
import jp.co.screentime.slackreporter.domain.usecase.GetAllAppsUseCase
import jp.co.screentime.slackreporter.domain.usecase.GetTodayUsedAppsUseCase
import jp.co.screentime.slackreporter.platform.AppLabelResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExclusionsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var context: Context
    private lateinit var getAllAppsUseCase: GetAllAppsUseCase
    private lateinit var getTodayUsedAppsUseCase: GetTodayUsedAppsUseCase
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var appLabelResolver: AppLabelResolver

    private val settingsFlow = MutableStateFlow(
        AppSettings(
            webhookUrl = "https://hooks.slack.com/services/xxx",
            sendEnabled = true,
            sendHour = 21,
            sendMinute = 0,
            excludedPackages = emptySet()
        )
    )

    private val showExcludedOnlyFlow = MutableStateFlow(false)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        context = mockk(relaxed = true) {
            every { packageName } returns "jp.co.screentime.slackreporter"
            every { getString(any()) } returns "Error"
        }
        getAllAppsUseCase = mockk()
        getTodayUsedAppsUseCase = mockk()
        settingsRepository = mockk {
            every { settingsFlow } returns this@ExclusionsViewModelTest.settingsFlow
            every { showExcludedOnlyFlow } returns this@ExclusionsViewModelTest.showExcludedOnlyFlow
        }
        appLabelResolver = mockk {
            every { getAppIcon(any()) } returns null
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `初期状態はローディング中`() = runTest {
        coEvery { getAllAppsUseCase() } coAnswers {
            kotlinx.coroutines.delay(1000)
            emptyList()
        }
        coEvery { getTodayUsedAppsUseCase() } returns emptyList()

        val viewModel = createViewModel()

        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `アプリ一覧が正しくロードされる`() = runTest {
        val mockApps = listOf(
            App("com.youtube.android", "YouTube"),
            App("com.chrome.android", "Chrome")
        )
        val mockUsage = listOf(
            AppUsage("com.youtube.android", 1800000L),
            AppUsage("com.chrome.android", 900000L)
        )
        coEvery { getAllAppsUseCase() } returns mockApps
        coEvery { getTodayUsedAppsUseCase() } returns mockUsage

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.apps.size)
        assertNull(state.errorMessage)
    }

    @Test
    fun `利用時間順にソートされる`() = runTest {
        val mockApps = listOf(
            App("com.app1", "App1"),
            App("com.app2", "App2"),
            App("com.app3", "App3")
        )
        val mockUsage = listOf(
            AppUsage("com.app1", 600000L),   // 10分
            AppUsage("com.app2", 1800000L),  // 30分
            AppUsage("com.app3", 1200000L)   // 20分
        )
        coEvery { getAllAppsUseCase() } returns mockApps
        coEvery { getTodayUsedAppsUseCase() } returns mockUsage

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("com.app2", state.apps[0].packageName) // 30分
        assertEquals("com.app3", state.apps[1].packageName) // 20分
        assertEquals("com.app1", state.apps[2].packageName) // 10分
    }

    @Test
    fun `除外状態が正しく反映される`() = runTest {
        settingsFlow.value = settingsFlow.value.copy(
            excludedPackages = setOf("com.excluded.app")
        )

        val mockApps = listOf(
            App("com.youtube.android", "YouTube"),
            App("com.excluded.app", "Excluded App")
        )
        val mockUsage = listOf(
            AppUsage("com.youtube.android", 1800000L),
            AppUsage("com.excluded.app", 900000L)
        )
        coEvery { getAllAppsUseCase() } returns mockApps
        coEvery { getTodayUsedAppsUseCase() } returns mockUsage

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        val excludedApp = state.apps.find { it.packageName == "com.excluded.app" }
        val normalApp = state.apps.find { it.packageName == "com.youtube.android" }

        assertNotNull(excludedApp)
        assertTrue(excludedApp!!.isExcluded)
        assertNotNull(normalApp)
        assertFalse(normalApp!!.isExcluded)
    }

    @Test
    fun `onExcludedChangedで除外状態が更新される`() = runTest {
        val mockApps = listOf(App("com.test.app", "Test App"))
        val mockUsage = listOf(AppUsage("com.test.app", 600000L))
        coEvery { getAllAppsUseCase() } returns mockApps
        coEvery { getTodayUsedAppsUseCase() } returns mockUsage
        coEvery { settingsRepository.setExcluded(any(), any()) } just Runs

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onExcludedChanged("com.test.app", true)
        advanceUntilIdle()

        coVerify { settingsRepository.setExcluded("com.test.app", true) }
        assertTrue(viewModel.uiState.value.apps[0].isExcluded)
    }

    @Test
    fun `onShowExcludedOnlyChangedでフィルタが更新される`() = runTest {
        val mockApps = listOf(App("com.test.app", "Test App"))
        coEvery { getAllAppsUseCase() } returns mockApps
        coEvery { getTodayUsedAppsUseCase() } returns emptyList()
        coEvery { settingsRepository.setShowExcludedOnly(any()) } just Runs

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onShowExcludedOnlyChanged(true)
        advanceUntilIdle()

        coVerify { settingsRepository.setShowExcludedOnly(true) }
    }

    @Test
    fun `showExcludedOnlyがtrueの場合filteredAppsは除外アプリのみ`() = runTest {
        settingsFlow.value = settingsFlow.value.copy(
            excludedPackages = setOf("com.excluded.app")
        )
        showExcludedOnlyFlow.value = true

        val mockApps = listOf(
            App("com.normal.app", "Normal App"),
            App("com.excluded.app", "Excluded App")
        )
        val mockUsage = listOf(
            AppUsage("com.normal.app", 1800000L),
            AppUsage("com.excluded.app", 900000L)
        )
        coEvery { getAllAppsUseCase() } returns mockApps
        coEvery { getTodayUsedAppsUseCase() } returns mockUsage

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.showExcludedOnly)
        assertEquals(1, state.filteredApps.size)
        assertEquals("com.excluded.app", state.filteredApps[0].packageName)
    }

    @Test
    fun `onShowAllAppsでフィルタがリセットされる`() = runTest {
        val mockApps = listOf(App("com.test.app", "Test App"))
        coEvery { getAllAppsUseCase() } returns mockApps
        coEvery { getTodayUsedAppsUseCase() } returns emptyList()
        coEvery { settingsRepository.setShowExcludedOnly(any()) } just Runs

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onShowAllApps()
        advanceUntilIdle()

        coVerify { settingsRepository.setShowExcludedOnly(false) }
    }

    @Test
    fun `自分自身のパッケージは除外される`() = runTest {
        val mockApps = listOf(
            App("jp.co.screentime.slackreporter", "This App"),
            App("com.other.app", "Other App")
        )
        coEvery { getAllAppsUseCase() } returns mockApps
        coEvery { getTodayUsedAppsUseCase() } returns emptyList()

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.apps.size)
        assertEquals("com.other.app", state.apps[0].packageName)
    }

    @Test
    fun `エラー発生時はerrorMessageが設定される`() = runTest {
        coEvery { getAllAppsUseCase() } throws RuntimeException("Test error")
        coEvery { getTodayUsedAppsUseCase() } returns emptyList()

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.errorMessage)
    }

    private fun createViewModel() = ExclusionsViewModel(
        context = context,
        getAllAppsUseCase = getAllAppsUseCase,
        getTodayUsedAppsUseCase = getTodayUsedAppsUseCase,
        settingsRepository = settingsRepository,
        appLabelResolver = appLabelResolver,
        ioDispatcher = testDispatcher
    )
}
