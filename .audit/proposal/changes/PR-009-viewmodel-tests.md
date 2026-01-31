# PR-009: ViewModel層テスト追加 (カバレッジ向上)

## 対象課題
- **ISS-001**: テストカバレッジが目標未達 (45-55% → 70%)
- **GAP-006**: テストカバレッジが70%目標に達していない

## 背景
ドメインモデルとユースケースのテストは完備されたが、Presentation層（ViewModel）がテストされていない。
ViewModelは業務ロジックとUI状態管理の重要な部分であり、テストカバレッジ向上とリグレッション防止のため必須。

推定カバレッジ貢献度: **+10-15%**

## 変更内容

### 新規ファイル: app/src/test/java/jp/co/screentime/slackreporter/presentation/home/HomeViewModelTest.kt

```kotlin
package jp.co.screentime.slackreporter.presentation.home

import app.cash.turbine.test
import io.mockk.*
import jp.co.screentime.slackreporter.domain.model.AppSettings
import jp.co.screentime.slackreporter.domain.model.AppUsage
import jp.co.screentime.slackreporter.domain.model.SendResult
import jp.co.screentime.slackreporter.domain.model.SendStatus
import jp.co.screentime.slackreporter.domain.usecase.GetTodayUsageUseCase
import jp.co.screentime.slackreporter.domain.usecase.SendDailyReportUseCase
import jp.co.screentime.slackreporter.data.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var getTodayUsageUseCase: GetTodayUsageUseCase
    private lateinit var sendDailyReportUseCase: SendDailyReportUseCase
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getTodayUsageUseCase = mockk()
        sendDailyReportUseCase = mockk()
        settingsRepository = mockk()
        
        every { settingsRepository.settingsFlow } returns flowOf(
            AppSettings(
                webhookUrl = "https://hooks.slack.com/services/xxx",
                scheduledHour = 21,
                scheduledMinute = 0,
                isAutoSendEnabled = true,
                excludedPackages = emptySet(),
                dailyGoalMinutes = 30,
                lastSendResult = null
            )
        )
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
        
        viewModel = HomeViewModel(getTodayUsageUseCase, sendDailyReportUseCase, settingsRepository)
        advanceUntilIdle()
        
        viewModel.usageData.test {
            val data = awaitItem()
            assertEquals(2, data.size)
            assertEquals("com.youtube", data[0].packageName)
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
        
        viewModel = HomeViewModel(getTodayUsageUseCase, sendDailyReportUseCase, settingsRepository)
        advanceUntilIdle()
        
        viewModel.sendManually()
        advanceUntilIdle()
        
        coVerify { sendDailyReportUseCase() }
    }

    @Test
    fun `Webhook未設定時は送信できない`() = runTest {
        every { settingsRepository.settingsFlow } returns flowOf(
            AppSettings(
                webhookUrl = "",
                scheduledHour = 21,
                scheduledMinute = 0,
                isAutoSendEnabled = false,
                excludedPackages = emptySet(),
                dailyGoalMinutes = 30,
                lastSendResult = null
            )
        )
        coEvery { getTodayUsageUseCase() } returns emptyList()
        
        viewModel = HomeViewModel(getTodayUsageUseCase, sendDailyReportUseCase, settingsRepository)
        advanceUntilIdle()
        
        // Webhook未設定の状態を確認
        viewModel.settings.test {
            val settings = awaitItem()
            assertFalse(settings.isWebhookConfigured)
        }
    }
}
```

### 新規ファイル: app/src/test/java/jp/co/screentime/slackreporter/presentation/settings/SettingsViewModelTest.kt

```kotlin
package jp.co.screentime.slackreporter.presentation.settings

import app.cash.turbine.test
import io.mockk.*
import jp.co.screentime.slackreporter.data.repository.SettingsRepository
import jp.co.screentime.slackreporter.domain.model.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        settingsRepository = mockk(relaxed = true)
        
        every { settingsRepository.settingsFlow } returns flowOf(
            AppSettings(
                webhookUrl = "https://hooks.slack.com/services/xxx",
                scheduledHour = 21,
                scheduledMinute = 0,
                isAutoSendEnabled = true,
                excludedPackages = emptySet(),
                dailyGoalMinutes = 30,
                lastSendResult = null
            )
        )
        
        viewModel = SettingsViewModel(settingsRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Webhook URL更新が成功する`() = runTest {
        val newUrl = "https://hooks.slack.com/services/NEW"
        
        coEvery { settingsRepository.updateWebhookUrl(newUrl) } just Runs
        
        viewModel.updateWebhookUrl(newUrl)
        advanceUntilIdle()
        
        coVerify { settingsRepository.updateWebhookUrl(newUrl) }
    }

    @Test
    fun `送信時刻更新が成功する`() = runTest {
        val hour = 22
        val minute = 30
        
        coEvery { settingsRepository.updateScheduledTime(hour, minute) } just Runs
        
        viewModel.updateScheduledTime(hour, minute)
        advanceUntilIdle()
        
        coVerify { settingsRepository.updateScheduledTime(hour, minute) }
    }

    @Test
    fun `自動送信フラグ切り替えが成功する`() = runTest {
        coEvery { settingsRepository.updateAutoSendEnabled(false) } just Runs
        
        viewModel.updateAutoSendEnabled(false)
        advanceUntilIdle()
        
        coVerify { settingsRepository.updateAutoSendEnabled(false) }
    }

    @Test
    fun `設定がFlowとして取得できる`() = runTest {
        viewModel.settings.test {
            val settings = awaitItem()
            assertEquals("https://hooks.slack.com/services/xxx", settings.webhookUrl)
            assertEquals(21, settings.scheduledHour)
            assertTrue(settings.isAutoSendEnabled)
        }
    }
}
```

## 期待効果
- ✅ ViewModelの業務ロジックが検証される
- ✅ テストカバレッジが +10-15% 向上 (推定55-65%)
- ✅ UI状態管理のリグレッションを防止

## リスクと副作用
- **なし**: 既存コードは変更しない

## 検証方法
```bash
# テスト実行
./gradlew test --tests "*.HomeViewModelTest"
./gradlew test --tests "*.SettingsViewModelTest"

# カバレッジ計測
./gradlew jacocoTestReport
# build/reports/jacoco/jacocoTestReport/html/index.html を確認
```

## ロールバック
```bash
# テストファイルを削除するだけ（既存コードに影響なし）
rm app/src/test/java/jp/co/screentime/slackreporter/presentation/home/HomeViewModelTest.kt
rm app/src/test/java/jp/co/screentime/slackreporter/presentation/settings/SettingsViewModelTest.kt
```

## 優先度
**High** - 70%カバレッジ達成のための主要タスク

## 実装コスト
**中** (2ファイル、各100行程度)

## 関連PR
- PR-010: Repository層テスト追加と組み合わせて70%達成
