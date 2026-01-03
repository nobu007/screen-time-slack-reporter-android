package jp.co.screentime.slackreporter.presentation.home

import jp.co.screentime.slackreporter.domain.model.SendStatus
import jp.co.screentime.slackreporter.presentation.model.UiAppUsage

/**
 * ホーム画面のUI状態
 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val hasUsageAccess: Boolean = true,
    val totalMinutes: Int = 0,
    val topApps: List<UiAppUsage> = emptyList(),
    val otherMinutes: Int = 0,
    val sendStatus: SendStatus = SendStatus.NOT_SENT,
    val lastSentTimeFormatted: String? = null,
    val isSending: Boolean = false,
    val sendError: String? = null
) {
    /**
     * フォーマットされた合計利用時間
     */
    val formattedTotalTime: String
        get() = when {
            totalMinutes < 1 -> "1分未満"
            totalMinutes < 60 -> "${totalMinutes}分"
            else -> {
                val hours = totalMinutes / 60
                val mins = totalMinutes % 60
                if (mins > 0) "${hours}時間${mins}分" else "${hours}時間"
            }
        }

    /**
     * フォーマットされたその他の利用時間
     */
    val formattedOtherTime: String
        get() = when {
            otherMinutes < 1 -> "1分未満"
            otherMinutes < 60 -> "${otherMinutes}分"
            else -> {
                val hours = otherMinutes / 60
                val mins = otherMinutes % 60
                if (mins > 0) "${hours}時間${mins}分" else "${hours}時間"
            }
        }

    /**
     * 利用があるかどうか
     */
    val hasUsage: Boolean
        get() = totalMinutes > 0 || topApps.isNotEmpty()
}
