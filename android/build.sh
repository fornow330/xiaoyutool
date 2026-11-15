#!/bin/bash
set -e
SDK="$HOME/android-sdk/platforms/android-33"
APP=app/src/main
cp "/home/user/Doubao/chats/38453450899965698/全能百宝箱/小羽工具箱.html" $APP/assets/index.html
rm -rf build/dex build/classes && mkdir -p build/classes build/dex
"$SDK/aapt2" compile --dir $APP/res -o build/res.zip
"$SDK/aapt2" link -o build/base.apk -I "$SDK/android.jar" --manifest $APP/AndroidManifest.xml \
  -A $APP/assets --min-sdk-version 21 --target-sdk-version 33 --version-code 11 --version-name 2.0 build/res.zip
java -jar /tmp/ecj.jar -source 8 -target 8 -cp "$SDK/android.jar" -d build/classes $APP/java/cc/fornow/tool/MainActivity.java 2>&1 | grep -E "ERROR|error:" || true
"$SDK/d8" --lib "$SDK/android.jar" --min-api 21 --output build/dex $(find build/classes -name '*.class')
cp build/base.apk build/app.apk
(cd build/dex && zip -q ../app.apk classes.dex)
"$SDK/zipalign" -f -p 4 build/app.apk build/app-aligned.apk
"$SDK/apksigner" sign --ks build/debug.keystore --ks-pass pass:toolbox123 --key-pass pass:toolbox123 \
  --ks-key-alias toolbox --min-sdk-version 21 --v1-signing-enabled true --v2-signing-enabled true \
  --out build/小羽工具箱.apk build/app-aligned.apk
echo built
