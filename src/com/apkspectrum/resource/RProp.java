package com.apkspectrum.resource;

import java.awt.Font;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.UIManager;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.apkspectrum.swing.tabbedpaneui.TabbedPaneUIManager;
import com.apkspectrum.util.SystemUtil;

public enum RProp implements ResProp<Object>, ResString<Object>
{
	LANGUAGE					(SystemUtil.getUserLanguage()),
	USE_EASY_UI					(false),
	SKIP_STARTUP_EASY_UI_DLG	(false),
	USE_UI_BOOSTER				(false),
	ESC_ACTION					(RConst.INT_ESC_ACT_NONE),
	EDITOR,						/* see getDefualtValue() */
	RECENT_EDITOR				(""),
	ADB_PATH					(""),
	ADB_POLICY_SHARED			(true),
	ADB_DEVICE_MONITORING		(true),
	LAUNCH_ACTIVITY_OPTION		(RConst.INT_LAUNCH_LAUNCHER_OR_MAIN_ACTIVITY),
	TRY_UNLOCK_AF_LAUNCH		(true),
	LAUNCH_AF_INSTALLED			(true),
	RECENT_ADB_INFO				(""),
	FRAMEWORK_RES				(""),
	LAST_FILE_OPEN_PATH			(""),
	LAST_FILE_SAVE_PATH			(""),
	SOVE_LEAD_TIME,
	CURRENT_THEME				(UIManager.getSystemLookAndFeelClassName()),
	TABBED_UI_THEME,			/* see getDefualtValue() */
	SAVE_WINDOW_SIZE			(false),
	BASE_FONT					(""),
	BASE_FONT_SIZE				(12),
	BASE_FONT_STYLE				(Font.PLAIN),
	PREFERRED_LANGUAGE,			/* see getDefualtValue() */
	PEM_FILE_PATH,				/* see getDefualtValue() */
	PK8_FILE_PATH,				/* see getDefualtValue() */
	PRINT_MULTILINE_ATTR		(true),
	AXML_VIEWER_TYPE			(RConst.AXML_VEIWER_TYPE_XML),
	COMP_FILTER_TYPE			(RConst.COMPONENT_FILTER_TYPE_XML),

	EASY_GUI_TOOLBAR			("1,2,3,4,5,7"),

	EASY_GUI_WINDOW_POSITION_X	(null),
	EASY_GUI_WINDOW_POSITION_Y	(null),

	DEFAULT_DECORDER			(RConst.STR_DECORDER_JADX_GUI),
	DEFAULT_SEARCHER			(RConst.STR_DEFAULT_SEARCHER),
	DEFAULT_EXPLORER			(RConst.STR_EXPLORER_ARCHIVE),
	DEFAULT_LAUNCH_MODE			(RConst.STR_LAUNCH_LAUNCHER),
	VISIBLE_TO_BASIC			(true),
	ALWAYS_TOOLBAR_EXTENDED		(false),

	PERM_MARK_RUNTIME			(true),
	PERM_MARK_COUNT				(true),
	PERM_TREAT_SIGN_AS_REVOKED	(true),
	; // ENUM END

	public enum B implements ResProp<Boolean> {
		USE_EASY_UI,
		SKIP_STARTUP_EASY_UI_DLG,
		USE_UI_BOOSTER,
		ADB_POLICY_SHARED,
		ADB_DEVICE_MONITORING,
		TRY_UNLOCK_AF_LAUNCH,
		LAUNCH_AF_INSTALLED,
		SAVE_WINDOW_SIZE,
		PRINT_MULTILINE_ATTR,
		VISIBLE_TO_BASIC,
		ALWAYS_TOOLBAR_EXTENDED,
		PERM_MARK_RUNTIME,
		PERM_MARK_COUNT,
		PERM_TREAT_SIGN_AS_REVOKED,
		; // ENUM END

		@Override
		public Boolean get() {
			return RProp.valueOf(name()).getBoolean();
		}

		@Override
		public void set(Boolean data) {
			RProp.valueOf(name()).setData(data);
		}
	}

	public enum S implements ResProp<String> {
		LANGUAGE,
		SKIP_STARTUP_EASY_UI_DLG,
		EDITOR,
		RECENT_EDITOR,
		ADB_PATH,
		RECENT_ADB_INFO,
		FRAMEWORK_RES,
		LAST_FILE_OPEN_PATH,
		LAST_FILE_SAVE_PATH,
		SOVE_LEAD_TIME,
		CURRENT_THEME,
		TABBED_UI_THEME,
		BASE_FONT,
		PREFERRED_LANGUAGE,
		PEM_FILE_PATH,
		PK8_FILE_PATH,
		EASY_GUI_TOOLBAR,
		DEFAULT_DECORDER,
		DEFAULT_SEARCHER,
		DEFAULT_EXPLORER,
		DEFAULT_LAUNCH_MODE,
		AXML_VIEWER_TYPE,
		COMP_FILTER_TYPE,
		; // ENUM END

		@Override
		public String get() {
			return RProp.valueOf(name()).getString();
		}

		@Override
		public void set(String data) {
			RProp.valueOf(name()).setData(data);
		}
	}

	public enum I implements ResProp<Integer> {
		ESC_ACTION,
		LAUNCH_ACTIVITY_OPTION,
		BASE_FONT_STYLE,
		BASE_FONT_SIZE,
		EASY_GUI_WINDOW_POSITION_X,
		EASY_GUI_WINDOW_POSITION_Y,
		; // ENUM END

		@Override
		public Integer get() {
			return RProp.valueOf(name()).getInt();
		}

		@Override
		public void set(Integer data) {
			RProp.valueOf(name()).setData(data);
		}
	}

	private static JSONObject property;
	private static PropertyChangeSupport pcs;

	static {
		loadProperty();
		if(property == null) property = new JSONObject();
		pcs = new PropertyChangeSupport(property);
	}

	private String value;
	private Object defValue;

	private RProp() {
		this(null);
	}

	private RProp(Object defValue) {
		this.value = name();
		this.defValue = defValue;
	}

	public Object getDefaultValue() {
		Object obj = null;
		if(defValue != null) {
			return defValue;
		}
		switch(this) {
		case EDITOR:
			try {
				obj = SystemUtil.getDefaultEditor();
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case PREFERRED_LANGUAGE:
			String propPreferredLanguage = SystemUtil.getUserLanguage();
			String propStrLanguage = (String) LANGUAGE.getData();
			if(!propPreferredLanguage.equals(propStrLanguage) && !"en".equals(propPreferredLanguage)) {
				propPreferredLanguage += ";" + (propStrLanguage.isEmpty() ? "en" : propStrLanguage);
			}
			obj = propPreferredLanguage + ";";
			break;
		case PEM_FILE_PATH:
			String pem = RFile.DATA_CERT_PEM_FILE.getPath();
			if(new File(pem).isFile()) {
				obj = pem;
			}
			break;
		case PK8_FILE_PATH:
			String pk8 = RFile.DATA_CERT_PK8_FILE.getPath();
			if(new File(pk8).isFile()) {
				obj = pk8;
			}
			break;
		case TABBED_UI_THEME:
			return TabbedPaneUIManager.DEFAULT_TABBED_UI;
		default:
			break;
		};
		defValue = obj;
		return obj;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return getString();
	}

	@Override
	public String getString() {
		return String.valueOf(getData());
	}

	@Override
	public Object get() {
		return getData();
	}

	@Override
	public void set(Object data) {
		setData(data);
	}

	public Object getData() {
		return getPropData(getValue(), getDefaultValue());
	}

	public Object getData(Object ref) {
		Object result = getData();
		return result != null ? result : ref;
	}

	public int getInt() {
		return getInt(0);
	}

	public int getInt(int ref) {
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

	public boolean getBoolean() {
		return getBoolean(false);
	}

	public boolean getBoolean(boolean ref) {
		Object data = getData();
		if(data == null) return ref;

		boolean ret = false;
		if(data instanceof Boolean) {
			ret = (Boolean)data;
		} else if(data instanceof String) {
			ret = "true".equalsIgnoreCase((String)data);
		} else if(data instanceof Integer) {
			ret = (Integer)data != 0;
		} else {
			ret = !String.valueOf(data).isEmpty();
		}

		return ret;
	}

	public void setData(Object data) {
		setPropData(getValue(), data);
	}

	public static Object getPropData(String key) {
		return getPropData(key, null);
	}

	public static Object getPropData(String key, Object ref) {
		Object data = property != null ? property.get(key) : ref;
		return data != null ? data : ref;
	}

	@SuppressWarnings("unchecked")
	public static void setPropData(String key, Object data) {
		Object old = null;
		if(data == null) {
			old = property.remove(key);
			if(old != null) {
				saveProperty();
				pcs.firePropertyChange(key, old, data);
			}
			return;
		}
		old = property.get(key);
		if(!data.equals(old)) {
			property.put(key, data);
			saveProperty();
			pcs.firePropertyChange(key, old, data);
		}
	}

    public void addPropertyChangeListener(PropertyChangeListener listener) {
    	addPropertyChangeListener(this, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
    	removePropertyChangeListener(this, listener);
    }

    public static void addPropertyChangeListener(RProp prop, PropertyChangeListener listener) {
    	pcs.addPropertyChangeListener(prop != null ? prop.getValue() : null, listener);
    }

    public static void removePropertyChangeListener(RProp prop, PropertyChangeListener listener) {
    	pcs.removePropertyChangeListener(prop != null ? prop.getValue() : null, listener);
    }

	private static void loadProperty() {
		if(property == null) {
			File file = new File(RFile.ETC_SETTINGS_FILE.getPath());
			if(!file.exists() || file.length() == 0) return;
			try (FileReader fileReader = new FileReader(file)) {
				JSONParser parser = new JSONParser();
				property = (JSONObject)parser.parse(fileReader);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static void saveProperty() {
		if(property == null) return;

		String transMultiLine = property.toJSONString()
				.replaceAll("^\\{(.*)\\}$", "{\n$1\n}")
				.replaceAll("(\"[^\"]*\":(\"[^\"]*\")?([^\",]*)?,)", "$1\n");
		//.replaceAll("(\"[^\"]*\":(\"[^\"]*\")?([^\",\\[]*(\\[[^\\]]\\])?)?,)", "$1\n");

		try( FileWriter fw = new FileWriter(RFile.ETC_SETTINGS_FILE.getPath());
			BufferedWriter writer = new BufferedWriter(fw) ) {
			writer.write(transMultiLine);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
