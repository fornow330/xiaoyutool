# 小羽工具箱（xiaoyutool）

255 个纯本地小工具，单文件网页 + Android APK。打开即用，计算与图片处理全部在本地浏览器完成，不上传任何数据。

- 在线版：https://fornow330.github.io/xiaoyutool/
- APK 下载：[xiaoyutool-1.7.apk](https://fornow330.github.io/xiaoyutool/xiaoyutool-1.7.apk)（Android 5.0 及以上）
- 作者：fornow
- 交流群：728403008
- 开源协议：GPL-3.0（见 `LICENSE`）

## 功能

| 分类 | 数量 | 示例 |
| --- | --- | --- |
| 设备测量 | 33 | 手电筒、指南针、水平仪、铅垂线、分贝计、节拍器、音频发生器、扬声器除尘、加速度曲线、麦克风电平、电池状态、多点触控、按键检测、手柄检测、摄像头预览、定位信息 |
| 图片处理 | 39 | 压缩、格式转换、九宫格、水印、取色、拼长图、裁剪旋转、像素化、反色、文字转图片、WebP、图标生成、拼图、任意旋转、画中画、渐变遮罩、半调、长图切段、比例裁剪、圆形头像 |
| 文字处理 | 39 | 统计、命名转换、摩斯电码、Base64、盲文、文字云、排序、提取链接、Markdown 转 HTML、大小写、查找替换、标点转换、自动编号、列对齐、模板填充、去 HTML 标签 |
| 计算转换 | 42 | 房贷、公积金贷、提前还款、等额本金、个税、税后工资、五险一金、油耗、卡路里、钢筋重量、坡度、混凝土、瓷砖地板用量、乳胶漆、电费、配速、预产期、睡眠规划、酒精代谢、投资回报 |
| 效率工具 | 36 | 番茄钟、秒表、白噪音、手持弹幕、全屏时钟、待办、记账、打卡、呼吸训练、屏幕常亮、随机数字、随机分组、打点计时、发言计时、HIIT、买菜清单、纪念日、排班、周报、时间块 |
| 查询识别 | 33 | 二维码生成/美化/扫码、WiFi/名片二维码、条形码、垃圾分类、亲戚关系、二十四节气、元素周期表、快捷键、车牌、身份证、手机号段、银行卡、食物热量、嘌呤、GI、纸张、端口、标准代号 |
| 开发者小工具 | 33 | UUID、JSON 格式化、JSON 转 CSV/TS/SQL、JSON 取值与压缩、JWT、正则测试与速查、Cron、颜色转换与对比度、配色生成、HTML/CSS 格式化、Unicode、MIME、Git/Docker 速查 |

全部功能在本地完成，不依赖服务器接口；不联网也能用（第三方库仅用于二维码与 PDF 生成）。
## 目录

```
index.html          网页版全部源码（单文件，内联 CSS/JS，无构建步骤）
xiaoyutool-1.7.apk  已签名安装包（包名 cc.fornow.tool）
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
