# PR-002: テストカバレッジ拡充計画

**優先度**: High  
**カテゴリ**: Quality  
**影響範囲**: 全レイヤー（ViewModel, UseCase, Repository）  
**推定工数**: 8-12時間  

## 現状の問題

### 観測された事実
- テストファイル2つのみ存在:
  - `CoreFunctionVerificationTest.kt` (5テストケース)
  - `SlackWebhookValidatorTest.kt` (詳細不明)
- プロダクションコード39ファイルに対してテストカバレッジが極めて低い
- 主要なビジネスロジック（ViewModel, UseCase）が未検証

### リスク
- リファクタリング時のリグレッション検出不能
- バグ修正時の影響範囲が不明
- コードの品質担保ができない

**証拠**:
- `@/home/jinno/.windsurf/worktrees/screen-time-slack-reporter-android/screen-time-slack-reporter-android-8ee80409/app/src/test/java/jp/co/screentime/slackreporter/`（テスト2ファイルのみ）
- `@/home/jinno/.windsurf/worktrees/screen-time-slack-reporter-android/screen-time-slack-reporter-android-8ee80409/app/src/main/java/`（プロダクションコード39ファイル）

## 提案する変更

### フェーズ1: UseCase層のテスト追加（最優先）

**対象**:
- `GetTodayUsageUseCase`
- `SendDailyReportUseCase` ← 既にCoreVerificationで部分カバー
- `GetAllAppsUseCase`
- `GetTodayUsedAppsUseCase`

**テンプレート例**:
```kotlin
// GetTodayUsageUseCaseTest.kt
class GetTodayUsageUseCaseTest {
    @Test
    fun `正常系_利用時間リストを取得できる`() = runTest {
        // Given: UsageRepository mock
        val repository = mockk<UsageRepository>()
        coEvery { repository.getTodayUsage() } returns listOf(...)
        
        // When: UseCase実行
        val result = GetTodayUsageUseCase(repository)()
        
        // Then: 期待結果検証
        assertEquals(expected, result)
    }
    
    @Test
    fun `異常系_リポジトリエラー時に空リストを返す`() { ... }
    
    @Test
    fun `境界値_0件の場合`() { ... }
}
```

### フェーズ2: Repository層のテスト追加

**対象**:
- `UsageRepository` ← UsageStatsDataSourceのモック化
- `SettingsRepository` ← DataStoreのモック化
- `SlackRepository` ← OkHttpのモック化
- `AppListRepository`

### フェーズ3: ViewModel層のテスト追加

**対象**:
- `HomeViewModel`
- `SettingsViewModel`
- `ExclusionsViewModel`

**テンプレート例**:
```kotlin
// HomeViewModelTest.kt
class HomeViewModelTest {
    @Test
    fun `手動送信_成功時にStateが更新される`() = runTest {
        // Given
        val sendUseCase = mockk<SendDailyReportUseCase>()
        coEvery { sendUseCase() } returns SendResult(status = SUCCESS)
        
        val viewModel = HomeViewModel(sendUseCase, ...)
        
        // When
        viewModel.sendManually()
        advanceUntilIdle()
        
        // Then
        assertEquals(SUCCESS, viewModel.uiState.value.lastSendStatus)
    }
}
```

### フェーズ4: Data層のテスト追加

**対象**:
- `SlackMessageBuilder` ← メッセージフォーマット検証
- `SlackWebhookValidator` ← 既に存在、拡充の必要性確認
- `DurationFormatter`
- `AppLabelResolver`

## 目標カバレッジ

### レイヤー別目標
| レイヤー | 目標カバレッジ | 理由 |
|---------|--------------|------|
| **UseCase** | 90% | ビジネスロジックの中核 |
| **Repository** | 80% | データアクセスの信頼性 |
| **ViewModel** | 80% | UI状態管理の正確性 |
| **Data Utils** | 70% | ヘルパー関数群 |
| **UI** | 50% | Compose UIテストで補完 |

### 全体目標
- **ユニットテストカバレッジ**: >= 70%（QA-001達成）
- **Line Coverage**: >= 70%
- **Branch Coverage**: >= 60%

## 実装手順

### ステップ1: テストファイル作成
```bash
cd app/src/test/java/jp/co/screentime/slackreporter/
mkdir -p domain/usecase
mkdir -p data/repository
mkdir -p presentation/{home,settings,exclusions}
```

### ステップ2: フェーズ1（UseCase）実装
- 1UseCaseあたり3-5テストケース
- 正常系、異常系、境界値を網羅

### ステップ3: カバレッジ計測
```bash
./gradlew clean testDebugUnitTest jacocoTestReport
```

### ステップ4: カバレッジ確認とギャップ分析
```bash
open app/build/reports/jacoco/jacocoTestReport/html/index.html
```

### ステップ5: 不足分を追加テスト

## 検証基準
- ✅ 全UseCaseに対してテストクラスが存在
- ✅ JaCoCoレポートでLine Coverage >= 70%
- ✅ 全テストがCI環境で実行可能（Android依存を適切にモック）
- ✅ テスト実行時間が5分以内

## 依存関係

### 前提条件
- PR-001（JaCoCo修正）が先に適用されていること

### テストライブラリ（既にbuild.gradle.ktsに存在）
- JUnit 4.x
- MockK 1.x
- Coroutines Test 1.x
- Turbine 1.x（Flow検証用）

## 副作用分析

### 正の影響
- コード品質の可視化
- リファクタリングの安全性向上
- バグの早期発見
- ドキュメントとしての役割（テストが仕様書になる）

### 負の影響
- テストコードメンテナンスコストの増加（ただし長期的にはメリット）
- ビルド時間の増加（数分程度）

## ロールバック手順
```bash
git checkout app/src/test/
./gradlew clean
```

## 関連課題
- ISS-001: テストカバレッジが不十分
- GAP-002: テストカバレッジが目標に達していない
- QA-001: テストカバレッジ >= 70%

## 仮定（Assumptions）
- ASM-007: MockKで十分なモック機能を提供可能
  - 根拠: Kotlin + Coroutinesの標準的なモックライブラリ
  - 信頼度: High

- ASM-008: Android依存コードはモック可能
  - 根拠: UsageStatsManager等はRepositoryレイヤーで隔離済み
  - 信頼度: High

---

**注意**: この計画は段階的実装を想定しています。
まずフェーズ1（UseCase）を完了させ、QA-001を達成してから次フェーズへ進むことを推奨します。
