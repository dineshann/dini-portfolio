#!/usr/bin/env bash
# Builds a signed, installable APK from www/index.html + android/ (no Android Studio needed).
set -euo pipefail
cd "$(dirname "$0")"
JAR=${ANDROID_JAR:-/tmp/android-34.jar}
OUT=build; rm -rf $OUT; mkdir -p $OUT/gen $OUT/obj
rm -rf android/assets && mkdir -p android/assets && cp www/index.html android/assets/index.html

aapt package -f -m -J $OUT/gen -M android/AndroidManifest.xml -S android/res -A android/assets \
     -I "$JAR" --min-sdk-version 23 --target-sdk-version 34 -F $OUT/base.apk
javac -nowarn -source 8 -target 8 -bootclasspath "$JAR" -classpath "$JAR" -d $OUT/obj \
      $(find android/src $OUT/gen -name '*.java') 2>&1 | grep -v "^Note:\|warning:" || true
dalvik-exchange --dex --min-sdk-version=23 --output=$OUT/classes.dex $OUT/obj
( cd $OUT && cp base.apk app.apk && aapt add app.apk classes.dex >/dev/null )
zipalign -f -p 4 $OUT/app.apk $OUT/aligned.apk
[ -f dini-release.jks ] || keytool -genkeypair -keystore dini-release.jks -storepass dinipass -keypass dinipass \
     -alias dini -keyalg RSA -keysize 2048 -validity 12000 -dname "CN=Dini, O=Amortree Tech, C=IN" >/dev/null 2>&1
apksigner sign --ks dini-release.jks --ks-pass pass:dinipass --key-pass pass:dinipass --out dini.apk $OUT/aligned.apk
apksigner verify --verbose dini.apk | head -6
ls -la dini.apk
