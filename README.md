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
This repository have .project and .class files for be build by eclipse.[(more..)](https://github.com/apk-spectrum/apk-scanner/wiki/2.-How-to-build-by-eclipse)  

### Requirements
JDK (7 or 8).  
Eclipse with git plugin

### Create Eclipse project  
1. source code download  
> git clone https://github.com/apk-spectrum/apk-scanner  

2. import Eclipse project  
> Eclipse > File > Import > Git > Project from Git  
> \> Existing local repository > add "your project path" > <b>Import existing Eclipse projects</b>  

3. Run Configuration, Main Class : <b>com.apkscanner.Main</b>  

## Setup & Launch  
Supported OS : Windows7/10, Linux Ubuntu 10.04/12.04/14.04  
※ Based on verified OS version.  

### Setup  

#### for Windows  
##### Using SetupWizard  
1. Launch APKScanner_xxx_win_setup.exe  
##### Using compressed file  
1. Uncompress APKScanner_xxx_legacy.tar.gz  
2. Launch setup.bat (right click > Run as administrator)  
##### Using portable  
1. Uncompress APKScanner_xxx_win_portable.zip  
2. Launch ApkScanner.exe  

#### for Linux (ubuntu)  
##### Using Debian installer  
$ sudo dpkg -i APKScanner_xxx_linux_setup.deb  
##### Using compressed file  
$ tar -xvf APKScanner_xxx_legacy.tar.gz  
$ cd APKScanner  
$ ./setup.sh  

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

#### External JAR Libraries  
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

