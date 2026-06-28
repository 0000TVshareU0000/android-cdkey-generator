# DUDOU AI - CDKey 生成器 (Android)

安卓端 CDKey 授权码生成工具，与桌面版生成算法完全一致。

## 算法说明

- **密钥**: `DudouAI_License_Secret_Key_2026_V1`
- **Payload**: 9 字节 `[type:1][days:2][timestamp:4][nonce:1][maxActivations:1]`
- **签名**: HMAC-SHA256，取前 8 字节
- **输出**: 17 字节 → HEX → 格式 `DDAI-XXXX-XXXX-XXXX-XXXX`

## 功能

1. **生成 CDKey** - 选择授权类型 + 最大激活数 + 数量，一键生成
2. **复制** - 生成后自动复制首条，也可手动复制全部
3. **保存** - 保存到 `Documents/CDKey/` 目录（.txt 文件）

## 授权类型

| 类型 | TypeID | 天数 |
|------|--------|------|
| 月卡 | 1 | 30 |
| 年卡 | 2 | 365 |
| 终生 | 3 | 65535 |

## 使用方式

1. 用 Android Studio 打开本项目
2. 连接安卓设备或启动模拟器
3. 点击 ▶ Run 安装运行
4. 选择参数 → 点击「� 生成 CDKey」

## 项目结构

```
android-cdkey-generator/
├── app/
│   └── src/main/
│       ├── java/com/dudouai/cdkeygenerator/
│       │   └── MainActivity.kt
│       ├── res/layout/
│       │   └── activity_main.xml
│       ├── res/values/
│       │   ├── strings.xml
│       │   └── styles.xml
│       ├── res/drawable/
│       │   └── bg_title_icon.xml
│       └── AndroidManifest.xml
├── build.gradle
├── app/build.gradle
├── settings.gradle
└── gradle/wrapper/gradle-wrapper.properties
```

## 依赖

- Android SDK 24+
- Kotlin 1.9.22
- Material Design 1.11.0

## 与桌面版一致性

本安卓应用与以下桌面版生成器算法 100% 兼容：
- `generator.html`（纯 JS 浏览器版）
- `cdkey-gen.py`（Python 桌面版）

生成的 CDKey 可被 DUDOU AI 主程序的授权验证模块正常识别。
trigger rebuild
