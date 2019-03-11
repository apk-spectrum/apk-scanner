; Script generated with the Venis Install Wizard

; Define your application name
!define PROJECTNAME "APK Scanner"
!define PROJECTNAMEANDVERSION "APK Scanner 2.4.2"

; Main Install settings
Name "${PROJECTNAMEANDVERSION}"
InstallDir "$PROGRAMFILES64\APKScanner"
InstallDirRegKey HKLM "Software\${PROJECTNAME}" ""
OutFile "APKScanner.exe"

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
	File "release\data\build-master-target-product-security\*"
	SetOutPath "$INSTDIR\lib\"
	File "release\lib\*.jar"
	File "release\lib\*.dll"
	SetOutPath "$INSTDIR\lib\proxy-vole"
	File "release\lib\proxy-vole\*"
	SetOutPath "$INSTDIR\plugin\"
	File "release\plugin\*"
	SetOutPath "$INSTDIR\security\"
	File "release\security\*"
	SetOutPath "$INSTDIR\tool\"
	File "release\tool\*.exe"
	File "release\tool\*.dll"
	File "release\tool\*.jar"
	File "release\tool\*.bat"
	File "release\tool\*.png"
	SetOutPath "$INSTDIR\tool\jadx\"
	File "release\tool\jadx\*"
	SetOutPath "$INSTDIR\tool\jadx\bin\"
	File "release\tool\jadx\bin\*.bat"
	SetOutPath "$INSTDIR\tool\jadx\lib\"
	File "release\tool\jadx\lib\*"
	SetOutPath "$INSTDIR\tool\lib\"
	File "release\tool\lib\*"

	Exec '"cmd.exe" /c icacls "$INSTDIR" /grant Users:(OI)(CI)F'

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
	Delete "$INSTDIR\*"
	Delete "$INSTDIR\data\*"
	Delete "$INSTDIR\data\build-master-target-product-security\*"
	Delete "$INSTDIR\lib\*"
	Delete "$INSTDIR\lib\proxy-vole\*"
	Delete "$INSTDIR\plugin\*"
	Delete "$INSTDIR\security\*"
	Delete "$INSTDIR\tool\*"
	Delete "$INSTDIR\tool\jadx\*"
	Delete "$INSTDIR\tool\jadx\bin\*"
	Delete "$INSTDIR\tool\jadx\lib\*"
	Delete "$INSTDIR\tool\lib\*"

	; Remove remaining directories
	RMDir "$SMPROGRAMS\APK Scanner"
	RMDir "$INSTDIR\tool\lib\"
	RMDir "$INSTDIR\tool\jadx\lib\"
	RMDir "$INSTDIR\tool\jadx\bin\"
	RMDir "$INSTDIR\tool\jadx\"
	RMDir "$INSTDIR\tool\"
	RMDir "$INSTDIR\security\"
	RMDir "$INSTDIR\plugin\"
	RMDir "$INSTDIR\lib\proxy-vole\"
	RMDir "$INSTDIR\lib\"
	RMDir "$INSTDIR\data\build-master-target-product-security\"
	RMDir "$INSTDIR\data\"
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