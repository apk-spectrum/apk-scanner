#!/bin/bash

APP_PATH="/opt/APKScanner"
APP_VERSION="2.9"
APP_FILE="ApkScanner.jar"

RELEASE_DIR=.
DEBIAN_PATH="${RELEASE_DIR}/debian"
DEBIAN_DATA_PATH="${DEBIAN_PATH}${APP_PATH}"
DEBIAN_CTRL_PATH="${DEBIAN_PATH}/DEBIAN"


##############################
# Clear workspace for Debian
##############################
rm -rf "${DEBIAN_PATH}"


##############################
# Copy for release data
##############################
mkdir -p "${DEBIAN_DATA_PATH}"
cp -f "${RELEASE_DIR}/${APP_FILE}" "${DEBIAN_DATA_PATH}"
cp -f "src/main/resources/icons/AppIcon.png" "${DEBIAN_DATA_PATH}"
cp -rf "${RELEASE_DIR}/data" "${DEBIAN_DATA_PATH}"
cp -rf "${RELEASE_DIR}/lib" "${DEBIAN_DATA_PATH}"
cp -rf "${RELEASE_DIR}/plugin" "${DEBIAN_DATA_PATH}"
cp -rf "${RELEASE_DIR}/security" "${DEBIAN_DATA_PATH}"
cp -rf "${RELEASE_DIR}/tool" "${DEBIAN_DATA_PATH}"
rm -rf "${DEBIAN_DATA_PATH}/tool/darwin"
rm -rf "${DEBIAN_DATA_PATH}/tool/windows"


##############################
# Create setting.txt
##############################
echo "{}" > "${DEBIAN_DATA_PATH}/settings.txt"
echo "{}" > "${DEBIAN_DATA_PATH}/plugin/plugins.conf"


##############################
# Make an execution script
##############################
cat << EOF > "${DEBIAN_DATA_PATH}/APKScanner.sh"
#!/bin/bash
java -Xms512m -Xmx1024m -jar "${APP_PATH}/${APP_FILE}" "\$@" > /dev/null
EOF


##############################
# Create shortcut icon to desktop
##############################
mkdir -p "${DEBIAN_PATH}/usr/share/applications/"
cat << EOF > "${DEBIAN_PATH}/usr/share/applications/apkscanner.desktop"
[Desktop Entry]
Encoding=UTF-8
Version=${APP_VERSION}
Type=Application
Exec=java -jar "${APP_PATH}/${APP_FILE}" %f
Name=APK Scanner
Comment=APK Scanner
Icon="${APP_PATH}/AppIcon.png"
MimeType=application/apk;application/vnd.android.package-archive;
EOF


##############################
# Make contorl file
##############################
mkdir -p "${DEBIAN_CTRL_PATH}"
cat << EOF > "${DEBIAN_CTRL_PATH}/control"
Package: apk-scanner
Version: ${APP_VERSION}-1
Section: utils
Architecture: all
Maintainer: Sunggyu Kam <sunggyu.kam@gmail.com>
Installed-Size: `du -ks "${DEBIAN_PATH}" | cut -f 1`
Description: APK Scanner ${APP_VERSION}
EOF
cat "${DEBIAN_CTRL_PATH}/control"


##############################
# Make mime type for APK file
##############################
cat << POST_EOF > "${DEBIAN_CTRL_PATH}/postinst"
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
    <glob pattern="*.apex"/>
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
    cat ~/.p4qt/ApplicationSettings.xml | sed '/EditorMappings/,/StringList/{/<String>apk/d; /<String>ppk/d; /<String>apex/d; s/.*<\/StringList>.*/  <String>apk\|default\|\/opt\/APKScanner\/APKScanner\.sh<\/String>\n  <String>ppk\|default\|\/opt\/APKScanner\/APKScanner\.sh<\/String>\n  <String>apex\|default\|\/opt\/APKScanner\/APKScanner\.sh<\/String>\n <\/StringList>/}' > .ApplicationSettings.xml
    chmod 666 .ApplicationSettings.xml
    mv .ApplicationSettings.xml ~/.p4qt/ApplicationSettings.xml
fi
echo end postinst
POST_EOF


##############################
# Adjust file permissions
##############################
chmod 775 "${DEBIAN_DATA_PATH}/APKScanner.sh"
chmod 666 "${DEBIAN_DATA_PATH}/settings.txt"
chmod 666 "${DEBIAN_DATA_PATH}/plugin/plugins.conf"
chmod 666 "${DEBIAN_DATA_PATH}/security/trustStore.jks"
chmod 775 "${DEBIAN_CTRL_PATH}/postinst"


##############################
# Build package for Debian
##############################
dpkg-deb --build "${DEBIAN_PATH}" "${RELEASE_DIR}/APKScanner.deb"


##############################
# Clear workspace for Debian
##############################
# rm -rf "${DEBIAN_PATH}"
