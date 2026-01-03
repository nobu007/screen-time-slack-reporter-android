package jp.co.screentime.slackreporter.presentation.exclusions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.co.screentime.slackreporter.data.repository.SettingsRepository
import jp.co.screentime.slackreporter.domain.usecase.GetTodayUsedAppsUseCase
import jp.co.screentime.slackreporter.platform.AppLabelResolver
import jp.co.screentime.slackreporter.presentation.model.UiAppUsage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExclusionsViewModel @Inject constructor(
    private val getTodayUsedAppsUseCase: GetTodayUsedAppsUseCase,
    private val settingsRepository: SettingsRepository,
    private val appLabelResolver: AppLabelResolver
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExclusionsUiState())
    val uiState: StateFlow<ExclusionsUiState> = _uiState.asStateFlow()

    init {
        loadData()
        observeShowExcludedOnly()
    }

    /**
     * データを読み込む
     */
    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val usageList = getTodayUsedAppsUseCase()

                // 除外設定とshowExcludedOnlyを監視
                combine(
                    settingsRepository.settingsFlow,
                    settingsRepository.showExcludedOnlyFlow
                ) { settings, showExcludedOnly ->
                    val apps = usageList.map { usage ->
                        UiAppUsage(
                            packageName = usage.packageName,
                            appName = appLabelResolver.getAppLabel(usage.packageName),
                            icon = appLabelResolver.getAppIcon(usage.packageName),
                            durationMinutes = usage.durationMinutes,
                            isExcluded = usage.packageName in settings.excludedPackages
                        )
                    }
                    Pair(apps, showExcludedOnly)
                }.collect { (apps, showExcludedOnly) ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            apps = apps,
                            showExcludedOnly = showExcludedOnly
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * showExcludedOnlyの変更を監視
     */
    private fun observeShowExcludedOnly() {
        viewModelScope.launch {
            settingsRepository.showExcludedOnlyFlow.collect { showExcludedOnly ->
                _uiState.update { it.copy(showExcludedOnly = showExcludedOnly) }
            }
        }
    }

    /**
     * 「対象外のみ表示」を切り替え
     */
    fun onShowExcludedOnlyChanged(showExcludedOnly: Boolean) {
        viewModelScope.launch {
            settingsRepository.setShowExcludedOnly(showExcludedOnly)
        }
    }

    /**
     * アプリの除外状態を切り替え
     */
    fun onExcludedChanged(packageName: String, excluded: Boolean) {
        viewModelScope.launch {
            settingsRepository.setExcluded(packageName, excluded)

            // UI状態も更新
            _uiState.update { state ->
                state.copy(
                    apps = state.apps.map { app ->
                        if (app.packageName == packageName) {
                            app.copy(isExcluded = excluded)
                        } else {
                            app
                        }
                    }
                )
            }
        }
    }

    /**
     * 一覧を表示（フィルタをOFF）
     */
    fun onShowAllApps() {
        onShowExcludedOnlyChanged(false)
    }
}
