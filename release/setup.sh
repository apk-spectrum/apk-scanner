#!/bin/bash

APP_PATH="/opt/APKInfo"
APP_FILE="APKInfoDlg.jar"


# java 버전 확인
java_ver=$(java -version 2>&1 | sed '1!{d}; /^java version/!d; s/java version \"\([0-9].[0-9]\).*\"/\1/')

if [ "$java_ver" == "" ] || [ "$java_ver" != "$(echo $java_ver | awk '{ if($1 >= 1.7) { print $1 } }')" ]; then
    if [ "$java_ver" != "" ]; then
        echo "Need JDK7..."
        echo "current version : $java_ver"
    fi
    echo "Please retry after setup JDK7.."
    echo "You can download the JDK7 from http://www.oracle.com/technetwork/java/javase/downloads/index.html"
    exit
fi

sudo chmod 755 APKInfoDlg.sh

sudo rm -rf $APP_PATH

sudo mkdir -p $APP_PATH
#sudo mkdir -p $APP_PATH/tool
if [ ! -d $APP_PATH ]; then
    echo Fail : Not create the folder : %APP_PATH%
    exit
fi
sudo cp -rf ./* $APP_PATH

#keytool_path=$(which java)
#keytool_path=$(readlink -f $keytool_path)
#keytool_path=$(echo $keytool_path | sed 's/\/java$/\/keytool/')
#if [ -x "$keytool_path" ]; then
#    sudo ln -sf $keytool_path $APP_PATH/tool/keytool
#else
#    echo "keytool 을 찾을수 없습니다."
#fi

cat << EOF > ./apkchecker.desktop
[Desktop Entry]
Encoding=UTF-8
Version=1.0
Type=Application
Exec=java -jar $APP_PATH/$APP_FILE %f
Name=APK Checker
Comment=APK Checker
NoDisplay=true
Icon=$APP_PATH/AppIcon.png
MimeType=application/apk;application/vnd.android.package-archive;
EOF

sudo mv -f ./apkchecker.desktop /usr/share/applications/apkchecker.desktop

if [ ! -e /usr/share/mime/application/vnd.android.package-archive.xml ]; then
    sudo cp -f ./vnd.android.package-archive.xml /usr/share/mime/packages/
    sudo update-mime-database /usr/share/mime/
    sudo rm -f /usr/share/mime/packages/vnd.android.package-archive.xml
fi

cp -f ~/.local/share/applications/mimeapps.list ~/.local/share/applications/mimeapps_old.list
cat ~/.local/share/applications/mimeapps_old.list \
	| sed '/application\/vnd\.android\.package-archive\=/d;/^$/d' \
	| sed 's/^\s*\[.*\]\s*$/&\napplication\/vnd.android.package-archive=apkchecker.desktop;/' > ~/.local/share/applications/mimeapps.list

if [ -e ~/.p4qt/ApplicationSettings.xml ]; then
    cat ~/.p4qt/ApplicationSettings.xml | sed '/EditorMappings/,/StringList/{/<String>apk/d; s/.*<\/StringList>.*/  <String>apk\|default\|\/opt\/APKInfo\/APKInfoDlg\.sh<\/String>\n <\/StringList>/}' > .ApplicationSettings.xml
    mv .ApplicationSettings.xml ~/.p4qt/ApplicationSettings.xml
fi

echo "Complete"

exit
