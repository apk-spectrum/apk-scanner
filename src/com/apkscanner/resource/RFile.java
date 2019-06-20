package com.apkscanner.resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

import com.apkscanner.util.SystemUtil;

public enum RFile implements ResFile<String>, ResString<String>
{
	BIN_PATH					(Type.BIN, ""),

	BIN_ADB_LNX					(Type.BIN, "adb", "nux"),
	BIN_ADB_WIN					(Type.BIN, "adb.exe", "win"),
	BIN_ADB						(Type.BIN, new RFile[]{ BIN_ADB_WIN, BIN_ADB_LNX }),

	BIN_AAPT_LNX				(Type.BIN, "aapt", "nux"),
	BIN_AAPT_WIN				(Type.BIN, "aapt.exe", "win"),
	BIN_AAPT					(Type.BIN, new RFile[]{ BIN_AAPT_WIN, BIN_AAPT_LNX }),

	BIN_JDGUI					(Type.BIN, "jd-gui-1.4.0.jar"),

	BIN_DEX2JAR_LNX				(Type.BIN, "d2j-dex2jar.sh", "nux"),
	BIN_DEX2JAR_WIN				(Type.BIN, "d2j-dex2jar.bat", "win"),
	BIN_DEX2JAR					(Type.BIN, new RFile[]{ BIN_DEX2JAR_WIN, BIN_DEX2JAR_LNX }),

	BIN_JADX_LNX				(Type.BIN, "jadx/bin/jadx-gui", "nux"),
	BIN_JADX_WIN				(Type.BIN, "jadx\\bin\\jadx-gui.bat", "win"),
	BIN_JADX_GUI				(Type.BIN, new RFile[]{ BIN_JADX_WIN, BIN_JADX_LNX }),

	BIN_BYTECODE_VIEWER			(Type.BIN, "Bytecode-Viewer.jar"),

	BIN_SIGNAPK					(Type.BIN, "signapk.jar"),

	PLUGIN_PATH					(Type.PLUGIN, ""),
	PLUGIN_CONF_PATH			(Type.PLUGIN, "plugins.conf"),

	SSL_TRUSTSTORE_PATH			(Type.SECURITY, "trustStore.jks"),

	LIB_JSON_JAR				(Type.LIB, "json-simple-1.1.1.jar"),
	LIB_CLI_JAR					(Type.LIB, "commons-cli-1.3.1.jar"),
	LIB_APKTOOL_JAR				(Type.LIB, "apktool.jar"),
	LIB_ALL						(Type.LIB, "*"),

	RAW_ABUOT_HTML				(Type.RAW, "/values/AboutLayout.html"),
	RAW_BASIC_INFO_LAYOUT_HTML	(Type.RAW, "/values/BasicInfoLayout.html"),
	RAW_PACKAGE_INFO_LAYOUT_HTML(Type.RAW, "/values/PackageInfoLayout.html"),
	RAW_ADB_INSTALL_BUTTON_HTML	(Type.RAW, "/values/AdbInstallButton.html"),
	RAW_PERMISSION_REFERENCE_HTML(Type.RAW, "/values/PermissionReference.html"),
	RAW_PROTECTION_LEVELS_HTML	(Type.RAW, "/values/ProtectionLevels.html"),

	ETC_SETTINGS_FILE			(Type.ETC, "settings.txt"),
	; // ENUM END

	private String value;
	private Type type;
	private String os;

	private RFile(Type type, String value) {
		this(type, value, null);
	}

	private RFile(Type type, String value, String os) {
		this.type = type;
		this.value = value;
		this.os = os;
	}

	private RFile(Type type, RFile[] cfgResources) {
		if(cfgResources == null | cfgResources.length == 0) {
			throw new IllegalArgumentException();
		}

		this.type = type;
		for(RFile r: cfgResources) {
			if(SystemUtil.OS.indexOf(r.os) > -1) {
				this.value = r.value;
				this.os = r.os;
				break;
			}
		}
		if(this.value == null || os == null) {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return getPath();
	}

	@Override
	public String getPath() {
		String subPath;
		switch(type){
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

	@Override
	public URL getURL() {
		switch(type){
		case RAW:
			return getClass().getResource(value);
		default:
			return null;
		}
	}

	@Override
	public String get() {
		return getPath();
	}

	@Override
	public String getString() {
		switch(type){
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

	public java.net.URL getResource() {
		return getClass().getResource(getPath());
	}

	public InputStream getResourceAsStream() {
		return getClass().getResourceAsStream(getPath());
	}

	public static String getUTF8Path() {
		String resourcePath = RFile.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		resourcePath = (new File(resourcePath)).getParentFile().getPath();

		try {
			resourcePath = URLDecoder.decode(resourcePath, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return resourcePath;
	}

	public static java.net.URL getResource(String path) {
		return RFile.class.getResource(path);
	}

	public static InputStream getResourceAsStream(String path) {
		return RFile.class.getResourceAsStream(path);
	}

}
