#!/bin/sh

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

# necessary variables
JAVA_HOME=`/usr/libexec/java_home -v 1.8`
RELEASE_DIR=.
APP_DIR_NAME="Apk Scanner.app"

# javapackager command notes:
#   - `-native image` creates a ".app" file (as opposed to DMG or other)
#   - `-name` is used as the app name in the menubar if you don't specify "-Bmac.CFBundleName"
#   - oracle notes says "use cms for desktop apps"
#   - `v` is for verbose mode. remove it if you don't want/need to see all of the output

# (1) create and sign the ".app" directory structure. this command creates the
#     "./release/bundles/ACME.app" directory.
${JAVA_HOME}/bin/javapackager \
  -deploy -Bruntime=${JAVA_HOME} \
  -native image \
  -outdir ${RELEASE_DIR} \
  -outfile "${APP_DIR_NAME}" \
  -srcdir release \
  -srcfiles ApkScanner.jar \
  -appclass com.apkscanner.Main \
  -name "APK Scanner" \
  -title "APK Scanner" \
  -vendor "APK Spectrum" \
  -Bicon=AppIcon.icns \
  -Bmac.CFBundleVersion=2.9 \
  -Bmac.CFBundleIdentifier=com.apkscanner \
  -Bmac.category=developer-tools \
  -v

# (2b) copy *all* resource files into the ".app" directory
cp Info.plist "${RELEASE_DIR}/bundles/${APP_DIR_NAME}/Contents/"
cp ApkIcon.icns "${RELEASE_DIR}/bundles/${APP_DIR_NAME}/Contents/Resources"
cp -R release/data "${RELEASE_DIR}/bundles/${APP_DIR_NAME}/Contents/Java/"
mkdir -p "${RELEASE_DIR}/bundles/${APP_DIR_NAME}/Contents/Java/lib/lib64"
cp -R release/lib/proxy-vole "${RELEASE_DIR}/bundles/${APP_DIR_NAME}/Contents/Java/lib/proxy-vole/"
cp -R release/lib/lib64/*.dylib "${RELEASE_DIR}/bundles/${APP_DIR_NAME}/Contents/Java/lib/lib64"
cp -R release/lib/*.jar "${RELEASE_DIR}/bundles/${APP_DIR_NAME}/Contents/Java/lib/"
cp -R release/security "${RELEASE_DIR}/bundles/${APP_DIR_NAME}/Contents/Java/"
cp -R release/tool "${RELEASE_DIR}/bundles/${APP_DIR_NAME}/Contents/Java/"
rm -rf "${RELEASE_DIR}/bundles/${APP_DIR_NAME}/Contents/Java/tool/linux"
rm -rf "${RELEASE_DIR}/bundles/${APP_DIR_NAME}/Contents/Java/tool/windows"
mkdir -p "${RELEASE_DIR}/bundles/${APP_DIR_NAME}/Contents/Java/plugin"
touch "${RELEASE_DIR}/bundles/${APP_DIR_NAME}/Contents/Java/plugin/plugins.conf"
cp -R release/plugin "${RELEASE_DIR}/bundles/${APP_DIR_NAME}/Contents/Java/"