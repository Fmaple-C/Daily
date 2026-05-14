# 枫叶计划

一个本地离线的 Android 计划应用。当前支持日计划、月计划、年计划和一言记录：可以记录周期计划、编辑 TODO 内容、保存每次打卡时间与备注，也可以随手写下一句话或灵感，并支持日夜主题切换。

## 文档

- [项目功能文档](docs/功能文档.md)：记录产品目标、已实现功能、待规划功能和后续功能更新记录。
- [项目技术文档](docs/技术文档.md)：记录项目结构、数据模型、存储迁移、架构分层和构建方式。

## 快速构建

```powershell
.\gradlew.bat assembleDebug
```

Debug APK 会生成在：

```text
app\build\outputs\apk\debug\Daily-0.6.0-YYYYMMDD.apk
```

同时会归档一份到本地目录，方便保留旧版安装包：

```text
release-apks\Daily-0.6.0-YYYYMMDD.apk
```
