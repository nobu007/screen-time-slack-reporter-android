package jp.co.screentime.slackreporter.platform

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * パッケージ名からアプリ名・アイコンを解決するヘルパー
 */
@Singleton
class AppLabelResolver @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val packageManager: PackageManager = context.packageManager

<<<<<<< /home/jinno/screen-time-slack-reporter-android/app/src/main/java/jp/co/screentime/slackreporter/platform/AppLabelResolver.kt
    // キャッシュ
    private val labelCache = mutableMapOf<String, String>()
    private val iconCache = mutableMapOf<String, Drawable?>()
=======
    companion object {
        private const val LABEL_CACHE_SIZE = 100
        // アイコンキャッシュの最大サイズ（メモリリーク防止）
        private const val ICON_CACHE_MAX_SIZE = 50
    }

    // キャッシュ
    private val labelCache = mutableMapOf<String, String>()
    // アイコンキャッシュ（LruCacheでメモリ使用量を制限）
    private val iconCache = object : LruCache<String, Drawable?>(ICON_CACHE_MAX_SIZE) {
        override fun sizeOf(key: String, value: Drawable?): Int = 1
    }
>>>>>>> /home/jinno/.windsurf/worktrees/screen-time-slack-reporter-android/screen-time-slack-reporter-android-5f9c6494/app/src/main/java/jp/co/screentime/slackreporter/platform/AppLabelResolver.kt

    /**
     * パッケージ名からアプリ名（ラベル）を取得
     *
     * @param packageName パッケージ名
     * @return アプリ名（取得できない場合はパッケージ名）
     */
    @Synchronized
    fun getAppLabel(packageName: String): String {
        return labelCache.getOrPut(packageName) {
            try {
                val appInfo = packageManager.getApplicationInfo(packageName, 0)
                packageManager.getApplicationLabel(appInfo).toString()
            } catch (e: PackageManager.NameNotFoundException) {
                packageName
            }
        }
    }

    /**
     * パッケージ名からアプリアイコンを取得
     *
     * @param packageName パッケージ名
     * @return アプリアイコン（取得できない場合はnull）
     */
    @Synchronized
    fun getAppIcon(packageName: String): Drawable? {
        val cached = iconCache.get(packageName)
        if (cached != null) {
            return cached
        }
        
        val icon = try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationIcon(appInfo)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
        
        iconCache.put(packageName, icon)
        return icon
    }

    /**
     * キャッシュをクリア
     */
    @Synchronized
    fun clearCache() {
        labelCache.clear()
        iconCache.evictAll()
    }
}
