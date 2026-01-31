# PR-010: Repository層・Builder層テスト追加 (カバレッジ70%達成)

## 対象課題
- **ISS-001**: テストカバレッジが目標未達 (55-65% → 70%)
- **GAP-006**: テストカバレッジが70%目標に達していない

## 背景
UsageRepositoryは既にテスト済みだが、SlackRepositoryとSlackMessageBuilderがテストされていない。
これらはSlack送信の核となるコンポーネントであり、テスト必須。

推定カバレッジ貢献度: **+5-10%**（PR-009と合わせて70%達成）

## 変更内容

### 新規ファイル: app/src/test/java/jp/co/screentime/slackreporter/data/repository/SlackRepositoryTest.kt

```kotlin
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
```

### 新規ファイル: app/src/test/java/jp/co/screentime/slackreporter/data/slack/SlackMessageBuilderTest.kt

```kotlin
package jp.co.screentime.slackreporter.data.slack

import jp.co.screentime.slackreporter.domain.model.AppUsage
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate

class SlackMessageBuilderTest {

    private val builder = SlackMessageBuilder()

    @Test
    fun `通常のUsageメッセージを生成できる`() {
        val usageList = listOf(
            AppUsage("YouTube", 2700000L),  // 45分
            AppUsage("Chrome", 1800000L),    // 30分
            AppUsage("LINE", 900000L)        // 15分
        )
        val goalMinutes = 30
        
        val message = builder.build(usageList, goalMinutes)
        
        assertNotNull(message)
        assertTrue(message.contains("YouTube"))
        assertTrue(message.contains("45分"))
        assertTrue(message.contains("Chrome"))
        assertTrue(message.contains("30分"))
        assertTrue(message.contains("合計"))
        assertTrue(message.contains("90分"))  // 45+30+15
    }

    @Test
    fun `目標時間との差分が表示される`() {
        val usageList = listOf(AppUsage("YouTube", 3600000L))  // 60分
        val goalMinutes = 30
        
        val message = builder.build(usageList, goalMinutes)
        
        assertTrue(message.contains("+30分") || message.contains("30分オーバー"))
    }

    @Test
    fun `空のUsageリストでもメッセージを生成できる`() {
        val message = builder.build(emptyList(), 30)
        
        assertNotNull(message)
        assertTrue(message.contains("0分") || message.contains("利用なし"))
    }

    @Test
    fun `日付が含まれる`() {
        val usageList = listOf(AppUsage("YouTube", 1800000L))
        val message = builder.build(usageList, 30)
        
        val today = LocalDate.now()
        assertTrue(
            message.contains(today.toString()) || 
            message.contains("${today.monthValue}/${today.dayOfMonth}") ||
            message.contains("${today.year}")
        )
    }

    @Test
    fun `使用時間が長い順にソートされる`() {
        val usageList = listOf(
            AppUsage("App1", 900000L),   // 15分
            AppUsage("App2", 2700000L),  // 45分
            AppUsage("App3", 1800000L)   // 30分
        )
        
        val message = builder.build(usageList, 30)
        
        val app2Index = message.indexOf("App2")
        val app3Index = message.indexOf("App3")
        val app1Index = message.indexOf("App1")
        
        assertTrue(app2Index < app3Index)
        assertTrue(app3Index < app1Index)
    }

    @Test
    fun `アプリが多い場合はトップ10に制限される`() {
        val usageList = (1..20).map { 
            AppUsage("App$it", (1000000 * it).toLong()) 
        }
        
        val message = builder.build(usageList, 30)
        
        // 最小の10個は含まれない
        assertFalse(message.contains("App1"))
        assertFalse(message.contains("App2"))
        // 最大の10個は含まれる
        assertTrue(message.contains("App20"))
        assertTrue(message.contains("App19"))
    }
}
```

## 期待効果
- ✅ Slack送信ロジックが検証される
- ✅ メッセージフォーマットが正しいことを保証
- ✅ テストカバレッジが70%に到達（PR-009と合わせて）

## リスクと副作用
- **なし**: 既存コードは変更しない

## 検証方法
```bash
# テスト実行
./gradlew test --tests "*.SlackRepositoryTest"
./gradlew test --tests "*.SlackMessageBuilderTest"

# 全テスト + カバレッジ計測
./gradlew test jacocoTestReport

# レポート確認
open build/reports/jacoco/jacocoTestReport/html/index.html
# 期待: Total coverage >= 70%
```

## ロールバック
```bash
rm app/src/test/java/jp/co/screentime/slackreporter/data/repository/SlackRepositoryTest.kt
rm app/src/test/java/jp/co/screentime/slackreporter/data/slack/SlackMessageBuilderTest.kt
```

## 優先度
**High** - 70%カバレッジ達成の最終ステップ

## 実装コスト
**中** (2ファイル、各100行程度)

## 関連PR
- PR-009: ViewModel層テストと組み合わせて70%達成
- PR-008: Java toolchain設定（テスト実行の前提条件）

## 実装順序
1. **PR-008**: Java toolchain設定（最優先、テスト実行の前提）
2. **PR-009**: ViewModel層テスト
3. **PR-010**: Repository/Builder層テスト ← 70%達成
4. カバレッジ計測で検証
