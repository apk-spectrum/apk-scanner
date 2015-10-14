#!/bin/bash
java -classpath /opt/APKScanner/APKInfoDlg.jar:/opt/APKScanner/lib/json-simple-1.1.1.jar:/opt/APKScanner/lib/commons-cli-1.3.1.jar: com.apkscanner.Main $* > /dev/null
