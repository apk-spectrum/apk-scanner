package com.apkscanner.resource;

import java.awt.Image;
import java.net.URL;

import javax.swing.ImageIcon;

import com.apkspectrum.resource.ResImage;
import com.apkspectrum.swing.ImageScaler;

public enum RImg implements ResImage<Image>
{
	TOOLBAR_OPEN				("toolbar_open.png"),
	TOOLBAR_MANIFEST			("toolbar_manifast.png"),
	TOOLBAR_EXPLORER			("toolbar_explorer.png"),
	TOOLBAR_INSTALL				("toolbar_install.png"),
	TOOLBAR_ABOUT				("toolbar_about.png"),
	TOOLBAR_SETTING				("toolbar_setting.png"),
	TOOLBAR_OPENCODE			("toolbar_opencode.png"),
	TOOLBAR_SEARCH				("toolbar_search.png"),
	TOOLBAR_SIGNNING			("toolbar_signning.png"),
	TOOLBAR_LOADING_OPEN_JD		("Loading_openJD_16_16.gif"),
	TOOLBAR_PACKAGETREE			("toolbar_packagetree.png"),
	TOOLBAR_LAUNCH				("toolbar_launch.png"),
	TOOLBAR_UNINSTALL			("toolbar_uninstall.png"),
	TOOLBAR_CLEAR				("toolbar_clear.png"),

	RESOURCE_TREE_XML			("resource_tab_tree_xml.gif"),
	RESOURCE_TREE_CODE			("resource_tab_tree_code.png"),
	RESOURCE_TREE_ARSC			("resource_tab_tree_arsc.png"),
	RESOURCE_TREE_OPEN_JD		("Loading_openJD_16_16.gif"),
	RESOURCE_TREE_JD_ICON		("resource_tab_JD.png"),
	RESOURCE_TREE_JADX_ICON		("javaicon.png"),
	RESOURCE_TREE_BCV_ICON		("BCVIcon.png"),
	RESOURCE_TREE_OPEN_ICON		("resource_tab_open.png"),
	RESOURCE_TREE_OPEN_OTHERAPPLICATION_ICON ("resource_tab_otherapplication.png"),
	RESOURCE_TREE_OPEN_JD_LOADING("Loading_openJD_80_80.gif"),
	RESOURCE_TREE_OPEN_TO_TEXT	("resource_tab_open_to_textviewer.png"),
	RESOURCE_TREE_TOOLBAR_REFRESH("resource_tab_tree_toolbar_refresh.png"),

	RESOURCE_TEXTVIEWER_TOOLBAR_OPEN ("ResourceTab_TextViewer_toolbar_open.png"),
	RESOURCE_TEXTVIEWER_TOOLBAR_SAVE ("ResourceTab_TextViewer_toolbar_save.png"),
	RESOURCE_TEXTVIEWER_TOOLBAR_FIND ("ResourceTab_TextViewer_toolbar_find.png"),
	RESOURCE_TEXTVIEWER_TOOLBAR_NEXT ("ResourceTab_TextViewer_toolbar_next.png"),
	RESOURCE_TEXTVIEWER_TOOLBAR_PREV ("ResourceTab_TextViewer_toolbar_previous.png"),
	RESOURCE_TEXTVIEWER_TOOLBAR_INDENT("ResourceTab_TextViewer_toolbar_text_indent.png"),

	TOOLBAR_OPEN_ARROW			("down_on.png"),

	APP_ICON					("AppIcon.png"),
	APK_FILE_ICON				("apk_file_icon.png"),
	QUESTION					("question.png"),
	WARNING						("warning.png"),
	WARNING2					("warning2.png"),
	INSTALL_WAIT				("install_wait.gif"),
	LOADING						("loading.gif"),
	APK_LOGO					("Logo.png"),
	WAIT_BAR					("wait_bar.gif"),
	USB_ICON					("ic_dialog_usb.png"),
	DEF_APP_ICON				("sym_def_app_icon.png"),
	QMG_IMAGE_ICON				("qmg_not_suporrted.png"),
	RESULT_SUCCESS				("result_success.png"),
	RESULT_FAIL					("result_fail.png"),

	TREE_MENU_LINK				("tree_link_menu.png"),
	TREE_MENU_CLEARDATA			("tree_icon_clear.png"),
	TREE_MENU_DELETE			("tree_menu_delete.png"),
	TREE_MENU_SAVE				("tree_menu_save.png"),
	TREE_MENU_OPEN				("tree_open_menu.png"),

	TREE_APK					("tree_icon_apk.png"),
	TREE_DEVICE					("tree_icon_device.png"),
	TREE_TOP					("tree_icon_top.gif"),
	TREE_FOLDER					("tree_icon_folder.png"),
	TREE_LOADING				("tree_loading.gif"),
	TREE_FAVOR					("tree_favor.png"),

	INSTALL_CHECK				("install_dlg_check.png"),
	INSTALL_BLOCK				("install_dlg_block.png"),
	INSTALL_LOADING				("install_dlg_loading.gif"),

	ADD_TO_DESKTOP				("add-to-desktop.png"),
	ASSOCIATE_APK				("associate.png"),
	UNASSOCIATE_APK				("unassociate.png"),

	//easy gui
	EASY_WINDOW_EXIT			("easy_gui_exit.png"),
	EASY_WINDOW_MINI			("easy_gui_mini.png"),
	EASY_WINDOW_SETTING			("easy_gui_setting.png"),
	EASY_WINDOW_CLIPBOARD_ICON	("easy_gui_clipboard.png"),

	EASY_WINDOW_ALLOW			("easy_gui_allow.png"),

	//http://chittagongit.com/icon/list-icon-png-10.html
	EASY_WINDOW_PERMISSION_ICON	("easy_gui_permission_icon.png"),

	//https://icon-icons.com/zh/%E5%9B%BE%E6%A0%87/%E7%94%B5%E8%AF%9D-iphone/89813#48
	EASY_WINDOW_DEVICE			("easy_gui_device.png"),
	EASY_WINDOW_DRAGANDDROP		("easy_gui_draganddrop.png"),

	//https://www.iconfinder.com/icons/686665/arrows_enlarge_expand_extend_increase_outward_resize_spread_icon
	EASY_WINDOW_SPREAD			("easy_spread.png"),

	PREVIEW_EASY				("preview_easymode.png"),
	PREVIEW_EASY1				("preview_easymode1.png"),
	PREVIEW_EASY2				("preview_easymode2.png"),
	PREVIEW_EASY3				("preview_easymode3.png"),

	PREVIEW_ORIGINAL			("preview_original.png"),

	PERM_MARKER_SETTING			("perm_marker_setting.png"),
	PERM_MARKER_CLOSE			("perm_marker_close.png"),
	; // ENUM END

	private String value;

	private RImg(String value) {
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
	public Image get() {
		return getImage();
	}

	@Override
	public Image getImage() {
		return getImageIcon().getImage();
	}

	@Override
	public Image getImage(int w, int h) {
		return ImageScaler.getScaledImage(getImage(), w, h);
	}

	public ImageIcon getImageIcon() {
		return new ImageIcon(getURL());
	}

	public ImageIcon getImageIcon(int w, int h) {
		return ImageScaler.getScaledImageIcon(getImage(), w, h);
	}

	public static Image getImage(String path) {
		return getImage(RImg.class.getResource(path));
	}

	public static Image getImage(URL url) {
		return getImageIcon(url).getImage();
	}

	public static Image getImage(String path, int w, int h) {
		return getImage(RImg.class.getResource(path), w, h);
	}

	public static Image getImage(URL url, int w, int h) {
		return ImageScaler.getScaledImage(getImage(url), w, h);
	}

	public static ImageIcon getImageIcon(String path) {
		return new ImageIcon(RImg.class.getResource(path));
	}

	public static ImageIcon getImageIcon(URL url) {
		return new ImageIcon(url);
	}

	public static ImageIcon getImageIcon(String path, int w, int h) {
		return getImageIcon(RImg.class.getResource(path), w, h);
	}

	public static ImageIcon getImageIcon(URL url, int w, int h) {
		return ImageScaler.getScaledImageIcon(getImageIcon(url), w, h);
	}
}
