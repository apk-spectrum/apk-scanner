# APK Scanner 
[![Software License](https://img.shields.io/badge/license-Apache%202.0-brightgreen.svg)](https://github.com/apk-spectrum/apk-scanner/blob/master/LICENSE)  
Welcome to the APK Scanner!  

WEBSITE : https://apk-spectrum.github.io/apk-scanner/  
WIKI : https://github.com/apk-spectrum/apk-scanner/wiki  


## Introduce the APK Scanner  
The APK scanner analyzes the APK file to show all the APK information,  
And provides functions such as installing in the device and extracting and analyzing the installed apps,  
It is a tool to dramatically streamline APK management. [(more..)](https://apk-spectrum.github.io/apk-scanner/)

![](https://github.com/apk-spectrum/apk-scanner/blob/gh-pages/img/manual/apk-scanner-launch-img.png)

## Build
This project uses the Maven. And include the Maven Wrapper (MVNW).  
> [Windows]  
> \> .\mvnw.cmd compile   
>  
> [Ubuntu/Mac]  
> $ ./mvnw compile  

You can build on IDE such as VSCODE or ECLIPSE.  

### Requirements
JDK 8 or later  
If using VSCode with "Extension Pack for Java"  

### Open a project in IDE
1. source code download with submodule  
> git clone **--recurse-submodule** https://github.com/apk-spectrum/apk-scanner  

2. Open project  

by VSCODE)  
> VSCODE > File > Open Folder > Select "apk-scanner path"  
> \> Wait open the "JAVA PROJECTS" into the EXPLORER side bar > more(…) > Clean Workspace > Reload and delete  
>  
> Run Configuration)  
> Run and Debug(Ctrl + Shift + D) > "create a launch.json file" > Java > Select "Launch Main" in combobox  
> \> Run > Start Debugging(F5) or Run Without Debugginng(Ctrl + F5)  
```
Set JVM Options)  
Add line below into each configuration of launch.json  
"vmArgs": ["-Dfile.encoding=UTF-8"]  
OR  
Add this line below into your user setting json file  
"java.debug.settings.vmArgs": "-Dfile.encoding=UTF-8"  
```

by ECLIPSE)  
> Eclipse > File > Open Projects from File System... > Directory... > Select "apk-scanner path" > Finish  
> \> Right click in Project Explorer > Maven > Update Project...(Alt + F5) > OK  
>  
> Run Configuration)  
> Run > Run As > Java Application(Alt+Shift+X, J) > Select a "Main - com.apkscanner" > OK  

## Show log for debugging  
> Press <b>F12</b> key on main window focused.

## Package  
### 1. maven package  
> [Windows]  
> \> .\mvnw.cmd package  
>  
> [Ubuntu/Mac]  
> $ ./mvnw package  
>  
> Output : target/APKScanner.jar
  
### 2. Make an installer or archive  
```
[ALL Platform on Ubuntu/Mac]  
Requirements : NSIS, dpkg  
  Ubuntu :  
    $ sudo apt update && sudo apt install -y nsis  
  MacOS :  
    $ sudo brew install makensis dpkg  

$ target/package.sh 2.x.x v2xxxx_r1_yyyymmdd  
OR  
$ APP_VERSION=2.x.x FILE_VERSION=v2xxxx_r1_yyyymmdd target/package.sh  

Output :  
target/APKScanner_v2xxxx_r1_yyyymmdd_win_setup.exe  
target/APKScanner_v2xxxx_r1_yyyymmdd_win_portable.zip  
target/APKScanner_v2xxxx_r1_yyyymmdd_ubuntu_setup.deb  
target/APKScanner_v2xxxx_r1_yyyymmdd_mac.dmg [Only on Mac]  

[Windows]  
Requirements : NSIS 3 or later - https://nsis.sourceforge.io/Download  
  
> .\target\package.bat 2.x.x v2xxxx_r1_yyyymmdd  
  
Output :  
target/APKScanner_v2xxxx_r1_yyyymmdd_win_setup.exe  
target/APKScanner_v2xxxx_r1_yyyymmdd_win_portable.zip  
```  
  
```
[Windows Installer]  
: on Windows10  
Requirements : NSIS 3 or later - https://nsis.sourceforge.io/Download  

> .\target\win_setup.bat
  
: on Ubuntu/Mac  
Requirements : NSIS  
  Ubuntu :  
    $ sudo apt update && sudo apt install -y nsis  
  MacOS :  
    $ sudo brew install makensis  
 
$ target/win_setup.sh  
   
Output : target/APKScanner_win_setup.exe
```

```
[Windows portable archive]  
: on Ubuntu/Mac  
$ target/win_portable.sh  
  
: on Windows10
> target\win_portable.bat  
  
Output : target/APKScanner_win_portable.zip  
```

```
[Ubuntu debian package]  
: on Ubuntu/Mac  
$ target/debian_setup.sh  
  
Output : target/APKScanner_ubuntu_setup.deb
```

```
[MacOS DMG image]  
: on Mac  
$ target/mac_setup.sh  
  
Output : target/APKScanner_mac.dmg
```

## Setup & Launch  
Supported OS : Windows10, Linux Ubuntu 18.04, macOS Catalina(10.15.7)  
※ Based on tested OS version.  

### Setup  

#### for Windows  
##### Using SetupWizard  
1. Launch APKScanner_xxx_win_setup.exe  
##### Using portable  
1. Uncompress APKScanner_xxx_win_portable.zip  
2. Launch ApkScanner.exe  

#### for Linux (ubuntu)  
##### Using Debian installer  
$ sudo dpkg -i APKScanner_xxx_linux_setup.deb  

### Launch
1. Double click to an APK File on the explorer.  


## External Tools & Resources  
Using following tools & libraries  

#### External Execute Tools  
Android Asset Packaging Tool, Android Debug Bridge [![Software License](https://img.shields.io/badge/license-Attribution%202.5-brightgreen.svg)](https://developer.android.com/license.html)  
\- http://developer.android.com/tools/help/adb.html  
JD-GUI [![Software License](https://img.shields.io/badge/license-GPLv3-brightgreen.svg)](https://github.com/java-decompiler/jd-gui/blob/master/LICENSE)  
\- http://jd.benow.ca/  
dex2jar [![Software License](https://img.shields.io/badge/license-Apache%202.0-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0)  
\- https://sourceforge.net/projects/dex2jar/  

#### External Dependency Libraries  
ddmlib [![Software License](https://img.shields.io/badge/license-Attribution%202.5-brightgreen.svg)](https://developer.android.com/license.html)  
\- https://android.googlesource.com/platform/tools/base/+/master/ddmlib/  
guava [![Software License](https://img.shields.io/badge/license-Apache%202.0-brightgreen.svg)](https://github.com/google/guava/blob/master/COPYING)  
\- https://github.com/google/guava/  
jna [![Software License](https://img.shields.io/badge/license-LGPL-brightgreen.svg)](https://github.com/java-native-access/jna/blob/master/LICENSE)  
\- https://github.com/java-native-access/jna/  
mslinks [![Software License](https://img.shields.io/badge/license-WTFPL-brightgreen.svg)](https://github.com/BlackOverlord666/mslinks/blob/master/LICENSE)  
\- https://github.com/BlackOverlord666/mslinks/  
RSyntaxTextArea with AutoComplete, RSTAUI [![Software License](https://img.shields.io/badge/license-BSD-brightgreen.svg)](https://github.com/bobbylight/RSyntaxTextArea/blob/master/src/main/dist/RSyntaxTextArea.License.txt)  
\- http://bobbylight.github.io/RSyntaxTextArea/  
json-simple [![Software License](https://img.shields.io/badge/license-Apache%202.0-brightgreen.svg)](https://github.com/fangyidong/json-simple/blob/master/LICENSE.txt)  
\- https://code.google.com/archive/p/json-simple/  
commons-cli [![Software License](https://img.shields.io/badge/license-Apache%202.0-brightgreen.svg)](http://www.apache.org/licenses/)  
\- https://commons.apache.org/proper/commons-cli/  
luciad/webp-imageio [![Software License](https://img.shields.io/badge/license-Apache%202.0-brightgreen.svg)](http://www.apache.org/licenses/)  
\- https://bitbucket.org/luciad/webp-imageio/  

### Images  
Toolbar icons : https://dribbble.com/shots/1925117-Filo-Icon-Set-Free   
Othres : https://github.com/ioBroker/ioBroker.icons-open-icon-library-png

## License [![Software License](https://img.shields.io/badge/license-Apache%202.0-brightgreen.svg)](https://github.com/apk-spectrum/apk-scanner/blob/master/LICENSE)  
APK Sanner are released under the Apache 2.0 license.  

