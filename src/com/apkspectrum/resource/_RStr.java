package com.apkspectrum.resource;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.apkspectrum.util.Log;
import com.apkspectrum.util.XmlPath;
import com.apkspectrum.util.ZipFileUtil;

public enum _RStr implements ResString<String>
{
	TITLE_EDIT_CONFIG			("@title_edit_config"),
	TITLE_NO_SUCH_NETWORK		("@title_no_such_network"),
	TITLE_NETWORK_TIMEOUT		("@title_network_timeout"),
	TITLE_SSL_EXCEPTION			("@title_ssl_exception"),
	TITLE_UPDATE_LIST			("@title_update_list"),

	BTN_CLOSE					("@btn_close"),
	BTN_ADD						("@btn_add"),
	BTN_DEL						("@btn_del"),
	BTN_EDIT					("@btn_edit"),

	BTN_RETRY					("@btn_retry"),
	BTN_APPLY					("@btn_apply"),
	BTN_REMOVE					("@btn_remove"),
	BTN_IMPORT					("@btn_import"),
	BTN_MANAGE_CERT				("@btn_manage_cert"),
	BTN_CHECK_UPDATE			("@btn_check_update"),
	BTN_GO_TO_WEBSITE			("@btn_go_to_website"),
	BTN_UPDATE					("@btn_update"),
	BTN_DOWNLOAD				("@btn_download"),
	BTN_NO_UPDATED				("@btn_no_updated"),
	BTN_CHOOSE_UPDATE			("@btn_choose_update"),

	FILE_SIZE_BYTES				("@file_size_Bytes"),
	FILE_SIZE_KB				("@file_size_KB"),
	FILE_SIZE_MB				("@file_size_MB"),
	FILE_SIZE_GB				("@file_size_GB"),
	FILE_SIZE_TB				("@file_size_TB"),

	LABEL_ERROR					("@label_error"),
	LABEL_WARNING				("@label_warning"),
	LABEL_INFO					("@label_info"),
	LABEL_QUESTION				("@label_question"),
	LABEL_APK_FILE_DESC			("@label_apk_file_description"),
	LABEL_SAVE_AS				("@label_save_as"),
	LABEL_PLUGINS_SETTINGS		("@label_plugins_settings"),
	LABEL_PLUGINS_DESCRIPTION	("@label_plugins_description"),
	LABEL_PLUGIN_PACKAGE_CONFIG	("@label_plugin_pcakge_config"),
	LABEL_KEY_NAME				("@label_key_name"),
	LABEL_KEY_VALUE				("@label_key_value"),
	LABEL_PROXY_SETTING			("@label_proxy_setting"),
	LABEL_TRUSTSTORE_SETTING	("@label_truststore_setting"),
	LABEL_PAC_SCRIPT_URL		("@label_pac_script_url"),
	LABEL_ALIAS					("@label_alias"),
	LABEL_DESCRIPTION			("@label_description"),
	LABEL_DO_NOT_LOOK_AGAIN		("@label_do_not_look_again"),
	LABEL_UPDATE_LIST			("@label_update_list"),

	TREE_NODE_PLUGINS_TOP		("@tree_node_plugins_top"),
	TREE_NODE_PLUGINS_TOP_DESC	("@tree_node_plugins_top_desc"),
	TREE_NODE_NO_PLUGINS		("@tree_node_no_plugins"),
	TREE_NODE_NO_PLUGINS_DESC	("@tree_node_no_plugins_desc"),
	TREE_NODE_NETWORK			("@tree_node_network_setting"),
	TREE_NODE_CONFIGURATION		("@tree_node_configurations"),
	TREE_NODE_GLOBAL_SETTING	("@tree_node_global_setting"),
	TREE_NODE_GLOBAL_SETTING_DESC("@tree_node_global_setting_desc"),

	MSG_CANNOT_WRITE_FILE		("@msg_cannot_write_file"),
	MSG_WARN_UNSUPPORTED_JVM	("@msg_warn_unsupported_jvm"),
	MSG_WARN_SSL_IGNORE			("@msg_warn_ssl_ignore"),
	MSG_NO_SUCH_NETWORK			("@msg_no_such_network"),
	MSG_FAILURE_PROXY_ERROR		("@msg_failure_proxy_error"),
	MSG_FAILURE_SSL_ERROR		("@msg_failure_ssl_error"),
	MSG_NO_UPDATE_INFO			("@msg_no_update_informations"),

	QUESTION_SAVE_OVERWRITE		("@question_save_overwrite"),

	PATTERN_PRINT_X509_CERT		("@pattern_print_x509_cert"),
	WITH_WEAK					("@with_weak"),
	KEY_BIT						("@key_bit"),
	KEY_BIT_WEAK				("@key_bit_weak"),
	NOT_A_SINGED_JAR_FILE		("@not_a_singed_jar_file"),
	TIMESTAMP					("@timestamp"),

	PROXY_MENU_NO_PROXY			("@proxy_menu_no_proxy"),
	PROXY_MENU_GLOBAL			("@proxy_menu_global"),
	PROXY_MENU_SYSTEM			("@proxy_menu_system"),
	PROXY_MENU_PAC_SCRIPT		("@proxy_menu_pac_script"),
	PROXY_MENU_MANUAL			("@proxy_menu_manual"),

	TRUSTSTORE_GLOBAL			("@truststore_golbal"),
	TRUSTSTORE_APKSCANNER		("@truststore_apkscanner"),
	TRUSTSTORE_JVM				("@truststore_jvm"),
	TRUSTSTORE_MANUAL			("@truststore_manual"),
	TRUSTSTORE_IGNORE			("@truststore_ignore"),

	COLUMN_ISSUE_TO				("@column_issued_to"),
	COLUMN_ISSUE_BY				("@column_issued_by"),
	COLUMN_EXPIRES_ON			("@column_expires_on"),
	COLUMN_ALIAS				("@column_alias"),
	COLUMN_NAME					("@column_name"),
	COLUMN_PACKAGE				("@column_package"),
	COLUMN_THIS_VERSION			("@column_this_version"),
	COLUMN_NEW_VERSION			("@column_new_version"),
	COLUMN_LAST_CHECKED_DATE	("@column_last_checked_date"),
	; // ENUM END

	static {
		_RProp.LANGUAGE.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				setLanguage((String) evt.getNewValue());
				fireLanguageChange((String) evt.getOldValue(), (String) evt.getNewValue());
			}
		});
	}

	private static List<LanguageChangeListener> listeners = new ArrayList<>();

	private static String lang = null;
	private static XmlPath[] stringXmlPath = null;

	private String value;

	private _RStr(String value) {
		this.value = value;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public String get() {
		return getString();
	}

	@Override
	public String toString() {
		return getString();
	}

	@Override
	public String getString() {
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
	}

	public static void setLanguage(String l) {
		if(lang != l) makeStringXmlPath(l);
		lang = l;
	}

	public static String getLanguage() {
		return lang;
	}

	private static void makeStringXmlPath(String lang) {
		ArrayList<XmlPath> xmlList = new ArrayList<XmlPath>();

		String value_path = _RFile.DATA_PATH.getPath();
		if(lang != null && !lang.isEmpty()) {
			String ext_lang_value_path = value_path + "strings-" + lang + ".xml";
			File extFile = new File(ext_lang_value_path);
			if(extFile.exists()) {
				xmlList.add(new XmlPath(extFile));
			}

			_RFile langRes = _RFile.valueOf("RAW_STRINGS_" + lang.toUpperCase());
			if(langRes != null) {
				try(InputStream xml = langRes.getResourceAsStream()) {
					if(xml != null) xmlList.add(new XmlPath(xml));
				} catch(IOException e) { }
			}
		}

		String ext_lang_value_path = _RFile.DATA_STRINGS_EN.getPath();
		File extFile = new File(ext_lang_value_path);
		if(extFile.exists()) {
			xmlList.add(new XmlPath(extFile));
		}

		InputStream xml = _RFile.RAW_STRINGS_EN.getResourceAsStream();
		if(xml != null) {
			xmlList.add(new XmlPath(xml));
		}

		stringXmlPath = xmlList.toArray(new XmlPath[0]);
	}

	public static String[] getSupportedLanguages() {
		ArrayList<String> languages = new ArrayList<String>();

		String value_path = _RFile.DATA_PATH.getPath();
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

		URL resource = _RFile.RAW_VALUES_PATH.getURL();
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

	public static void addLanguageChangeListener(LanguageChangeListener listener) {
		if(listener == null) return;
		if(!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public static void removeLanguageChangeListener(LanguageChangeListener listener) {
		if(listener == null) return;
		if(listeners.contains(listener)) {
			listeners.remove(listener);
		}
	}

	public static LanguageChangeListener[] getLanguageChangeListener() {
		return listeners.toArray(new LanguageChangeListener[listeners.size()]);
	}

	public static void fireLanguageChange(String oldLanguage, String newLanguage) {
		for(LanguageChangeListener l: listeners) {
			l.languageChange(oldLanguage, newLanguage);
		}
	}
}
