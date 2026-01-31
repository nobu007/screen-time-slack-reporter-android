# PR-003: GitHub Actions CI/CDパイプライン構築

**優先度**: Medium  
**カテゴリ**: DevOps/Quality  
**影響範囲**: プロジェクト全体（自動化）  
**推定工数**: 2-3時間  

## 現状の問題

### 観測された事実
- `.github/workflows/` ディレクトリが空
- ビルド・テスト・Lint実行が手動
- PRレビュー時の品質チェックが未自動化

### リスク
- 品質低下の見逃し
- 手動確認の属人化
- リリース時の不具合混入

**証拠**:
- ISS-004: CI/CDパイプラインなし
- `.github/workflows/` 空ディレクトリ

## 提案する変更

### ワークフロー構成

#### 1. Build & Test Workflow

```yaml
# .github/workflows/ci.yml
name: CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v4
    
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: gradle
    
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
    
    - name: Build with Gradle
      run: ./gradlew assembleDebug
    
    - name: Run unit tests
      run: ./gradlew testDebugUnitTest
    
    - name: Generate test coverage report
      run: ./gradlew jacocoTestReport
    
    - name: Upload coverage to Codecov
      uses: codecov/codecov-action@v4
      with:
        files: ./app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml
        fail_ci_if_error: false
    
    - name: Archive test results
      if: always()
      uses: actions/upload-artifact@v4
      with:
        name: test-results
        path: app/build/test-results/
    
    - name: Archive coverage report
      uses: actions/upload-artifact@v4
      with:
        name: coverage-report
        path: app/build/reports/jacoco/
```

#### 2. Code Quality Workflow

```yaml
# .github/workflows/quality.yml
name: Code Quality

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  lint:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v4
    
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: gradle
    
    - name: Run Android Lint
      run: ./gradlew lint
    
    - name: Upload lint results
      if: always()
      uses: actions/upload-artifact@v4
      with:
        name: lint-results
        path: app/build/reports/lint-results-debug.html
```

#### 3. Coverage Enforcement (Optional)

```yaml
# .github/workflows/coverage-check.yml
name: Coverage Check

on:
  pull_request:
    branches: [ main ]

jobs:
  coverage:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v4
    
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: gradle
    
    - name: Run tests with coverage
      run: ./gradlew jacocoTestReport
    
    - name: Check coverage threshold
      run: |
        COVERAGE=$(grep -oP 'Total.*?(\d+)%' app/build/reports/jacoco/jacocoTestReport/html/index.html | grep -oP '\d+')
        echo "Current coverage: $COVERAGE%"
        if [ "$COVERAGE" -lt 70 ]; then
          echo "Coverage $COVERAGE% is below threshold 70%"
          exit 1
        fi
```

## 実装手順

### ステップ1: ワークフローファイル作成
```bash
mkdir -p .github/workflows
touch .github/workflows/ci.yml
touch .github/workflows/quality.yml
```

### ステップ2: PR-001適用
JaCoCo修正が完了していることを確認

### ステップ3: ローカルテスト
```bash
# ワークフローで実行されるコマンドを手動確認
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew jacocoTestReport
./gradlew lint
```

### ステップ4: コミット&プッシュ
```bash
git add .github/workflows/
git commit -m "feat: Add CI/CD pipeline with GitHub Actions"
git push
```

### ステップ5: PRで動作確認

## 検証基準
- ✅ PR作成時にCI/CD自動実行
- ✅ ビルドエラー時にワークフロー失敗
- ✅ テスト失敗時にワークフロー失敗
- ✅ カバレッジレポートがアーティファクトとして保存される
- ✅ Lint結果がアーティファクトとして保存される

## 副作用分析

### 正の影響
- コード品質の自動チェック
- PRレビューの効率化
- 品質基準の明確化
- デグレーションの早期発見

### 負の影響
- GitHub Actions実行時間（月2000分無料、通常は十分）
- 初期設定コスト

## 依存関係

### 前提条件
- PR-001（JaCoCo修正）が適用済み
- GitHubリポジトリが存在

### 推奨追加設定
- Branch Protection Rules設定（mainブランチ）
  - Require status checks to pass before merging
  - Require CI workflow to pass

## ロールバック手順
```bash
git rm -r .github/workflows/
git commit -m "revert: Remove CI/CD pipeline"
```

## 関連課題
- ISS-004: CI/CDパイプラインなし
- QA-001: テストカバレッジ >= 70%（自動検証）
- QA-003: Lintエラー（自動検証）

## 仮定（Assumptions）
- ASM-009: GitHub Actionsの無料枠で十分
  - 根拠: 個人プロジェクト、月2000分無料
  - 信頼度: High

- ASM-010: Ubuntu latest環境でビルド可能
  - 根拠: Gradleは環境依存が少ない
  - 信頼度: High
