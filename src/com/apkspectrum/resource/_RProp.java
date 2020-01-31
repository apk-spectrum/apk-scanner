package com.apkspectrum.resource;

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

import com.apkspectrum.util.SystemUtil;

public enum _RProp implements ResProp<Object>, ResString<Object>
{
	LANGUAGE					(SystemUtil.getUserLanguage()),
	USE_EASY_UI					(false),
	SKIP_STARTUP_EASY_UI_DLG	(false),
	EDITOR,						/* see getDefualtValue() */
	ADB_PATH					(""),
	ADB_POLICY_SHARED			(true),
	ADB_DEVICE_MONITORING		(true),
	TRY_UNLOCK_AF_LAUNCH		(true),
	LAUNCH_AF_INSTALLED			(true),
	FRAMEWORK_RES				(""),
	LAST_FILE_OPEN_PATH			(""),
	LAST_FILE_SAVE_PATH			(""),
	CURRENT_THEME				(UIManager.getSystemLookAndFeelClassName()),
	SAVE_WINDOW_SIZE			(false),
	PREFERRED_LANGUAGE,			/* see getDefualtValue() */
	PRINT_MULTILINE_ATTR		(true),
	; // ENUM END

	public enum B implements ResProp<Boolean> {
		USE_EASY_UI,
		SKIP_STARTUP_EASY_UI_DLG,
		ADB_POLICY_SHARED,
		ADB_DEVICE_MONITORING,
		TRY_UNLOCK_AF_LAUNCH,
		LAUNCH_AF_INSTALLED,
		SAVE_WINDOW_SIZE,
		PRINT_MULTILINE_ATTR,
		; // ENUM END

		@Override
		public Boolean get() {
			return _RProp.valueOf(name()).getBoolean();
		}

		@Override
		public void set(Boolean data) {
			_RProp.valueOf(name()).setData(data);
		}
	}

	public enum S implements ResProp<String> {
		LANGUAGE,
		EDITOR,
		ADB_PATH,
		FRAMEWORK_RES,
		LAST_FILE_OPEN_PATH,
		LAST_FILE_SAVE_PATH,
		CURRENT_THEME,
		PREFERRED_LANGUAGE,
		; // ENUM END

		@Override
		public String get() {
			return _RProp.valueOf(name()).getString();
		}

		@Override
		public void set(String data) {
			_RProp.valueOf(name()).setData(data);
		}
	}

	public enum I implements ResProp<Integer> {
		// EMPTY
		; // ENUM END

		@Override
		public Integer get() {
			return _RProp.valueOf(name()).getInt();
		}

		@Override
		public void set(Integer data) {
			_RProp.valueOf(name()).setData(data);
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

	private _RProp() {
		this(null);
	}

	private _RProp(Object defValue) {
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

    public static void addPropertyChangeListener(_RProp prop, PropertyChangeListener listener) {
    	pcs.addPropertyChangeListener(prop != null ? prop.getValue() : null, listener);
    }

    public static void removePropertyChangeListener(_RProp prop, PropertyChangeListener listener) {
    	pcs.removePropertyChangeListener(prop != null ? prop.getValue() : null, listener);
    }

	private static void loadProperty() {
		if(property == null) {
			File file = new File(_RFile.ETC_SETTINGS_FILE.getPath());
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

		try( FileWriter fw = new FileWriter(_RFile.ETC_SETTINGS_FILE.getPath());
			BufferedWriter writer = new BufferedWriter(fw) ) {
			writer.write(transMultiLine);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
