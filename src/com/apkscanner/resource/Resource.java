package com.apkscanner.resource;

import java.awt.Font;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.ImageIcon;
import javax.swing.UIManager;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.apkscanner.gui.theme.TabbedPaneUIManager;
import com.apkscanner.gui.util.ImageScaler;
import com.apkscanner.util.Log;
import com.apkscanner.util.SystemUtil;
import com.apkscanner.util.XmlPath;
import com.apkscanner.util.ZipFileUtil;

public enum Resource
{
	STR_APP_NAME				(Type.TEXT, "@app_name"),
	STR_APP_VERSION				(Type.TEXT, "2.4.1"),
	STR_APP_BUILD_MODE			(Type.TEXT, "eng"),
	STR_APP_MAKER				(Type.TEXT, "jin_h.lee / sunggyu.kam"),
	STR_APP_MAKER_EMAIL			(Type.TEXT, "jin_h.lee@samsung.com;sunggyu.kam@samsung.com"),

	STR_SAMSUNG_KEY_MD5			(Type.TEXT, "D0:87:E7:29:12:FB:A0:64:CA:FA:78:DC:34:AE:A8:39"),
	STR_SS_TEST_KEY_MD5			(Type.TEXT, "8D:DB:34:2F:2D:A5:40:84:02:D7:56:8A:F2:1E:29:F9"),

	STR_SDK_INFO_FILE_PATH		(Type.TEXT, "/values/sdk-info.xml"),

	STR_TITLE_INSTALL_WIZARD	(Type.TEXT, "@title_install_wizard"),
	STR_TITLE_APK_SIGNER		(Type.TEXT, "@title_apk_signer"),
	STR_TITLE_EDIT_CONFIG		(Type.TEXT, "@title_edit_config"),
	STR_TITLE_NO_SUCH_NETWORK	(Type.TEXT, "@title_no_such_network"),
	STR_TITLE_NETWORK_TIMEOUT	(Type.TEXT, "@title_network_timeout"),
	STR_TITLE_SSL_EXCEPTION		(Type.TEXT, "@title_ssl_exception"),
	STR_TITLE_UPDATE_LIST		(Type.TEXT, "@title_update_list"),

	STR_BTN_OPEN				(Type.TEXT, "@btn_open"),
	STR_BTN_OPEN_PACKAGE		(Type.TEXT, "@btn_open_pacakge"),
	STR_BTN_MANIFEST			(Type.TEXT, "@btn_manifest"),
	STR_BTN_MANIFEST_SAVE_AS	(Type.TEXT, "@btn_manifest_save"),
	STR_BTN_EXPLORER			(Type.TEXT, "@btn_explorer"),
	STR_BTN_UNPACK				(Type.TEXT, "@btn_unpack"),
	STR_BTN_PACK				(Type.TEXT, "@btn_pack"),
	STR_BTN_INSTALL				(Type.TEXT, "@btn_install"),
	STR_BTN_INSTALL_DOWNGRAD	(Type.TEXT, "@btn_install_downgrad"),
	STR_BTN_INSTALL_UPDATE		(Type.TEXT, "@btn_install_update"),
	STR_BTN_LAUNCH				(Type.TEXT, "@btn_launch"),
	STR_BTN_LAUNCH_SELECT		(Type.TEXT, "@btn_launch_select_launcher"),
	STR_BTN_SIGN				(Type.TEXT, "@btn_sign"),
	STR_BTN_PUSH				(Type.TEXT, "@btn_push"),
	STR_BTN_SETTING				(Type.TEXT, "@btn_setting"),
	STR_BTN_ABOUT				(Type.TEXT, "@btn_about"),
	STR_BTN_SAVE				(Type.TEXT, "@btn_save"),
	STR_BTN_CLOSE				(Type.TEXT, "@btn_close"),
	STR_BTN_OK					(Type.TEXT, "@btn_ok"),
	STR_BTN_YES					(Type.TEXT, "@btn_yes"),
	STR_BTN_NO					(Type.TEXT, "@btn_no"),
	STR_BTN_CANCEL				(Type.TEXT, "@btn_cancel"),
	STR_BTN_REFRESH				(Type.TEXT, "@btn_refresh"),
	STR_BTN_ADD					(Type.TEXT, "@btn_add"),
	STR_BTN_DEL					(Type.TEXT, "@btn_del"),
	STR_BTN_EDIT				(Type.TEXT, "@btn_edit"),
	STR_BTN_EXPORT				(Type.TEXT, "@btn_export"),
	STR_BTN_OPENCODE			(Type.TEXT, "@btn_opencode"),
	STR_BTN_OPENING_CODE		(Type.TEXT, "@btn_opening_code"),
	STR_BTN_SEARCH				(Type.TEXT, "@btn_search"),
	STR_BTN_MORE				(Type.TEXT, "@btn_more"),
	STR_BTN_ASSOC_FTYPE			(Type.TEXT, "@btn_assoc_ftype"),
	STR_BTN_UNASSOC_FTYPE		(Type.TEXT, "@btn_unassoc_ftype"),
	STR_BTN_CREATE_SHORTCUT		(Type.TEXT, "@btn_create_shortcut"),
	STR_BTN_SELF_SEARCH			(Type.TEXT, "@btn_self_search"),
	STR_BTN_DETAILS_INFO		(Type.TEXT, "@btn_details_info"),
	STR_BTN_TO_INSTALL			(Type.TEXT, "@btn_to_install"),
	STR_BTN_TO_CANNOT_INSTALL	(Type.TEXT, "@btn_to_cannot_install"),
	STR_BTN_TO_PUSH				(Type.TEXT, "@btn_to_push"),
	STR_BTN_TO_CANNOT_PUSH		(Type.TEXT, "@btn_to_cannot_push"),
	STR_BTN_NO_INSTALL			(Type.TEXT, "@btn_no_install"),
	STR_BTN_IMPOSSIBLE_INSTALL	(Type.TEXT, "@btn_impossible_install"),
	STR_BTN_APPLY_ALL_MODELS	(Type.TEXT, "@btn_apply_all_models"),
	STR_BTN_LAUNCH_AF_INSTALLED	(Type.TEXT, "@btn_launch_after_installed"),
	STR_BTN_REPLACE_EXISTING_APP(Type.TEXT, "@btn_replace_existing_app"),
	STR_BTN_ALLOW_DOWNGRADE		(Type.TEXT, "@btn_allow_downgrade"),
	STR_BTN_INSTALL_ON_SDCARD	(Type.TEXT, "@btn_install_on_sdcard"),
	STR_BTN_GRANT_RUNTIME_PERM	(Type.TEXT, "@btn_grant_runtime_perm"),
	STR_BTN_FORWARD_LOCK		(Type.TEXT, "@btn_forward_lock"),
	STR_BTN_ALLOW_TEST_PACKAGE	(Type.TEXT, "@btn_allow_test_package"),
	STR_BTN_REBOOT_AF_PUSHED	(Type.TEXT, "@btn_reboot_after_pushed"),

	STR_BTN_INSTALLED			(Type.TEXT, "@btn_installed"),
	STR_BTN_NOT_INSTALLED		(Type.TEXT, "@btn_not_installed"),
	STR_BTN_WAITING				(Type.TEXT, "@btn_waiting"),
	STR_BTN_SUCCESS				(Type.TEXT, "@btn_success"),
	STR_BTN_FAIL				(Type.TEXT, "@btn_fail"),
	STR_BTN_RETRY				(Type.TEXT, "@btn_retry"),
	STR_BTN_APPLY				(Type.TEXT, "@btn_apply"),
	STR_BTN_REMOVE				(Type.TEXT, "@btn_remove"),
	STR_BTN_IMPORT				(Type.TEXT, "@btn_import"),
	STR_BTN_MANAGE_CERT			(Type.TEXT, "@btn_manage_cert"),
	STR_BTN_CHECK_UPDATE		(Type.TEXT, "@btn_check_update"),
	STR_BTN_GO_TO_WEBSITE		(Type.TEXT, "@btn_go_to_website"),
	STR_BTN_UPDATE				(Type.TEXT, "@btn_update"),
	STR_BTN_DOWNLOAD			(Type.TEXT, "@btn_download"),
	STR_BTN_NO_UPDATED			(Type.TEXT, "@btn_no_updated"),
	STR_BTN_CHOOSE_UPDATE		(Type.TEXT, "@btn_choose_update"),

	STR_BTN_OPEN_LAB			(Type.TEXT, "@btn_open_lab"),
	STR_BTN_OPEN_PACKAGE_LAB	(Type.TEXT, "@btn_open_pacakge_lab"),
	STR_BTN_MANIFEST_LAB		(Type.TEXT, "@btn_manifest_lab"),
	STR_BTN_EXPLORER_LAB		(Type.TEXT, "@btn_explorer_lab"),
	STR_BTN_UNPACK_LAB			(Type.TEXT, "@btn_unpack_lab"),
	STR_BTN_PACK_LAB			(Type.TEXT, "@btn_pack_lab"),
	STR_BTN_INSTALL_LAB			(Type.TEXT, "@btn_install_lab"),
	STR_BTN_INSTALL_DOWNGRAD_LAB(Type.TEXT, "@btn_install_downgrad_lab"),
	STR_BTN_INSTALL_UPDATE_LAB	(Type.TEXT, "@btn_install_update_lab"),
	STR_BTN_LAUNCH_LAB			(Type.TEXT, "@btn_launch_lab"),
	STR_BTN_SIGN_LAB			(Type.TEXT, "@btn_sign_lab"),
	STR_BTN_SETTING_LAB			(Type.TEXT, "@btn_setting_lab"),
	STR_BTN_ABOUT_LAB			(Type.TEXT, "@btn_about_lab"),
	STR_BTN_OPENCODE_LAB		(Type.TEXT, "@btn_opencode_lab"),
	STR_BTN_OPENING_CODE_LAB	(Type.TEXT, "@btn_opening_code_lab"),
	STR_BTN_SEARCH_LAB			(Type.TEXT, "@btn_search_lab"),
	STR_BTN_MORE_LAB			(Type.TEXT, "@btn_more_lab"),
	STR_BTN_ASSOC_FTYPE_LAB		(Type.TEXT, "@btn_assoc_ftype_lab"),
	STR_BTN_UNASSOC_FTYPE_LAB	(Type.TEXT, "@btn_unassoc_ftype_lab"),
	STR_BTN_CREATE_SHORTCUT_LAB	(Type.TEXT, "@btn_create_shortcut_lab"),
	STR_BTN_SELF_SEARCH_LAB		(Type.TEXT, "@btn_self_search_lab"),

	STR_MENU_NEW				(Type.TEXT, "@menu_new"),
	STR_MENU_NEW_WINDOW			(Type.TEXT, "@menu_new_window"),
	STR_MENU_NEW_APK_FILE		(Type.TEXT, "@menu_new_apk_file"),
	STR_MENU_NEW_PACKAGE		(Type.TEXT, "@menu_new_package"),
	STR_MENU_APK_FILE			(Type.TEXT, "@menu_apk_file"),
	STR_MENU_PACKAGE			(Type.TEXT, "@menu_package"),
	STR_MENU_INSTALL			(Type.TEXT, "@menu_install"),
	STR_MENU_UNINSTALL			(Type.TEXT, "@menu_uninstall"),
	STR_MENU_CLEAR_DATA			(Type.TEXT, "@menu_clear_data"),
	STR_MENU_CHECK_INSTALLED	(Type.TEXT, "@menu_check_installed"),
	STR_MENU_DECODER_JD_GUI		(Type.TEXT, "@menu_decorder_jd_gui"),
	STR_MENU_DECODER_JADX_GUI	(Type.TEXT, "@menu_decorder_jadx_gui"),
	STR_MENU_DECODER_BYTECODE	(Type.TEXT, "@menu_decorder_bytecode_viewer"),
	STR_MENU_DECODER_JEB		(Type.TEXT, "@menu_decorder_jeb"),
	STR_MENU_SEARCH_RESOURCE	(Type.TEXT, "@menu_search_resource"),
	STR_MENU_EXPLORER_ARCHIVE	(Type.TEXT, "@menu_explorer_archive"),
	STR_MENU_EXPLORER_FOLDER	(Type.TEXT, "@menu_explorer_folder"),
	STR_MENU_LAUNCH_LAUNCHER	(Type.TEXT, "@menu_launch_lanucher"),
	STR_MENU_LAUNCH_SELECT		(Type.TEXT, "@menu_launch_select"),
	STR_MENU_SELECT_DEFAULT		(Type.TEXT, "@menu_select_default"),
	STR_MENU_VISIBLE_TO_BASIC	(Type.TEXT, "@menu_visible_to_basic"),
	STR_MENU_VISIBLE_TO_BASIC_EACH(Type.TEXT, "@menu_visible_to_basic_each"),

	STR_TAB_BASIC_INFO			(Type.TEXT, "@tab_basic_info"),
	STR_TAB_WIDGETS				(Type.TEXT, "@tab_widgets"),
	STR_TAB_LIBRARIES			(Type.TEXT, "@tab_libraries"),
	STR_TAB_RESOURCES			(Type.TEXT, "@tab_resources"),
	STR_TAB_COMPONENTS			(Type.TEXT, "@tab_components"),
	STR_TAB_SIGNATURES			(Type.TEXT, "@tab_signatures"),
	STR_TAB_PACAKGE_INFO		(Type.TEXT, "@tab_package_info"),
	STR_TAB_DUMPSYS				(Type.TEXT, "@tab_dumpsys"),
	STR_TAB_SYS_PACAKGE_INFO	(Type.TEXT, "@tab_sys_package_info"),
	STR_TAB_ABOUT				(Type.TEXT, "@tab_about"),
	STR_TAB_UPDATE				(Type.TEXT, "@tab_update"),

	STR_TAB_SETTING_GENERIC		(Type.TEXT, "@tab_setting_generic"),
	STR_TAB_SETTING_GENERIC_LAB	(Type.TEXT, "@tab_setting_generic_lab"),
	STR_TAB_SETTING_ANALYSIS	(Type.TEXT, "@tab_setting_analysis"),
	STR_TAB_SETTING_ANALYSIS_LAB(Type.TEXT, "@tab_setting_analysis_lab"),
	STR_TAB_SETTING_DEVICE		(Type.TEXT, "@tab_setting_device"),
	STR_TAB_SETTING_DEVICE_LAB	(Type.TEXT, "@tab_setting_device_lab"),
	STR_TAB_SETTING_DISPLAY		(Type.TEXT, "@tab_setting_display"),
	STR_TAB_SETTING_DISPLAY_LAB	(Type.TEXT, "@tab_setting_display_lab"),
	STR_TAB_SETTING_PLUGINS		(Type.TEXT, "@tab_setting_plugins"),
	STR_TAB_SETTING_PLUGINS_LAB	(Type.TEXT, "@tab_setting_plugins_lab"),

	STR_BASIC_PERMISSIONS		(Type.TEXT, "@basic_permissions"),
	STR_BASIC_PERMLAB_DISPLAY	(Type.TEXT, "@basic_permlab_display_list"),
	STR_BASIC_PERMDESC_DISPLAY	(Type.TEXT, "@basic_permdesc_display_list"),
	STR_BASIC_PERM_LIST_TITLE	(Type.TEXT, "@basic_perm_list_title"),
	STR_BASIC_PERM_DISPLAY_TITLE(Type.TEXT, "@basic_perm_display_title"),

	STR_FEATURE_LAB				(Type.TEXT, "@feature_lab"),
	STR_FEATURE_DESC			(Type.TEXT, "@feature_desc"),
	STR_FEATURE_LAUNCHER_LAB	(Type.TEXT, "@feature_launcher_lab"),
	STR_FEATURE_LAUNCHER_DESC	(Type.TEXT, "@feature_launcher_desc"),
	STR_FEATURE_HIDDEN_LAB		(Type.TEXT, "@feature_hidden_lab"),
	STR_FEATURE_HIDDEN_DESC		(Type.TEXT, "@feature_hidden_desc"),
	STR_FEATURE_STARTUP_LAB		(Type.TEXT, "@feature_startup_lab"),
	STR_FEATURE_STARTUP_DESC	(Type.TEXT, "@feature_startup_desc"),
	STR_FEATURE_SIGNATURE_LAB	(Type.TEXT, "@feature_signature_lab"),
	STR_FEATURE_SIGNATURE_DESC	(Type.TEXT, "@feature_signature_desc"),
	STR_FEATURE_SIGNATURE_UNSIGNED (Type.TEXT, "@feature_signature_unsigned"),
	STR_FEATURE_SIGNATURE_SIGNED (Type.TEXT, "@feature_signature_signed"),
	
	STR_FEATURE_SYSTEM_UID_LAB  (Type.TEXT, "@feature_system_user_id_lab"),
	STR_FEATURE_SYSTEM_UID_DESC (Type.TEXT, "@feature_system_user_id_desc"),
	STR_FEATURE_SHAREDUSERID_LAB  (Type.TEXT, "@feature_shared_user_id_lab"),
	STR_FEATURE_SHAREDUSERID_DESC (Type.TEXT, "@feature_shared_user_id_desc"),
	STR_FEATURE_PLATFORM_SIGN_LAB (Type.TEXT, "@feature_platform_sign_lab"),
	STR_FEATURE_PLATFORM_SIGN_DESC(Type.TEXT, "@feature_platform_sign_desc"),
	STR_FEATURE_SAMSUNG_SIGN_LAB  (Type.TEXT, "@feature_samsung_sign_lab"),
	STR_FEATURE_SAMSUNG_SIGN_DESC (Type.TEXT, "@feature_samsung_sign_desc"),
	STR_FEATURE_REVOKE_PERM_LAB	(Type.TEXT, "@feature_revoke_permissions_lab"),
	STR_FEATURE_REVOKE_PERM_DESC(Type.TEXT, "@feature_revoke_permissions_desc"),
	STR_FEATURE_DEPRECATED_PREM_LAB  (Type.TEXT, "@feature_deprecated_prem_lab"),
	STR_FEATURE_DEPRECATED_PREM_DESC (Type.TEXT, "@feature_deprecated_prem_desc"),
	STR_FEATURE_DEBUGGABLE_LAB  (Type.TEXT, "@feature_debuggable_lab"),
	STR_FEATURE_DEBUGGABLE_DESC (Type.TEXT, "@feature_debuggable_desc"),
	STR_FEATURE_INSTRUMENTATION_LAB  (Type.TEXT, "@feature_instrumentation_lab"),
	STR_FEATURE_INSTRUMENTATION_DESC (Type.TEXT, "@feature_instrumentation_desc"),
	STR_FEATURE_DEVICE_REQ_LAB  (Type.TEXT, "@feature_device_requirements_lab"),
	STR_FEATURE_DEVICE_REQ_DESC (Type.TEXT, "@feature_device_requirements_desc"),
	STR_FEATURE_FLAG_LAB  (Type.TEXT, "@feature_flag_lab"),
	STR_FEATURE_FLAG_DESC (Type.TEXT, "@feature_flag_desc"),
	STR_FEATURE_INSTALLER_LAB	(Type.TEXT, "@feature_installer_lab"),
	STR_FEATURE_INSTALLER_DESC	(Type.TEXT, "@feature_installer_desc"),
	STR_FEATURE_HIDDEN_SYS_PACK_LAB(Type.TEXT, "@feature_hidden_system_package_lab"),
	STR_FEATURE_HIDDEN_SYS_PACK_DESC(Type.TEXT, "@feature_hidden_system_package_desc"),
	STR_FEATURE_DISABLED_PACK_LAB(Type.TEXT, "@feature_disabled_package_lab"),
	STR_FEATURE_DISABLED_PACK_DESC(Type.TEXT, "@feature_disabled_package_desc"),

	STR_FEATURE_ILOCATION_INTERNAL_LAB  (Type.TEXT, "@feature_install_location_internal_only_lab"),
	STR_FEATURE_ILOCATION_INTERNAL_DESC (Type.TEXT, "@feature_install_location_internal_only_desc"),
	STR_FEATURE_ILOCATION_AUTO_LAB  (Type.TEXT, "@feature_install_location_auto_lab"),
	STR_FEATURE_ILOCATION_AUTO_DESC (Type.TEXT, "@feature_install_location_auto_desc"),
	STR_FEATURE_ILOCATION_EXTERNAL_LAB  (Type.TEXT, "@feature_install_location_prefer_external_lab"),
	STR_FEATURE_ILOCATION_EXTERNAL_DESC (Type.TEXT, "@feature_install_location_prefer_external_desc"),

	STR_WIDGET_COLUMN_IMAGE		(Type.TEXT, "@widget_column_image"),
	STR_WIDGET_COLUMN_LABEL		(Type.TEXT, "@widget_column_label"),
	STR_WIDGET_COLUMN_SIZE		(Type.TEXT, "@widget_column_size"),
	STR_WIDGET_COLUMN_ACTIVITY	(Type.TEXT, "@widget_column_activity"),
	STR_WIDGET_COLUMN_TYPE		(Type.TEXT, "@widget_column_type"),
	STR_WIDGET_RESIZE_MODE		(Type.TEXT, "@widget_resize_mode"),
	STR_WIDGET_HORIZONTAL		(Type.TEXT, "@widget_horizontal"),
	STR_WIDGET_VERTICAL			(Type.TEXT, "@widget_vertical"),
	STR_WIDGET_TYPE_NORMAL		(Type.TEXT, "@widget_type_nomal"),
	STR_WIDGET_TYPE_SHORTCUT	(Type.TEXT, "@widget_type_shortcut"),

	STR_LIB_COLUMN_INDEX		(Type.TEXT, "@lib_column_index"),
	STR_LIB_COLUMN_PATH			(Type.TEXT, "@lib_column_path"),
	STR_LIB_COLUMN_SIZE			(Type.TEXT, "@lib_column_size"),
	STR_LIB_COLUMN_COMPRESS		(Type.TEXT, "@lib_column_compressibility"),

	STR_ACTIVITY_COLUME_CLASS	(Type.TEXT, "@activity_column_class"),
	STR_ACTIVITY_COLUME_TYPE	(Type.TEXT, "@activity_column_type"),
	STR_ACTIVITY_COLUME_STARTUP	(Type.TEXT, "@activity_column_startup"),
	STR_ACTIVITY_COLUME_ENABLED	(Type.TEXT, "@activity_column_enabled"),
	STR_ACTIVITY_COLUME_EXPORT	(Type.TEXT, "@activity_column_export"),
	STR_ACTIVITY_COLUME_PERMISSION(Type.TEXT, "@activity_column_permission"),
	STR_ACTIVITY_TYPE_ACTIVITY	(Type.TEXT, "@activity_type_activity"),
	STR_ACTIVITY_TYPE_SERVICE	(Type.TEXT, "@activity_type_service"),
	STR_ACTIVITY_TYPE_RECEIVER	(Type.TEXT, "@activity_type_receiver"),
	STR_ACTIVITY_LABEL_INTENT	(Type.TEXT, "@activity_label_intent_filter"),

	STR_CERT_SUMMURY			(Type.TEXT, "@cert_summury"),
	STR_CERT_CERTIFICATE		(Type.TEXT, "@cert_certificate"),

	STR_FILE_SIZE_BYTES			(Type.TEXT, "@file_size_Bytes"),
	STR_FILE_SIZE_KB			(Type.TEXT, "@file_size_KB"),
	STR_FILE_SIZE_MB			(Type.TEXT, "@file_size_MB"),
	STR_FILE_SIZE_GB			(Type.TEXT, "@file_size_GB"),
	STR_FILE_SIZE_TB			(Type.TEXT, "@file_size_TB"),

	STR_TREE_OPEN_PACKAGE		(Type.TEXT, "@tree_open_package"),

	STR_SETTINGS_TITLE			(Type.TEXT, "@settings_title"),
	STR_SETTINGS_EDITOR			(Type.TEXT, "@settings_editor"),
	STR_SETTINGS_TOOLBAR		(Type.TEXT, "@settings_toolbar"),
	STR_SETTINGS_RES			(Type.TEXT, "@settings_res"),
	STR_SETTINGS_CHECK_INSTALLED(Type.TEXT, "@settings_check_installed"),
	STR_SETTINGS_LANGUAGE		(Type.TEXT, "@settings_language"),
	STR_SETTINGS_REMEMBER_WINDOW_SIZE(Type.TEXT, "@settings_remember_window_size"),
	STR_SETTINGS_THEME			(Type.TEXT, "@settings_theme"),
	STR_SETTINGS_TABBED_UI		(Type.TEXT, "@settings_tabbed_ui"),
	STR_SETTINGS_FONT			(Type.TEXT, "@settings_font"),
	STR_SETTINGS_THEME_FONT		(Type.TEXT, "@settings_theme_font"),
	STR_SETTINGS_THEME_PREVIEW	(Type.TEXT, "@settings_theme_preview"),
	STR_SETTINGS_PREFERRED_LANG	(Type.TEXT, "@settings_preferred_language"),
	STR_SETTINGS_PREFERRED_EX	(Type.TEXT, "@settings_preferred_example"),
	STR_SETTINGS_ADB_POLICY		(Type.TEXT, "@settings_adb_policy"),
	STR_SETTINGS_ADB_SHARED		(Type.TEXT, "@settings_adb_shared"),
	STR_SETTINGS_ADB_RESTART	(Type.TEXT, "@settings_adb_restart"),
	STR_SETTINGS_ADB_PATH		(Type.TEXT, "@settings_adb_path"),
	STR_SETTINGS_ADB_AUTO_SEL	(Type.TEXT, "@settings_adb_auto_selected"),
	STR_SETTINGS_ADB_SHARED_LAB	(Type.TEXT, "@settings_adb_shared_lab"),
	STR_SETTINGS_ADB_RESTART_LAB(Type.TEXT, "@settings_adb_restart_lab"),
	STR_SETTINGS_ADB_MONITOR	(Type.TEXT, "@settings_adb_monitor"),
	STR_SETTINGS_LAUNCH_OPTION	(Type.TEXT, "@settings_launch_option"),
	STR_SETTINGS_LAUNCHER_ONLY	(Type.TEXT, "@settings_launcher_only"),
	STR_SETTINGS_LAUNCHER_OR_MAIN(Type.TEXT, "@settings_launcher_or_main"),
	STR_SETTINGS_LAUNCHER_CONFIRM(Type.TEXT, "@settings_launcher_confirm"),
	STR_SETTINGS_TRY_UNLOCK		 (Type.TEXT, "@settings_try_unlock"),
	STR_SETTINGS_LAUNCH_INSTALLED(Type.TEXT, "@settings_launch_installed"),
	STR_SETTINGS_TOOLBAR_WITH_SHIFT		(Type.TEXT, "@settings_ext_toolbar_with_shift"),
	STR_SETTINGS_TOOLBAR_WITHOUT_SHIFT	(Type.TEXT, "@settings_ext_toolbar_without_shift"),
	STR_SETTINGS_EASYMODE_OPTION_STANDARD	(Type.TEXT, "@settings_easymode_option_standard"),
	STR_SETTINGS_EASYMODE_OPTION_EASY	(Type.TEXT, "@settings_easymode_option_easy"),
	
	
	STR_LABEL_ERROR				(Type.TEXT, "@label_error"),
	STR_LABEL_WARNING			(Type.TEXT, "@label_warning"),
	STR_LABEL_INFO				(Type.TEXT, "@label_info"),
	STR_LABEL_QUESTION			(Type.TEXT, "@label_question"),
	STR_LABEL_LOG				(Type.TEXT, "@label_log"),
	STR_LABEL_INSTALLING		(Type.TEXT, "@label_installing"),
	STR_LABEL_UNINSTALLING		(Type.TEXT, "@label_uninstalling"),
	STR_LABEL_SELECT_DEVICE		(Type.TEXT, "@label_sel_device"),
	STR_LABEL_DEVICE_LIST		(Type.TEXT, "@label_device_list"),
	STR_LABEL_APP_NAME_LIST		(Type.TEXT, "@label_app_name_list"),
	STR_LABEL_NO_PERMISSION		(Type.TEXT, "@label_no_permission"),
	STR_LABEL_SEARCH			(Type.TEXT, "@label_search"),
	STR_LABEL_LOADING			(Type.TEXT, "@label_loading"),
	STR_LABEL_APK_FILE_DESC		(Type.TEXT, "@label_apk_file_description"),
	STR_LABEL_DEVICE			(Type.TEXT, "@label_device"),
	STR_LABEL_PATH				(Type.TEXT, "@label_path"),
	STR_LABEL_USES_RESOURCE		(Type.TEXT, "@label_uses_resource"),
	STR_LABEL_OPEN_WITH			(Type.TEXT, "@label_open_with"),
	STR_LABEL_OPEN_WITH_SYSTEM	(Type.TEXT, "@label_open_with_system"),
	STR_LABEL_OPEN_WITH_JDGUI	(Type.TEXT, "@label_open_with_jdgui"),
	STR_LABEL_OPEN_WITH_JADXGUI	(Type.TEXT, "@label_open_with_jadxgui"),
	STR_LABEL_OPEN_WITH_BYTECODE(Type.TEXT, "@label_open_with_bytecode_viewer"),
	STR_LABEL_OPEN_WITH_EXPLORER(Type.TEXT, "@label_open_with_explorer"),
	STR_LABEL_OPEN_WITH_SCANNER	(Type.TEXT, "@label_open_with_apkscanner"),
	STR_LABEL_OPEN_WITH_CHOOSE	(Type.TEXT, "@label_open_with_choose"),
	STR_LABEL_HOW_TO_INSTALL	(Type.TEXT, "@label_how_to_install"),
	STR_LABEL_INSTALL_OPTIONS	(Type.TEXT, "@label_install_options"),
	STR_LABEL_PUSH_OPTIONS		(Type.TEXT, "@label_push_options"),
	STR_LABEL_ADDITIONAL_OPTIONS(Type.TEXT, "@label_additional_install_options"),
	STR_LABEL_LAUNCHER_AF_INSTALLED(Type.TEXT, "@label_launcher_af_installed"),
	STR_LABEL_WITH_LIBRARIES	(Type.TEXT, "@label_with_libraies"),
	STR_LABEL_NUM				(Type.TEXT, "@label_num"),
	STR_LABEL_ABI				(Type.TEXT, "@label_abi"),
	STR_LABEL_DESTINATION		(Type.TEXT, "@label_destination"),
	STR_LABEL_APK_VERIFY		(Type.TEXT, "@label_apk_verify"),
	STR_LABEL_WAIT_FOR_DEVICE	(Type.TEXT, "@label_wait_for_device"),
	STR_LABEL_APK_INSTALL_OPT	(Type.TEXT, "@label_apk_install_option"),
	STR_LABEL_APK_INSTALLING	(Type.TEXT, "@label_apk_installing"),
	STR_LABEL_APK_INSTALL_FINISH(Type.TEXT, "@label_apk_install_finish"),
	STR_LABEL_SAVE_AS			(Type.TEXT, "@label_save_as"),
	STR_LABEL_BY_PACKAGE_NAME	(Type.TEXT, "@label_by_package_name"),
	STR_LABEL_BY_APP_LABEL		(Type.TEXT, "@label_by_app_label"),
	STR_LABEL_PLUGINS_SETTINGS	(Type.TEXT, "@label_plugins_settings"),
	STR_LABEL_PLUGINS_DESCRIPTION(Type.TEXT, "@label_plugins_description"),
	STR_LABEL_PLUGIN_PACKAGE_CONFIG(Type.TEXT, "@label_plugin_pcakge_config"),
	STR_LABEL_KEY_NAME			(Type.TEXT, "@label_key_name"),
	STR_LABEL_KEY_VALUE			(Type.TEXT, "@label_key_value"),
	STR_LABEL_PROXY_SETTING		(Type.TEXT, "@label_proxy_setting"),
	STR_LABEL_TRUSTSTORE_SETTING(Type.TEXT, "@label_truststore_setting"),
	STR_LABEL_PAC_SCRIPT_URL	(Type.TEXT, "@label_pac_script_url"),
	STR_LABEL_ALIAS				(Type.TEXT, "@label_alias"),
	STR_LABEL_DESCRIPTION		(Type.TEXT, "@label_description"),
	STR_LABEL_DO_NOT_LOOK_AGAIN	(Type.TEXT, "@label_do_not_look_again"),
	STR_LABEL_UPDATE_LIST		(Type.TEXT, "@label_update_list"),
	STR_LABEL_SHOW_UPDATE_STARTUP(Type.TEXT, "@label_show_update_at_startup"),
	STR_LABEL_MARK_A_RUNTIME	(Type.TEXT, "@label_mark_a_runtime"),
	STR_LABEL_MARK_A_COUNT		(Type.TEXT, "@label_mark_a_count"),
	STR_LABEL_TREAT_SIGN_AS_REVOKED(Type.TEXT, "@label_treat_sign_as_revoked"),

	STR_TREE_NODE_DEVICE 		(Type.TEXT, "@tree_node_device"),
	STR_TREE_NODE_DISPLAYED		(Type.TEXT, "@tree_node_displayed"),
	STR_TREE_NODE_RECENTLY		(Type.TEXT, "@tree_node_recently"),
	STR_TREE_NODE_RUNNING_PROC 	(Type.TEXT, "@tree_node_running"),
	STR_TREE_NODE_PLUGINS_TOP 	(Type.TEXT, "@tree_node_plugins_top"),
	STR_TREE_NODE_PLUGINS_TOP_DESC(Type.TEXT, "@tree_node_plugins_top_desc"),
	STR_TREE_NODE_NO_PLUGINS 	(Type.TEXT, "@tree_node_no_plugins"),
	STR_TREE_NODE_NO_PLUGINS_DESC(Type.TEXT, "@tree_node_no_plugins_desc"),
	STR_TREE_NODE_NETWORK		(Type.TEXT, "@tree_node_network_setting"),
	STR_TREE_NODE_CONFIGURATION	(Type.TEXT, "@tree_node_configurations"),
	STR_TREE_NODE_GLOBAL_SETTING(Type.TEXT, "@tree_node_global_setting"),
	STR_TREE_NODE_GLOBAL_SETTING_DESC(Type.TEXT, "@tree_node_global_setting_desc"),

	STR_MSG_FAILURE_OPEN_APK	(Type.TEXT, "@msg_failure_open_apk"),
	STR_MSG_NO_SUCH_APK_FILE	(Type.TEXT, "@msg_no_such_apk"),
	STR_MSG_DEVICE_NOT_FOUND	(Type.TEXT, "@msg_device_not_found"),
	STR_MSG_ALREADY_INSTALLED	(Type.TEXT, "@msg_already_installed"),
	STR_MSG_NO_SUCH_PACKAGE		(Type.TEXT, "@msg_no_such_package"),
	STR_MSG_NO_SUCH_LAUNCHER	(Type.TEXT, "@msg_no_such_launcher"),
	STR_MSG_NO_SUCH_PACKAGE_DEVICE(Type.TEXT, "@msg_no_such_package_device"),
	STR_MSG_NO_SUCH_CLASSES_DEX	(Type.TEXT, "@msg_no_such_classes_dex"),
	STR_MSG_FAILURE_LAUNCH_APP	(Type.TEXT, "@msg_failure_launch_app"),
	STR_MSG_FAILURE_INSTALLED	(Type.TEXT, "@msg_failure_installed"),
	STR_MSG_FAILURE_UNINSTALLED	(Type.TEXT, "@msg_failure_uninstalled"),
	STR_MSG_FAILURE_PULL_APK	(Type.TEXT, "@msg_failure_pull_apk"),
	STR_MSG_FAILURE_DEX2JAR		(Type.TEXT, "@msg_failure_dex2jar"),
	STR_MSG_SUCCESS_INSTALLED	(Type.TEXT, "@msg_success_installed"),
	STR_MSG_SUCCESS_REMOVED		(Type.TEXT, "@msg_success_removed"),
	STR_MSG_SUCCESS_PULL_APK	(Type.TEXT, "@msg_success_pull_apk"),
	STR_MSG_SUCCESS_PULLED		(Type.TEXT, "@msg_success_pulled"),
	STR_MSG_FAILURE_PULLED		(Type.TEXT, "@msg_failure_pulled"),
	STR_MSG_DEVICE_UNAUTHORIZED	(Type.TEXT, "@msg_device_unauthorized"),
	STR_MSG_DEVICE_UNKNOWN		(Type.TEXT, "@msg_device_unknown"),
	STR_MSG_DEVICE_HAS_NOT_ROOT	(Type.TEXT, "@msg_cannot_run_root"),
	STR_MSG_UNSUPPORTED_PREVIEW	(Type.TEXT, "@msg_unsupported_preview"),
	STR_MSG_CANNOT_WRITE_FILE	(Type.TEXT, "@msg_cannot_write_file"),
	STR_MSG_DISABLED_PACKAGE	(Type.TEXT, "@msg_disabled_package"),
	STR_MSG_SUCCESS_CLEAR_DATA	(Type.TEXT, "@msg_success_clear_data"),
	STR_MSG_FAILURE_CLEAR_DATA	(Type.TEXT, "@msg_failure_clear_data"),
	STR_MSG_WARN_UNSUPPORTED_JVM(Type.TEXT, "@msg_warn_unsupported_jvm"),
	STR_MSG_WARN_SSL_IGNORE		(Type.TEXT, "@msg_warn_ssl_ignore"),
	STR_MSG_NO_SUCH_NETWORK		(Type.TEXT, "@msg_no_such_network"),
	STR_MSG_FAILURE_PROXY_ERROR	(Type.TEXT, "@msg_failure_proxy_error"),
	STR_MSG_FAILURE_SSL_ERROR	(Type.TEXT, "@msg_failure_ssl_error"),
	STR_MSG_NO_UPDATE_INFO		(Type.TEXT, "@msg_no_update_informations"),

	STR_QUESTION_REBOOT_DEVICE	(Type.TEXT, "@question_reboot_device"),
	STR_QUESTION_CONTINUE_INSTALL(Type.TEXT, "@question_continue_install"),
	STR_QUESTION_OPEN_OR_INSTALL(Type.TEXT, "@question_open_or_install"),
	STR_QUESTION_PUSH_OR_INSTALL(Type.TEXT, "@question_push_or_install"),
	STR_QUESTION_SAVE_OVERWRITE	(Type.TEXT, "@question_save_overwrite"),
	STR_QUESTION_PACK_INFO_CLOSE(Type.TEXT, "@question_pack_info_close"),
	STR_QUESTION_PACK_INFO_REFRESH(Type.TEXT, "@question_pack_info_refresh"),
	STR_QUESTION_REMOVE_SYSTEM_APK(Type.TEXT, "@question_remove_system_apk"),
	STR_QUESTION_REMOVED_REBOOT (Type.TEXT, "@question_removed_reboot"),

	STR_PATTERN_PRINT_X509_CERT (Type.TEXT, "@pattern_print_x509_cert"),
	STR_WITH_WEAK				(Type.TEXT, "@with_weak"),
	STR_KEY_BIT					(Type.TEXT, "@key_bit"),
	STR_KEY_BIT_WEAK			(Type.TEXT, "@key_bit_weak"),
	STR_NOT_A_SINGED_JAR_FILE	(Type.TEXT, "@not_a_singed_jar_file"),
	STR_EXTENSIONS				(Type.TEXT, "@extensions"),
	STR_EMPTY_VALUE				(Type.TEXT, "@empty_value"),
	STR_TIMESTAMP				(Type.TEXT, "@timestamp"),

	STR_PROXY_MENU_NO_PROXY		(Type.TEXT, "@proxy_menu_no_proxy"),
	STR_PROXY_MENU_GLOBAL		(Type.TEXT, "@proxy_menu_global"),
	STR_PROXY_MENU_SYSTEM		(Type.TEXT, "@proxy_menu_system"),
	STR_PROXY_MENU_PAC_SCRIPT	(Type.TEXT, "@proxy_menu_pac_script"),
	STR_PROXY_MENU_MANUAL		(Type.TEXT, "@proxy_menu_manual"),

	STR_TRUSTSTORE_GLOBAL		(Type.TEXT, "@truststore_golbal"),
	STR_TRUSTSTORE_APKSCANNER	(Type.TEXT, "@truststore_apkscanner"),
	STR_TRUSTSTORE_JVM			(Type.TEXT, "@truststore_jvm"),
	STR_TRUSTSTORE_MANUAL		(Type.TEXT, "@truststore_manual"),
	STR_TRUSTSTORE_IGNORE		(Type.TEXT, "@truststore_ignore"),

	STR_COLUMN_ISSUE_TO			(Type.TEXT, "@column_issued_to"),
	STR_COLUMN_ISSUE_BY			(Type.TEXT, "@column_issued_by"),
	STR_COLUMN_EXPIRES_ON		(Type.TEXT, "@column_expires_on"),
	STR_COLUMN_ALIAS			(Type.TEXT, "@column_alias"),
	STR_COLUMN_NAME				(Type.TEXT, "@column_name"),
	STR_COLUMN_PACKAGE			(Type.TEXT, "@column_package"),
	STR_COLUMN_THIS_VERSION		(Type.TEXT, "@column_this_version"),
	STR_COLUMN_NEW_VERSION		(Type.TEXT, "@column_new_version"),
	STR_COLUMN_LAST_CHECKED_DATE(Type.TEXT, "@column_last_checked_date"),
	
	STR_EASY_GUI_FEATURE1		(Type.TEXT, "@easy_gui_feature1"),
	STR_EASY_GUI_FEATURE2		(Type.TEXT, "@easy_gui_feature2"),
	STR_EASY_GUI_FEATURE3		(Type.TEXT, "@easy_gui_feature3"),
	STR_EASY_GUI_UES_QUESTION		(Type.TEXT, "@easy_gui_use_question"),
	
	
	IMG_TOOLBAR_OPEN			(Type.IMAGE, "toolbar_open.png"),
	IMG_TOOLBAR_MANIFEST		(Type.IMAGE, "toolbar_manifast.png"),
	IMG_TOOLBAR_EXPLORER		(Type.IMAGE, "toolbar_explorer.png"),
	IMG_TOOLBAR_INSTALL			(Type.IMAGE, "toolbar_install.png"),
	IMG_TOOLBAR_ABOUT			(Type.IMAGE, "toolbar_about.png"),
	IMG_TOOLBAR_SETTING			(Type.IMAGE, "toolbar_setting.png"),
	IMG_TOOLBAR_OPENCODE		(Type.IMAGE, "toolbar_opencode.png"),
	IMG_TOOLBAR_SEARCH			(Type.IMAGE, "toolbar_search.png"),
	IMG_TOOLBAR_SIGNNING		(Type.IMAGE, "toolbar_signning.png"),
	IMG_TOOLBAR_LOADING_OPEN_JD (Type.IMAGE, "Loading_openJD_16_16.gif"),
	IMG_TOOLBAR_PACKAGETREE		(Type.IMAGE, "toolbar_packagetree.png"),
	IMG_TOOLBAR_LAUNCH			(Type.IMAGE, "toolbar_launch.png"),
	IMG_TOOLBAR_UNINSTALL		(Type.IMAGE, "toolbar_uninstall.png"),
	IMG_TOOLBAR_CLEAR			(Type.IMAGE, "toolbar_clear.png"),

	IMG_RESOURCE_IMG_BACKGROUND (Type.IMAGE, "resource_tap_image_background.jpg"),
	IMG_RESOURCE_IMG_BACKGROUND_DARK (Type.IMAGE, "resource_tap_image_background_dark.jpg"),
	IMG_RESOURCE_TREE_XML		(Type.IMAGE, "resource_tab_tree_xml.gif"),
	IMG_RESOURCE_TREE_CODE		(Type.IMAGE, "resource_tab_tree_code.png"),
	IMG_RESOURCE_TREE_ARSC		(Type.IMAGE, "resource_tab_tree_arsc.png"),
	IMG_RESOURCE_TREE_OPEN_JD	(Type.IMAGE, "Loading_openJD_16_16.gif"),
	IMG_RESOURCE_TREE_JD_ICON	(Type.IMAGE, "resource_tab_JD.png"),
	IMG_RESOURCE_TREE_JADX_ICON	(Type.IMAGE, "javaicon.png"),
	IMG_RESOURCE_TREE_BCV_ICON	(Type.IMAGE, "BCVIcon.png"),
	IMG_RESOURCE_TREE_OPEN_ICON (Type.IMAGE, "resource_tab_open.png"),
	IMG_RESOURCE_TREE_OPEN_OTHERAPPLICATION_ICON (Type.IMAGE, "resource_tab_otherapplication.png"),
	IMG_RESOURCE_TREE_OPEN_JD_LOADING (Type.IMAGE, "Loading_openJD_80_80.gif"),
	IMG_RESOURCE_TREE_TOOLBAR_REFRESH (Type.IMAGE, "resource_tab_tree_toolbar_refresh.png"),

	IMG_RESOURCE_TEXTVIEWER_TOOLBAR_OPEN (Type.IMAGE, "ResourceTab_TextViewer_toolbar_open.png"),
	IMG_RESOURCE_TEXTVIEWER_TOOLBAR_SAVE (Type.IMAGE, "ResourceTab_TextViewer_toolbar_save.png"),
	IMG_RESOURCE_TEXTVIEWER_TOOLBAR_FIND (Type.IMAGE, "ResourceTab_TextViewer_toolbar_find.png"),
	IMG_RESOURCE_TEXTVIEWER_TOOLBAR_NEXT (Type.IMAGE, "ResourceTab_TextViewer_toolbar_next.png"),
	IMG_RESOURCE_TEXTVIEWER_TOOLBAR_PREV (Type.IMAGE, "ResourceTab_TextViewer_toolbar_previous.png"),
	IMG_RESOURCE_TEXTVIEWER_TOOLBAR_INDENT(Type.IMAGE, "ResourceTab_TextViewer_toolbar_text_indent.png"),

	IMG_PERM_GROUP_PHONE_CALLS	(Type.IMAGE, "perm_group_phone_calls.png"),
	IMG_TOOLBAR_OPEN_ARROW		(Type.IMAGE, "down_on.png"),

	IMG_APP_ICON				(Type.IMAGE, "AppIcon.png"),
	IMG_APK_FILE_ICON			(Type.IMAGE, "apk_file_icon.png"),
	IMG_QUESTION				(Type.IMAGE, "question.png"),
	IMG_WARNING					(Type.IMAGE, "warning.png"),
	IMG_WARNING2				(Type.IMAGE, "warning2.png"),
	IMG_INSTALL_WAIT			(Type.IMAGE, "install_wait.gif"),
	IMG_LOADING					(Type.IMAGE, "loading.gif"),
	IMG_APK_LOGO				(Type.IMAGE, "Logo.png"),
	IMG_WAIT_BAR				(Type.IMAGE, "wait_bar.gif"),
	IMG_USB_ICON				(Type.IMAGE, "ic_dialog_usb.png"),
	IMG_DEF_APP_ICON			(Type.IMAGE, "sym_def_app_icon.png"),
	IMG_QMG_IMAGE_ICON			(Type.IMAGE, "qmg_not_suporrted.png"),
	IMG_RESULT_SUCCESS			(Type.IMAGE, "result_success.png"),
	IMG_RESULT_FAIL				(Type.IMAGE, "result_fail.png"),

	IMG_TREE_MENU_LINK			(Type.IMAGE, "tree_link_menu.png"),
	IMG_TREE_MENU_CLEARDATA		(Type.IMAGE, "tree_icon_clear.png"),
	IMG_TREE_MENU_DELETE		(Type.IMAGE, "tree_menu_delete.png"),
	IMG_TREE_MENU_SAVE			(Type.IMAGE, "tree_menu_save.png"),
	IMG_TREE_MENU_OPEN			(Type.IMAGE, "tree_open_menu.png"),

	IMG_TREE_APK				(Type.IMAGE, "tree_icon_apk.png"),
	IMG_TREE_DEVICE				(Type.IMAGE, "tree_icon_device.png"),
	IMG_TREE_TOP				(Type.IMAGE, "tree_icon_top.gif"),
	IMG_TREE_FOLDER				(Type.IMAGE, "tree_icon_folder.png"),
	IMG_TREE_LOADING			(Type.IMAGE, "tree_loading.gif"),
	IMG_TREE_FAVOR				(Type.IMAGE, "tree_favor.png"),

	IMG_TREE_GLOBAL_SETTING		(Type.IMAGE, "configure-2.png"),
	IMG_TREE_NETWORK_SETTING	(Type.IMAGE, "internet-connection_manager.png"),
	IMG_TREE_CONFIG_SETTING		(Type.IMAGE, "kservices.png"),

	IMG_INSTALL_CHECK			(Type.IMAGE, "install_dlg_check.png"),
	IMG_INSTALL_BLOCK			(Type.IMAGE, "install_dlg_block.png"),
	IMG_INSTALL_LOADING			(Type.IMAGE, "install_dlg_loading.gif"),

	IMG_ADD_TO_DESKTOP			(Type.IMAGE, "add-to-desktop.png"),
	IMG_ASSOCIATE_APK			(Type.IMAGE, "associate.png"),
	IMG_UNASSOCIATE_APK			(Type.IMAGE, "unassociate.png"),

	//easy gui
	IMG_EASY_WINDOW_EXIT			(Type.IMAGE, "easy_gui_exit.png"),
	IMG_EASY_WINDOW_MINI			(Type.IMAGE, "easy_gui_mini.png"),
	IMG_EASY_WINDOW_SETTING			(Type.IMAGE, "perm_group_system_tools.png"),
	IMG_EASY_WINDOW_CLIPBOARD_ICON			(Type.IMAGE, "perm_group_user_dictionary.png"),
	
	IMG_EASY_WINDOW_ALLOW			(Type.IMAGE, "easy_gui_allow.png"),

	//http://chittagongit.com/icon/list-icon-png-10.html
	IMG_EASY_WINDOW_PERMISSION_ICON			(Type.IMAGE, "easy_gui_permission_icon.png"),
	
	//https://icon-icons.com/zh/%E5%9B%BE%E6%A0%87/%E7%94%B5%E8%AF%9D-iphone/89813#48
	IMG_EASY_WINDOW_DEVICE			(Type.IMAGE, "easy_gui_device.png"),
	IMG_EASY_WINDOW_DRAGANDDROP			(Type.IMAGE, "easy_gui_draganddrop.png"),

	//https://www.iconfinder.com/icons/686665/arrows_enlarge_expand_extend_increase_outward_resize_spread_icon
	IMG_EASY_WINDOW_SPREAD			(Type.IMAGE, "easy_spread.png"),
	
	IMG_PREVIEW_EASY			(Type.IMAGE, "preview_easymode.png"),
	IMG_PREVIEW_EASY1			(Type.IMAGE, "preview_easymode1.png"),
	IMG_PREVIEW_EASY2			(Type.IMAGE, "preview_easymode2.png"),
	
	IMG_PREVIEW_ORIGINAL			(Type.IMAGE, "preview_original.png"),
	
	IMG_PERM_MARKER_SETTING		(Type.IMAGE, "perm_marker_setting.png"),
	IMG_PERM_MARKER_CLOSE		(Type.IMAGE, "perm_marker_close.png"),

	BIN_PATH					(Type.BIN, ""),

	BIN_ADB_LNX					(Type.BIN, "adb", "nux"),
	BIN_ADB_WIN					(Type.BIN, "adb.exe", "win"),
	BIN_ADB						(Type.BIN, new Resource[]{ BIN_ADB_WIN, BIN_ADB_LNX }),

	BIN_AAPT_LNX				(Type.BIN, "aapt", "nux"),
	BIN_AAPT_WIN				(Type.BIN, "aapt.exe", "win"),
	BIN_AAPT					(Type.BIN, new Resource[]{ BIN_AAPT_WIN, BIN_AAPT_LNX }),

	BIN_JDGUI					(Type.BIN, "jd-gui-1.4.0.jar"),

	BIN_DEX2JAR_LNX				(Type.BIN, "d2j-dex2jar.sh", "nux"),
	BIN_DEX2JAR_WIN				(Type.BIN, "d2j-dex2jar.bat", "win"),
	BIN_DEX2JAR					(Type.BIN, new Resource[]{ BIN_DEX2JAR_WIN, BIN_DEX2JAR_LNX }),

	BIN_JADX_LNX				(Type.BIN, "jadx/bin/jadx-gui", "nux"),
	BIN_JADX_WIN				(Type.BIN, "jadx\\bin\\jadx-gui.bat", "win"),
	BIN_JADX_GUI				(Type.BIN, new Resource[]{ BIN_JADX_WIN, BIN_JADX_LNX }),

	BIN_BYTECODE_VIEWER			(Type.BIN, "Bytecode-Viewer.jar"),

	BIN_SIGNAPK					(Type.BIN, "signapk.jar"),

	PLUGIN_PATH					(Type.PLUGIN, ""),
	PLUGIN_CONF_PATH			(Type.PLUGIN, "plugins.conf"),

	SSL_TRUSTSTORE_PATH			(Type.SECURITY, "trustStore.jks"),

	PROP_USE_EASY_UI			(Type.PROP, "use_easy_ui", false),
	PROP_SKIP_STARTUP_EASY_UI_DLG			(Type.PROP, "skip_startup_easy_ui_dlg", false),
	PROP_LANGUAGE				(Type.PROP, "language", SystemUtil.getUserLanguage()),
	PROP_EDITOR					(Type.PROP, "editor", null /* see getDefValue() */),
	PROP_RECENT_EDITOR			(Type.PROP, "recent_editor", ""),
	PROP_ADB_PATH				(Type.PROP, "adb_path", ""),
	PROP_ADB_POLICY_SHARED		(Type.PROP, "adb_policy_shared", true),
	PROP_ADB_DEVICE_MONITORING	(Type.PROP, "adb_device_monitoring", true),
	PROP_LAUNCH_ACTIVITY_OPTION	(Type.PROP, "adb_launch_activity_option", Resource.INT_LAUNCH_LAUNCHER_OR_MAIN_ACTIVITY),
	PROP_TRY_UNLOCK_AF_LAUNCH	(Type.PROP, "adb_try_unlock_af_launch", true),
	PROP_LAUNCH_AF_INSTALLED	(Type.PROP, "adb_launch_af_installed", true),
	PROP_RECENT_ADB_INFO		(Type.PROP, "recent_adb_info", ""),
	PROP_FRAMEWORK_RES			(Type.PROP, "framewokr-res", ""),
	PROP_LAST_FILE_OPEN_PATH	(Type.PROP, "last_file_open_path", ""),
	PROP_LAST_FILE_SAVE_PATH	(Type.PROP, "last_file_save_path", ""),
	PROP_SOVE_LEAD_TIME			(Type.PROP, "solve_lead_time"),
	PROP_CURRENT_THEME			(Type.PROP, "Current_theme", UIManager.getSystemLookAndFeelClassName()),
	PROP_TABBED_UI_THEME		(Type.PROP, "tabbed_pane_ui", null /* see getDefValue() */),
	PROP_SAVE_WINDOW_SIZE		(Type.PROP, "save_window_size", false),
	PROP_BASE_FONT				(Type.PROP, "base_font", ""),
	PROP_BASE_FONT_SIZE			(Type.PROP, "base_font_size", 12),
	PROP_BASE_FONT_STYLE		(Type.PROP, "base_font_style", Font.PLAIN),
	PROP_PREFERRED_LANGUAGE		(Type.PROP, "preferred_language", null /* see getDefValue() */),
	PROP_PEM_FILE_PATH			(Type.PROP, "last_pem_file_path", null /* see getDefValue() */),
	PROP_PK8_FILE_PATH			(Type.PROP, "last_pk8_file_path", null /* see getDefValue() */),
	PROP_PRINT_MULTILINE_ATTR	(Type.PROP, "print_multiline_attr", true),

	PROP_EASY_GUI_TOOLBAR				(Type.PROP, "easy_gui_show_button", "1,2,3,4,5"),
	PROP_EASY_GUI_WINDOW_POSITION				(Type.PROP, "easy_gui_window_position", null),
	
	PROP_EASY_GUI_WINDOW_POSITION_X				(Type.PROP, "easy_gui_window_position_x", null),
	PROP_EASY_GUI_WINDOW_POSITION_Y				(Type.PROP, "easy_gui_window_position_y", null),

	PROP_DEFAULT_DECORDER		(Type.PROP, "default_decorder", Resource.STR_DECORDER_JD_GUI),
	PROP_DEFAULT_SEARCHER		(Type.PROP, "default_searcher", Resource.STR_DEFAULT_SEARCHER),
	PROP_DEFAULT_EXPLORER		(Type.PROP, "default_explorer", Resource.STR_EXPLORER_ARCHIVE),
	PROP_DEFAULT_LAUNCH_MODE	(Type.PROP, "default_launch_mode", Resource.STR_LAUNCH_LAUNCHER),
	PROP_VISIBLE_TO_BASIC		(Type.PROP, "visible_to_basic", true),
	PROP_ALWAYS_TOOLBAR_EXTENDED(Type.PROP, "always_toolbar_extended", false),

	PROP_PERM_MARK_RUNTIME		(Type.PROP, "mark-runtime", true),
	PROP_PERM_MARK_COUNT		(Type.PROP, "mark-count", true),
	PROP_PERM_TREAT_SIGN_AS_REVOKED(Type.PROP, "treat-sign-as-revoked", true),

	LIB_JSON_JAR				(Type.LIB, "json-simple-1.1.1.jar"),
	LIB_CLI_JAR					(Type.LIB, "commons-cli-1.3.1.jar"),
	LIB_APKTOOL_JAR				(Type.LIB, "apktool.jar"),
	LIB_ALL						(Type.LIB, "*"),

	RAW_ABUOT_HTML				(Type.RAW, "/values/AboutLayout.html"),
	RAW_BASIC_INFO_LAYOUT_HTML	(Type.RAW, "/values/BasicInfoLayout.html"),
	RAW_PACKAGE_INFO_LAYOUT_HTML(Type.RAW, "/values/PackageInfoLayout.html"),

	ETC_SETTINGS_FILE			(Type.ETC, "settings.txt");

	public static final int INT_WINDOW_SIZE_WIDTH_MIN = 650;
	public static final int INT_WINDOW_SIZE_HEIGHT_MIN = 490;

	public static final int INT_LAUNCH_LAUNCHER_OR_MAIN_ACTIVITY = 0;
	public static final int INT_LAUNCH_ONLY_LAUNCHER_ACTIVITY = 1;
	public static final int INT_LAUNCH_ALWAYS_CONFIRM_ACTIVITY = 2;

	public static final String STR_DECORDER_JD_GUI = "jd-gui";
	public static final String STR_DECORDER_JADX_GUI = "jadx-gui";
	public static final String STR_DECORDER_BYTECOD = "Bytecode-viewer";
	public static final String STR_DEFAULT_SEARCHER = "resource";
	public static final String STR_EXPLORER_ARCHIVE = "archive";
	public static final String STR_EXPLORER_FOLDER = "folder";
	public static final String STR_LAUNCH_LAUNCHER = "launcher";
	public static final String STR_LAUNCH_SELECT = "select";

	private enum Type {
		IMAGE,
		TEXT,
		BIN,
		LIB,
		PLUGIN,
		SECURITY,
		PROP,
		RAW,
		ETC
	}

	private String value;
	private Type type;
	private Object extValue;

	private static JSONObject property = null;
	private static String lang = null;
	private static XmlPath[] stringXmlPath = null;

	public static void setLanguage(String l) { if(lang != l) makeStringXmlPath(l); lang = l; }
	public static String getLanguage() { return lang; }

	private static void makeStringXmlPath(String lang)
	{
		ArrayList<XmlPath> xmlList = new ArrayList<XmlPath>();

		String value_path = getUTF8Path() + File.separator + "data" + File.separator;
		if(lang != null && !lang.isEmpty()) {
			String ext_lang_value_path = value_path + "strings-" + lang + ".xml";
			File extFile = new File(ext_lang_value_path);
			if(extFile.exists()) {
				xmlList.add(new XmlPath(extFile));
			}

			try(InputStream xml = Resource.class.getResourceAsStream("/values/strings-" + lang + ".xml")) {
				if(xml != null) xmlList.add(new XmlPath(xml));
			} catch(IOException e) { }
		}

		String ext_lang_value_path = value_path + "strings.xml";
		File extFile = new File(ext_lang_value_path);
		if(extFile.exists()) {
			xmlList.add(new XmlPath(extFile));
		}

		InputStream xml = Resource.class.getResourceAsStream("/values/strings.xml");
		if(xml != null) {
			xmlList.add(new XmlPath(xml));
		}

		stringXmlPath = xmlList.toArray(new XmlPath[0]);
	}

	public static String[] getSupportedLanguages() {
		ArrayList<String> languages = new ArrayList<String>();

		String value_path = getUTF8Path() + File.separator + "data" + File.separator;
		File valueDir = new File(value_path);
		if(valueDir != null && valueDir.isDirectory()) {
			for(String name: valueDir.list()) {
				if(name.startsWith("strings-") && name.endsWith(".xml")) {
					name = name.substring(8,name.length()-4);
					if(!languages.contains(name)) {
						languages.add(name);
					}
				}
			}
		}

		URL resource = Resource.class.getResource("/values");
		String resFilePath = resource.getFile();
		try {
			resFilePath = URLDecoder.decode(resource.getFile(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		if("file".equals(resource.getProtocol())) {
			valueDir = new File(resFilePath);
			if(valueDir != null && valueDir.isDirectory()) {
				for(String name: valueDir.list()) {
					if(name.startsWith("strings-") && name.endsWith(".xml")) {
						name = name.substring(8,name.length()-4);
						if(!languages.contains(name)) {
							languages.add(name);
						}
					}
				}
			}
		} else if("jar".equals(resource.getProtocol())) {
			String[] jarPath = resFilePath.split("!");
			if(jarPath != null && jarPath.length == 2) {
				String[] list = ZipFileUtil.findFiles(jarPath[0].substring(5), ".xml", "^"+jarPath[1].substring(1) + "/.*");
				for(String name : list) {
					if(name.startsWith("values/strings-") && name.endsWith(".xml")) {
						name = name.substring(15,name.length()-4);
						if(!languages.contains(name)) {
							languages.add(name);
						}
					}
				}

			}
		} else {
			Log.e("Unknown protocol " + resource);
		}

		Collections.sort(languages);
		languages.add(0, "");

		return languages.toArray(new String[languages.size()]);
	}

	private Resource(Type type, String value)
	{
		this(type, value, null);
	}

	private Resource(Type type, String value, Object extValue)
	{
		this.type = type;
		this.value = value;
		this.extValue = extValue;
	}

	private Resource(Type type, Resource[] cfgResources)
	{
		if(cfgResources == null | cfgResources.length == 0) {
			throw new IllegalArgumentException();
		}

		this.type = type;
		for(Resource r: cfgResources) {
			if(SystemUtil.OS.indexOf((String)r.extValue) > -1) {
				this.value = r.value;
				this.extValue = r.extValue;
				break;
			}
		}
		if(this.value == null || extValue == null) {
			throw new IllegalArgumentException();
		}
	}

	public String getValue()
	{
		return value;
	}

	public String getPath()
	{
		String subPath;
		switch(type){
		case IMAGE:
			return getClass().getResource("/icons/" + value).toString();
		case RAW:
			return getClass().getResource(value).toString();
		case BIN:
			subPath = File.separator + "tool";
			break;
		case LIB:
			subPath = File.separator + "lib";
			break;
		case PLUGIN:
			subPath = File.separator + "plugin";
			break;
		case SECURITY:
			subPath = File.separator + "security";
			break;
		case ETC:
			subPath = "";
			break;
		default:
			return null;
		}
		return getUTF8Path() + subPath + File.separator + value;
	}

	public URL getURL()
	{
		switch(type){
		case IMAGE:
			return getClass().getResource("/icons/" + value);
		case RAW:
			return getClass().getResource(value);
		default:
			return null;
		}
	}

	public ImageIcon getImageIcon()
	{
		if(type != Type.IMAGE) return null;
		return new ImageIcon(getURL());
	}

	public ImageIcon getImageIcon(int width, int height)
	{
		if(type != Type.IMAGE) return null;
		return new ImageIcon(ImageScaler.getScaledImage(new ImageIcon(getURL()),width,height));
	}

	public String getString()
	{
		switch(type){
		case TEXT:
			String id = getValue();
			String value = null;
	
			if(!id.startsWith("@")) return id;
			id = id.substring(1);
	
			if(stringXmlPath == null) {
				makeStringXmlPath(lang);
			}
	
			for(XmlPath xPath: stringXmlPath) {
				XmlPath node = xPath.getNode("/resources/string[@name='" + id + "']");
				if(node != null) {
					value = node.getTextContent();
					if(value != null) break;
				}
			}
	
			return value != null ? value.replaceAll("\\\\n", "\n").replaceAll("\\\\t", "\t") : null;
		case RAW:
			try (InputStream is= getURL().openStream();
				 InputStreamReader ir = new InputStreamReader(is);
				 BufferedReader br = new BufferedReader(ir)) {
		        StringBuilder out = new StringBuilder();
		        String line;
		        while ((line = br.readLine()) != null) {
		            out.append(line);
		        }
		        return out.toString();
			} catch (IOException e) {
				e.printStackTrace();
			}
		default:
			return null;
		}
	}

	private static void loadProperty()
	{
		if(property == null) {
			File file = new File(ETC_SETTINGS_FILE.getPath());
			if(!file.exists() || file.length() == 0) return;
			try (FileReader fileReader = new FileReader(file)) {
				JSONParser parser = new JSONParser();
				property = (JSONObject)parser.parse(fileReader);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static void saveProperty()
	{
		if(property == null)
			return;

		String transMultiLine = property.toJSONString()
				.replaceAll("^\\{(.*)\\}$", "{\n$1\n}")
				.replaceAll("(\"[^\"]*\":(\"[^\"]*\")?([^\",]*)?,)", "$1\n");
		//.replaceAll("(\"[^\"]*\":(\"[^\"]*\")?([^\",\\[]*(\\[[^\\]]\\])?)?,)", "$1\n");

		try( FileWriter fw = new FileWriter(Resource.ETC_SETTINGS_FILE.getPath());
			 BufferedWriter writer = new BufferedWriter(fw) ) {
			writer.write(transMultiLine);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Object getDefValue() {
		Object obj = null;
		if(extValue != null) {
			return extValue;
		}
		switch(this) {
		case PROP_EDITOR:
			try {
				obj = SystemUtil.getDefaultEditor();
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case PROP_PREFERRED_LANGUAGE:
			String propPreferredLanguage = SystemUtil.getUserLanguage();
			String propStrLanguage = (String) PROP_LANGUAGE.getData();
			if(!propPreferredLanguage.equals(propStrLanguage) && !"en".equals(propPreferredLanguage)) {
				propPreferredLanguage += ";" + (propStrLanguage.isEmpty() ? "en" : propStrLanguage);
			}
			obj = propPreferredLanguage + ";";
			break;
		case PROP_PEM_FILE_PATH:
		case PROP_PK8_FILE_PATH:
			String defPath = getUTF8Path() + File.separator +
							"data" + File.separator +
							"build-master-target-product-security" + File.separator +
							(this == PROP_PEM_FILE_PATH ? "platform.x509.pem" : "platform.pk8");
			if(new File(defPath).isFile()) {
				obj = defPath;
			}
			break;
		case PROP_TABBED_UI_THEME:
			return TabbedPaneUIManager.DEFAULT_TABBED_UI;
		default:
			break;
		};
		extValue = obj;
		return obj;
	}

	public Object getData()
	{
		if(type != Type.PROP) return null;

		Object obj = null;

		loadProperty();
		if(property != null) {
			obj = property.get(getValue());
		}

		if(obj == null) {
			obj = getDefValue();
		}

		return obj;
	}

	public Object getData(Object ref)
	{
		if(type != Type.PROP) return null;

		Object result = getData();
		if(result == null) return ref;

		return result;
	}

	public int getInt()
	{
		if(type != Type.PROP) return -1;

		Object data = getData();
		if(data == null) return 0;

		int ret = 0;
		if(data instanceof Long) {
			ret = (int)(long)data;
		} else if(data instanceof Integer) {
			ret = (int)data;
		}

		return ret;
	}

	public int getInt(int ref)
	{
		if(type != Type.PROP) return ref;

		Object data = getData(ref);
		if(data == null) return ref;

		int ret = ref;
		if(data instanceof Long) {
			ret = (int)(long)data;
		} else if(data instanceof Integer) {
			ret = (int)data;
		}

		return ret;
	}

	public static Object getPropData(String key)
	{
		loadProperty();
		if(property == null)
			return null;

		return property.get(key);
	}

	public static Object getPropData(String key, Object ref)
	{
		Object data = getPropData(key);
		return data!=null?data:ref;
	}

	@SuppressWarnings("unchecked")
	public void setData(Object value)
	{
		if(type != Type.PROP) return;

		loadProperty();
		if(property == null) {
			property = new JSONObject();
		}

		if(!value.equals(property.get(getValue()))) {
			property.put(getValue(), value);
			saveProperty();
		}
	}

	@SuppressWarnings("unchecked")
	public static void setPropData(String key, Object data)
	{
		loadProperty();
		if(property == null) {
			property = new JSONObject();
		}
		property.put(key, data);
		saveProperty();
	}

	public static String getUTF8Path()
	{
		String resourcePath = Resource.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		resourcePath = (new File(resourcePath)).getParentFile().getPath();

		try {
			resourcePath = URLDecoder.decode(resourcePath, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return resourcePath;
	}
}
