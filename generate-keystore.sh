#!/bin/bash

# 生成签名密钥库脚本
# 运行此脚本生成 release-keystore.jks 文件

# 签名配置（请修改为您自己的信息）
KEYSTORE_FILE="app/release-keystore.jks"
STORE_PASSWORD="your_keystore_password"
KEY_ALIAS="overtime"
KEY_PASSWORD="your_key_password"
VALIDITY_YEARS=100
ORGANIZATION="Mars"
COMMON_NAME="Mars"

# 检查 keystore 是否已存在
if [ -f "$KEYSTORE_FILE" ]; then
    echo "⚠️  $KEYSTORE_FILE 已存在，如需重新生成请先删除"
    exit 0
fi

# 生成 keystore
keytool -genkeypair \
    -v \
    -storetype JKS \
    -keystore "$KEYSTORE_FILE" \
    -alias "$KEY_ALIAS" \
    -keyalg RSA \
    -keysize 2048 \
    -validity $VALIDITY_YEARS \
    -storepass "$STORE_PASSWORD" \
    -keypass "$KEY_PASSWORD" \
    -dname "CN=$COMMON_NAME, O=$ORGANIZATION"

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ 签名密钥库生成成功！"
    echo ""
    echo "📝 请保存以下信息："
    echo "   Keystore 文件: $KEYSTORE_FILE"
    echo "   Store Password: $STORE_PASSWORD"
    echo "   Key Alias: $KEY_ALIAS"
    echo "   Key Password: $KEY_PASSWORD"
    echo ""
    echo "⚠️  重要提醒："
    echo "   1. 请将以上信息保存到安全的地方"
    echo "   2. 不要将 release-keystore.jks 提交到 Git"
    echo "   3. 将签名信息添加到 GitHub Secrets"
    echo ""
fi
