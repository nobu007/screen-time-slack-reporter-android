# PR-001: Gradle/JaCoCo互換性問題の修正

**優先度**: High  
**カテゴリ**: Build/Quality  
**影響範囲**: テストカバレッジ計測環境  
**推定工数**: 1時間  

## 現状の問題

### 観測された事実
- `./gradlew jacocoTestReport` 実行時に `Type T not present` エラーが発生
- JaCocoReportタスクの生成に失敗
- 現在のテストコードが存在してもカバレッジ計測不可能

### 根本原因
Gradle 8.13とJaCoCo 0.8.12の組み合わせにおける既知の互換性問題。
特に、Gradle内部のReportingAPI変更により、JaCocoReportsContainerImplの生成に失敗する。

**証拠**:
- `@/home/jinno/.windsurf/worktrees/screen-time-slack-reporter-android/screen-time-slack-reporter-android-8ee80409/app/build.gradle.kts:7-12`（JaCoCo設定）
- エラーログ: `Could not create task of type 'JacocoReport'`

## 提案する変更

### オプションA: JaCoCoプラグイン設定の修正（推奨）

**変更内容**:
```kotlin
// app/build.gradle.kts

// JaCoCo設定を一旦削除し、Gradle組み込みサポートを利用
// jacoco { ... } ブロックを削除

// タスク定義を簡素化
tasks.withType<JacocoReport> {
    dependsOn("testDebugUnitTest")
    
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    
    // フィルタ設定はそのまま維持
}
```

**メリット**:
- Gradleデフォルトの仕組みに依存し、互換性問題を回避
- 最小限の変更

**デメリット**:
- JaCoCoバージョンがGradleバンドル版に固定される

### オプションB: Gradle 8.10へのダウングレード

**変更内容**:
```bash
# gradle/wrapper/gradle-wrapper.properties
distributionUrl=https\://services.gradle.org/distributions/gradle-8.10-bin.zip
```

**メリット**:
- 検証済みの安定版

**デメリット**:
- 最新のGradle機能が使えなくなる可能性

## 推奨アクション

**オプションA（JaCoCo設定修正）を推奨**

### 実装手順
1. `app/build.gradle.kts` からJaCoCo明示的バージョン指定を削除
2. JacocoReportタスクを簡素化
3. `./gradlew clean testDebugUnitTest` で既存テストを実行
4. `./gradlew jacocoTestReport` でレポート生成を確認
5. `app/build/reports/jacoco/jacocoTestReport/html/index.html` を開いてカバレッジ確認

### 検証基準
- ✅ `./gradlew jacocoTestReport` がエラーなく完了
- ✅ `app/build/reports/jacoco/` にXML/HTMLレポートが生成される
- ✅ 既存テスト（CoreFunctionVerificationTest等）のカバレッジが表示される

### ロールバック手順
```bash
git checkout app/build.gradle.kts
./gradlew clean
```

## 副作用分析

### 正の影響
- テストカバレッジの可視化が可能になる
- QA-001（カバレッジ >= 70%）の検証が可能に
- CI/CD導入時の品質ゲート設定が容易に

### 負の影響
- なし（設定変更のみ）

## 関連課題
- ISS-001: テストカバレッジが不十分
- GAP-001: カバレッジ計測環境が未構築
- QA-001: テストカバレッジ >= 70%

## 仮定（Assumptions）
- ASM-006: Gradleバンドル版JaCoCoで十分（明示的バージョン指定不要）
  - 根拠: 一般的なAndroidプロジェクトでは問題なし
  - 信頼度: High

---

**注意**: この提案は、現在のGradle設定を分析した結果に基づいています。
実際の互換性問題がより深刻な場合、Gradle全体の更新が必要になる可能性があります。
