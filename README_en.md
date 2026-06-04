## English | [中文](README.md)

# OverTime-Note

<img src="https://img.shields.io/badge/Android-10%2B-blue" alt="Android 10+"> <img src="https://img.shields.io/badge/Kotlin-1.9.24-purple" alt="Kotlin 1.9.24"> <img src="https://img.shields.io/badge/Jetpack%20Compose-2024.10.01-green" alt="Compose">

A minimalist overtime tracking app designed for professionals, featuring local-first architecture, multi-channel push notifications, calendar sync, and intelligent backup.

## ✨ Core Features

### 📊 Overtime Record Management
- **Three Overtime Types**: Workday overtime, Weekend overtime, Statutory holiday overtime
- **Leave Records**: Support half-day/full-day leave with automatic deduction of corresponding weekend overtime hours
- **Smart Duration Calculation**: Automatically calculate overtime duration with customizable start/end times

### 💰 Salary Calculation
- **Custom Base Salary**: Default base salary 2200 CNY, fully customizable
- **Independent Multipliers**: Set different multipliers for each overtime type (Workday 1.5x, Weekend 2x, Holiday 3x)
- **Real-time Preview**: Display estimated salary in real-time when adding records

### 📅 Calendar Sync
- **Auto Sync**: Overtime records automatically sync to system calendar
- **Standard Format**: Calendar events titled as "Type-Duration"
- **Bidirectional**: View and manage overtime records in calendar

### 🔔 Smart Push Notifications
- **Single Channel**: Choose one channel for push (DingTalk/Feishu/WeCom/WxPusher/Telegram/Discord/Custom)
- **Security Verification**: DingTalk, Feishu, and WeCom support signature key verification
- **One-click Test**: Test push notifications immediately after configuration

### 🗓️ Holiday Management
- **Auto Recognition**: Automatically recognize statutory holidays and compensatory workdays
- **Manual Updates**: Support manual holiday rule updates to adapt to latest policies
- **Smart Judgment**: Correctly identify compensatory workdays as workday overtime

### 💾 Data Backup
- **Local Backup**: Export/Import JSON format backup files
- **Cloud Sync**: WebDAV cloud backup for multi-device synchronization
- **Auto Backup**: Automatically backup configuration changes to local or cloud when enabled

### 🎨 Personalization
- **Theme Switching**: Light/Dark mode, follow system or manual toggle
- **Accent Color**: Multiple accent colors for personalized interface
- **Bottom Bar Style**: Icon+Text / Icon only / Text only modes
- **Quick Report Mode**: One-click to add record screen for efficiency

## 🛠️ Tech Stack

| Tech | Version | Description |
|------|---------|-------------|
| Language | Kotlin 1.9.24 | Modern Android development language |
| UI Framework | Jetpack Compose 2024.10.01 | Declarative UI framework |
| Database | Room 2.6.1 | Local data persistence |
| Network | OkHttp 4.12.0 | Network requests and push |
| Min SDK | 24 (Android 7.0) | Compatible with most devices |
| Target SDK | 35 (Android 15) | Support latest Android features |

## 📁 Project Structure

```
app/src/main/java/com/mars/overtime/
├── database/              # Room database layer
│   ├── OvertimeRecord.kt     # Overtime record entity
│   ├── AppConfig.kt          # App config entity
│   ├── AppDatabase.kt        # Database class
│   ├── OvertimeDao.kt        # Overtime record DAO
│   └── ConfigDao.kt          # Config DAO
├── push/                  # Push and sync
│   ├── PushManager.kt        # Multi-channel push management
│   └── CalendarSyncManager.kt # Calendar sync management
├── ui/                    # User interface
│   ├── MainNav.kt            # Navigation management
│   ├── theme/                # Theme configuration
│   │   ├── Theme.kt
│   │   ├── Color.kt
│   │   └── Type.kt
│   └── screen/               # Screen components
│       ├── HomePage.kt
│       ├── StatisticsPage.kt
│       ├── SettingsPage.kt
│       ├── AddEditRecordPage.kt
│       ├── PushSettingsPage.kt
│       ├── BackupSettingsPage.kt
│       ├── SalarySettingsPage.kt
│       ├── AppearanceSettingsPage.kt
│       ├── CalendarSettingsPage.kt
│       ├── HolidaySettingsPage.kt
│       └── AboutPage.kt
└── util/                  # Utilities
    ├── SalaryCalculator.kt    # Salary calculation
    ├── HolidayManager.kt      # Holiday management
    ├── BackupManager.kt       # Backup & restore
    ├── WebDavManager.kt       # WebDAV operations
    └── DataMigrationUtil.kt   # Data migration
```

## 🚀 Build & Run

### Requirements

- Android Studio Hedgehog (2023.1.1) or higher
- JDK 17
- Android SDK 35

### Build Steps

```bash
# Clone repository
git clone https://github.com/moonbai/overtime-note.git
cd overtime-note

# Build Debug version
./gradlew assembleDebug

# Build Release version
./gradlew assembleRelease
```

### Output Locations

- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Release APK: `app/build/outputs/apk/release/app-release.apk`

## 📦 CI/CD

This project uses GitHub Actions for automated builds. See [`.github/workflows/build.yml`](.github/workflows/build.yml).

Workflow features:
1. Build Release APK with fixed signing
2. Automatically upload APK to Actions Artifacts
3. Support manual and automatic triggers

## 📱 Screenshots

| Home | Statistics | Settings |
|------|------------|----------|
| Overtime record list | Monthly/Yearly statistics | Feature configuration |

| Add Record | Push Settings | Backup |
|------------|---------------|--------|
| Quick record overtime | Multi-channel push config | Local/Cloud backup |

## 🤝 Contributing

Contributions are welcome! Please feel free to submit issues and pull requests.

1. Fork this repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Create a Pull Request

## 📝 Changelog

### v1.0.16 (2026-05-27)
- ✨ Added leave feature, support half-day/full-day leave
- ✨ Push settings changed to single-channel mode
- ✨ Added auto backup feature, support local/cloud
- ✨ Unified navigation transition animations
- ✨ Optimized top and bottom bar heights
- 🐛 Fixed known issues

## 👤 Author & License

- **Author**: Mars
- **License**: [MIT License](LICENSE)

---

<p align="center">Made with ❤️ by Mars</p>
