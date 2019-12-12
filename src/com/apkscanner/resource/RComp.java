package com.apkscanner.resource;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;

public enum RComp implements ResValue<RComp>
{
	BTN_TOOLBAR_OPEN				(RStr.BTN_OPEN, RImg.TOOLBAR_OPEN, RStr.BTN_OPEN_LAB),
	BTN_TOOLBAR_OPEN_PACKAGE		(RStr.BTN_OPEN_PACKAGE, RImg.TOOLBAR_PACKAGETREE, RStr.BTN_OPEN_PACKAGE_LAB),
	BTN_TOOLBAR_MANIFEST			(RStr.BTN_MANIFEST, RImg.TOOLBAR_MANIFEST, RStr.BTN_MANIFEST_LAB),
	BTN_TOOLBAR_EXPLORER			(RStr.BTN_EXPLORER, RImg.TOOLBAR_EXPLORER, RStr.BTN_EXPLORER_LAB),
	BTN_TOOLBAR_OPEN_CODE			(RStr.BTN_OPENCODE, RImg.TOOLBAR_OPENCODE, RStr.BTN_OPENCODE_LAB),
	BTN_TOOLBAR_OPEN_CODE_LODING	(RStr.BTN_OPENING_CODE, RImg.TOOLBAR_LOADING_OPEN_JD, RStr.BTN_OPENING_CODE_LAB),
	BTN_TOOLBAR_SEARCH				(RStr.BTN_SEARCH, RImg.TOOLBAR_SEARCH, RStr.BTN_SEARCH_LAB),
	BTN_TOOLBAR_PLUGIN_EXTEND		(RStr.BTN_MORE, RImg.TOOLBAR_OPEN_ARROW, RStr.BTN_MORE_LAB),
	BTN_TOOLBAR_INSTALL				(RStr.BTN_INSTALL, RImg.TOOLBAR_INSTALL, RStr.BTN_INSTALL_LAB),
	BTN_TOOLBAR_INSTALL_UPDATE		(RStr.BTN_INSTALL_UPDATE, RImg.TOOLBAR_INSTALL, RStr.BTN_INSTALL_UPDATE_LAB),
	BTN_TOOLBAR_INSTALL_DOWNGRADE	(RStr.BTN_INSTALL_DOWNGRAD, RImg.TOOLBAR_INSTALL, RStr.BTN_INSTALL_DOWNGRAD_LAB),
	BTN_TOOLBAR_LAUNCH				(RStr.BTN_LAUNCH, RImg.TOOLBAR_LAUNCH, RStr.BTN_LAUNCH_LAB),
	BTN_TOOLBAR_SIGN				(RStr.BTN_SIGN, RImg.TOOLBAR_SIGNNING, RStr.BTN_SIGN_LAB),
	BTN_TOOLBAR_INSTALL_EXTEND		(RStr.BTN_MORE, RImg.TOOLBAR_OPEN_ARROW, RStr.BTN_MORE_LAB),
	BTN_TOOLBAR_SETTING				(RStr.BTN_SETTING, RImg.TOOLBAR_SETTING, RStr.BTN_SETTING_LAB),
	BTN_TOOLBAR_ABOUT				(RStr.BTN_ABOUT, RImg.TOOLBAR_ABOUT, RStr.BTN_ABOUT_LAB),

	MENU_TOOLBAR_NEW_WINDOW			(RStr.MENU_NEW),
	MENU_TOOLBAR_NEW_EMPTY			(RStr.MENU_NEW_WINDOW, RImg.TOOLBAR_MANIFEST.getImageIcon(16,16)),
	MENU_TOOLBAR_NEW_APK			(RStr.MENU_NEW_APK_FILE, RImg.TOOLBAR_OPEN.getImageIcon(16,16)),
	MENU_TOOLBAR_NEW_PACKAGE		(RStr.MENU_NEW_PACKAGE, RImg.TOOLBAR_PACKAGETREE.getImageIcon(16,16)),
	MENU_TOOLBAR_OPEN_APK			(RStr.MENU_APK_FILE, RImg.TOOLBAR_OPEN.getImageIcon(16,16)),
	MENU_TOOLBAR_OPEN_PACKAGE		(RStr.MENU_PACKAGE, RImg.TOOLBAR_PACKAGETREE.getImageIcon(16,16)),
	MENU_TOOLBAR_INSTALL_APK		(RStr.MENU_INSTALL, RImg.TOOLBAR_INSTALL.getImageIcon(16,16)),
	MENU_TOOLBAR_UNINSTALL_APK		(RStr.MENU_UNINSTALL, RImg.TOOLBAR_UNINSTALL.getImageIcon(16,16)),
	MENU_TOOLBAR_CLEAR_DATA			(RStr.MENU_CLEAR_DATA, RImg.TOOLBAR_CLEAR.getImageIcon(16,16)),
	MENU_TOOLBAR_INSTALLED_CHECK	(RStr.MENU_CHECK_INSTALLED, RImg.TOOLBAR_PACKAGETREE.getImageIcon(16,16)),
	MENU_TOOLBAR_DECODER_JD_GUI		(RStr.MENU_DECODER_JD_GUI),
	MENU_TOOLBAR_DECODER_JADX_GUI	(RStr.MENU_DECODER_JADX_GUI),
	MENU_TOOLBAR_DECODER_BYTECODE	(RStr.MENU_DECODER_BYTECODE),
	MENU_TOOLBAR_SEARCH_RESOURCE	(RStr.MENU_SEARCH_RESOURCE),
	MENU_TOOLBAR_EXPLORER_ARCHIVE	(RStr.MENU_EXPLORER_ARCHIVE),
	MENU_TOOLBAR_EXPLORER_FOLDER	(RStr.MENU_EXPLORER_FOLDER),
	MENU_TOOLBAR_LAUNCH_LAUNCHER	(RStr.MENU_LAUNCH_LAUNCHER),
	MENU_TOOLBAR_LAUNCH_SELECT		(RStr.MENU_LAUNCH_SELECT),
	MENU_TOOLBAR_SELECT_DEFAULT		(RStr.MENU_SELECT_DEFAULT),
	MENU_TOOLBAR_SEARCH_BY_PACKAGE	(RStr.LABEL_BY_PACKAGE_NAME),
	MENU_TOOLBAR_SEARCH_BY_NAME		(RStr.LABEL_BY_APP_LABEL),
	MENU_TOOLBAR_TO_BASIC_INFO		(RStr.MENU_VISIBLE_TO_BASIC_EACH),
	; // ENUM END


	private static Map<Window, Map<Component, RComp>> map = new HashMap<>();
	static {
		RStr.addLanguageChangeListener(new LanguageChangeListener() {
			@Override
			public void languageChange(String oldLang, String newLang) {
				for(Entry<Window, Map<Component, RComp>> w: map.entrySet()) {
					for(Entry<Component, RComp> e: w.getValue().entrySet()) {
						e.getValue().applyText(e.getKey());
					}
				}
			}
		});
	}

	private RStr text, toolTipText;
	private RImg image;
	private Icon icon;
	private Dimension iconSize;

	private RComp(RStr text) {
		this(text, (Icon) null, null);
	}

	private RComp(RStr text, Icon icon) {
		this(text, icon, null);
	}

	private RComp(RStr text, RImg image) {
		this(text, (Icon) null, null);
		this.image = image;
	}

	private RComp(RStr text, Icon icon, RStr toolTipText) {
		this.text = text;
		this.icon = icon;
		this.toolTipText = toolTipText;
	}

	private RComp(RStr text, RImg image, RStr toolTipText) {
		this(text, (Icon) null, toolTipText);
		this.image = image;
	}

	@Override
	public String getValue() {
		return text != null ? text.getValue() : null;
	}

	@Override
	public RComp get() {
		return this;
	}

	public String getText() {
		return text != null ? text.getString() : null;
	}

	public Icon getIcon() {
		if(image == null) return icon;
		if(iconSize == null) return image.getImageIcon();
		return image.getImageIcon(iconSize.width, iconSize.height);
	}

	public String getToolTipText() {
		return toolTipText != null ? toolTipText.getString() : null;
	}

	public RImg getImageRes() {
		return image;
	}

	public void setImageSize(Dimension iconSize) {
		this.iconSize = iconSize;
	}

	public void apply(Component c) {
		applyText(c);
		applyIcon(c);
	}

	public void applyIcon(Component c) {
		Icon icon = getIcon();
		if(icon != null) {
			if(c instanceof AbstractButton) {
				AbstractButton btn = (AbstractButton) c;
				if(c.isEnabled()) {
					btn.setIcon(icon);
					btn.setDisabledIcon(null);
				} else {
					btn.setDisabledIcon(icon);
				}
			} else if(c instanceof JLabel) {
				((JLabel) c).setIcon(icon);
			}
		}
	}

	public void applyText(Component c) {
		if(text != null) {
			if(c instanceof AbstractButton) {
				((AbstractButton) c).setText(text.getString());
			} else if(c instanceof JLabel) {
				((JLabel) c).setText(text.getString());
			} else {
				c.setName(text.getString());
			}
		}
		if(c instanceof JComponent && toolTipText != null) {
			((JComponent)c).setToolTipText(toolTipText.getString());
		}
	}

	public void autoReapply(Window window, Component c) {
		autoReapply(window, c, true);
	}

	public void autoReapply(Window window, Component c, boolean useAutoReapply) {
		if(c == null) return;
		apply(c);
		if(useAutoReapply) {
			registeReapply(window, c);
		} else {
			removeReapply(window, c);
		}
	}

	public void registeReapply(final Window window, Component c) {
		if(c == null) return;
		Map<Component, RComp> matchMap = map.get(window);
		if(matchMap == null) {
			matchMap = new HashMap<>();
			map.put(window, matchMap);
			if(window != null) {
				window.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent e) {
						window.removeWindowListener(this);
						removeReapply(window, null);
					}
					@Override
					public void windowClosed(WindowEvent e) {
						window.removeWindowListener(this);
						removeReapply(window, null);
					}
				});
			}
		} else {
			if(matchMap.containsKey(c)) matchMap.remove(c);
		}
		matchMap.put(c, this);
	}

	public void removeReapply(Window window, Component c) {
		Map<Component, RComp> matchMap = map.get(window);
		if(matchMap != null) {
			if(c != null) {
				matchMap.remove(c);
			}
			if(matchMap.isEmpty() || c == null) {
				map.remove(window);
			}
		}
	}
}