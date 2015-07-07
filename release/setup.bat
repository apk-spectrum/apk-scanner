@echo off
cd
set APP_PATH=C:\Program Files\APKScanner
set APP_FILE=ApkScanner.exe
set SRC_PATH=.

if not exist "%SRC_PATH%\%APP_FILE%" (
    set SRC_PATH=C:
    if not exist "%SRC_PATH%\%APP_FILE%" (
        echo Fail : No such %APP_FILE% file
        echo Info : Please copy the APKScanner folder to the C:\ path.
        echo Info : And run setup.bat as administrator.
        goto exit
    )
)

rem --- Java 버전확인 ---
java -version > javaver.txt 2>&1
set /p java_ver=<javaver.txt
del javaver.txt

if "%java_ver%" == "%java_ver:java version =%" (
    goto nosuch_java
)
set java_ver=%java_ver:java version =%
set java_ver=%java_ver:"=%
set java_ver=%java_ver:~,3%

rem Java 1.7버전 이상필요
if not "%java_ver%" GEQ "1.7" (
    echo Need JDK7...
    echo current version : %java_ver%
    goto nosuch_java
)

rmdir /s /q "%APP_PATH%"

rem --- 폴더 생성 ---
if not exist "%APP_PATH%" (
    echo Create folder : %APP_PATH%
    mkdir "%APP_PATH%"
    rem mkdir "%APP_PATH%\tool"
)
if not exist "%APP_PATH%\tool" (
     echo Create folder : %APP_PATH%\tool
     mkdir "%APP_PATH%\tool"
)
if not exist "%APP_PATH%\res" (
     echo Create folder : %APP_PATH%\res
     mkdir "%APP_PATH%\res"
)
if not exist "%APP_PATH%\res\values" (
     echo Create folder : %APP_PATH%\res\values
     mkdir "%APP_PATH%\res\values"
)
if not exist "%APP_PATH%" (
    echo Fail : Not create the folder : %APP_PATH%
    goto exit
)

rem --- 파일 복사 ---
copy /Y %SRC_PATH%\ApkScanner.exe "%APP_PATH%"
copy /Y %SRC_PATH%\APKInfoDlg.jar "%APP_PATH%"
copy /Y %SRC_PATH%\tool\apktool.jar "%APP_PATH%\tool"
copy /Y %SRC_PATH%\tool\adb.exe "%APP_PATH%\tool"
copy /Y %SRC_PATH%\res\AppIcon.png "%APP_PATH%\res"
copy /Y %SRC_PATH%\res\warning.png "%APP_PATH%\res"
copy /Y %SRC_PATH%\res\question.png "%APP_PATH%\res"
copy /Y %SRC_PATH%\res\Succes.png "%APP_PATH%\res"
copy /Y %SRC_PATH%\res\toolbar_about.png "%APP_PATH%\res"
copy /Y %SRC_PATH%\res\toolbar_install.png "%APP_PATH%\res"
copy /Y %SRC_PATH%\res\toolbar_open.png "%APP_PATH%\res"
copy /Y %SRC_PATH%\res\toolbar_pack.png "%APP_PATH%\res"
copy /Y %SRC_PATH%\res\toolbar_explorer.png "%APP_PATH%\res"
copy /Y %SRC_PATH%\res\toolbar_manifast.png "%APP_PATH%\res"
copy /Y %SRC_PATH%\res\toolbar_unpack.png "%APP_PATH%\res"
copy /Y %SRC_PATH%\res\toolbar_about_hover.png "%APP_PATH%\res"
copy /Y %SRC_PATH%\res\toolbar_install_hover.png "%APP_PATH%\res"
copy /Y %SRC_PATH%\res\toolbar_open_hover.png "%APP_PATH%\res"
copy /Y %SRC_PATH%\res\toolbar_pack_hover.png "%APP_PATH%\res"
copy /Y %SRC_PATH%\res\toolbar_explorer_hover.png "%APP_PATH%\res"
copy /Y %SRC_PATH%\res\toolbar_manifast_hover.png "%APP_PATH%\res"
copy /Y %SRC_PATH%\res\toolbar_unpack_hover.png "%APP_PATH%\res"
copy /Y %SRC_PATH%\res\loading.gif "%APP_PATH%\res"
copy /Y %SRC_PATH%\res\install_wait.gif "%APP_PATH%\res"
copy /Y %SRC_PATH%\res\values\strings.xml "%APP_PATH%\res\values"
copy /Y %SRC_PATH%\res\values\strings-ko.xml "%APP_PATH%\res\values"
rem copy /Y .\tool\* "%APP_PATH%\tool\"


rem --- 연결프로그램 지정 ---
assoc .apk=vnd.android.package-archive
rem ftype vnd.android.package-archive=javaw -jar "-Dfile.encoding=utf-8" "%APP_PATH%\%APP_FILE%" %%1 %%*
reg add "HKCR\vnd.android.package-archive\DefaultIcon" /t REG_SZ /d "%APP_PATH%\%APP_FILE%,2" /f
ftype vnd.android.package-archive="%APP_PATH%\%APP_FILE%" "%%1"

rem attrib -h %USERPROFILE%\AppData\Local\IconCache.db
rem del %USERPROFILE%\AppData\Local\IconCache.db

echo Complete

goto exit

:nosuch_java
set java_ver=
echo Please retry after setup JDK7..
echo You can download the JDK7 from http://www.oracle.com/technetwork/java/javase/downloads/index.html
goto exit

:exit
pause