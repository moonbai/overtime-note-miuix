## 中文 | [English](README_en.md)

# 加班记 (OverTime-Note) - MiuiX 版本

<img src="https://img.shields.io/badge/Android-10%2B-blue" alt="Android 10+"> <img src="https://img.shields.io/badge/Kotlin-1.9.24-purple" alt="Kotlin 1.9.24"> <img src="https://img.shields.io/badge/MiuiX-0.8.8-orange" alt="MiuiX">

一款专为职场人设计的极简加班记录工具，采用本地优先架构，支持全渠道推送、日历同步、智能备份，让加班记录变得简单高效。

## ✨ 核心功能

### 📊 加班记录管理
- **三类加班分类**：工作日延时、周末休息日、法定节假日
- **请假记录**：支持请假半天/全天，自动扣除对应休息日加班时长
- **智能时长计算**：自动计算加班时长，支持自定义起止时间

### 💰 薪资计算
- **自定义薪资基数**：默认基础薪资 2200 元，支持自定义
- **独立倍率设置**：三类加班可独立设置倍率（工作日 1.5x、休息日 2x、节假日 3x）
- **实时金额预览**：添加记录时实时显示预计薪资

### 📅 日历同步
- **自动同步**：加班记录自动同步到系统日历
- **格式规范**：日历标题格式为「加班类型-加班时长」
- **双向管理**：支持在日历中查看和管理加班记录

### 🔔 智能推送
- **单渠道推送**：支持选择单一渠道进行推送（钉钉/飞书/企业微信/WxPusher/Telegram/Discord/自定义）
- **安全验证**：钉钉、飞书、企业微信支持签名密钥验证
- **一键测试**：配置完成后可立即测试推送效果

### 🗓️ 节假日管理
- **自动识别**：自动识别法定节假日和调休工作日
- **手动更新**：支持手动更新节假日规则，适应最新政策
- **智能判定**：调休工作日正确判定为工作日加班

### 💾 数据备份
- **本地备份**：支持导出/导入 JSON 格式备份文件
- **云端同步**：支持 WebDAV 云端备份，多设备同步
- **自动备份**：开启后可自动备份配置变更到本地或云端

### 🎨 个性化设置
- **主题切换**：支持浅色/深色模式，跟随系统或手动切换
- **强调色定制**：多种强调色可选，个性化界面
- **底栏样式**：支持图标+文字/仅图标/仅文字三种模式
- **快速提报模式**：一键进入添加记录界面，提升效率

## 🛠️ 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| 语言 | Kotlin 1.9.24 | 现代化 Android 开发语言 |
| UI 框架 | MiuiX 0.8.8 | Compose Multiplatform UI 库 |
| 数据库 | Room 2.6.1 | 本地数据持久化 |
| 网络 | OkHttp 4.12.0 | 网络请求与推送 |
| 最低 SDK | 24 (Android 7.0) | 兼容绝大多数设备 |
| 目标 SDK | 35 (Android 15) | 支持最新 Android 特性 |

## 关于 MiuiX

本项目使用 [MiuiX](https://github.com/compose-miuix-ui/miuix) 作为 UI 框架，这是一个为 Compose Multiplatform 设计的 UI 库，提供了美观的组件和主题系统。

## 📁 项目结构

```
app/src/main/java/com/mars/overtime/
├── database/              # Room 数据库层
│   ├── OvertimeRecord.kt     # 加班记录实体
│   ├── AppConfig.kt          # 应用配置实体
│   ├── AppDatabase.kt        # 数据库类
│   ├── OvertimeDao.kt        # 加班记录 DAO
│   └── ConfigDao.kt          # 配置 DAO
├── push/                  # 推送与同步
│   ├── PushManager.kt        # 多渠道推送管理
│   └── CalendarSyncManager.kt # 日历同步管理
├── ui/                    # 用户界面
│   ├── MainNav.kt            # 导航管理
│   ├── theme/                # 主题配置
│   │   ├── Theme.kt
│   │   ├── Color.kt
│   │   └── Type.kt
│   └── screen/               # 页面组件
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
└── util/                  # 工具类
    ├── SalaryCalculator.kt    # 薪资计算
    ├── HolidayManager.kt      # 节假日管理
    ├── BackupManager.kt       # 备份恢复
    ├── WebDavManager.kt       # WebDAV 操作
    └── DataMigrationUtil.kt   # 数据迁移
```

## 🚀 构建与运行

### 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK 35

### 编译步骤

```bash
# 克隆仓库
git clone https://github.com/moonbai/overtime-note-miuix.git
cd overtime-note-miuix

# 编译 Debug 版本
./gradlew assembleDebug

# 编译 Release 版本
./gradlew assembleRelease
```

### 输出位置

- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Release APK: `app/build/outputs/apk/release/app-release.apk`

## 📦 自动打包

本项目配置了 GitHub Actions 自动打包工作流，详见 [`.github/workflows/build.yml`](.github/workflows/build.yml)。

工作流功能：
1. 使用固定签名构建 Release APK
2. 自动上传 APK 到 Actions Artifacts
3. 支持手动触发和自动触发

## 📱 界面预览

| 首页 | 统计 | 设置 |
|------|------|------|
| 加班记录列表 | 月度/年度统计 | 功能配置中心 |

| 添加记录 | 推送设置 | 备份恢复 |
|----------|----------|----------|
| 快速记录加班 | 多渠道推送配置 | 本地/云端备份 |

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 📝 更新日志

### v1.0.16 (2026-05-27)
- ✨ 新增请假功能，支持半天/全天请假
- ✨ 推送设置改为单选模式，优化推送逻辑
- ✨ 新增自动备份功能，支持本地/云端
- ✨ 统一界面导航过渡动画
- ✨ 优化顶栏和底栏高度
- 🐛 修复已知问题

## 👤 作者与许可

- **作者**: Mars
- **许可**: [MIT License](LICENSE)

---

<p align="center">Made with ❤️ by Mars</p>
