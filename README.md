# Gomoku Android Game

这是一个使用 Kotlin 编写的简单五子棋（Gomoku）安卓游戏项目。棋盘支持 15x15 网格、自动判定胜负和平局，并提供重新开始按钮。

## 主要功能
- 自定义 `GomokuView` 绘制棋盘、棋子以及最新落子高亮。
- 自动判断五子连珠，提示胜者或平局。
- 状态栏实时显示当前轮到的玩家。
- 提供“重新开始”按钮重置棋局。

## 构建说明
由于运行环境无法直接下载 Android Gradle 插件依赖，默认提供的 `gradlew` 会调用系统 `gradle`。如果你在本地构建，请确保已经安装 Android SDK 并可以访问 Google Maven 仓库。

```bash
./gradlew assembleDebug
```

构建完成后，APK 将位于 `app/build/outputs/apk/debug/app-debug.apk`。

## APK 位置
请在成功构建后，将生成的 APK 拷贝到仓库的 `release` 目录中。
