@setlocal enableextensions
@cd /d "%~dp0"
@echo off

cd

set APP_PATH=C:\Program Files\APKScanner
set APP_FILE=ApkScanner.exe

set SRC_PATH=%~dp0.
echo SRC_PATH : %SRC_PATH%

if not exist "%SRC_PATH%\%APP_FILE%" (
    set SRC_PATH=C:
    if not exist "%SRC_PATH%\%APP_FILE%" (
        echo Fail : No such %APP_FILE% file
        echo Info : Please copy the APKScanner folder to the C:\ path.
        echo Info : And run setup.bat as administrator.
        goto exit
    )
)

rem --- Check java version ---
java -version > javaver.txt 2>&1
set /p java_ver=<javaver.txt
del javaver.txt

if "%java_ver%" == "%java_ver:java version =%" (
    goto nosuch_java
)
set java_ver=%java_ver:java version =%
set java_ver=%java_ver:"=%
set java_ver=%java_ver:~,3%

rem --- Need Java 1.7 ---
if not "%java_ver%" GEQ "1.7" (
    echo Need JDK7...
    echo current version : %java_ver%
    goto nosuch_java
)

rem --- Kill a running demon of adb ---
if exist "%APP_PATH%\tool\adb.exe" (
    echo adb kill-server
    "%APP_PATH%\tool\adb.exe" kill-server
)

rem --- Remove an existed folder ---
rmdir /s /q "%APP_PATH%"

rem --- Create folders ---
if not exist "%APP_PATH%" (
    echo Create folder : %APP_PATH%
    mkdir "%APP_PATH%"
)
if not exist "%APP_PATH%\tool" (
     echo Create folder : %APP_PATH%\tool
     mkdir "%APP_PATH%\tool"
)
if not exist "%APP_PATH%\tool\lib" (
     echo Create folder : %APP_PATH%\tool\lib
     mkdir "%APP_PATH%\tool\lib"
)
if not exist "%APP_PATH%\lib" (
     echo Create folder : %APP_PATH%\lib
     mkdir "%APP_PATH%\lib"
)
if not exist "%APP_PATH%" (
    echo Fail : No created a folder : %APP_PATH%
    goto exit
)

rem --- Copy files ---
copy /Y "%SRC_PATH%\ApkScanner.exe" "%APP_PATH%"
copy /Y "%SRC_PATH%\APKInfoDlg.jar" "%APP_PATH%"
copy /Y "%SRC_PATH%\lib\*" "%APP_PATH%\lib"
copy /Y "%SRC_PATH%\tool\*" "%APP_PATH%\tool"
copy /Y "%SRC_PATH%\tool\lib\*" "%APP_PATH%\tool\lib"

rem --- Launch APK Scanner  ---
"%APP_PATH%\%APP_FILE%"

echo Complete

goto exit

:nosuch_java
set java_ver=
echo Please retry after setup JDK7..
echo You can download the JDK7 from http://www.oracle.com/technetwork/java/javase/downloads/index.html
goto exit

:exit
pause