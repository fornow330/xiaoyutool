# 小羽工具箱

离线优先的单文件工具集：一个 `index.html` 装下全部小工具，浏览器打开即用，不依赖服务器。

- 在线：<https://fornow330.github.io/xiaoyutool/>
- 作者：fornow
- 许可：GPL-3.0（见 `LICENSE`）

分流镜像（Pages 慢时用）：`fastly.jsdelivr.net/gh/fornow330/xiaoyutool@main/`、`cdn.jsdelivr.net/gh/fornow330/xiaoyutool@main/`、`ghproxy.net` + raw 地址。

## 分类与计算器

工具按分类组织，覆盖财务理财、工程建筑、设计排版、设备测量、图片处理、文字处理、计算转换、效率工具、查询识别、开发者小工具、健康运动、生活实用、学习考试、音视频、安全应急、职场办公、儿童启蒙、旅行出行、家居装修、娱乐随机等方向；同一功能的多个入口已合并为综合计算器，完整清单以在线页为准。

全部功能在本地完成，不依赖服务器接口，不联网也能用（二维码与 PDF 生成首次需联网加载库）。

## 目录

| 路径 | 说明 |
|---|---|
| `index.html` | 网页版全部源码，单文件内联 CSS/JS，无构建步骤 |
| `android/` | Android 壳工程源码（WebView + 原生震动/传感器/摄像头权限） |
| `LICENSE` | GPL-3.0 |

## 说明

直接用浏览器打开 `index.html` 或访问在线地址。相机、麦克风、传感器类工具需浏览器授权，且必须在 **https 或 localhost** 环境下，`file://` 打开时这些硬件工具不可用。

## Android 版

`android/` 是完整可编译的壳工程：

- `MainActivity.java`：WebView 承载页面，提供 `AndroidSaver`（保存文件）、`AndroidHaptic`（震动）、`AndroidSensor`（加速度）、`AndroidPerm`（摄像头/麦克风授权），并处理拍照、相册多选与返回键。
- 在线优先：启动先用内置/缓存页面秒开，后台按 Pages → jsDelivr(fastly) → jsDelivr(cdn) → ghproxy 拉取最新 `index.html` 写入缓存，下次启动生效；全部不通则继续用内置页面，离线可用。
- `AndroidManifest.xml`：权限声明 CAMERA / RECORD_AUDIO / VIBRATE / INTERNET。
- `build.sh`：不依赖 Gradle，用 aapt2 + d8 + zipalign + apksigner 直接打包；源文件可用 `SRC_HTML` 变量指定，默认 `../小羽工具箱.html`。

```bash
SDK=~/android-sdk/platforms/android-33 ./android/build.sh
```

## 声明

所有工具均为本项目独立实现，未复制第三方源码；二维码、PDF 等库通过 jsDelivr 加载，其余功能完全离线。
