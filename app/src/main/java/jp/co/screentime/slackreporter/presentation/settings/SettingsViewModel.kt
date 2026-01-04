package jp.co.screentime.slackreporter.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jp.co.screentime.slackreporter.R
import jp.co.screentime.slackreporter.data.repository.SettingsRepository
import jp.co.screentime.slackreporter.data.repository.SlackRepository
import jp.co.screentime.slackreporter.data.slack.SlackWebhookValidator
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
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val slackRepository: SlackRepository,
    private val workScheduler: WorkScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun normalizeWebhookUrl(url: String): String {
        return url.trim()
    }

    private fun validateWebhookUrl(url: String, requireNonBlank: Boolean): String? {
        val normalizedUrl = normalizeWebhookUrl(url)
        if (normalizedUrl.isBlank()) {
            return if (requireNonBlank) {
                context.getString(R.string.settings_webhook_not_set)
            } else {
                null
            }
        }

        return if (SlackWebhookValidator.validate(normalizedUrl).isSuccess) {
            null
        } else {
            context.getString(R.string.settings_webhook_invalid)
        }
    }

    /**
     * 設定を読み込む
     */
    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val settings = settingsRepository.settingsFlow.first()
            val normalizedWebhookUrl = normalizeWebhookUrl(settings.webhookUrl)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    webhookUrl = normalizedWebhookUrl,
                    sendEnabled = settings.sendEnabled,
                    sendHour = settings.sendHour,
                    sendMinute = settings.sendMinute,
                    // 初期値も設定
                    initialWebhookUrl = normalizedWebhookUrl,
                    initialSendEnabled = settings.sendEnabled,
                    initialSendHour = settings.sendHour,
                    initialSendMinute = settings.sendMinute,
                    webhookError = null
                )
            }
        }
    }

    /**
     * Webhook URLを更新
     */
    fun onWebhookUrlChanged(url: String) {
        _uiState.update { it.copy(webhookUrl = url, webhookError = null) }
    }

    /**
     * 送信有効/無効を切り替え
     */
    fun onSendEnabledChanged(enabled: Boolean) {
        _uiState.update {
            it.copy(
                sendEnabled = enabled,
                webhookError = if (!enabled) null else it.webhookError
            )
        }
    }

    /**
     * 送信時刻を更新
     */
    fun onSendTimeChanged(hour: Int, minute: Int) {
        _uiState.update { it.copy(sendHour = hour, sendMinute = minute) }
    }

    /**
     * 設定を保存
     */
    fun onSaveSettings() {
        viewModelScope.launch {
            val state = _uiState.value
            val normalizedWebhookUrl = normalizeWebhookUrl(state.webhookUrl)
            val validationError = validateWebhookUrl(
                normalizedWebhookUrl,
                requireNonBlank = state.sendEnabled
            )

            if (validationError != null) {
                _uiState.update {
                    it.copy(
                        webhookUrl = normalizedWebhookUrl,
                        webhookError = validationError
                    )
                }
                return@launch
            }

            settingsRepository.setWebhookUrl(normalizedWebhookUrl)
            settingsRepository.setSendEnabled(state.sendEnabled)
            settingsRepository.setSendTime(state.sendHour, state.sendMinute)

            // WorkManagerのスケジュールを更新
            if (state.sendEnabled && normalizedWebhookUrl.isNotBlank()) {
                workScheduler.scheduleOrUpdateDailyWorker(state.sendHour, state.sendMinute)
            } else {
                workScheduler.cancelDailyWorker()
            }

            // 保存が完了したので、初期値を更新
            _uiState.update {
                it.copy(
                    isSaved = true,
                    webhookUrl = normalizedWebhookUrl,
                    initialWebhookUrl = normalizedWebhookUrl,
                    initialSendEnabled = state.sendEnabled,
                    initialSendHour = state.sendHour,
                    initialSendMinute = state.sendMinute,
                    webhookError = null
                )
            }
        }
    }

    /**
     * テスト送信
     */
    fun onClickTestWebhook() {
        viewModelScope.launch {
            val rawWebhookUrl = _uiState.value.webhookUrl
            val normalizedWebhookUrl = normalizeWebhookUrl(rawWebhookUrl)
            if (normalizedWebhookUrl != rawWebhookUrl) {
                _uiState.update { it.copy(webhookUrl = normalizedWebhookUrl) }
            }
            val validationError = validateWebhookUrl(
                normalizedWebhookUrl,
                requireNonBlank = true
            )
            if (validationError != null) {
                _uiState.update {
                    it.copy(
                        webhookError = validationError,
                        testResult = TestResult.Failure(validationError)
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    isTesting = true,
                    testResult = null,
                    webhookError = null,
                    webhookUrl = normalizedWebhookUrl
                )
            }

            val result = slackRepository.sendTestMessage(normalizedWebhookUrl)

            _uiState.update {
                it.copy(
                    isTesting = false,
                    testResult = if (result.isSuccess) {
                        TestResult.Success
                    } else {
                        TestResult.Failure(result.exceptionOrNull()?.message ?: context.getString(R.string.common_unknown_error))
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
