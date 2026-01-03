package jp.co.screentime.slackreporter.domain.model

/**
 * 送信ステータス
 */
enum class SendStatus {
    NOT_SENT,
    SUCCESS,
    FAILED
}

/**
 * 送信結果
 *
 * @property status 送信ステータス
 * @property lastSentEpochMillis 最終送信時刻（エポックミリ秒）
 * @property errorMessage エラーメッセージ（失敗時のみ）
 */
data class SendResult(
    val status: SendStatus,
    val lastSentEpochMillis: Long? = null,
    val errorMessage: String? = null
)
