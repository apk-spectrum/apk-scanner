#!/bin/bash

# purpose: this script creates a signed ".app" application directory for the
#          ACME application
#
# known assumptions for this script:
#   - the application jar files are in the 'lib' directory
#   - the icon file is in the current directory
#   - the necessary resource files are in the 'resources' directory (.ini, etc.)
#   - the necessary apple certificates are installed on the Mac this script is run on
#
# see this URL for details about the `javapackager` command:
# https://docs.oracle.com/javase/8/docs/technotes/tools/unix/javapackager.html

if [[ $OSTYPE != 'darwin'* ]]; then
echo "This script support only MacOS"
exit 1;
fi

if [ -n "${FILE_VERSION}" ]; then
  FILE_VERSION="${FILE_VERSION}_"
fi

# necessary variables
JAVA_HOME=`/usr/libexec/java_home -v 1.8`

export RELEASE_DIR=`dirname "$0"`
if [[ $RELEASE_DIR != '/'* ]]; then
export RELEASE_DIR=`pwd`/${RELEASE_DIR}
fi
export APP_DIR_NAME="Apk Scanner.app"

mkdir -p "${RELEASE_DIR}/plugin"

# javapackager command notes:
#   - `-native image` creates a ".app" file (as opposed to DMG or other)
#   - `-name` is used as the app name in the menubar if you don't specify "-Bmac.CFBundleName"
#   - oracle notes says "use cms for desktop apps"
#   - `v` is for verbose mode. remove it if you don't want/need to see all of the output

# (1) create and sign the ".app" directory structure. this command creates the
#     "./release/bundles/ACME.app" directory.
${JAVA_HOME}/bin/javapackager \
  -deploy -Bruntime=${JAVA_HOME} \
  -native dmg \
  -outdir "${RELEASE_DIR}" \
  -outfile "${APP_DIR_NAME}" \
  -srcdir "${RELEASE_DIR}" \
  -srcfiles ApkScanner.jar:data:lib:security:tool:plugin \
  -appclass com.apkscanner.Main \
  -name "APK Scanner" \
  -title "APK Scanner" \
  -vendor "APK Spectrum" \
  -Bicon="${RELEASE_DIR}/AppIcon.icns" \
  -BdropinResourcesRoot="${RELEASE_DIR}" \
  -v


## if native is image, use below commands.
# (2b) copy *all* resource files into the ".app" directory
# cp ApkIcon.icns "${RELEASE_DIR}/bundles/${APP_DIR_NAME}/Contents/Resources"

# rm -rf "${RELEASE_DIR}/bundles/${APP_DIR_NAME}/Contents/Java/tool/linux"
# rm -rf "${RELEASE_DIR}/bundles/${APP_DIR_NAME}/Contents/Java/tool/windows"
# if [ -e plugin/plugins.conf ]; then
# rm -f "${RELEASE_DIR}/bundles/${APP_DIR_NAME}/Contents/Java/plugin/plugins.conf"
# fi

mv "${RELEASE_DIR}/bundles/APK Scanner-1.0.dmg" "${RELEASE_DIR}/APKScanner_${FILE_VERSION}mac.dmg"

echo output : "${RELEASE_DIR}/APKScanner_${FILE_VERSION}mac.dmg"

rm -rf "${RELEASE_DIR}/bundles"
