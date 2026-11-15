# 小羽工具箱（xiaoyutool）

126 个纯本地小工具，单文件网页 + Android APK。打开即用，计算与图片处理全部在本地浏览器完成，不上传任何数据。

- 在线版：https://fornow330.github.io/xiaoyutool/
- APK 下载：[xiaoyutool-1.5.apk](https://fornow330.github.io/xiaoyutool/xiaoyutool-1.5.apk)（Android 5.0 及以上）
- 作者：fornow
- 交流群：728403008
- 开源协议：GPL-3.0（见 `LICENSE`）

## 功能

| 分类 | 数量 | 示例 |
| --- | --- | --- |
| 开发者 | 15 | UUID 生成、JSON 格式化、JWT 解析、正则测试、Cron 解析、Markdown 预览 |
| 图片 | 21 | 批量压缩、格式转换、字符画、图片转 PDF、加水印、取色、圆角 |
| 文字 | 21 | 大小写/全角半角、盲文、文字云、去重、统计、编码解码 |
| 数值计算 | 24 | 房贷/公积金贷、年终奖个税、五险一金、单位换算、钢筋重量 |
| 效果 | 18 | 二维码生成与美化、条形码、图片滤镜、字符画 |
| 查询 | 15 | 二维码美化、快递单号识别、条形码 |
| 瑞士刀工具 | 12 | 水平仪、铅垂线、挂画助手、放大镜、测距、补光灯、录音、指南针 |

另有扫码、证件照、九宫格切图、GIF 拆帧、表情包等。

## 目录

```
index.html          网页版全部源码（单文件，内联 CSS/JS，无构建步骤）
xiaoyutool-1.5.apk  已签名安装包（包名 cc.fornow.tool）
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
