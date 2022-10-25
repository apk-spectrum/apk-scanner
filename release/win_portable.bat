@setlocal enableextensions
@cd /d "%~dp0"
@echo off

cd

set RELEASE_DIR=%~dp0.
set OUT_DIR="%RELEASE_DIR%\portable"

echo RELEASE_DIR : %RELEASE_DIR%

if not exist "%RELEASE_DIR%\plugin" (
    echo Create folder : "%RELEASE_DIR%\plugin"
    mkdir "%RELEASE_DIR%\plugin"
)

rem --- Remove an existed folder ---
rmdir /s /q "%OUT_DIR%"

echo Create folder : %APP_PATH%
mkdir "%OUT_DIR%"

rem --- Copy files ---
copy /Y "%RELEASE_DIR%\ApkScanner.exe" "%OUT_DIR%"
copy /Y "%RELEASE_DIR%\ApkScanner.jar" "%OUT_DIR%"
xcopy /E /I "%RELEASE_DIR%\data" "%OUT_DIR%\data"
xcopy /E /I "%RELEASE_DIR%\lib" "%OUT_DIR%\lib"
xcopy /E /I "%RELEASE_DIR%\plugin" "%OUT_DIR%\plugin"
xcopy /E /I "%RELEASE_DIR%\security" "%OUT_DIR%\security"
xcopy /E /I "%RELEASE_DIR%\tool" "%OUT_DIR%\tool"

rmdir /s /q "%OUT_DIR%\tool\darwin"
rmdir /s /q "%OUT_DIR%\tool\linux"
del /F "%OUT_DIR%\plugin\plugins.conf"

pushd .
cd "%OUT_DIR%"
tar.exe -a -c -f "%RELEASE_DIR%\APKScanner_win_portable.zip" *
popd

rem --- Remove an existed folder ---
rmdir /s /q "%OUT_DIR%"

echo output : "%RELEASE_DIR%\APKScanner_win_portable.zip"

pause
