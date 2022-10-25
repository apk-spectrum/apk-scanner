#!/bin/bash

if [[ $OSTYPE == 'darwin'* ]]; then
RELEASE_DIR=`pwd`/`dirname "$0"`
else
RELEASE_DIR=$(dirname $(readlink -f $0))
fi

echo RELEASE_DIR : ${RELEASE_DIR}

OUT_DIR="${RELEASE_DIR}/APKScanner"

mkdir -p "${RELEASE_DIR}/plugin"

rm -rf "${OUT_DIR}"
mkdir -p "${OUT_DIR}"

cp "${RELEASE_DIR}/ApkScanner.exe" "${OUT_DIR}"
cp "${RELEASE_DIR}/ApkScanner.jar" "${OUT_DIR}"
cp "${RELEASE_DIR}/legacy_setup/setup.bat" "${OUT_DIR}"
cp "${RELEASE_DIR}/legacy_setup/setup.sh" "${OUT_DIR}"
cp -r "${RELEASE_DIR}/data" "${OUT_DIR}"
cp -r "${RELEASE_DIR}/lib" "${OUT_DIR}"
cp -r "${RELEASE_DIR}/plugin" "${OUT_DIR}"
cp -r "${RELEASE_DIR}/security" "${OUT_DIR}"
cp -r "${RELEASE_DIR}/tool" "${OUT_DIR}"

rm -f "${OUT_DIR}/plugin/plugins.conf"

cd "${RELEASE_DIR}"
tar -zcvf "APKScanner_legacy.tar.gz" "APKScanner"
cd -

rm -rf "${OUT_DIR}"

echo output : "${RELEASE_DIR}/APKScanner_legacy.tar.gz"
