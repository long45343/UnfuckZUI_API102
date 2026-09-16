# UnfuckZUI (API 102)

[![Build & Release](https://github.com/long45343/UnfuckZUI_API102/actions/workflows/build.yml/badge.svg)](https://github.com/long45343/UnfuckZUI_API102/actions/workflows/build.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![libxposed API](https://img.shields.io/badge/libxposed-API%20102-brightgreen.svg)](https://github.com/libxposed/api)

适用于联想 ZUI 系统的现代 LSPosed 模块，基于 **libxposed API 102** 全面重构，采用全量 Kotlin 与 Jetpack Compose (Material 3) 编写。

---

## 🌟 特性一览

本模块针对联想 ZUI 系统中的异常行为与过度限制进行了深度修复与体验优化：

### 🎨 外观
- **原生通知图标**：还原原生白底单色通知图标，避免状态栏与通知抽屉被强制替换为彩色应用图标（支持 Android 13/14/15+ 动态适配与前景色自适应计算）。

### ⚡ 行为
- **允许禁用杜比全景声**：解除设置、SystemUI 磁贴与游戏助手中必须连接耳机的限制，允许全局自由关闭内置扬声器的杜比音效。
- **禁用划卡强杀后台**：多任务界面上划移除卡片时，仅关闭任务界面，不调用 `forceStopPackage` 强杀后台进程。
- **阻止自动创建访客**：修复多用户未开启时联想系统仍然自动创建并切换访客账户的 Bug。
- **默认屏幕方向改为竖屏**：强制系统底层初始旋转基准为 0 度竖屏。
- **禁用游戏助手激活弹窗**：屏蔽每次启动游戏时浮窗强行弹出的提示。
- **延长后台任务超时**：将多任务界面最近任务的超时移除时间由默认 6 小时延长至 7 天，且保留数量设为无限。

### 🇨🇳 国行专属优化
- **原生应用安装器风格**：将国行安全安装器重构为 AOSP 原生弹窗样式。
- **原生权限管理器重定向**：将联想权限管理与权限请求弹窗重定向至 AOSP 原生权限控制器。
- **禁用安全中心病毒扫描**：彻底屏蔽联想安全中心内置的腾讯病毒引擎与病毒库升级。
- **总是允许获取应用列表**：拦截系统 AppOps 214 权限检测，解除读取已安装应用列表限制。
- **默认允许应用自启**：联想安全中心后台自启动与跨进程关联唤醒白名单默认放行。
- **移除中国版 Google Play 服务限制**：剥离国行限制属性包，解锁完整 GMS 服务能力（跨设备服务、快速分享等）。

---

## 🛠️ 技术架构

- **现代 Xposed API**：遵循 `libxposed API 102` 规范（`minApiVersion=102`, `targetApiVersion=102`）。
- **生命周期精确切分**：`system_server` 严格在 `onSystemServerStarting` 安装，普通应用在 `onPackageReady` 安装，杜绝 ClassLoader 污染。
- **跨进程配置通信**：采用官方 `io.github.libxposed:service` 的 `RemotePreferences`，支持内存监听无感即时生效。
- **现代 UI 技术栈**：基于 **Jetpack Compose + Material 3** 构建全新设置界面，支持系统深色模式与动态取色。
- **防御性容灾**：针对 ZUI 15 / 16 / 17 跨 Android 版本差异封装 `safeHook` 细粒度拦截，未适配 Hook 自动降级且不拖垮宿主。

---

## 📦 构建与安装

### 源码构建
```bash
git clone https://github.com/long45343/UnfuckZUI_API102.git
cd UnfuckZUI_API102
./gradlew assembleRelease
```
构建产物位于：`app/build/outputs/apk/release/app-release.apk`

### 安装与激活
1. 安装 APK 到平板设备；
2. 打开 LSPosed 管理器，启用 **Unfuck ZUI Tablet** 模块；
3. 框架将自动匹配推荐的 8 个静态作用域；
4. 重启设备或相关应用即可生效。

---

## 📄 开源协议

本项目采用 [Apache License 2.0](LICENSE) 协议开源。
