# Android 签名配置指南

## 🔐 为什么要签名？

Android APK 必须经过签名才能安装到设备上。签名用于：
- 验证应用开发者身份
- 确保应用未被篡改
- 支持应用更新（需要相同签名）

## 📝 本地签名配置

### 1. 生成签名密钥

运行项目根目录下的脚本生成签名密钥：

```bash
chmod +x generate-keystore.sh
./generate-keystore.sh
```

或手动生成：

```bash
keytool -genkeypair \
  -v \
  -storetype JKS \
  -keystore app/release-keystore.jks \
  -alias overtime \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -storepass your_store_password \
  -keypass your_key_password \
  -dname "CN=Mars, O=Mars"
```

### 2. 配置签名信息

创建 `gradle.properties` 文件（如果不存在）：

```properties
# 签名配置
ANDROID_KEYSTORE_PASSWORD=your_store_password
ANDROID_KEY_ALIAS=overtime
ANDROID_KEY_PASSWORD=your_key_password
```

### 3. 本地打包

```bash
./gradlew assembleRelease
```

APK 文件位置：`app/build/outputs/apk/release/`

## ☁️ GitHub Actions 自动签名配置

### 1. 生成签名密钥（本地）

```bash
keytool -genkeypair \
  -v \
  -storetype JKS \
  -keystore app/release-keystore.jks \
  -alias overtime \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -storepass your_store_password \
  -keypass your_key_password \
  -dname "CN=Mars, O=Mars"
```

### 2. 导出 Base64 编码（本地）

```bash
base64 app/release-keystore.jks > keystore_base64.txt
```

### 3. 添加 GitHub Secrets

在 GitHub 仓库 Settings → Secrets and variables → Actions 中添加：

| Secret Name | 说明 |
|-------------|------|
| `ANDROID_KEYSTORE_BASE64` | keystore 文件的 Base64 编码（从 keystore_base64.txt 中复制） |
| `ANDROID_KEYSTORE_PASSWORD` | 密钥库密码 |
| `ANDROID_KEY_ALIAS` | 密钥别名（通常是 `overtime`） |
| `ANDROID_KEY_PASSWORD` | 密钥密码 |

### 4. GitHub Actions 自动构建

推送到 main 分支后，GitHub Actions 会自动：
1. 解码 keystore 文件
2. 使用签名配置构建 Release APK
3. 上传到 Artifacts

## ⚠️ 注意事项

### 安全提醒

1. **不要提交 keystore 文件到 Git**
   - 已添加到 `.gitignore`
   - keystore 丢失会导致无法更新已发布的应用

2. **备份签名密钥**
   - 将 keystore 文件和密码备份到安全的地方
   - 建议使用密码管理器存储

3. **GitHub Secrets 保密**
   - Base64 编码的 keystore 包含所有签名信息
   - 不要与他人分享

### 签名验证

构建完成后，可以验证 APK 签名：

```bash
keytool -list -v -keystore app/release-keystore.jks
```

或检查已构建 APK 的签名：

```bash
apksigner verify -v app/build/outputs/apk/release/*.apk
```

## 🔄 多环境签名配置

如需多个签名配置（如测试环境、生产环境），可以修改 `app/build.gradle.kts`：

```kotlin
signingConfigs {
    create("release") {
        // 生产环境配置
    }
    create("staging") {
        // 测试环境配置
    }
}

buildTypes {
    release {
        signingConfig = signingConfigs.getByName("release")
    }
    create("staging") {
        signingConfig = signingConfigs.getByName("staging")
    }
}
```

## 📞 常见问题

### Q: 忘记了 keystore 密码怎么办？

A: 无法恢复，只能重新生成新的 keystore。这意味着已发布应用无法更新。

### Q: 可以更换签名密钥吗？

A: 可以，但已安装的应用需要卸载后重新安装。建议保留原有密钥。

### Q: Debug APK 可以用于测试吗？

A: 可以，但 Debug 签名不能用于应用市场上架，仅用于开发和测试。

## ✅ 快速检查清单

- [ ] 生成 keystore 文件
- [ ] 配置 gradle.properties
- [ ] 添加到 .gitignore
- [ ] 备份 keystore 和密码
- [ ] 配置 GitHub Secrets（如果使用 CI/CD）
- [ ] 测试本地构建
- [ ] 验证构建产物签名

---

如有问题，请提交 Issue。
