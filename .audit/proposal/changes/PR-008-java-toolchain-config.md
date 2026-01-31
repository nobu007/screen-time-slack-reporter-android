# PR-008: Java Toolchain設定 (環境互換性問題解消)

## 対象課題
- **ISS-005**: Java 24互換性問題
- **GAP-005**: Java 24環境でビルド・テスト実行不可

## 背景
Java 24環境でGradle 8.13/AGPとの互換性問題により、`./gradlew test` が実行不可。
開発環境とCI/CD環境の両方で影響する可能性がある。

## 変更内容

### gradle.properties
```properties
# Java toolchain設定（追加）
# AGP 8.x と Gradle 8.13 は Java 17 toolchain を推奨
org.gradle.java.home=
kotlin.daemon.jvmargs=-Xmx2048m -XX:+UseParallelGC
org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=512m -XX:+HeapDumpOnOutOfMemoryError

# Java toolchain version（追加）
# このプロジェクトは Java 17 でビルド・実行される
# ホストJDKのバージョンに依存しない
kotlin.jvm.target.validation.mode=warning
```

### app/build.gradle.kts (既存のtoolchain設定を確認)
```kotlin
kotlin {
    jvmToolchain(17)  // 現在は21だが、17に変更を推奨
}
```

**変更推奨:**
```kotlin
kotlin {
    jvmToolchain(17)  // AGP 8.x と完全互換
}
```

## 期待効果
- ✅ Java 24環境でも `./gradlew test` が実行可能
- ✅ CI/CD環境での互換性問題を回避
- ✅ 開発者のホストJDKバージョンに依存しない

## リスクと副作用
- **低リスク**: Java 17 は Android開発の標準LTS版
- **注意点**: 既存の開発環境で Java 17 JDK がインストールされていない場合、Gradleが自動ダウンロードする（初回のみ時間がかかる）

## 検証方法
```bash
# 1. 設定変更後、キャッシュをクリーンアップ
./gradlew clean

# 2. テスト実行
./gradlew test

# 期待結果: Java 24環境でもテストが正常実行される
```

## ロールバック
```bash
# gradle.properties と build.gradle.kts を元に戻す
git checkout gradle.properties app/build.gradle.kts
./gradlew clean
```

## 優先度
**High** - 開発環境でテストが実行できないため最優先

## 実装コスト
**小** (1行変更)

## 関連PR
- なし（独立した環境修正）
