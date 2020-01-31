package com.apkspectrum.resource;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public enum _RComp implements ResValue<_RComp>
{
	BTN_TOOLBAR_OPEN				(_RStr.BTN_OPEN, _RImg.TOOLBAR_OPEN, _RStr.BTN_OPEN_LAB),
	BTN_TOOLBAR_OPEN_PACKAGE		(_RStr.BTN_OPEN_PACKAGE, _RImg.TOOLBAR_PACKAGETREE, _RStr.BTN_OPEN_PACKAGE_LAB),
	BTN_TOOLBAR_MANIFEST			(_RStr.BTN_MANIFEST, _RImg.TOOLBAR_MANIFEST, _RStr.BTN_MANIFEST_LAB),
	BTN_TOOLBAR_EXPLORER			(_RStr.BTN_EXPLORER, _RImg.TOOLBAR_EXPLORER, _RStr.BTN_EXPLORER_LAB),
	BTN_TOOLBAR_OPEN_CODE			(_RStr.BTN_OPENCODE, _RImg.TOOLBAR_OPENCODE, _RStr.BTN_OPENCODE_LAB),
	BTN_TOOLBAR_OPEN_CODE_LODING	(_RStr.BTN_OPENING_CODE, _RImg.TOOLBAR_LOADING_OPEN_JD, _RStr.BTN_OPENING_CODE_LAB),
	BTN_TOOLBAR_SEARCH				(_RStr.BTN_SEARCH, _RImg.TOOLBAR_SEARCH, _RStr.BTN_SEARCH_LAB),
	BTN_TOOLBAR_PLUGIN_EXTEND		(_RStr.BTN_MORE, _RImg.TOOLBAR_OPEN_ARROW, _RStr.BTN_MORE_LAB),
	BTN_TOOLBAR_INSTALL				(_RStr.BTN_INSTALL, _RImg.TOOLBAR_INSTALL, _RStr.BTN_INSTALL_LAB),
	BTN_TOOLBAR_INSTALL_UPDATE		(_RStr.BTN_INSTALL_UPDATE, _RImg.TOOLBAR_INSTALL, _RStr.BTN_INSTALL_UPDATE_LAB),
	BTN_TOOLBAR_INSTALL_DOWNGRADE	(_RStr.BTN_INSTALL_DOWNGRAD, _RImg.TOOLBAR_INSTALL, _RStr.BTN_INSTALL_DOWNGRAD_LAB),
	BTN_TOOLBAR_LAUNCH				(_RStr.BTN_LAUNCH, _RImg.TOOLBAR_LAUNCH, _RStr.BTN_LAUNCH_LAB),
	BTN_TOOLBAR_SIGN				(_RStr.BTN_SIGN, _RImg.TOOLBAR_SIGNNING, _RStr.BTN_SIGN_LAB),
	BTN_TOOLBAR_INSTALL_EXTEND		(_RStr.BTN_MORE, _RImg.TOOLBAR_OPEN_ARROW, _RStr.BTN_MORE_LAB),
	BTN_TOOLBAR_SETTING				(_RStr.BTN_SETTING, _RImg.TOOLBAR_SETTING, _RStr.BTN_SETTING_LAB),
	BTN_TOOLBAR_ABOUT				(_RStr.BTN_ABOUT, _RImg.TOOLBAR_ABOUT, _RStr.BTN_ABOUT_LAB),

	BTN_OPEN_WITH_SYSTEM_SET		(_RStr.LABEL_OPEN_WITH_SYSTEM, _RImg.RESOURCE_TREE_OPEN_ICON, _RStr.LABEL_OPEN_WITH_SYSTEM),
	BTN_OPEN_WITH_JD_GUI			(_RStr.LABEL_OPEN_WITH_JDGUI, _RImg.RESOURCE_TREE_JD_ICON, _RStr.LABEL_OPEN_WITH_JDGUI),
	BTN_OPEN_WITH_JADX_GUI			(_RStr.LABEL_OPEN_WITH_JADXGUI, _RImg.RESOURCE_TREE_JADX_ICON, _RStr.LABEL_OPEN_WITH_JADXGUI),
	BTN_OPEN_WITH_BYTECODE_VIEWER	(_RStr.LABEL_OPEN_WITH_BYTECODE, _RImg.RESOURCE_TREE_BCV_ICON, _RStr.LABEL_OPEN_WITH_BYTECODE),
	BTN_OPEN_WITH_APK_SCANNER		(_RStr.LABEL_OPEN_WITH_SCANNER, _RImg.APP_ICON, _RStr.LABEL_OPEN_WITH_SCANNER),
	BTN_OPEN_WITH_EXPLORER			(_RStr.LABEL_OPEN_WITH_EXPLORER, _RImg.TOOLBAR_EXPLORER, _RStr.LABEL_OPEN_WITH_EXPLORER),
	BTN_OPEN_WITH_TEXTVIEWER		(_RStr.LABEL_OPEN_TO_TEXTVIEWER, _RImg.RESOURCE_TREE_OPEN_TO_TEXT, _RStr.LABEL_OPEN_TO_TEXTVIEWER),
	BTN_OPEN_WITH_CHOOSER			(_RStr.LABEL_OPEN_WITH_CHOOSE, _RImg.RESOURCE_TREE_OPEN_OTHERAPPLICATION_ICON, _RStr.LABEL_OPEN_WITH_CHOOSE),
	BTN_OPEN_WITH_LOADING			(_RStr.BTN_OPENING_CODE, _RImg.RESOURCE_TREE_OPEN_JD_LOADING, _RStr.BTN_OPENING_CODE),

	MENU_TOOLBAR_NEW_WINDOW			(_RStr.MENU_NEW),
	MENU_TOOLBAR_NEW_EMPTY			(_RStr.MENU_NEW_WINDOW, _RImg.TOOLBAR_MANIFEST.getImageIcon(16,16)),
	MENU_TOOLBAR_NEW_APK			(_RStr.MENU_NEW_APK_FILE, _RImg.TOOLBAR_OPEN.getImageIcon(16,16)),
	MENU_TOOLBAR_NEW_PACKAGE		(_RStr.MENU_NEW_PACKAGE, _RImg.TOOLBAR_PACKAGETREE.getImageIcon(16,16)),
	MENU_TOOLBAR_OPEN_APK			(_RStr.MENU_APK_FILE, _RImg.TOOLBAR_OPEN.getImageIcon(16,16)),
	MENU_TOOLBAR_OPEN_PACKAGE		(_RStr.MENU_PACKAGE, _RImg.TOOLBAR_PACKAGETREE.getImageIcon(16,16)),
	MENU_TOOLBAR_INSTALL_APK		(_RStr.MENU_INSTALL, _RImg.TOOLBAR_INSTALL.getImageIcon(16,16)),
	MENU_TOOLBAR_UNINSTALL_APK		(_RStr.MENU_UNINSTALL, _RImg.TOOLBAR_UNINSTALL.getImageIcon(16,16)),
	MENU_TOOLBAR_CLEAR_DATA			(_RStr.MENU_CLEAR_DATA, _RImg.TOOLBAR_CLEAR.getImageIcon(16,16)),
	MENU_TOOLBAR_INSTALLED_CHECK	(_RStr.MENU_CHECK_INSTALLED, _RImg.TOOLBAR_PACKAGETREE.getImageIcon(16,16)),
	MENU_TOOLBAR_DECODER_JD_GUI		(_RStr.MENU_DECODER_JD_GUI),
	MENU_TOOLBAR_DECODER_JADX_GUI	(_RStr.MENU_DECODER_JADX_GUI),
	MENU_TOOLBAR_DECODER_BYTECODE	(_RStr.MENU_DECODER_BYTECODE),
	MENU_TOOLBAR_SEARCH_RESOURCE	(_RStr.MENU_SEARCH_RESOURCE),
	MENU_TOOLBAR_EXPLORER_ARCHIVE	(_RStr.MENU_EXPLORER_ARCHIVE),
	MENU_TOOLBAR_EXPLORER_FOLDER	(_RStr.MENU_EXPLORER_FOLDER),
	MENU_TOOLBAR_LAUNCH_LAUNCHER	(_RStr.MENU_LAUNCH_LAUNCHER),
	MENU_TOOLBAR_LAUNCH_SELECT		(_RStr.MENU_LAUNCH_SELECT),
	MENU_TOOLBAR_SELECT_DEFAULT		(_RStr.MENU_SELECT_DEFAULT),
	MENU_TOOLBAR_SEARCH_BY_PACKAGE	(_RStr.LABEL_BY_PACKAGE_NAME),
	MENU_TOOLBAR_SEARCH_BY_NAME		(_RStr.LABEL_BY_APP_LABEL),
	MENU_TOOLBAR_TO_BASIC_INFO		(_RStr.MENU_VISIBLE_TO_BASIC_EACH),

	TABBED_BASIC_INFO				(_RStr.TAB_BASIC_INFO, _RStr.TAB_BASIC_INFO),
	TABBED_APEX_INFO				(_RStr.TAB_APEX_INFO, _RStr.TAB_APEX_INFO),
	TABBED_WIDGET					(_RStr.TAB_WIDGETS, _RStr.TAB_WIDGETS),
	TABBED_LIBRARIES				(_RStr.TAB_LIBRARIES, _RStr.TAB_LIBRARIES),
	TABBED_RESOURCES				(_RStr.TAB_RESOURCES, _RStr.TAB_RESOURCES),
	TABBED_COMPONENTS				(_RStr.TAB_COMPONENTS, _RStr.TAB_COMPONENTS),
	TABBED_SIGNATURES				(_RStr.TAB_SIGNATURES, _RStr.TAB_SIGNATURES),

	LABEL_XML_CONSTRUCTION			(_RStr.COMPONENT_LABEL_XML),
	COMPONENT_FILTER_PROMPT_XML		(_RStr.COMPONENT_FILTER_PROMPT_XML),
	COMPONENT_FILTER_PROMPT_NAME	(_RStr.COMPONENT_FILTER_PROMPT_NAME),
	; // ENUM END

	public static final String RCOMP_SET_TEXT_KEY = "RcompApplyText";
	public static final String RCOMP_SET_ICON_KEY = "RcompApplyIcon";

	private static Map<Window, Map<Component, _RComp>> map = new HashMap<>();
	static {
		_RStr.addLanguageChangeListener(new LanguageChangeListener() {
			@Override
			public void languageChange(String oldLang, String newLang) {
				for(Map<Component, _RComp> w: map.values()) {
					for(Entry<Component, _RComp> e: w.entrySet()) {
						e.getValue().applyText(e.getKey());
					}
				}
			}
		});
	}

	private _RStr text, toolTipText;
	private _RImg image;
	private Icon icon;
	private Dimension iconSize;

	private _RComp(_RStr text) {
		this(text, (Icon) null, null);
	}

	private _RComp(_RStr text, _RStr toolTipText) {
		this(text, (Icon) null, toolTipText);
	}

	private _RComp(_RStr text, _RImg image) {
		this(text, image, null);
	}

	private _RComp(_RStr text, Icon icon) {
		this(text, icon, null);
	}

	private _RComp(_RStr text, _RImg image, _RStr toolTipText) {
		this(text, (Icon) null, toolTipText);
		this.image = image;
	}

	private _RComp(_RStr text, Icon icon, _RStr toolTipText) {
		this.text = text;
		this.icon = icon;
		this.toolTipText = toolTipText;
	}

	@Override
	public String getValue() {
		return text != null ? text.getValue() : null;
	}

	@Override
	public _RComp get() {
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

	public _RImg getImageRes() {
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
		if(c == null) return;
		Icon icon = getIcon();
		if(icon == null) return;
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
		c.firePropertyChange(RCOMP_SET_ICON_KEY, 0, 1);
	}

	public void applyText(Component c) {
		if(c == null || (text == null && toolTipText == null)) return;
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
		c.firePropertyChange(RCOMP_SET_TEXT_KEY, 0, 1);
	}

	public void set(Component c) {
		apply(c);
		register(c);
	}

	public void register(Component c) {
		if(c == null) return;
		register(SwingUtilities.getWindowAncestor(c), c);
	}

	public void register(final Window window, final Component c) {
		if(c == null) return;

		Map<Component, _RComp> matchMap = map.get(window);
		if(matchMap == null) {
			matchMap = new HashMap<>();
			map.put(window, matchMap);
			if(window != null) {
				window.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent e) {
						window.removeWindowListener(this);
						unregister(window, null);
					}
					@Override
					public void windowClosed(WindowEvent e) {
						window.removeWindowListener(this);
						unregister(window, null);
					}
				});
			}
		} else {
			if(matchMap.containsKey(c)) matchMap.remove(c);
		}
		matchMap.put(c, this);

		if(window == null) {
			c.addHierarchyListener(new HierarchyListener() {
				@Override
				public void hierarchyChanged(HierarchyEvent e) {
					if(e.getID() != HierarchyEvent.HIERARCHY_CHANGED)
						return;
					Window win = SwingUtilities.getWindowAncestor(c);
					if(win != null) {
						unregister(null, c);
						register(win, c);
						c.removeHierarchyListener(this);
					}
				}
			});
		}
	}

	public void unregister(Window window, Component c) {
		Map<Component, _RComp> matchMap = map.get(window);
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