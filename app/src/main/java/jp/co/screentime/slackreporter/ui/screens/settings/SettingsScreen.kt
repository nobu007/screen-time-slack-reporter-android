package jp.co.screentime.slackreporter.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jp.co.screentime.slackreporter.R
import jp.co.screentime.slackreporter.presentation.settings.SettingsViewModel
import jp.co.screentime.slackreporter.presentation.settings.TestResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onOpenSlackDeveloperPage: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var showWebhookUrl by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // テスト結果のスナックバー
    LaunchedEffect(uiState.testResult) {
        when (val result = uiState.testResult) {
            is TestResult.Success -> {
                snackbarHostState.showSnackbar(context.getString(R.string.settings_test_success))
                viewModel.clearTestResult()
            }
            is TestResult.Failure -> {
                snackbarHostState.showSnackbar(context.getString(R.string.settings_test_failed, result.message))
                viewModel.clearTestResult()
            }
            null -> {}
        }
    }

    // 保存完了のスナックバー
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            snackbarHostState.showSnackbar(context.getString(R.string.settings_saved))
            viewModel.clearSavedFlag()
        }
    }

    if (showTimePicker) {
        ModernTimePickerDialog(
            initialHour = uiState.sendHour,
            initialMinute = uiState.sendMinute,
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                viewModel.onSendTimeChanged(hour, minute)
                showTimePicker = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.nav_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                SettingsContent(
                    uiState = uiState,
                    showWebhookUrl = showWebhookUrl,
                    onShowWebhookUrlChanged = { showWebhookUrl = it },
                    onWebhookUrlChanged = viewModel::onWebhookUrlChanged,
                    onSendEnabledChanged = viewModel::onSendEnabledChanged,
                    onSendTimeClick = { showTimePicker = true },
                    onSaveSettings = viewModel::onSaveSettings,
                    onTestWebhook = viewModel::onClickTestWebhook,
                    onOpenSlackDeveloperPage = onOpenSlackDeveloperPage
                )
            }
        }
    }
}

@Composable
private fun SettingsContent(
    uiState: jp.co.screentime.slackreporter.presentation.settings.SettingsUiState,
    showWebhookUrl: Boolean,
    onShowWebhookUrlChanged: (Boolean) -> Unit,
    onWebhookUrlChanged: (String) -> Unit,
    onSendEnabledChanged: (Boolean) -> Unit,
    onSendTimeClick: () -> Unit,
    onSaveSettings: () -> Unit,
    onTestWebhook: () -> Unit,
    onOpenSlackDeveloperPage: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Webhook URL入力
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_webhook_url),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.webhookUrl,
                    onValueChange = onWebhookUrlChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.settings_webhook_url_hint)) },
                    visualTransformation = if (showWebhookUrl) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    singleLine = true,
                    isError = uiState.webhookError != null,
                    supportingText = {
                        uiState.webhookError?.let { message ->
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    trailingIcon = {
                        IconButton(onClick = { onShowWebhookUrlChanged(!showWebhookUrl) }) {
                            Text(
                                text = if (showWebhookUrl) stringResource(R.string.settings_webhook_hide) else stringResource(R.string.settings_webhook_show),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 警告メッセージ
                Row(
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.settings_webhook_warning),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Slack開発者ページを開く
                OutlinedButton(
                    onClick = onOpenSlackDeveloperPage,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.settings_open_slack_developer))
                }
            }
        }

        // 送信設定
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // 自動送信ON/OFF
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.settings_send_enabled),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = uiState.sendEnabled,
                        onCheckedChange = onSendEnabledChanged
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 送信時刻
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.settings_send_time),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    OutlinedButton(onClick = onSendTimeClick) {
                        Text(uiState.formattedSendTime)
                    }
                }
            }
        }

        // テスト送信
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Button(
                    onClick = onTestWebhook,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.isWebhookConfigured && uiState.webhookError == null && !uiState.isTesting
                ) {
                    if (uiState.isTesting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(stringResource(R.string.settings_test_webhook))
                }

                if (!uiState.isWebhookConfigured) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.settings_webhook_not_set),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 保存ボタン
        Button(
            onClick = onSaveSettings,
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.hasUnsavedChanges && uiState.webhookError == null
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.settings_save))
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.settings_time_picker_title),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                // 数値入力モードのTimePicker
                TimeInput(state = timePickerState)
                
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text(stringResource(R.string.common_cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onConfirm(timePickerState.hour, timePickerState.minute) }) {
                        Text(stringResource(R.string.common_ok))
                    }
                }
            }
        }
    }
}
