package com.apkspectrum.resource;

import java.awt.Image;
import java.net.URL;

import javax.swing.ImageIcon;

import com.apkspectrum.swing.ImageScaler;

public enum _RImg implements ResImage<URL>
{
	PERM_GROUP_ACCESSIBILITY_FEATURES	("perm_group_accessibility_features.png"),
	PERM_GROUP_ACCOUNTS					("perm_group_accounts.png"),
	PERM_GROUP_AFFECTS_BATTERY			("perm_group_affects_battery.png"),
	PERM_GROUP_APP_INFO					("perm_group_app_info.png"),
	PERM_GROUP_AUDIO_SETTINGS			("perm_group_audio_settings.png"),
	PERM_GROUP_BLUETOOTH				("perm_group_bluetooth.png"),
	PERM_GROUP_BOOKMARKS				("perm_group_bookmarks.png"),
	PERM_GROUP_CALENDAR					("perm_group_calendar.png"),
	PERM_GROUP_CALL_LOG					("perm_group_call_log.png"),
	PERM_GROUP_CAMERA					("perm_group_camera.png"),
	PERM_GROUP_CONTACTS					("perm_group_contacts.png"),
	PERM_GROUP_COST_MONEY				("perm_group_cost_money.png"),
	PERM_GROUP_DECLARED					("perm_group_declared.png"),
	PERM_GROUP_DEVELOPMENT_TOOLS		("perm_group_development_tools.png"),
	PERM_GROUP_DEVICE_ALARMS			("perm_group_device_alarms.png"),
	PERM_GROUP_DISPLAY					("perm_group_display.png"),
	PERM_GROUP_HARDWARE_CONTROLS		("perm_group_hardware_controls.png"),
	PERM_GROUP_LOCATION					("perm_group_location.png"),
	PERM_GROUP_MESSAGES					("perm_group_messages.png"),
	PERM_GROUP_MICROPHONE				("perm_group_microphone.png"),
	PERM_GROUP_NETWORK					("perm_group_network.png"),
	PERM_GROUP_PERSONAL_INFO			("perm_group_personal_info.png"),
	PERM_GROUP_PHONE_CALLS				("perm_group_phone_calls.png"),
	PERM_GROUP_REVOKED					("perm_group_revoked.png"),
	PERM_GROUP_SCREENLOCK				("perm_group_screenlock.png"),
	PERM_GROUP_SENSORS					("perm_group_sensors.png"),
	PERM_GROUP_SHORTRANGE_NETWORK		("perm_group_shortrange_network.png"),
	PERM_GROUP_SMS						("perm_group_sms.png"),
	PERM_GROUP_SOCIAL_INFO				("perm_group_social_info.png"),
	PERM_GROUP_STATUS_BAR				("perm_group_status_bar.png"),
	PERM_GROUP_STORAGE					("perm_group_storage.png"),
	PERM_GROUP_SYNC_SETTINGS			("perm_group_sync_settings.png"),
	PERM_GROUP_SYSTEM_CLOCK				("perm_group_system_clock.png"),
	PERM_GROUP_SYSTEM_TOOLS				("perm_group_system_tools.png"),
	PERM_GROUP_UNKNOWN					("perm_group_unknown.png"),
	PERM_GROUP_USER_DICTIONARY			("perm_group_user_dictionary.png"),
	PERM_GROUP_USER_DICTIONARY_WRITE	("perm_group_user_dictionary_write.png"),
	PERM_GROUP_VOICEMAIL				("perm_group_voicemail.png"),
	PERM_GROUP_WALLPAPER				("perm_group_wallpaper.png"),

	RESOURCE_BACKGROUND			("resource_tap_image_background.jpg"),
	RESOURCE_BACKGROUND_DARK	("resource_tap_image_background_dark.jpg"),

	TREE_GLOBAL_SETTING			("configure-2.png"),
	TREE_NETWORK_SETTING		("internet-connection_manager.png"),
	TREE_CONFIG_SETTING			("kservices.png"),

	; // ENUM END

	private String value;

	private _RImg(String value) {
		this.value = value;
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
		return getURL().toExternalForm();
	}

	@Override
	public URL getURL() {
		return getClass().getResource("/icons/" + value);
	}

	@Override
	public URL get() {
		return getURL();
	}

	@Override
	public Image getImage() {
		return getImageIcon().getImage();
	}

	@Override
	public Image getImage(int w, int h) {
		return ImageScaler.getScaledImage(getImage(), w, h);
	}

	@Override
	public ImageIcon getImageIcon() {
		return new ImageIcon(getURL());
	}

	@Override
	public ImageIcon getImageIcon(int w, int h) {
		return ImageScaler.getScaledImageIcon(getImage(), w, h);
	}

	public static Image getImage(String path) {
		return getImage(_RImg.class.getResource(path));
	}

	public static Image getImage(URL url) {
		return getImageIcon(url).getImage();
	}

	public static Image getImage(String path, int w, int h) {
		return getImage(_RImg.class.getResource(path), w, h);
	}

	public static Image getImage(URL url, int w, int h) {
		return ImageScaler.getScaledImage(getImage(url), w, h);
	}

	public static ImageIcon getImageIcon(String path) {
		return new ImageIcon(_RImg.class.getResource(path));
	}

	public static ImageIcon getImageIcon(URL url) {
		return new ImageIcon(url);
	}

	public static ImageIcon getImageIcon(String path, int w, int h) {
		return getImageIcon(_RImg.class.getResource(path), w, h);
	}

	public static ImageIcon getImageIcon(URL url, int w, int h) {
		return ImageScaler.getScaledImageIcon(getImageIcon(url), w, h);
	}
}
