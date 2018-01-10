#!/bin/bash

APP_PATH="/opt/APKScanner"
APP_VERSION="2.3.3"
APP_FILE="ApkScanner.jar"

DEBIAN_DATA_PATH="./debian"$APP_PATH

rm -rf ./debian

##############################
# make contorl file
##############################
mkdir -p ./debian/DEBIAN
cat << EOF > ./debian/DEBIAN/control
Package: apk-scanner
Version: $APP_VERSION-1
Section: utils
Architecture: all
Maintainer: Sunggyu Kam <sunggyu.kam@samsung.com>
Installed-Size: 62060
Description: APK Scanner $APP_VERSION
EOF
cat ./debian/DEBIAN/control


##############################
# copy for release data
##############################
TARGET_PATH="$DEBIAN_DATA_PATH/"
mkdir -p "$TARGET_PATH"
cp -f "release/ApkScanner.jar" "$TARGET_PATH"
cp -f "res/icons/AppIcon.png" "$TARGET_PATH"

TARGET_PATH="$DEBIAN_DATA_PATH/data/build-master-target-product-security/"
mkdir -p "$TARGET_PATH"
cp -f "release/data/build-master-target-product-security/Android.mk" "$TARGET_PATH"
cp -f "release/data/build-master-target-product-security/media.pk8" "$TARGET_PATH"
cp -f "release/data/build-master-target-product-security/media.x509.pem" "$TARGET_PATH"
cp -f "release/data/build-master-target-product-security/platform.pk8" "$TARGET_PATH"
cp -f "release/data/build-master-target-product-security/platform.x509.pem" "$TARGET_PATH"
cp -f "release/data/build-master-target-product-security/README" "$TARGET_PATH"
cp -f "release/data/build-master-target-product-security/shared.pk8" "$TARGET_PATH"
cp -f "release/data/build-master-target-product-security/shared.x509.pem" "$TARGET_PATH"
cp -f "release/data/build-master-target-product-security/testkey.pk8" "$TARGET_PATH"
cp -f "release/data/build-master-target-product-security/testkey.x509.pem" "$TARGET_PATH"
cp -f "release/data/build-master-target-product-security/verity.pk8" "$TARGET_PATH"
cp -f "release/data/build-master-target-product-security/verity.x509.pem" "$TARGET_PATH"
cp -f "release/data/build-master-target-product-security/verity_key" "$TARGET_PATH"

TARGET_PATH="$DEBIAN_DATA_PATH/lib/"
mkdir -p "$TARGET_PATH"
cp -f "release/lib/commons-cli-1.3.1.jar" "$TARGET_PATH"
cp -f "release/lib/ddmlib.jar" "$TARGET_PATH"
cp -f "release/lib/guava-18.0.jar" "$TARGET_PATH"
cp -f "release/lib/jna-4.4.0.jar" "$TARGET_PATH"
cp -f "release/lib/jna-platform-4.4.0.jar" "$TARGET_PATH"
cp -f "release/lib/json-simple-1.1.1.jar" "$TARGET_PATH"
cp -f "release/lib/mslinks.jar" "$TARGET_PATH"
cp -f "release/lib/rsyntaxtextarea-2.6.1.jar" "$TARGET_PATH"
cp -f "release/lib/rstaui-2.6.0.jar" "$TARGET_PATH"
cp -f "release/lib/autocomplete-2.6.0.jar" "$TARGET_PATH"

TARGET_PATH="$DEBIAN_DATA_PATH/tool/"
mkdir -p "$TARGET_PATH"
cp -f "release/tool/aapt" "$TARGET_PATH"
cp -f "release/tool/adb" "$TARGET_PATH"
cp -f "release/tool/apktool.jar" "$TARGET_PATH"
cp -f "release/tool/d2j-dex2jar.sh" "$TARGET_PATH"
cp -f "release/tool/d2j_invoke.sh" "$TARGET_PATH"
cp -f "release/tool/jd-gui-1.4.0.jar" "$TARGET_PATH"
cp -f "release/tool/jd_icon_128.png" "$TARGET_PATH"
cp -f "release/tool/libAaptNativeWrapper32.so" "$TARGET_PATH"
cp -f "release/tool/libAaptNativeWrapper64.so" "$TARGET_PATH"
cp -f "release/tool/libc++32.so" "$TARGET_PATH"
cp -f "release/tool/libc++64.so" "$TARGET_PATH"
cp -f "release/tool/signapk.jar" "$TARGET_PATH"

TARGET_PATH="$DEBIAN_DATA_PATH/tool/lib/"
mkdir -p "$TARGET_PATH"
cp -f "release/tool/lib/antlr-runtime-3.5.jar" "$TARGET_PATH"
cp -f "release/tool/lib/asm-debug-all-4.1.jar" "$TARGET_PATH"
cp -f "release/tool/lib/d2j-base-cmd-2.0.jar" "$TARGET_PATH"
cp -f "release/tool/lib/d2j-jasmin-2.0.jar" "$TARGET_PATH"
cp -f "release/tool/lib/d2j-smali-2.0.jar" "$TARGET_PATH"
cp -f "release/tool/lib/dex-ir-2.0.jar" "$TARGET_PATH"
cp -f "release/tool/lib/dex-reader-2.0.jar" "$TARGET_PATH"
cp -f "release/tool/lib/dex-reader-api-2.0.jar" "$TARGET_PATH"
cp -f "release/tool/lib/dex-tools-2.0.jar" "$TARGET_PATH"
cp -f "release/tool/lib/dex-translator-2.0.jar" "$TARGET_PATH"
cp -f "release/tool/lib/dex-writer-2.0.jar" "$TARGET_PATH"
cp -f "release/tool/lib/dx-1.7.jar" "$TARGET_PATH"

TARGET_PATH="$DEBIAN_DATA_PATH/tool/lib64/"
mkdir -p "$TARGET_PATH"
cp -f "release/tool/lib64/libc++.so" "$TARGET_PATH"

TARGET_PATH="$DEBIAN_DATA_PATH/plugin/"
mkdir -p "$TARGET_PATH"

##############################
# etc
##############################
cat << EOF > $DEBIAN_DATA_PATH/APKScanner.sh
#!/bin/bash
java -Xms512m -Xmx1024m -jar $APP_PATH/$APP_FILE "\$@" > /dev/null
EOF

chmod 775 $DEBIAN_DATA_PATH/APKScanner.sh

echo "{}" > $DEBIAN_DATA_PATH/settings.txt
chmod 666 $DEBIAN_DATA_PATH/settings.txt

mkdir -p ./debian/usr/share/applications/
cat << EOF > ./debian/usr/share/applications/apkscanner.desktop
[Desktop Entry]
Encoding=UTF-8
Version=$APP_VERSION
Type=Application
Exec=java -jar $APP_PATH/$APP_FILE %f
Name=APK Scanner
Comment=APK Scanner
Icon=$APP_PATH/AppIcon.png
MimeType=application/apk;application/vnd.android.package-archive;
EOF

cat << POST_EOF > ./debian/DEBIAN/postinst
#!/bin/bash
echo postinst
if [ ! -e /usr/share/mime/application/vnd.android.package-archive.xml ]; then
cat << EOF > ./vnd.android.package-archive.xml
<?xml version="1.0" encoding="utf-8"?>
<mime-info xmlns="http://www.freedesktop.org/standards/shared-mime-info">
  <!--Created automatically by update-mime-database. DO NOT EDIT!-->
  <mime-type type="application/vnd.android.package-archive">
    <comment>Android package</comment>
    <comment xml:lang="bg">Пакет — Android</comment>
    <comment xml:lang="ca">paquet d'Android</comment>
    <comment xml:lang="cs">Balíčky systému Android</comment>
    <comment xml:lang="de">Android-Paket</comment>
    <comment xml:lang="en_AU">Android package</comment>
    <comment xml:lang="en_GB">Android package</comment>
    <comment xml:lang="eo">Android-pakaĵo</comment>
    <comment xml:lang="es">Paquete de Android</comment>
    <comment xml:lang="fi">Android-paketti</comment>
    <comment xml:lang="fr">paquet Android</comment>
    <comment xml:lang="gl">paquete de Android</comment>
    <comment xml:lang="he">חבילת אנדרויד</comment>
    <comment xml:lang="hu">Android csomag</comment>
    <comment xml:lang="id">Paket Android</comment>
    <comment xml:lang="it">Pacchetto Android</comment>
    <comment xml:lang="ja">Android パッケージ</comment>
    <comment xml:lang="kk">Android дестесі</comment>
    <comment xml:lang="ko">안드로이드 패키지</comment>
    <comment xml:lang="lv">Android pakotne</comment>
    <comment xml:lang="pl">Pakiet Androida</comment>
    <comment xml:lang="pt_BR">Pacote Android</comment>
    <comment xml:lang="ru">пакет Android</comment>
    <comment xml:lang="sl">Paket Android</comment>
    <comment xml:lang="sv">Android-paket</comment>
    <comment xml:lang="uk">пакунок Android</comment>
    <comment xml:lang="zh_CN">Android</comment>
    <comment xml:lang="zh_TW">Android 套件</comment>
    <sub-class-of type="application/x-java-archive"/>
    <glob pattern="*.apk"/>
  </mime-type>
</mime-info>
EOF
    sudo cp -f ./vnd.android.package-archive.xml /usr/share/mime/packages/
    sudo update-mime-database /usr/share/mime/
    sudo rm -f /usr/share/mime/packages/vnd.android.package-archive.xml
fi

if [ -e ~/.local/share/applications/mimeapps.list ]; then
cp -f ~/.local/share/applications/mimeapps.list ~/.local/share/applications/mimeapps_old.list
cat ~/.local/share/applications/mimeapps_old.list \
	| sed '/application\/vnd\.android\.package-archive\=/d;/^$/d' \
	| sed 's/^\s*\[.*\]\s*$/&\napplication\/vnd.android.package-archive=apkscanner.desktop;/' > ~/.local/share/applications/mimeapps.list
else
cat << EOF > ~/.local/share/applications/mimeapps.list
[Added Associations]
application/vnd.android.package-archive=apkscanner.desktop;
EOF
fi

if [ -e ~/.p4qt/ApplicationSettings.xml ]; then
    cat ~/.p4qt/ApplicationSettings.xml | sed '/EditorMappings/,/StringList/{/<String>apk/d; /<String>ppk/d; s/.*<\/StringList>.*/  <String>apk\|default\|\/opt\/APKScanner\/APKScanner\.sh<\/String>\n  <String>ppk\|default\|\/opt\/APKScanner\/APKScanner\.sh<\/String>\n <\/StringList>/}' > .ApplicationSettings.xml
    mv .ApplicationSettings.xml ~/.p4qt/ApplicationSettings.xml
fi
echo end postinst
POST_EOF
chmod 775 ./debian/DEBIAN/postinst

##############################
# build
##############################
dpkg-deb --build debian
mv debian.deb APKScanner.deb
