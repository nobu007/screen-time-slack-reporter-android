package jp.co.screentime.slackreporter.presentation.model

import android.graphics.drawable.Drawable

/**
 * UI表示用のアプリ利用情報
 *
 * @property packageName パッケージ名
 * @property appName アプリ名（表示名）
 * @property icon アプリアイコン
 * @property durationMinutes 利用時間（分）
 * @property isExcluded 除外対象かどうか
 */
data class UiAppUsage(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val durationMinutes: Int,
    val isExcluded: Boolean
) {
    /**
     * フォーマットされた利用時間
     */
    val formattedDuration: String
        get() = when {
            durationMinutes < 1 -> "1分未満"
            durationMinutes < 60 -> "${durationMinutes}分"
            else -> {
                val hours = durationMinutes / 60
                val mins = durationMinutes % 60
                if (mins > 0) "${hours}時間${mins}分" else "${hours}時間"
            }
        }
}
