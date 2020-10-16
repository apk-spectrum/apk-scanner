package com.apkscanner.resource;

import java.awt.Font;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.UIManager;

import com.apkspectrum.resource.DefaultResProp;
import com.apkspectrum.resource.ResProp;
import com.apkspectrum.swing.tabbedpaneui.TabbedPaneUIManager;
import com.apkspectrum.util.SystemUtil;

public enum RProp implements ResProp<Object>
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

		@Override
		public String getValue() {
			return RProp.valueOf(name()).getValue();
		}

		@Override
		public void addPropertyChangeListener(PropertyChangeListener listener) {
			RProp.valueOf(name()).addPropertyChangeListener(listener);
		}

		@Override
		public void removePropertyChangeListener(PropertyChangeListener listener) {
			RProp.valueOf(name()).removePropertyChangeListener(listener);
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

		@Override
		public String getValue() {
			return RProp.valueOf(name()).getValue();
		}

		@Override
		public void addPropertyChangeListener(PropertyChangeListener listener) {
			RProp.valueOf(name()).addPropertyChangeListener(listener);
		}

		@Override
		public void removePropertyChangeListener(PropertyChangeListener listener) {
			RProp.valueOf(name()).removePropertyChangeListener(listener);
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

		@Override
		public String getValue() {
			return RProp.valueOf(name()).getValue();
		}

		@Override
		public void addPropertyChangeListener(PropertyChangeListener listener) {
			RProp.valueOf(name()).addPropertyChangeListener(listener);
		}

		@Override
		public void removePropertyChangeListener(PropertyChangeListener listener) {
			RProp.valueOf(name()).removePropertyChangeListener(listener);
		}
	}

	private DefaultResProp res;

	private RProp() {
		res = new DefaultResProp(name(), getDefaultValue());
	}

	private RProp(Object defValue) {
		res = new DefaultResProp(name(), defValue);
	}

	public Object getDefaultValue() {
		Object obj = res != null ? res.getDefaultValue() : null;
		if(obj != null) return obj;

		switch(name()) {
		case "EDITOR":
			try {
				obj = SystemUtil.getDefaultEditor();
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case "PREFERRED_LANGUAGE":
			String propPreferredLanguage = SystemUtil.getUserLanguage();
			String propStrLanguage = (String) LANGUAGE.getData();
			if(!propPreferredLanguage.equals(propStrLanguage) && !"en".equals(propPreferredLanguage)) {
				propPreferredLanguage += ";" + (propStrLanguage.isEmpty() ? "en" : propStrLanguage);
			}
			obj = propPreferredLanguage + ";";
			break;
		case "PEM_FILE_PATH":
			String pem = RFile.DATA_CERT_PEM_FILE.getPath();
			if(new File(pem).isFile()) {
				obj = pem;
			}
			break;
		case "PK8_FILE_PATH":
			String pk8 = RFile.DATA_CERT_PK8_FILE.getPath();
			if(new File(pk8).isFile()) {
				obj = pk8;
			}
			break;
		case "TABBED_UI_THEME":
			obj = TabbedPaneUIManager.DEFAULT_TABBED_UI;
			break;
		default:
			break;
		};

		if(res != null && obj != null) res.setDefaultValue(obj);
		return obj;
	}

	@Override
	public String getValue() {
		return res.getValue();
	}

	@Override
	public String toString() {
		return res.toString();
	}

	@Override
	public Object get() {
		return res.get();
	}

	@Override
	public void set(Object data) {
		res.set(data);
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		res.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		res.removePropertyChangeListener(listener);
	}

	public Object getData() {
		return res.getData();
	}

	public Object getData(Object ref) {
		return res.getData(ref);
	}

	public String getString() {
		return res.getString();
	}

	public int getInt() {
		return res.getInt();
	}

	public int getInt(int ref) {
		return res.getInt(ref);
	}

	public boolean getBoolean() {
		return res.getBoolean();
	}

	public boolean getBoolean(boolean ref) {
		return res.getBoolean();
	}

	public void setData(Object data) {
		res.setData(data);
	}

	public static Object getPropData(String key) {
		return DefaultResProp.getPropData(key);
	}

	public static Object getPropData(String key, Object ref) {
		return DefaultResProp.getPropData(key, ref);
	}

	public static void setPropData(String key, Object data) {
		DefaultResProp.setPropData(key, data);
	}

    public static void addPropertyChangeListener(ResProp<?> prop, PropertyChangeListener listener) {
    	DefaultResProp.addPropertyChangeListener(prop, listener);
    }

    public static void removePropertyChangeListener(ResProp<?> prop, PropertyChangeListener listener) {
    	DefaultResProp.removePropertyChangeListener(prop, listener);
    }
}
