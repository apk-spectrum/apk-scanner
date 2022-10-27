#!/bin/bash

export APP_VERSION=${1:-"$APP_VERSION"}
export FILE_VERSION=${2:-"$FILE_VERSION"}

if [[ $OSTYPE == 'darwin'* ]]; then
RELEASE_DIR=`dirname "$0"`
if [[ $RELEASE_DIR != '/'* ]]; then
RELEASE_DIR=`pwd`/${RELEASE_DIR}
fi
else
RELEASE_DIR=$(dirname $(readlink -f $0))
fi

echo RELEASE_DIR : ${RELEASE_DIR}

if [[ -n `which makensis` ]]; then
"${RELEASE_DIR}/win_setup.sh"
fi
if [[ -n `which dpkg-deb` ]]; then
"${RELEASE_DIR}/debian_setup.sh"
fi
if [[ $OSTYPE == 'darwin'* ]]; then
"${RELEASE_DIR}/mac_setup.sh"
fi
"${RELEASE_DIR}/win_portable.sh"
"${RELEASE_DIR}/legacy_setup.sh"
