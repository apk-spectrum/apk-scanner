package com.apkscanner.resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;

import com.apkspectrum.resource.ResFile;
import com.apkspectrum.resource.ResString;
import com.apkspectrum.util.SystemUtil;

public enum RFile implements ResFile<File>, ResString<File>
{
	BIN_PATH					(Type.BIN, ""),

	DATA_PATH					(Type.DATA, ""),
	DATA_STRINGS_EN				(Type.DATA, "strings.xml"),
	DATA_CERT_PEM_FILE			(Type.DATA, "build-master-target-product-security" + File.separator + "platform.x509.pem"),
	DATA_CERT_PK8_FILE			(Type.DATA, "build-master-target-product-security" + File.separator + "platform.pk8"),

	RAW_ROOT_PATH				(Type.RES_ROOT, ""),

	RAW_VALUES_PATH				(Type.RES_VALUE, ""),
	RAW_STRINGS_EN				(Type.RES_VALUE, "strings.xml"),
	RAW_STRINGS_KO				(Type.RES_VALUE, "strings-ko.xml"),

	RAW_ABUOT_HTML				(Type.RES_VALUE, "AboutLayout.html"),
	RAW_APEX_INFO_LAYOUT_HTML	(Type.RES_VALUE, "ApexInfoLayout.html"),
	RAW_BASIC_INFO_LAYOUT_HTML	(Type.RES_VALUE, "BasicInfoLayout.html"),
	RAW_PACKAGE_INFO_LAYOUT_HTML(Type.RES_VALUE, "PackageInfoLayout.html"),
	RAW_ADB_INSTALL_BUTTON_HTML	(Type.RES_VALUE, "AdbInstallButton.html"),

	ETC_APKSCANNER_EXE			(Type.ETC, "ApkScanner.exe"),
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
			if(SystemUtil.OS.contains(r.os)) {
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
		if(type == Type.RES_VALUE || type == Type.RES_ROOT) {
			return getURL().toExternalForm();
		}
		return getUTF8Path() + value;
	}

	@Override
	public URL getURL() {
		switch(type){
		case RES_ROOT:
			return getClass().getResource("/" + value);
		case RES_VALUE:
			if(value == null || value.isEmpty())
				return getClass().getResource("/values");
			else
				return getClass().getResource("/values/" + value);
		default:
			try {
				return new File(getPath()).toURI().toURL();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public File get() {
		try {
			return new File(getURL().toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getString() {
		switch(type){
		case RES_VALUE:
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

	public URL getResource() {
		return getURL();
	}

	public InputStream getResourceAsStream() {
		switch(type){
		case RES_VALUE:
			return getClass().getResourceAsStream("/values/" + value);
		default:
			try {
				return getURL().openStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private String getUTF8Path() {
		String resourcePath = RFile.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		resourcePath = (new File(resourcePath)).getParentFile().getPath()
				+ File.separator + type.getValue() + File.separator;

		try {
			resourcePath = URLDecoder.decode(resourcePath, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return resourcePath;
	}
}
