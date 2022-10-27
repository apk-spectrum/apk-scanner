#!/bin/bash

CONTENTS_PATH=../images/`ls ../images/`/${APP_DIR_NAME}/Contents
echo CONTENTS_PATH : $CONTENTS_PATH

echo "Copy an APK file icon"
cp ${RELEASE_DIR}/ApkIcon.icns "${CONTENTS_PATH}/Resources"

echo "Remove other platform tools"
rm -rf "${CONTENTS_PATH}/Java/tool/linux"
rm -rf "${CONTENTS_PATH}/Java/tool/windows"

if [ ! -e "${CONTENTS_PATH}/Java/plugin" ]; then
echo "Make a plugin folder"
mkdir -p "${CONTENTS_PATH}/Java/plugin"
fi
rm -f "${CONTENTS_PATH}/Java/plugin/plugins.conf"
