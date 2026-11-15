# 小羽工具箱（xiaoyutool）

459 个纯本地小工具，单文件网页 + Android APK。打开即用，计算与图片处理全部在本地浏览器完成，不上传任何数据。

- 在线版：https://fornow330.github.io/xiaoyutool/
- APK 下载：[xiaoyutool-1.8.apk](https://fornow330.github.io/xiaoyutool/xiaoyutool-1.8.apk)（Android 5.0 及以上）
- 作者：fornow
- 交流群：728403008
- 开源协议：GPL-3.0（见 `LICENSE`）

## 功能

| 分类 | 数量 | 示例 |
| --- | --- | --- |
| 设备测量 | 54 | 手电筒、指南针、水平仪、分贝计、音频频谱、陀螺仪、距离传感器、摄像头分辨率、录音回放、彩条测试、权限状态、网络延迟、存储估算、传感器支持 |
| 图片处理 | 69 | 压缩、九宫格、水印、暗角、噪点、油画、卡通化、胶片、双重曝光、背景虚化、文字标签、构图线、前后对比、点阵化、通道分离、动感模糊、色阶、移轴、拼接长图 |
| 文字处理 | 69 | 统计、提取数字/英文/中文、批量加引号、清单转 JSON/SQL、竖排、田字格、文字加框、分组拆分、随机行序、全角半角、信息打码、词频统计、文本转表格 |
| 计算转换 | 72 | 房贷、个税、分期还款、增值税、加班费、墙纸窗帘用量、空调匹数、电线线径、轮胎尺寸、刹车距离、砂浆砌砖、管道流量、定投收益、年化收益、房租回报 |
| 效率工具 | 63 | 番茄钟、倒数日、循环提醒、专注计时、久坐与护眼提醒、计分板、投票器、单词卡、口算练习、座位表、预算清单、习惯打卡、周月复盘 |
| 查询识别 | 66 | 二维码、垃圾分类、节气、节日速查、省份简称、长途区号、交通标志、台风风力等级、空气质量、货币符号、机场航司、快递公司、血型遗传 |
| 开发者小工具 | 66 | UUID、JSON 格式化、JSON 转 YAML/XML/SQL、Schema、子网计算、Base32/58、MAC/UA 解析、Cookie、.env、Cron、代码行数、RGB 转 CMYK |

全部功能在本地完成，不依赖服务器接口；不联网也能用（第三方库仅用于二维码与 PDF 生成）。
## 目录

```
index.html          网页版全部源码（单文件，内联 CSS/JS，无构建步骤）
xiaoyutool-1.8.apk  已签名安装包（包名 cc.fornow.tool）
android/            Android 壳工程源码（WebView + 原生震动/传感器/摄像头权限）
LICENSE             GPL-3.0
```

## 网页版

直接用浏览器打开 `index.html` 即可，或访问 Pages 在线地址。

相机、麦克风、传感器类工具需要浏览器授权，且必须在 **https 或 localhost** 环境下；`file://` 打开时这些硬件工具不可用。

## Android 版

`android/` 里是完整可编译的壳工程：

- `MainActivity.java`：WebView 承载内置页面，提供 `AndroidSaver`（保存文件到下载目录）、`AndroidHaptic`（原生震动）、`AndroidSensor`（加速度传感器）、`AndroidPerm`（主动申请摄像头/麦克风权限），并处理拍照、相册多选与返回键逻辑。
- `AndroidManifest.xml`：包名 `cc.fornow.tool`，权限声明 CAMERA / RECORD_AUDIO / VIBRATE / INTERNET。
- `build.sh`：不依赖 Gradle，用 aapt2 + ecj + d8 + zipalign + apksigner 直接打包全流程脚本。

### 编译

```bash
# 需要 android.jar、aapt2、d8、zipalign、apksigner（platform-33）
# 以及 ecj.jar 作为 javac 替代（无 JDK 时可只装 JRE）
SDK=~/android-sdk/platforms/android-33
./android/build.sh
```

脚本会从 `index.html` 复制到 `assets/index.html`，产物为签名好的 APK。

## 声明

所有工具均为本项目独立实现，未复制第三方源码；二维码、PDF、扫码等库通过 jsDelivr CDN 加载（首次使用需联网），其余功能完全离线。
