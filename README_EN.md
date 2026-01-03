# ScreenTime Slack Reporter

Child device "daily usage time" and "apps used (usage time by app)" automatically notified to Slack at the end of the day.

[日本語版はこちら](README.md)

## Features

- 📊 **Usage Statistics**: Get app-wise usage time from 0:00 of the day using UsageStatsManager
- 🔔 **Slack Notification**: Post to Slack Incoming Webhook once a day at specified time
- ⚙️ **Exclude Apps**: Set apps to exclude from notifications
- 📱 **Manual Send**: Send manually anytime
- 📝 **Send Log**: Display last send time and status

## Setup

### 1. Install the App

Open the project in Android Studio and install it on your device.

```bash
./gradlew installDebug
```

### 2. Grant Usage Access Permission

When you launch the app, you will be asked for permission to access usage data.
Find "ScreenTime Slack Reporter" in the settings and allow access.

### 3. Create Slack Incoming Webhook

1. Tap "Open Slack Developer Page" in the app settings
2. Create a Slack app (or select an existing one)
3. Enable "Incoming Webhooks"
4. Select the destination channel with "Add New Webhook to Workspace"
5. Copy the generated Webhook URL

### 4. Configure Webhook URL

1. Open app settings
2. Paste the Webhook URL
3. Set the send time (default: 21:00)
4. Turn on "Enable automatic sending"
5. Tap "Save Settings"

### 5. Test Send

You can confirm correct configuration with the "Test Send" button.

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Architecture**: MVVM + Repository Pattern
- **DI**: Hilt
- **Async**: Coroutines + Flow
- **Persistence**: Preferences DataStore
- **Scheduling**: WorkManager
- **Network**: OkHttp

## License

MIT License
