package jp.co.screentime.slackreporter.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.co.screentime.slackreporter.data.repository.SettingsRepository
import jp.co.screentime.slackreporter.data.repository.SlackRepository
import jp.co.screentime.slackreporter.workers.WorkScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val slackRepository: SlackRepository,
    private val workScheduler: WorkScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    /**
     * 設定を読み込む
     */
    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            settingsRepository.settingsFlow.collect { settings ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        webhookUrl = settings.webhookUrl,
                        webhookUrlMasked = maskWebhookUrl(settings.webhookUrl),
                        sendEnabled = settings.sendEnabled,
                        sendHour = settings.sendHour,
                        sendMinute = settings.sendMinute
                    )
                }
            }
        }
    }

    /**
     * Webhook URLをマスクする
     */
    private fun maskWebhookUrl(url: String): String {
        if (url.isBlank()) return ""
        return if (url.length > 20) {
            url.take(30) + "..." + url.takeLast(10)
        } else {
            url
        }
    }

    /**
     * Webhook URLを更新
     */
    fun onWebhookUrlChanged(url: String) {
        _uiState.update {
            it.copy(
                webhookUrl = url,
                webhookUrlMasked = maskWebhookUrl(url),
                isSaved = false
            )
        }
    }

    /**
     * 送信有効/無効を切り替え
     */
    fun onSendEnabledChanged(enabled: Boolean) {
        _uiState.update { it.copy(sendEnabled = enabled, isSaved = false) }
    }

    /**
     * 送信時刻を更新
     */
    fun onSendTimeChanged(hour: Int, minute: Int) {
        _uiState.update {
            it.copy(
                sendHour = hour,
                sendMinute = minute,
                isSaved = false
            )
        }
    }

    /**
     * 設定を保存
     */
    fun onSaveSettings() {
        viewModelScope.launch {
            val state = _uiState.value

            settingsRepository.setWebhookUrl(state.webhookUrl)
            settingsRepository.setSendEnabled(state.sendEnabled)
            settingsRepository.setSendTime(state.sendHour, state.sendMinute)

            // WorkManagerのスケジュールを更新
            if (state.sendEnabled && state.webhookUrl.isNotBlank()) {
                workScheduler.scheduleOrUpdateDailyWorker(state.sendHour, state.sendMinute)
            } else {
                workScheduler.cancelDailyWorker()
            }

            _uiState.update { it.copy(isSaved = true) }
        }
    }

    /**
     * テスト送信
     */
    fun onClickTestWebhook() {
        viewModelScope.launch {
            val webhookUrl = _uiState.value.webhookUrl
            if (webhookUrl.isBlank()) {
                _uiState.update {
                    it.copy(testResult = TestResult.Failure("Webhook URLが設定されていません"))
                }
                return@launch
            }

            _uiState.update { it.copy(isTesting = true, testResult = null) }

            val result = slackRepository.sendTestMessage(webhookUrl)

            _uiState.update {
                it.copy(
                    isTesting = false,
                    testResult = if (result.isSuccess) {
                        TestResult.Success
                    } else {
                        TestResult.Failure(result.exceptionOrNull()?.message ?: "Unknown error")
                    }
                )
            }
        }
    }

    /**
     * テスト結果をクリア
     */
    fun clearTestResult() {
        _uiState.update { it.copy(testResult = null) }
    }

    /**
     * 保存済みフラグをクリア
     */
    fun clearSavedFlag() {
        _uiState.update { it.copy(isSaved = false) }
    }
}
