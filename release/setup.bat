@echo off

set APP_PATH=C:\Program Files\APKInfo
set APP_FILE=APKInfoDlg.jar

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


del /q "%APP_PATH%"

rem --- 폴더 생성 ---
if not exist "%APP_PATH%" (
    echo Create folder : %APP_PATH%
    mkdir "%APP_PATH%"
    rem mkdir "%APP_PATH%/tool"
)
rem if not exist "%APP_PATH%/tool" (
rem     echo Create folder : %APP_PATH%/tool
rem     mkdir "%APP_PATH%/tool"
rem )
if not exist "%APP_PATH%" (
    echo Fail : Not create the folder : %APP_PATH%
    goto exit
)

rem --- 파일 복사 ---
copy /Y .\* "%APP_PATH%"
rem copy /Y .\tool\* "%APP_PATH%\tool\"


rem --- 연결프로그램 지정 ---
assoc .apk=vnd.android.package-archive
rem ftype vnd.android.package-archive=javaw -jar "-Dfile.encoding=utf-8" "%APP_PATH%\%APP_FILE%" %%1 %%*
ftype vnd.android.package-archive="%APP_PATH%/apktool.bat" %%1 %%*

echo Complete

goto exit

:nosuch_java
set java_ver=
echo Please retry after setup JDK7..
echo You can download the JDK7 from http://www.oracle.com/technetwork/java/javase/downloads/index.html
goto exit

:exit
pause