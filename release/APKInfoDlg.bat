@echo off

set APP_PATH=C:\Program Files\APKInfo
set APP_FILE=APKInfoDlg.jar

call java -jar "-Dfile.encoding=utf-8" "%APP_PATH%\%APP_FILE%" "%1%"

exit