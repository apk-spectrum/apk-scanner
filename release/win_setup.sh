#!/bin/bash

if [[ -z `which makensis` ]]; then
    echo "No such makensis."
    echo "Please install the makensis by below command."
    if [[ $OSTYPE == 'darwin'* ]]; then
        echo "$ brew install makensis"
    else
        echo "$ sudo apt-get update && sudo apt-get install -y nsis"
    fi
    exit 1;
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

if [ -n "${APP_VERSION}" ]; then
NSIS_OPTS="-DAPP_VERSION=${APP_VERSION}"
fi

if [ -n "${FILE_VERSION}" ]; then
NSIS_OPTS="${NSIS_OPTS} -DFILE_VERSION=${FILE_VERSION}"
fi

NSIS_SCRIPT="${RELEASE_DIR}/setup.nsi"

if [[ `makensis -version` < 'v3' ]]; then
iconv -c --from-code=UTF-8 --to-code=EUC-KR --output="${RELEASE_DIR}/setup-kr.nsi" "${NSIS_SCRIPT}"
NSIS_SCRIPT="${RELEASE_DIR}/setup-kr.nsi"
else
NSIS_OPTS="${NSIS_OPTS} -INPUTCHARSET UTF8"
fi

echo NSIS_SCRIPT : ${NSIS_SCRIPT}

if [ ! -e ${NSIS_SCRIPT} ]; then
echo "No such ${NSIS_SCRIPT}"
exit 1
fi

echo makensis ${NSIS_OPTS} \"${NSIS_SCRIPT}\"
makensis ${NSIS_OPTS} "${NSIS_SCRIPT}"
