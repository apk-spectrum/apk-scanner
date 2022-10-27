@setlocal enableextensions
@cd /d "%~dp0"
@echo off

cd

if not "%1" == "" ( set APP_VERSION=%1 )
if not "%2" == "" ( set FILE_VERSION=%2 )

set RELEASE_DIR=%~dp0.
echo RELEASE_DIR : %RELEASE_DIR%

if not "%APP_VERSION%" == "" ( echo APP_VERSION : %APP_VERSION% )
if not "%FILE_VERSION%" == "" ( echo FILE_VERSION : %FILE_VERSION% )

set SKIP_PAUSE=true

call "%RELEASE_DIR%\win_portable.bat"
call "%RELEASE_DIR%\win_setup.bat"

pause
