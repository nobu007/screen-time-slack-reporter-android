package jp.co.screentime.slackreporter.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hiltのモジュール定義
 *
 * 現時点では@Injectでほぼ自動的にDIできるため、
 * 特別なバインディングは不要。拡張用に空のモジュールを用意。
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule
