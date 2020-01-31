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
	// Empty
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