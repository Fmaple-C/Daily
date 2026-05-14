# 枫叶计划

一个本地离线的 Android 计划应用。当前支持日计划、月计划和年计划：可以记录每天、每月、每年的周期计划，也可以记录当日、当月、当年或任意日期范围内的规划。每次打卡都会保存具体时间和备注，并支持日夜主题切换。

## 文档

- [项目功能文档](docs/功能文档.md)：记录产品目标、已实现功能、待规划功能和后续功能更新记录。
- [项目技术文档](docs/技术文档.md)：记录项目结构、数据模型、存储迁移、架构分层和构建方式。

## 快速构建

```powershell
.\gradlew.bat assembleDebug
```

Debug APK 会生成在：

```text
app\build\outputs\apk\debug\Daily-0.5.0-YYYYMMDD.apk
```
