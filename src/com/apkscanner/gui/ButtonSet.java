package com.apkscanner.gui;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import com.apkscanner.gui.component.ExtensionButton;
import com.apkscanner.resource.RComp;
import com.apkscanner.resource.RImg;

public enum ButtonSet
{
	OPEN			(Type.NORMAL, RComp.BTN_TOOLBAR_OPEN, UiEventHandler.ACT_CMD_OPEN_APK),
	OPEN_PACKAGE	(Type.NORMAL, RComp.BTN_TOOLBAR_OPEN_PACKAGE, UiEventHandler.ACT_CMD_OPEN_PACKAGE),
	OPEN_EXTEND		(Type.EXTEND, null, RImg.TOOLBAR_OPEN_ARROW.getImageIcon(16,16)),
	MANIFEST		(Type.NORMAL, RComp.BTN_TOOLBAR_MANIFEST, UiEventHandler.ACT_CMD_SHOW_MANIFEST),
	EXPLORER		(Type.SUB_TOOLBAR, RComp.BTN_TOOLBAR_EXPLORER, UiEventHandler.ACT_CMD_SHOW_EXPLORER, true),
	OPEN_CODE		(Type.SUB_TOOLBAR, RComp.BTN_TOOLBAR_OPEN_CODE, UiEventHandler.ACT_CMD_OPEN_DECOMPILER, true),
	SEARCH			(Type.SUB_TOOLBAR, RComp.BTN_TOOLBAR_SEARCH, UiEventHandler.ACT_CMD_OPEN_SEARCHER, true),
	PLUGIN_EXTEND	(Type.SUB_TOOLBAR, RComp.BTN_TOOLBAR_PLUGIN_EXTEND, null, true),
	INSTALL			(Type.NORMAL, RComp.BTN_TOOLBAR_INSTALL, UiEventHandler.ACT_CMD_INSTALL_APK),
	INSTALL_UPDATE	(Type.NORMAL, RComp.BTN_TOOLBAR_INSTALL_UPDATE, UiEventHandler.ACT_CMD_INSTALL_APK),
	INSTALL_DOWNGRADE(Type.NORMAL, RComp.BTN_TOOLBAR_INSTALL_DOWNGRADE, UiEventHandler.ACT_CMD_INSTALL_APK),
	LAUNCH			(Type.NORMAL, RComp.BTN_TOOLBAR_LAUNCH, UiEventHandler.ACT_CMD_LAUNCH_APP),
	SIGN			(Type.NORMAL, RComp.BTN_TOOLBAR_SIGN, UiEventHandler.ACT_CMD_SIGN_APK),
	INSTALL_EXTEND	(Type.SUB_TOOLBAR, RComp.BTN_TOOLBAR_INSTALL_EXTEND, null),
	SETTING			(Type.NORMAL, RComp.BTN_TOOLBAR_SETTING, UiEventHandler.ACT_CMD_OPEN_SETTINGS),
	ABOUT			(Type.NORMAL, RComp.BTN_TOOLBAR_ABOUT, UiEventHandler.ACT_CMD_SHOW_ABOUT),

	SUB_INSTALL			(Type.SUB_TOOLBAR, RComp.BTN_TOOLBAR_SUB_INSTALL, UiEventHandler.ACT_CMD_INSTALL_APK),
	SUB_INSTALL_UPDATE	(Type.SUB_TOOLBAR, RComp.BTN_TOOLBAR_SUB_INSTALL_UPDATE, UiEventHandler.ACT_CMD_INSTALL_APK),
	SUB_INSTALL_DOWNGRADE(Type.SUB_TOOLBAR,RComp.BTN_TOOLBAR_SUB_INSTALL_DOWNGRADE, UiEventHandler.ACT_CMD_INSTALL_APK),
	SUB_LAUNCH			(Type.SUB_TOOLBAR, RComp.BTN_TOOLBAR_SUB_LAUNCH, UiEventHandler.ACT_CMD_LAUNCH_APP, true),
	SUB_SIGN			(Type.SUB_TOOLBAR, RComp.BTN_TOOLBAR_SUB_SIGN, UiEventHandler.ACT_CMD_SIGN_APK),

	NEED_TARGET_APK	(Type.NONE, (String) null, null),
	NEED_DEVICE		(Type.NONE, (String) null, null),
	ALL				(Type.NONE, (String) null, null);

	enum Type {
		NONE, NORMAL, HOVER, EXTEND, SUB_TOOLBAR
	}

	static final int SubIconSize = 16;

	Type type = null;
	RComp res = null;
	String text = null;
	String toolTipText = null;
	ImageIcon icon = null;
	ImageIcon hoverIcon = null;
	String actionCommand = null;
	boolean extension = false;

	ButtonSet(Type type, RComp res, String actCommand) {
		this.type = type;
		this.res = res;
		this.actionCommand = actCommand != null ? actCommand : getClass().getName()+"."+this.toString();
	}

	ButtonSet(Type type, RComp res, String actCommand, boolean extension) {
		this(type, res, actCommand);
		this.extension = extension;
	}

	ButtonSet(Type type, String text, ImageIcon icon)
	{
		this(type, text, null, icon, icon);
	}

	ButtonSet(Type type, String text, String toolTipText, ImageIcon icon, ImageIcon hoverIcon)
	{
		this.type = type;
		this.text = text;
		this.toolTipText = toolTipText;
		this.icon = icon;
		this.hoverIcon = hoverIcon;
		this.actionCommand = this.getClass().getName()+"."+this.toString();
	}

	ButtonSet(Type type, String text, String toolTipText, ImageIcon icon, boolean extension)
	{
		this(type, text, toolTipText, icon, icon);
		this.extension = extension;
	}

	ButtonSet(Type type, String text, String toolTipText, ImageIcon icon, ImageIcon hoverIcon, boolean extension)
	{
		this(type, text, toolTipText, icon, hoverIcon);
		this.extension = extension;
	}

	public boolean matchActionEvent(ActionEvent e)
	{
		return actionCommand.equals(e.getActionCommand());
	}

	private JButton getButton(Window owner, ActionListener listener)
	{
		if(type == Type.NONE) {
			return null;
		}

		ExtensionButton button = new ExtensionButton();
		if(res != null) {
			res.autoReapply(owner, button);
		} else {
			button.setText(text);
			button.setIcon(icon);
			button.setToolTipText(toolTipText);
		}
		button.addActionListener(listener);
		if(!"Windows".equals(UIManager.getLookAndFeel().getName())) {
			button.setBorderPainted(false);
		}
		button.setOpaque(false);
		button.setFocusable(false);
		button.setActionCommand(actionCommand);

		switch(type) {
		case NORMAL:
		case HOVER:
			button.setVerticalTextPosition(JLabel.BOTTOM);
			button.setHorizontalTextPosition(JLabel.CENTER);
			button.setPreferredSize(new Dimension(63,65));
			if(type == Type.HOVER) {
				button.setRolloverIcon(hoverIcon);
			}
			break;
		case SUB_TOOLBAR:
			button.setHorizontalAlignment(SwingConstants.LEFT);
			button.setHorizontalTextPosition(AbstractButton.RIGHT);
			button.setVerticalTextPosition(AbstractButton.CENTER);
			button.setPreferredSize(new Dimension(75,20));
			if(extension) {
				button.setArrowStyle(SwingConstants.EAST, 1, 4);
			}
			break;
		case EXTEND:
			button.setPreferredSize(new Dimension(20,65));
			button.removeActionListener(listener);
			break;
		default:
			break;
		}

		return button;
	}

	static HashMap<ButtonSet, JButton> getButtonMap(Window owner, ActionListener listener)
	{
		HashMap<ButtonSet, JButton> buttonMap = new HashMap<ButtonSet, JButton>();
		for(ButtonSet bs: values()) {
			buttonMap.put(bs, bs.getButton(owner, listener));
		}
		return buttonMap;
	}
}