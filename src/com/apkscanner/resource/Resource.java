package com.apkscanner.resource;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;

import javax.swing.ImageIcon;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.apkscanner.gui.util.ImageScaler;
import com.apkscanner.util.MyXPath;

public enum Resource
{
	STR_APP_NAME				(Type.TEXT, "@app_name"),
	STR_APP_VERSION				(Type.TEXT, "2.2 beta 20160615"),
	STR_APP_BUILD_MODE			(Type.TEXT, "eng"),
	STR_APP_MAKER				(Type.TEXT, "jin_h.lee / sunggyu.kam"),
	STR_APP_MAKER_EMAIL			(Type.TEXT, "jin_h.lee@samsung.com;sunggyu.kam@samsung.com"),

	STR_SAMSUNG_KEY_SERIAL		(Type.TEXT, "d20995a79c0daad6"),
	STR_SS_TEST_KEY_SERIAL		(Type.TEXT, "b3998086d056cffa"),
	
	STR_SDK_INFO_FILE_PATH		(Type.TEXT, "/values/sdk-info.xml"),

	STR_BTN_OPEN				(Type.TEXT, "@btn_open"),
	STR_BTN_MANIFEST			(Type.TEXT, "@btn_manifest"),
	STR_BTN_EXPLORER			(Type.TEXT, "@btn_explorer"),
	STR_BTN_UNPACK				(Type.TEXT, "@btn_unpack"),
	STR_BTN_PACK				(Type.TEXT, "@btn_pack"),
	STR_BTN_INSTALL				(Type.TEXT, "@btn_install"),
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
	STR_BTN_EXPORT				(Type.TEXT, "@btn_export"),
	STR_BTN_OPENCODE			(Type.TEXT, "@btn_opencode"),
	
	STR_BTN_OPEN_LAB			(Type.TEXT, "@btn_open_lab"),
	STR_BTN_MANIFEST_LAB		(Type.TEXT, "@btn_manifest_lab"),
	STR_BTN_EXPLORER_LAB		(Type.TEXT, "@btn_explorer_lab"),
	STR_BTN_UNPACK_LAB			(Type.TEXT, "@btn_unpack_lab"),
	STR_BTN_PACK_LAB			(Type.TEXT, "@btn_pack_lab"),
	STR_BTN_INSTALL_LAB			(Type.TEXT, "@btn_install_lab"),
	STR_BTN_SETTING_LAB			(Type.TEXT, "@btn_setting_lab"),
	STR_BTN_ABOUT_LAB			(Type.TEXT, "@btn_about_lab"),
	STR_BTN_OPENCODE_LAB			(Type.TEXT, "@btn_opencode_lab"),
	
	STR_MENU_NEW				(Type.TEXT, "@menu_new"),
	STR_MENU_NEW_WINDOW			(Type.TEXT, "@menu_new_window"),
	STR_MENU_NEW_APK_FILE		(Type.TEXT, "@menu_new_apk_file"),
	STR_MENU_NEW_PACKAGE		(Type.TEXT, "@menu_new_package"),
	STR_MENU_APK_FILE			(Type.TEXT, "@menu_apk_file"),
	STR_MENU_PACKAGE			(Type.TEXT, "@menu_package"),
	STR_MENU_INSTALL			(Type.TEXT, "@menu_install"),
	STR_MENU_CHECK_INSTALLED	(Type.TEXT, "@menu_check_installed"),

	STR_TAB_BASIC_INFO			(Type.TEXT, "@tab_basic_info"),
	STR_TAB_WIDGET				(Type.TEXT, "@tab_widget"),
	STR_TAB_LIB					(Type.TEXT, "@tab_lib"),
	STR_TAB_IMAGE				(Type.TEXT, "@tab_image"),
	STR_TAB_ACTIVITY			(Type.TEXT, "@tab_activity"),
	STR_TAB_CERT				(Type.TEXT, "@tab_cert"),
	
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
	STR_SETTINGS_RES			(Type.TEXT, "@settings_res"),
	STR_SETTINGS_CHECK_INSTALLED(Type.TEXT, "@settings_check_installed"),
	STR_SETTINGS_LANGUAGE		(Type.TEXT, "@settings_language"),

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
	STR_LABEL_OPEN_WITH_EXPLORER(Type.TEXT, "@label_open_with_explorer"),
	STR_LABEL_OPEN_WITH_SCANNER	(Type.TEXT, "@label_open_with_apkscanner"),
	STR_LABEL_OPEN_WITH_CHOOSE	(Type.TEXT, "@label_open_with_choose"),
	
	STR_TREE_MESSAGE_DEVICE 	(Type.TEXT, "@tree_message_device"),
	STR_TREE_MESSAGE_VERSION 	(Type.TEXT, "@tree_message_version"),
	STR_TREE_MESSAGE_ROOT 		(Type.TEXT, "@tree_message_root"),
	STR_TREE_MESSAGE_REBOOT 	(Type.TEXT, "@tree_message_reboot"),
	
	
	STR_MSG_FAILURE_OPEN_APK	(Type.TEXT, "@msg_failure_open_apk"),
	STR_MSG_DEVICE_NOT_FOUND	(Type.TEXT, "@msg_device_not_found"),
	STR_MSG_ALREADY_INSTALLED	(Type.TEXT, "@msg_already_installed"),
	STR_MSG_NO_SUCH_PACKAGE		(Type.TEXT, "@msg_no_such_package"),
	STR_MSG_FAILURE_INSTALLED	(Type.TEXT, "@msg_failure_installed"),
	STR_MSG_SUCCESS_INSTALLED	(Type.TEXT, "@msg_success_installed"),
	STR_MSG_DEVICE_UNAUTHORIZED	(Type.TEXT, "@msg_device_unauthorized"),
	STR_MSG_DEVICE_UNKNOWN		(Type.TEXT, "@msg_device_unknown"),
	STR_MSG_DEVICE_HAS_NOT_ROOT	(Type.TEXT, "@msg_cannot_run_root"),
	STR_MSG_UNSUPPORTED_PREVIEW	(Type.TEXT, "@msg_unsupported_preview"),
	
	STR_QUESTION_REBOOT_DEVICE	(Type.TEXT, "@question_reboot_device"),
	STR_QUESTION_CONTINUE_INSTALL(Type.TEXT, "@question_continue_install"),
	STR_QUESTION_OPEN_OR_INSTALL(Type.TEXT, "@question_open_or_install"),
	STR_QUESTION_PUSH_OR_INSTALL(Type.TEXT, "@question_push_or_install"),

	IMG_TOOLBAR_OPEN			(Type.IMAGE, "toolbar_open.png"),
	IMG_TOOLBAR_MANIFEST		(Type.IMAGE, "toolbar_manifast.png"),
	IMG_TOOLBAR_EXPLORER		(Type.IMAGE, "toolbar_explorer.png"),
	IMG_TOOLBAR_INSTALL			(Type.IMAGE, "toolbar_install.png"),
	IMG_TOOLBAR_ABOUT			(Type.IMAGE, "toolbar_about.png"),
	IMG_TOOLBAR_SETTING			(Type.IMAGE, "toolbar_setting.png"),
	IMG_TOOLBAR_OPENCODE		(Type.IMAGE, "toolbar_opencode.png"),
	IMG_TOOLBAR_SEARCH		(Type.IMAGE, "toolbar_search.png"),
	IMG_TOOLBAR_SIGNNING		(Type.IMAGE, "toolbar_signning.png"),
	IMG_TOOLBAR_LOADING_OPEN_JD (Type.IMAGE, "loading_openJD.gif"),
	IMG_TOOLBAR_PACKAGETREE		(Type.IMAGE, "toolbar_packagetree.png"),
	
	IMG_RESOURCE_IMG_BACKGROUND (Type.IMAGE, "resource_tap_image_background.jpg"),
	IMG_RESOURCE_IMG_BACKGROUND_DARK (Type.IMAGE, "resource_tap_image_background_dark.jpg"),
	IMG_RESOURCE_TREE_XML		(Type.IMAGE, "resource_tab_tree_xml.gif"),
	IMG_RESOURCE_TREE_CODE		(Type.IMAGE, "resource_tab_tree_code.png"),
	IMG_RESOURCE_TREE_ARSC		(Type.IMAGE, "resource_tab_tree_arsc.png"),
	IMG_RESOURCE_TREE_OPEN_JD (Type.IMAGE, "Loading_openJD_16_16.gif"),
	IMG_RESOURCE_TREE_JD_ICON (Type.IMAGE, "resource_tab_JD.png"),	
	IMG_RESOURCE_TREE_OPEN_ICON (Type.IMAGE, "resource_tab_open.png"),
	IMG_RESOURCE_TREE_OPEN_OTHERAPPLICATION_ICON (Type.IMAGE, "resource_tab_otherapplication.png"),
	IMG_RESOURCE_TREE_OPEN_JD_LOADING (Type.IMAGE, "Loading_openJD_80_80.gif"),	
	
	IMG_PERM_GROUP_PHONE_CALLS	(Type.IMAGE, "perm_group_phone_calls.png"),	
	IMG_TOOLBAR_OPEN_ARROW		(Type.IMAGE, "down_on.png"),	
	
	IMG_APP_ICON				(Type.IMAGE, "AppIcon.png"),
	IMG_APK_FILE_ICON			(Type.IMAGE, "apk_file_icon.png"),
	IMG_QUESTION				(Type.IMAGE, "question.png"),
	IMG_WARNING					(Type.IMAGE, "warning.png"),
	IMG_WARNING2				(Type.IMAGE, "warning2.png"),
	IMG_SUCCESS					(Type.IMAGE, "Succes.png"),
	IMG_INSTALL_WAIT			(Type.IMAGE, "install_wait.gif"),
	IMG_LOADING					(Type.IMAGE, "loading.gif"),
	IMG_APK_LOGO				(Type.IMAGE, "Logo.png"),
	IMG_WAIT_BAR				(Type.IMAGE, "wait_bar.gif"),
	IMG_USB_ICON				(Type.IMAGE, "ic_dialog_usb.png"),
	IMG_DEF_APP_ICON			(Type.IMAGE, "sym_def_app_icon.png"),
	IMG_QMG_IMAGE_ICON			(Type.IMAGE, "qmg_not_suporrted.png"),

	IMG_TREE_MENU_LINK			(Type.IMAGE, "tree_link_menu.png"),
	IMG_TREE_MENU_DELETE		(Type.IMAGE, "tree_menu_delete.png"),
	IMG_TREE_MENU_SAVE			(Type.IMAGE, "tree_menu_save.png"),
	IMG_TREE_MENU_OPEN			(Type.IMAGE, "tree_open_menu.png"),
	
	IMG_TREE_APK				(Type.IMAGE, "tree_icon_apk.png"),
	IMG_TREE_DEVICE				(Type.IMAGE, "tree_icon_device.png"),
	IMG_TREE_TOP				(Type.IMAGE, "tree_icon_top.gif"),
	IMG_TREE_FOLDER				(Type.IMAGE, "tree_icon_folder.png"),
	
	IMG_INSTALL_TABLE_DONE		(Type.IMAGE, "done_icon.png"),
	IMG_INSTALL_TABLE_WAIT		(Type.IMAGE, "install_wait_icon.GIF"),
	IMG_INSTALL_TABLE_QUESTION	(Type.IMAGE, "question_icon.png"),
	IMG_INSTALL_TABLE_ERROR		(Type.IMAGE, "install_error_icon.png"),
	
	
	BIN_ADB_LNX					(Type.BIN, "adb"),
	BIN_ADB_WIN					(Type.BIN, "adb.exe"),
	BIN_AAPT_LNX				(Type.BIN, "aapt"),
	BIN_AAPT_WIN				(Type.BIN, "aapt.exe"),

	BIN_JDGUI					(Type.BIN, "jd-gui-1.4.0.jar"),
	BIN_DEX2JAR_LNX				(Type.BIN, "d2j-dex2jar.sh"),
	BIN_DEX2JAR_WIN				(Type.BIN, "d2j-dex2jar.bat"),	
	
	PROP_EDITOR					(Type.PROP, "editor"),
	PROP_FRAMEWORK_RES			(Type.PROP, "framewokr-res"),
	PROP_CHECK_INSTALLED		(Type.PROP, "check-installed"),
	PROP_LANGUAGE				(Type.PROP, "language"),
	PROP_WITH_FRAMEWORK_RES		(Type.PROP, "with_framework_res"),
	PROP_LAST_FILE_OPEN_PATH	(Type.PROP, "last_file_open_path"),
	PROP_LAST_FILE_SAVE_PATH	(Type.PROP, "last_file_save_path"),
	PROP_SOVE_LEAD_TIME			(Type.PROP, "solve_lead_time"),
	
	LIB_JSON_JAR				(Type.LIB, "json-simple-1.1.1.jar"),
	LIB_CLI_JAR					(Type.LIB, "commons-cli-1.3.1.jar"),
	LIB_APKTOOL_JAR				(Type.LIB, "apktool.jar"),
	
	ETC_SETTINGS_FILE			(Type.ETC, "settings.txt");
	
	private enum Type {
		IMAGE,
		TEXT,
		BIN,
		LIB,
		PROP,
		ETC
	}

	private String value;
	private Type type;

	private static JSONObject property = null;
	private static String lang = null;
	private static MyXPath[] stringXmlPath = null;

	public static void setLanguage(String l) { if(lang != l) makeStringXmlPath(l); lang = l; }
	public static String getLanguage() { return lang; }
	
	private static void makeStringXmlPath(String lang)
	{
		ArrayList<MyXPath> xmlList = new ArrayList<MyXPath>();

		String value_path = getUTF8Path() + File.separator + "data" + File.separator;
		if(lang != null) {
			String ext_lang_value_path = value_path + "strings-" + lang + ".xml";
			if((new File(ext_lang_value_path)).exists()) {
				xmlList.add(new MyXPath(ext_lang_value_path));
			}

			InputStream xml = Resource.class.getResourceAsStream("/values/strings-" + lang + ".xml");
			if(xml != null) {
				xmlList.add(new MyXPath(xml));
			}
		}
		
		String ext_lang_value_path = value_path + "strings.xml";
		if((new File(ext_lang_value_path)).exists()) {
			xmlList.add(new MyXPath(ext_lang_value_path));
		}
		
		InputStream xml = Resource.class.getResourceAsStream("/values/strings.xml");
		if(xml != null) {
			xmlList.add(new MyXPath(xml));
		}

		stringXmlPath = xmlList.toArray(new MyXPath[0]);
	}

	private Resource(Type type, String value)
	{
		this.type = type;
		this.value = value;
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
		case BIN:
			subPath = File.separator + "tool";
			break;
		case LIB:
			subPath = File.separator + "lib";
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
		if(type != Type.IMAGE) return null;
		return getClass().getResource("/icons/" + value);
	}
	
	public ImageIcon getImageIcon()
	{
		if(type != Type.IMAGE) return null;
		return new ImageIcon(getURL());
	}
	
	public ImageIcon getImageIcon(int width, int height)
	{
		if(type != Type.IMAGE) return null;
		ImageIcon tempImg = new ImageIcon(ImageScaler.getScaledImage(new ImageIcon(getURL()),width,height));
		
		return tempImg;
	}
	
	public String getString()
	{
		if(type != Type.TEXT) return null;
		
		String id = getValue();
		String value = null;
		
		if(!id.startsWith("@")) return id;
		id = id.substring(1);

		if(stringXmlPath == null) {
			makeStringXmlPath(lang);
		}

		for(MyXPath xPath: stringXmlPath) {
			value = xPath.getNode("/resources/string[@name='" + id + "']").getTextContent();
			if(value != null) break;
		}

		return value;
	}

	static private void loadProperty()
	{
		if(property == null) {
			File file = new File(ETC_SETTINGS_FILE.getPath());
			if(!file.exists()) return;
			try {
				FileReader fileReader = new FileReader(file);
				JSONParser parser = new JSONParser();
				property = (JSONObject)parser.parse(fileReader);

				fileReader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	static private void saveProperty()
	{
		if(property == null)
			return;
		
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(ETC_SETTINGS_FILE.getPath()));
			writer.write(property.toJSONString());
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Object getData()
	{
		if(type != Type.PROP) return null;

		loadProperty();
		if(property == null)
			return null;
		
		return property.get(getValue());
	}
	
	public Object getData(Object ref)
	{
		if(type != Type.PROP) return null;
		
		Object result = getData();
		if(result == null) return ref;
		
		return result;
	}
	
	static public Object getPropData(String key)
	{
		loadProperty();
		if(property == null)
			return null;
		
		return property.get(key);
	}
	
	static public Object getPropData(String key, Object ref)
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
	static public void setPropData(String key, Object data)
	{
		loadProperty();
		if(property == null) {
			property = new JSONObject();
		}
		property.put(key, data);
		saveProperty();
	}

	private static String getUTF8Path()
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
