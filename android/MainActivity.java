package cc.fornow.tool;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private WebView web;
    private ValueCallback<Uri[]> pending;
    private Uri cameraUri;
    private PermissionRequest pendingPerm;
    private Runnable afterPerm;

    private static final int PICK_CODE = 1001;
    private static final int REQ_CAM = 3001;
    private static final int REQ_CHOOSER = 3002;
    private static final int REQ_BRIDGE = 3003;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        Window w = getWindow();
        w.setStatusBarColor(Color.parseColor("#F1F3F5"));
        w.setNavigationBarColor(Color.parseColor("#FFFFFF"));
        if (Build.VERSION.SDK_INT >= 23) {
            w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.parseColor("#F1F3F5"));

        web = new WebView(this);
        web.setBackgroundColor(Color.parseColor("#F1F3F5"));
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setAllowFileAccessFromFileURLs(true);
        s.setAllowUniversalAccessFromFileURLs(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setSupportZoom(false);
        s.setTextZoom(100);

        web.addJavascriptInterface(new Saver(), "AndroidSaver");
        web.addJavascriptInterface(new Perm(), "AndroidPerm");
        web.addJavascriptInterface(new Haptic(), "AndroidHaptic");
        web.addJavascriptInterface(new Sensors(), "AndroidSensor");
        // 用 https 虚拟域名加载内置页面：file:// 不是「安全上下文」，
        // 会导致 navigator.vibrate / getUserMedia / 传感器接口在 WebView 里被禁用
        final WebViewClient assetClient = new WebViewClient() {
            @Override
            public android.webkit.WebResourceResponse shouldInterceptRequest(
                    WebView v, android.webkit.WebResourceRequest req) {
                Uri u = req.getUrl();
                if (u != null && "https".equals(u.getScheme())
                        && "appassets.androidplatform.net".equals(u.getHost())) {
                    String path = u.getPath();
                    if (path == null || "/".equals(path)) path = "/index.html";
                    try {
                        java.io.InputStream in = getAssets().open(path.substring(1));
                        String mime = guessMime(path);
                        return new android.webkit.WebResourceResponse(mime, "UTF-8", in);
                    } catch (Exception e) {
                        return new android.webkit.WebResourceResponse("text/plain", "UTF-8", null);
                    }
                }
                return null;
            }
        };
        web.setWebViewClient(assetClient);
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> cb,
                                             FileChooserParams params) {
                return MainActivity.this.fileChooser(cb, params);
            }

            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        List<String> need = new ArrayList<String>();
                        for (String r : request.getResources()) {
                            if (PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(r)) {
                                need.add(Manifest.permission.CAMERA);
                            } else if (PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(r)) {
                                need.add(Manifest.permission.RECORD_AUDIO);
                            }
                        }
                        List<String> miss = missing(need);
                        if (miss.isEmpty()) {
                            request.grant(request.getResources());
                            pendingPerm = null;
                        } else {
                            pendingPerm = request;
                            requestPermissions(miss.toArray(new String[0]), REQ_CAM);
                        }
                    }
                });
            }
        });
        web.loadUrl("https://appassets.androidplatform.net/index.html");
        root.addView(web, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(root);
    }

    private String guessMime(String path) {
        String lo = path.toLowerCase();
        if (lo.endsWith(".html") || lo.endsWith(".htm")) return "text/html";
        if (lo.endsWith(".js")) return "application/javascript";
        if (lo.endsWith(".css")) return "text/css";
        if (lo.endsWith(".png")) return "image/png";
        if (lo.endsWith(".jpg") || lo.endsWith(".jpeg")) return "image/jpeg";
        if (lo.endsWith(".svg")) return "image/svg+xml";
        if (lo.endsWith(".woff2")) return "font/woff2";
        if (lo.endsWith(".json")) return "application/json";
        return "application/octet-stream";
    }

    private List<String> missing(List<String> perms) {
        List<String> out = new ArrayList<String>();
        if (Build.VERSION.SDK_INT < 23) return out;
        for (String p : perms) {
            if (checkSelfPermission(p) != PackageManager.PERMISSION_GRANTED) out.add(p);
        }
        return out;
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] res) {
        if (code == REQ_CAM && pendingPerm != null) {
            boolean ok = true;
            for (int r : res) if (r != PackageManager.PERMISSION_GRANTED) ok = false;
            if (ok) pendingPerm.grant(pendingPerm.getResources());
            else pendingPerm.deny();
            pendingPerm = null;
            return;
        }
        if (code == REQ_BRIDGE) {
            boolean ok = true;
            for (int r : res) if (r != PackageManager.PERMISSION_GRANTED) ok = false;
            permDone(ok);
            return;
        }
        if (code == REQ_CHOOSER && afterPerm != null) {
            Runnable r = afterPerm;
            afterPerm = null;
            r.run();
            return;
        }
        super.onRequestPermissionsResult(code, perms, res);
    }

    /** 供网页在调用 getMediaDevices 前主动申请权限，返回结果回传 __permDone */
    public class Perm {
        @JavascriptInterface
        public void request(final String need) {
            runOnUiThread(new Runnable() {
                @Override public void run() {
                    List<String> want = new ArrayList<String>();
                    if (need != null && need.contains("camera")) want.add(Manifest.permission.CAMERA);
                    if (need != null && need.contains("audio")) want.add(Manifest.permission.RECORD_AUDIO);
                    List<String> miss = missing(want);
                    if (miss.isEmpty()) {
                        permDone(true);
                    } else {
                        bridgePerms = miss.toArray(new String[0]);
                        requestPermissions(bridgePerms, REQ_BRIDGE);
                    }
                }
            });
        }
    }

    private String[] bridgePerms;

    private void permDone(boolean ok) {
        if (web != null) {
            web.evaluateJavascript("window.__permDone&&window.__permDone(" + (ok ? "true" : "false") + ")", null);
        }
    }

    /** 原生方向传感器：WebView 不派发 deviceorientation，这里用加速度计算 beta/gamma 喂给网页 */
    public class Sensors {
        private android.hardware.SensorManager sm;
        private android.hardware.Sensor acc;
        private android.hardware.SensorEventListener listener;
        private float fx, fy, fz;
        private boolean ready;
        private long lastSend;

        @JavascriptInterface
        public void start() {
            runOnUiThread(new Runnable() {
                @Override public void run() {
                    if (listener != null) return;
                    sm = (android.hardware.SensorManager) getSystemService(SENSOR_SERVICE);
                    if (sm == null) return;
                    acc = sm.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER);
                    if (acc == null) return;
                    ready = false;
                    lastSend = 0;
                    listener = new android.hardware.SensorEventListener() {
                        @Override public void onSensorChanged(android.hardware.SensorEvent e) {
                            float ax = e.values[0], ay = e.values[1], az = e.values[2];
                            if (!ready) { fx = ax; fy = ay; fz = az; ready = true; }
                            else {
                                fx = fx * 0.82f + ax * 0.18f;
                                fy = fy * 0.82f + ay * 0.18f;
                                fz = fz * 0.82f + az * 0.18f;
                            }
                            long now = System.currentTimeMillis();
                            if (now - lastSend < 50) return;
                            lastSend = now;
                            double beta = Math.toDegrees(Math.atan2(fy, fz));
                            double gx = Math.sqrt(fy * fy + fz * fz);
                            double gamma = Math.toDegrees(Math.atan2(-fx, gx));
                            if (gamma > 90) gamma = 90;
                            if (gamma < -90) gamma = -90;
                            if (web != null) {
                                web.evaluateJavascript("window.__nativeOrient&&window.__nativeOrient("
                                        + round1(beta) + "," + round1(gamma) + ")", null);
                            }
                        }
                        @Override public void onAccuracyChanged(android.hardware.Sensor s, int a) { }
                    };
                    sm.registerListener(listener, acc, android.hardware.SensorManager.SENSOR_DELAY_UI);
                }
            });
        }

        @JavascriptInterface
        public void stop() {
            runOnUiThread(new Runnable() {
                @Override public void run() {
                    if (sm != null && listener != null) sm.unregisterListener(listener);
                    listener = null;
                }
            });
        }
    }

    private double round1(double v) {
        return Math.round(v * 10) / 10.0;
    }

    /** 原生震动，网页里 navigator.vibrate 会被替换到这里 */
    public class Haptic {
        @JavascriptInterface
        public void vibrate(String pattern) {
            final long[] arr = parsePattern(pattern);
            if (arr == null || arr.length == 0) {
                say("震动参数无效");
                return;
            }
            if (arr.length == 1 && arr[0] <= 0) {
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        try { getVib().cancel(); } catch (Exception e) { }
                    }
                });
                return;
            }
            runOnUiThread(new Runnable() {
                @Override public void run() {
                    buzz(arr);
                }
            });
        }
    }

    private long[] parsePattern(String pattern) {
        try {
            if (pattern != null && pattern.trim().startsWith("[")) {
                org.json.JSONArray ja = new org.json.JSONArray(pattern);
                long[] arr = new long[ja.length()];
                for (int i = 0; i < ja.length(); i++) arr[i] = (long) ja.optDouble(i, 0);
                return arr;
            }
            return new long[]{(long) Double.parseDouble(pattern == null ? "0" : pattern.trim())};
        } catch (Exception e) {
            return null;
        }
    }

    private void buzz(long[] arr) {
        try {
            android.os.Vibrator vib = getVib();
            if (vib == null) { say("本机没有振动马达"); return; }
            if (!vib.hasVibrator()) { say("本机没有振动马达"); return; }
            if (Build.VERSION.SDK_INT >= 26) {
                android.os.VibrationEffect eff = android.os.VibrationEffect.createWaveform(arr, -1);
                if (Build.VERSION.SDK_INT >= 33) {
                    vib.vibrate(eff, android.os.VibrationAttributes.createForUsage(
                            android.os.VibrationAttributes.USAGE_ALARM));
                } else {
                    vib.vibrate(eff);
                }
            } else {
                vib.vibrate(arr, -1);
            }
        } catch (Throwable t) {
            say("震动调用失败：" + t.getClass().getSimpleName());
        }
    }

    private void say(final String msg) {
        if (web == null) return;
        web.evaluateJavascript("window.__vibMsg&&window.__vibMsg('" + msg.replace("'", "") + "')", null);
    }

    @SuppressWarnings("deprecation")
    private android.os.Vibrator getVib() {
        if (Build.VERSION.SDK_INT >= 31) {
            android.os.VibratorManager vm =
                    (android.os.VibratorManager) getSystemService(VIBRATOR_MANAGER_SERVICE);
            return vm.getDefaultVibrator();
        }
        return (android.os.Vibrator) getSystemService(VIBRATOR_SERVICE);
    }

    /** 网页的 download() 会走到这里，文件落到系统「下载」目录 */
    public class Saver {
        @JavascriptInterface
        public void save(final String name, final String base64, final String mime) {
            runOnUiThread(new Runnable() {
                @Override public void run() {
                    boolean ok = write(name, base64, mime);
                    String msg = ok ? "已保存到「下载」目录" : "保存失败，请检查存储权限";
                    if (web != null) {
                        web.evaluateJavascript("toast('" + msg + "')", null);
                    }
                }
            });
        }
    }

    private boolean write(String name, String base64, String mime) {
        byte[] data;
        try { data = Base64.decode(base64, Base64.DEFAULT); } catch (Exception e) { return false; }
        if (mime == null || mime.length() == 0) mime = "application/octet-stream";
        try {
            if (Build.VERSION.SDK_INT >= 29) {
                ContentValues cv = new ContentValues();
                cv.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
                cv.put(MediaStore.MediaColumns.MIME_TYPE, mime);
                cv.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv);
                if (uri == null) return false;
                java.io.OutputStream os = getContentResolver().openOutputStream(uri);
                if (os == null) return false;
                os.write(data); os.flush(); os.close();
                return true;
            }
            if (Build.VERSION.SDK_INT >= 23
                    && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2001);
            }
            java.io.File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (dir != null && !dir.exists()) dir.mkdirs();
            java.io.File f = new java.io.File(dir, name);
            java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
            fos.write(data); fos.flush(); fos.close();
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(f)));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean fileChooser(ValueCallback<Uri[]> cb, WebChromeClient.FileChooserParams params) {
        if (pending != null) pending.onReceiveValue(null);
        pending = cb;
        cameraUri = null;
        final WebChromeClient.FileChooserParams p = params;
        Runnable launch = new Runnable() {
            @Override public void run() {
                startChooser(p);
            }
        };
        if (Build.VERSION.SDK_INT >= 23
                && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            afterPerm = launch;
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQ_CHOOSER);
        } else {
            launch.run();
        }
        return true;
    }

    private void startChooser(WebChromeClient.FileChooserParams params) {
        try {
            boolean multi = params.getMode() == WebChromeClient.FileChooserParams.MODE_OPEN_MULTIPLE;
            Intent content = params.createIntent();
            Intent capture = null;
            if (!multi && acceptsImage(params)) {
                cameraUri = newImageUri();
                if (cameraUri != null) {
                    capture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    capture.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
                    capture.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
            }
            if (capture != null) {
                Intent chooser = Intent.createChooser(content, "选择图片来源");
                chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{capture});
                chooser.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(chooser, PICK_CODE);
            } else {
                startActivityForResult(content, PICK_CODE);
            }
        } catch (Exception e) {
            if (pending != null) pending.onReceiveValue(null);
            pending = null;
        }
    }

    private boolean acceptsImage(WebChromeClient.FileChooserParams params) {
        String[] types = params.getAcceptTypes();
        if (types == null || types.length == 0) return true;
        for (String t : types) {
            if (t == null) continue;
            String lo = t.toLowerCase();
            if (lo.startsWith("image") || lo.equals("*/*") || lo.contains("image")) return true;
        }
        return false;
    }

    private Uri newImageUri() {
        try {
            ContentValues v = new ContentValues();
            v.put(MediaStore.Images.Media.DISPLAY_NAME, "XY_" + System.currentTimeMillis() + ".jpg");
            v.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            return getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, v);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        if (req != PICK_CODE || pending == null) {
            super.onActivityResult(req, res, data);
            return;
        }
        Uri[] out = null;
        if (res == RESULT_OK) {
            if (data != null && data.getClipData() != null) {
                int n = data.getClipData().getItemCount();
                out = new Uri[n];
                for (int i = 0; i < n; i++) out[i] = data.getClipData().getItemAt(i).getUri();
            } else if (data != null && data.getData() != null) {
                out = new Uri[]{data.getData()};
            } else if (cameraUri != null && !isEmpty(cameraUri)) {
                out = new Uri[]{cameraUri};
            }
        }
        pending.onReceiveValue(out);
        pending = null;
        cameraUri = null;
    }

    private boolean isEmpty(Uri uri) {
        try {
            java.io.InputStream in = getContentResolver().openInputStream(uri);
            if (in == null) return true;
            int b = in.read();
            in.close();
            return b == -1;
        } catch (Exception e) {
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        if (web == null) { super.onBackPressed(); return; }
        web.evaluateJavascript(
                "(function(){try{var s=document.getElementById('sheet');" +
                "if(s&&s.classList.contains('open')){closeSheet();return 'closed';}" +
                "return 'exit';}catch(e){return 'exit';}})()",
                value -> {
                    if (value != null && value.contains("closed")) return;
                    finish();
                });
    }

    @Override
    protected void onDestroy() {
        try {
            if (web != null) web.evaluateJavascript("window.AndroidSensor&&AndroidSensor.stop()", null);
        } catch (Exception e) {
        }
        if (web != null) {
            web.loadUrl("about:blank");
            web.removeJavascriptInterface("AndroidSaver");
            web.destroy();
            web = null;
        }
        super.onDestroy();
    }
}
