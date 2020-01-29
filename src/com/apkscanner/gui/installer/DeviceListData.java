package com.apkscanner.gui.installer;

import java.awt.Color;

import com.android.ddmlib.IDevice;
import com.apkspectrum.core.installer.OptionsBundle;

public class DeviceListData
{
	public static final int STATUS_SETTING = 0;
	public static final int STATUS_INSTALLING = 1;
	public static final int STATUS_SUCESSED = 2;
	public static final int STATUS_FAILED = 3;
	public static final int STATUS_NO_ACTION = 4;
	public static final int STATUS_CONNECTING_DEVICE = 5;

	private IDevice device;
	private OptionsBundle options = null;
	private int state = STATUS_SETTING;
	private String errorMessage;

	private String osVersion;
	private String deviceName;
	private Color circleColor;

	public DeviceListData(IDevice device, OptionsBundle bundle) {
		this.device = device;
		this.options = bundle;
	}

	public IDevice getDevice() {
		return device;
	}

	public OptionsBundle getOptionsBundle() {
		return options;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getState() {
		return state;
	}

	public void setErrorMessage(String message) {
		errorMessage = message;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	private int hashCode(String str) {
		int hash = 0;
		for (int i = 0; i < str.length(); i++) {
			//hash = str.charAt(i)^10 + ((hash << 4) - hash);
			hash = str.charAt(i) + ((hash << 10) * hash);
		}
		return hash;
	}

	private String intToARGB(int i) {
		String hex = ""+ Integer.toHexString((i>>24)&0xFF) + Integer.toHexString((i>>16)&0xFF) +
				Integer.toHexString((i>>8)&0xFF) + Integer.toHexString(i&0xFF);
		// Sometimes the string returned will be too short so we
		// add zeros to pad it out, which later get removed if
		// the length is greater than six.
		//hex += "000000";
		return hex.substring(0, 6);
	}

	public Color getCircleColor() {
		if(circleColor == null && getDeviceName() != null) {
			circleColor = Color.decode("#"+intToARGB(hashCode(deviceName)));
		} else if(circleColor == null) {
			return Color.GRAY;
		}
		return circleColor;
	}

	public String getDeviceName() {
		if(deviceName == null || deviceName.isEmpty()) {
			deviceName = device.getProperty(IDevice.PROP_DEVICE_MODEL);
			if(deviceName != null) deviceName = deviceName.trim();
			if(deviceName != null && deviceName.isEmpty()) {
				deviceName = null;
			}
		}
		return deviceName;
	}

	public String getOsVersion() {
		if(osVersion == null || osVersion.isEmpty()) {
			osVersion = device.getProperty(IDevice.PROP_BUILD_VERSION);
			if(osVersion != null) {
				osVersion = osVersion.trim();
				if(osVersion.isEmpty()) osVersion = null;
			}
		}
		return osVersion;
	}

}
