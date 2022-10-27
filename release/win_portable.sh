#!/bin/bash

if [ -n "${FILE_VERSION}" ]; then
  FILE_VERSION="${FILE_VERSION}_"
fi

if [[ $OSTYPE == 'darwin'* ]]; then
RELEASE_DIR=`dirname "$0"`
if [[ $RELEASE_DIR != '/'* ]]; then
RELEASE_DIR=`pwd`/${RELEASE_DIR}
fi
else
RELEASE_DIR=$(dirname $(readlink -f $0))
fi

echo RELEASE_DIR : ${RELEASE_DIR}

OUT_DIR="${RELEASE_DIR}/portable"

mkdir -p "${RELEASE_DIR}/plugin"

rm -rf "${OUT_DIR}"
mkdir -p "${OUT_DIR}"

cp "${RELEASE_DIR}/ApkScanner.exe" "${OUT_DIR}"
cp "${RELEASE_DIR}/ApkScanner.jar" "${OUT_DIR}"
cp -r "${RELEASE_DIR}/data" "${OUT_DIR}"
cp -r "${RELEASE_DIR}/lib" "${OUT_DIR}"
cp -r "${RELEASE_DIR}/plugin" "${OUT_DIR}"
cp -r "${RELEASE_DIR}/security" "${OUT_DIR}"
cp -r "${RELEASE_DIR}/tool" "${OUT_DIR}"

rm -rf "${OUT_DIR}/tool/darwin"
rm -rf "${OUT_DIR}/tool/linux"
rm -f "${OUT_DIR}/plugin/plugins.conf"

cd "${OUT_DIR}"
zip -r "${RELEASE_DIR}/APKScanner_${FILE_VERSION}win_portable.zip" *
cd -

rm -rf "${OUT_DIR}"

echo output : "${RELEASE_DIR}/APKScanner_${FILE_VERSION}win_portable.zip"
