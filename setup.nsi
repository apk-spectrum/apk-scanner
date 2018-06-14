; Script generated with the Venis Install Wizard

; Define your application name
!define PROJECTNAME "APK Scanner"
!define PROJECTNAMEANDVERSION "APK Scanner 2.3.5"

; Main Install settings
Name "${PROJECTNAMEANDVERSION}"
InstallDir "$PROGRAMFILES64\APKScanner"
InstallDirRegKey HKLM "Software\${PROJECTNAME}" ""
OutFile "setup.exe"

; Use compression
SetCompressor Zlib

; Modern interface settings
!include "MUI.nsh"

!define MUI_ABORTWARNING
!define MUI_FINISHPAGE_RUN "$INSTDIR\ApkScanner.exe"

!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_COMPONENTS
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH

!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

; Set languages (first is default language)
!insertmacro MUI_LANGUAGE "English"
!insertmacro MUI_LANGUAGE "Korean"
!insertmacro MUI_RESERVEFILE_LANGDLL

LangString APP_NAME ${LANG_ENGLISH} "APK Scanner"
LangString APP_NAME ${LANG_KOREAN} "APK 스캐너"
LangString APP_NAME_DESC ${LANG_ENGLISH} "APK Scanner"
LangString APP_NAME_DESC ${LANG_KOREAN} "APK 스캐너"
LangString ASSOCITATE_APK ${LANG_ENGLISH} "Associate APK File"
LangString ASSOCITATE_APK ${LANG_KOREAN} "APK파일 연결"
LangString ASSOCITATE_APK_DESC ${LANG_ENGLISH} "Associate APK File. Open apk file by double click."
LangString ASSOCITATE_APK_DESC ${LANG_KOREAN} "APK파일 연결합니다. APK 파일을 더블클릭하여 분석 할수 있습니다."
LangString ADD_STARTMENU ${LANG_ENGLISH} "Start Menu Shortcuts"
LangString ADD_STARTMENU ${LANG_KOREAN} "시작메뉴에 추가"
LangString ADD_STARTMENU_DESC ${LANG_ENGLISH} "Start Menu Shortcuts"
LangString ADD_STARTMENU_DESC ${LANG_KOREAN} "시작메뉴에 바로가기 아이콘을 추가 합니다."
LangString ADD_DESKTOP ${LANG_ENGLISH} "Desktop Shortcut"
LangString ADD_DESKTOP ${LANG_KOREAN} "바탕화면에 추가"
LangString ADD_DESKTOP_DESC ${LANG_ENGLISH} "Desktop Shortcut"
LangString ADD_DESKTOP_DESC ${LANG_KOREAN} "바탕화면에 바로가기 아이콘을 추가 합니다."

Section $(APP_NAME) Section1

	; Set Section properties
	SectionIn RO
	SetOverwrite on

	; Set Section Files and Shortcuts
	SetOutPath "$INSTDIR\"
	Delete "$INSTDIR\APKInfoDlg.jar"

	File "release\ApkScanner.jar"
	File "release\ApkScanner.exe"
	SetOutPath "$INSTDIR\data\build-master-target-product-security\"
	File "release\data\build-master-target-product-security\Android.mk"
	File "release\data\build-master-target-product-security\media.pk8"
	File "release\data\build-master-target-product-security\media.x509.pem"
	File "release\data\build-master-target-product-security\platform.pk8"
	File "release\data\build-master-target-product-security\platform.x509.pem"
	File "release\data\build-master-target-product-security\README"
	File "release\data\build-master-target-product-security\shared.pk8"
	File "release\data\build-master-target-product-security\shared.x509.pem"
	File "release\data\build-master-target-product-security\testkey.pk8"
	File "release\data\build-master-target-product-security\testkey.x509.pem"
	File "release\data\build-master-target-product-security\verity.pk8"
	File "release\data\build-master-target-product-security\verity.x509.pem"
	File "release\data\build-master-target-product-security\verity_key"
	SetOutPath "$INSTDIR\lib\"
	File "release\lib\commons-cli-1.3.1.jar"
	File "release\lib\ddmlib.jar"
	File "release\lib\guava-18.0.jar"
	File "release\lib\jna-4.4.0.jar"
	File "release\lib\jna-platform-4.4.0.jar"
	File "release\lib\json-simple-1.1.1.jar"
	File "release\lib\luciad-webp-imageio.jar"
	File "release\lib\mslinks.jar"
	File "release\lib\rsyntaxtextarea-2.6.1.jar"
	File "release\lib\rstaui-2.6.0.jar"
	File "release\lib\autocomplete-2.6.0.jar"
	File "release\lib\webp-imageio32.dll"
	File "release\lib\webp-imageio64.dll"
	SetOutPath "$INSTDIR\tool\"
	File "release\tool\aapt.exe"
	File "release\tool\AaptNativeWrapper32.dll"
	File "release\tool\AaptNativeWrapper64.dll"
	File "release\tool\adb.exe"
	File "release\tool\AdbWinApi.dll"
	File "release\tool\AdbWinUsbApi.dll"
	File "release\tool\apktool.jar"
	File "release\tool\d2j-dex2jar.bat"
	File "release\tool\d2j_invoke.bat"
	File "release\tool\jd-gui-1.4.0.jar"
	File "release\tool\jd_icon_128.png"
	File "release\tool\signapk.jar"
	SetOutPath "$INSTDIR\tool\lib\"
	File "release\tool\lib\antlr-runtime-3.5.jar"
	File "release\tool\lib\asm-debug-all-4.1.jar"
	File "release\tool\lib\d2j-base-cmd-2.0.jar"
	File "release\tool\lib\d2j-jasmin-2.0.jar"
	File "release\tool\lib\d2j-smali-2.0.jar"
	File "release\tool\lib\dex-ir-2.0.jar"
	File "release\tool\lib\dex-reader-2.0.jar"
	File "release\tool\lib\dex-reader-api-2.0.jar"
	File "release\tool\lib\dex-tools-2.0.jar"
	File "release\tool\lib\dex-translator-2.0.jar"
	File "release\tool\lib\dex-writer-2.0.jar"
	File "release\tool\lib\dx-1.7.jar"
	SetOutPath "$INSTDIR\plugin\"

SectionEnd

Section $(ASSOCITATE_APK) Section2

	; Set Section properties
	SetOverwrite on

	; Set Section Files and Shortcuts
    WriteRegStr HKCR "ApkScanner.apk\CLSID" "" "{E88DCCE0-B7B3-11d1-A9F0-00AA0060FA31}"
    WriteRegStr HKCR "ApkScanner.apk\DefaultIcon" "" "$INSTDIR\ApkScanner.exe,1"
    WriteRegStr HKCR "ApkScanner.apk\OpenWithProgids" "CompressedFolder" ""
    WriteRegExpandStr HKCR "ApkScanner.apk\Shell\Open\Command" "" "$\"$INSTDIR\ApkScanner.exe$\" $\"%1$\""
    WriteRegExpandStr HKCR "ApkScanner.apk\Shell\Install\Command" "" "$\"$INSTDIR\ApkScanner.exe$\" install $\"%1$\""
    WriteRegStr HKCR ".apk" "" "ApkScanner.apk"

    Exec '"cmd.exe" /c assoc .apk=ApkScanner.apk'

SectionEnd

Section $(ADD_STARTMENU) Section3

	; Set Section properties
	SetOverwrite on

	; Set Section Files and Shortcuts
	CreateDirectory "$SMPROGRAMS\APK Scanner"
	CreateShortCut "$SMPROGRAMS\APK Scanner\$(APP_NAME).lnk" "$INSTDIR\ApkScanner.exe"
	CreateShortCut "$SMPROGRAMS\APK Scanner\Uninstall.lnk" "$INSTDIR\uninstall.exe"

SectionEnd

Section $(ADD_DESKTOP) Section4

	; Set Section properties
	SetOverwrite on

	; Set Section Files and Shortcuts
	CreateShortCut "$DESKTOP\$(APP_NAME).lnk" "$INSTDIR\ApkScanner.exe"

SectionEnd

Section -FinishSection

	WriteRegStr HKLM "Software\${PROJECTNAME}" "" "$INSTDIR"
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROJECTNAME}" "DisplayName" "${PROJECTNAME}"
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROJECTNAME}" "UninstallString" "$INSTDIR\uninstall.exe"
	WriteUninstaller "$INSTDIR\uninstall.exe"

SectionEnd

; Modern install component descriptions
!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
	!insertmacro MUI_DESCRIPTION_TEXT ${Section1} $(APP_NAME_DESC)
	!insertmacro MUI_DESCRIPTION_TEXT ${Section2} $(ASSOCITATE_APK_DESC)
	!insertmacro MUI_DESCRIPTION_TEXT ${Section3} $(ADD_STARTMENU_DESC)
	!insertmacro MUI_DESCRIPTION_TEXT ${Section4} $(ADD_DESKTOP_DESC)
!insertmacro MUI_FUNCTION_DESCRIPTION_END

;Uninstall section
Section Uninstall

	;Remove from registry...
	DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROJECTNAME}"
	DeleteRegKey HKLM "SOFTWARE\${PROJECTNAME}"

	DeleteRegKey HKCR "ApkScanner.apk"

	; Delete self
	Delete "$INSTDIR\uninstall.exe"

	; Delete Shortcuts
	Delete "$SMPROGRAMS\APK Scanner\$(APP_NAME).lnk"
	Delete "$SMPROGRAMS\APK Scanner\Uninstall.lnk"
	Delete "$DESKTOP\$(APP_NAME).lnk"

	; Clean up APK Scanner
	Delete "$INSTDIR\ApkScanner.jar"
	Delete "$INSTDIR\ApkScanner.exe"
	Delete "$INSTDIR\data\build-master-target-product-security\Android.mk"
	Delete "$INSTDIR\data\build-master-target-product-security\media.pk8"
	Delete "$INSTDIR\data\build-master-target-product-security\media.x509.pem"
	Delete "$INSTDIR\data\build-master-target-product-security\platform.pk8"
	Delete "$INSTDIR\data\build-master-target-product-security\platform.x509.pem"
	Delete "$INSTDIR\data\build-master-target-product-security\README"
	Delete "$INSTDIR\data\build-master-target-product-security\shared.pk8"
	Delete "$INSTDIR\data\build-master-target-product-security\shared.x509.pem"
	Delete "$INSTDIR\data\build-master-target-product-security\testkey.pk8"
	Delete "$INSTDIR\data\build-master-target-product-security\testkey.x509.pem"
	Delete "$INSTDIR\data\build-master-target-product-security\verity.pk8"
	Delete "$INSTDIR\data\build-master-target-product-security\verity.x509.pem"
	Delete "$INSTDIR\data\build-master-target-product-security\verity_key"
	Delete "$INSTDIR\lib\commons-cli-1.3.1.jar"
	Delete "$INSTDIR\lib\ddmlib.jar"
	Delete "$INSTDIR\lib\guava-18.0.jar"
	Delete "$INSTDIR\lib\jna-4.4.0.jar"
	Delete "$INSTDIR\lib\jna-platform-4.4.0.jar"
	Delete "$INSTDIR\lib\json-simple-1.1.1.jar"
	Delete "$INSTDIR\lib\luciad-webp-imageio.jar"
	Delete "$INSTDIR\lib\mslinks.jar"
	Delete "$INSTDIR\lib\rsyntaxtextarea-2.6.1.jar"
	Delete "$INSTDIR\lib\rstaui-2.6.0.jar"
	Delete "$INSTDIR\lib\autocomplete-2.6.0.jar"
	Delete "$INSTDIR\lib\webp-imageio32.dll"
	Delete "$INSTDIR\lib\webp-imageio64.dll"
	Delete "$INSTDIR\tool\aapt.exe"
	Delete "$INSTDIR\tool\AaptNativeWrapper32.dll"
	Delete "$INSTDIR\tool\AaptNativeWrapper64.dll"
	Delete "$INSTDIR\tool\adb.exe"
	Delete "$INSTDIR\tool\AdbWinApi.dll"
	Delete "$INSTDIR\tool\AdbWinUsbApi.dll"
	Delete "$INSTDIR\tool\apktool.jar"
	Delete "$INSTDIR\tool\d2j-dex2jar.bat"
	Delete "$INSTDIR\tool\d2j_invoke.bat"
	Delete "$INSTDIR\tool\jd-gui-1.4.0.jar"
	Delete "$INSTDIR\tool\jd_icon_128.png"
	Delete "$INSTDIR\tool\signapk.jar"
	Delete "$INSTDIR\tool\lib\antlr-runtime-3.5.jar"
	Delete "$INSTDIR\tool\lib\asm-debug-all-4.1.jar"
	Delete "$INSTDIR\tool\lib\d2j-base-cmd-2.0.jar"
	Delete "$INSTDIR\tool\lib\d2j-jasmin-2.0.jar"
	Delete "$INSTDIR\tool\lib\d2j-smali-2.0.jar"
	Delete "$INSTDIR\tool\lib\dex-ir-2.0.jar"
	Delete "$INSTDIR\tool\lib\dex-reader-2.0.jar"
	Delete "$INSTDIR\tool\lib\dex-reader-api-2.0.jar"
	Delete "$INSTDIR\tool\lib\dex-tools-2.0.jar"
	Delete "$INSTDIR\tool\lib\dex-translator-2.0.jar"
	Delete "$INSTDIR\tool\lib\dex-writer-2.0.jar"
	Delete "$INSTDIR\tool\lib\dx-1.7.jar"

	; Remove remaining directories
	RMDir "$SMPROGRAMS\APK Scanner"
	RMDir "$INSTDIR\tool\lib\"
	RMDir "$INSTDIR\tool\"
	RMDir "$INSTDIR\lib\"
	RMDir "$INSTDIR\data\build-master-target-product-security\"
	RMDir "$INSTDIR\data\"
	RMDir "$INSTDIR\plugin\"
	RMDir "$INSTDIR\"

	Var /GLOBAL associate
	ReadRegStr $associate HKCR .apk ""
	DetailPrint "Associate .apk: $associate"
	${If} $associate == "ApkScanner.apk"
		WriteRegStr HKCR ".apk" "" ""
    	ExecWait '"cmd.exe" /c assoc .apk=.apk'
    	Exec '"cmd.exe" /c assoc .apk='
	${EndIf}

SectionEnd

; On initialization
Function .onInit

	!insertmacro MUI_LANGDLL_DISPLAY

FunctionEnd

; eof