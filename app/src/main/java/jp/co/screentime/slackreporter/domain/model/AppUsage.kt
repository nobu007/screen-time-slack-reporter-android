package jp.co.screentime.slackreporter.domain.model

/**
 * アプリ利用情報（ドメインモデル）
 *
 * @property packageName パッケージ名
 * @property durationMillis 利用時間（ミリ秒）
 */
data class AppUsage(
    val packageName: String,
    val durationMillis: Long
) {
    /**
     * 利用時間を分単位で取得
     */
    val durationMinutes: Int
        get() = (durationMillis / 1000 / 60).toInt()

    /**
     * 利用があるかどうか
     */
    val hasUsage: Boolean
        get() = durationMillis > 0
}
