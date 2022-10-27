@setlocal enableextensions
@cd /d "%~dp0"
@echo off

cd

if "%NSIS_PATH%" == "" (
set NSIS_PATH=C:\Program Files (x86^)\NSIS\makensis.exe
)
echo NSIS_PATH : "%NSIS_PATH%"

if not exist "%NSIS_PATH%" (
    echo Please install NSIS(https://nsis.sourceforge.io/Download^)
    echo If NSIS was installed on another path, set NSIS_PATH to the environment variable.
    goto exit
)

set RELEASE_DIR=%~dp0.

echo RELEASE_DIR : %RELEASE_DIR%

set NSIS_OPTS=/INPUTCHARSET UTF8
if not "%APP_VERSION%" == "" (
set NSIS_OPTS=%NSIS_OPTS% /DAPP_VERSION=%APP_VERSION%
)

if not "%FILE_VERSION%" == "" (
set NSIS_OPTS=%NSIS_OPTS% /DFILE_VERSION=%FILE_VERSION%
)

echo NSIS_OPTS : %NSIS_OPTS%

"%NSIS_PATH%" %NSIS_OPTS% %RELEASE_DIR%\setup.nsi

:exit
if %SKIP_PAUSE% == "" ( pause )
