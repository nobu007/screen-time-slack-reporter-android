package jp.co.screentime.slackreporter.domain.usecase

import jp.co.screentime.slackreporter.domain.model.AppUsage
import javax.inject.Inject

/**
 * 今日使ったアプリ一覧を取得するユースケース
 *
 * 除外設定画面で表示する「今日使ったアプリ」を取得する
 */
class GetTodayUsedAppsUseCase @Inject constructor(
    private val getTodayUsageUseCase: GetTodayUsageUseCase
) {
    /**
     * 今日使ったアプリ一覧を取得
     *
     * @return 利用があったアプリのリスト（利用時間降順）
     */
    suspend operator fun invoke(): List<AppUsage> {
        return getTodayUsageUseCase()
    }
}
