package com.apkscanner.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import com.apkscanner.gui.component.NoCloseCheckBoxMenuItem;
import com.apkscanner.resource.RComp;
import com.apkscanner.resource.RConst;
import com.apkscanner.resource.RProp;

enum MenuItemSet
{
	NEW_WINDOW		(RComp.MENU_TOOLBAR_NEW_WINDOW, null, null, '\0', true),
	NEW_EMPTY		(RComp.MENU_TOOLBAR_NEW_EMPTY, UiEventHandler.ACT_CMD_NEW_WINDOW, KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK, false), 'N'),
	NEW_APK			(RComp.MENU_TOOLBAR_NEW_APK, UiEventHandler.ACT_CMD_OPEN_APK_TO_NEW, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, false), 'O'),
	NEW_PACKAGE		(RComp.MENU_TOOLBAR_NEW_PACKAGE, UiEventHandler.ACT_CMD_OPEN_PACKAGE_TO_NEW, KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, false), 'P'),
	OPEN_APK		(RComp.MENU_TOOLBAR_OPEN_APK, UiEventHandler.ACT_CMD_OPEN_APK, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK, false), 'O'),
	OPEN_PACKAGE	(RComp.MENU_TOOLBAR_OPEN_PACKAGE, UiEventHandler.ACT_CMD_OPEN_PACKAGE, KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK, false), 'P'),
	INSTALL_APK		(RComp.MENU_TOOLBAR_INSTALL_APK, UiEventHandler.ACT_CMD_INSTALL_APK, KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK, false), 'I'),
	UNINSTALL_APK	(RComp.MENU_TOOLBAR_UNINSTALL_APK, UiEventHandler.ACT_CMD_UNINSTALL_APP, KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK, false), 'U'),
	CLEAR_DATA		(RComp.MENU_TOOLBAR_CLEAR_DATA, UiEventHandler.ACT_CMD_CLEAR_APP_DATA, null, '\0'),
	INSTALLED_CHECK	(RComp.MENU_TOOLBAR_INSTALLED_CHECK, UiEventHandler.ACT_CMD_SHOW_INSTALLED_PACKAGE_INFO, KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK, false), 'T'),
	DECODER_JD_GUI	(RComp.MENU_TOOLBAR_DECODER_JD_GUI, UiEventHandler.ACT_CMD_OPEN_DECOMPILER_JDGUI, null, '\0', RConst.STR_DECORDER_JD_GUI),
	DECODER_JADX_GUI(RComp.MENU_TOOLBAR_DECODER_JADX_GUI, UiEventHandler.ACT_CMD_OPEN_DECOMPILER_JADXGUI, null, '\0', RConst.STR_DECORDER_JADX_GUI),
	DECODER_BYTECODE(RComp.MENU_TOOLBAR_DECODER_BYTECODE, UiEventHandler.ACT_CMD_OPEN_DECOMPILER_BYTECODE, null, '\0', RConst.STR_DECORDER_BYTECOD),
	SEARCH_RESOURCE	(RComp.MENU_TOOLBAR_SEARCH_RESOURCE, UiEventHandler.ACT_CMD_OPEN_SEARCHER, null, '\0', RConst.STR_DEFAULT_SEARCHER),
	EXPLORER_ARCHIVE(RComp.MENU_TOOLBAR_EXPLORER_ARCHIVE, UiEventHandler.ACT_CMD_SHOW_EXPLORER_ARCHIVE, null, '\0', RConst.STR_EXPLORER_ARCHIVE),
	EXPLORER_FOLDER	(RComp.MENU_TOOLBAR_EXPLORER_FOLDER, UiEventHandler.ACT_CMD_SHOW_EXPLORER_FOLDER, null, '\0', RConst.STR_EXPLORER_FOLDER),
	LAUNCH_LAUNCHER	(RComp.MENU_TOOLBAR_LAUNCH_LAUNCHER, UiEventHandler.ACT_CMD_LAUNCH_MAIN_APP, null, '\0', RConst.STR_LAUNCH_LAUNCHER),
	LAUNCH_SELECT	(RComp.MENU_TOOLBAR_LAUNCH_SELECT, UiEventHandler.ACT_CMD_LAUNCH_CHOOSE_APP, null, '\0', RConst.STR_LAUNCH_SELECT),
	SELECT_DEFAULT	(RComp.MENU_TOOLBAR_SELECT_DEFAULT, null, null, '\0');

	private RComp res = null;
	private String text = null;
	private String toolTipText = null;
	private ImageIcon icon = null;
	private String actionCommand = null;
	private KeyStroke keyStroke = null;
	private char mnemonic = '\0';
	private boolean extend = false;

	MenuItemSet(RComp res, String actCommand, KeyStroke keyStroke, char mnemonic) {
		this.res = res;
		this.keyStroke = keyStroke;
		this.mnemonic = mnemonic;
		this.actionCommand = actCommand != null ? actCommand : getClass().getName()+"."+this.toString();
	}

	MenuItemSet(RComp res, String actCommand, KeyStroke keyStroke, char mnemonic, String propValue) {
		this(res, actCommand, keyStroke, mnemonic);
		this.actionCommand += (propValue != null ? ":" + propValue : "");
	}

	MenuItemSet(RComp res, String actCommand, KeyStroke keyStroke, char mnemonic, boolean extend) {
		this(res, actCommand, keyStroke, mnemonic);
		this.extend = extend;
	}

	MenuItemSet(String text, String toolTipText, ImageIcon icon, KeyStroke keyStroke, char mnemonic) {
		this(text, toolTipText, icon, keyStroke, mnemonic, false);
	}

	MenuItemSet(String text, String toolTipText, ImageIcon icon, KeyStroke keyStroke, char mnemonic, String propValue) {
		this(text, toolTipText, icon, keyStroke, mnemonic, false);
		this.actionCommand += (propValue != null ? ":" + propValue : "");
	}

	MenuItemSet(String text, String toolTipText, ImageIcon icon, KeyStroke keyStroke, char mnemonic, boolean extend) {
		this.text = text;
		this.toolTipText = toolTipText;
		this.icon = icon;
		this.keyStroke = keyStroke;
		this.mnemonic = mnemonic;
		this.extend = extend;
		this.actionCommand = this.getClass().getName()+"."+this.toString();
	}

	public boolean matchActionEvent(ActionEvent e)
	{
		return actionCommand.equals(e.getActionCommand());
	}

	private JMenuItem getMenuItem(final ActionListener listener)
	{
		JMenuItem menuItem = null;
		if(!extend) {
			menuItem = new JMenuItem();
			menuItem.setAccelerator(keyStroke);
		} else {
			menuItem = new JMenu();
		}

		if(res != null) {
			res.set(menuItem);
		} else {
			menuItem.setText(text);
			menuItem.setIcon(icon);
			menuItem.setToolTipText(toolTipText);
		}

		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean isSaveDefault = false;
				for(Component c: ((JMenuItem)e.getSource()).getParent().getComponents()) {
					if(c instanceof NoCloseCheckBoxMenuItem) {
						if(ToolBar.CMD_SELECT_DEFAULT_MENU.equals(((JCheckBoxMenuItem)c).getActionCommand())) {
							isSaveDefault = ((JCheckBoxMenuItem)c).isSelected();
						}
					}
				}
				String[] value = e.getActionCommand().split(":");
				if(isSaveDefault && value.length > 1) {
					if(DECODER_JD_GUI.matchActionEvent(e)
						|| DECODER_JADX_GUI.matchActionEvent(e)
						|| DECODER_BYTECODE.matchActionEvent(e)) {
						RProp.DEFAULT_DECORDER.setData(value[1]);
					} else if(LAUNCH_LAUNCHER.matchActionEvent(e)
						|| LAUNCH_SELECT.matchActionEvent(e)) {
						RProp.DEFAULT_LAUNCH_MODE.setData(value[1]);
					} else if(EXPLORER_ARCHIVE.matchActionEvent(e)
						|| EXPLORER_FOLDER.matchActionEvent(e)) {
						RProp.DEFAULT_EXPLORER.setData(value[1]);
					} else if(SEARCH_RESOURCE.matchActionEvent(e)) {
						RProp.DEFAULT_SEARCHER.setData(value[1]);
					}
				}
				listener.actionPerformed(new ActionEvent(e.getSource(), e.getID(), value[0], e.getWhen(), 0));
			}
		});
		menuItem.setActionCommand(actionCommand);

		menuItem.setMnemonic(mnemonic);
		return menuItem;
	}

	static Map<MenuItemSet, JMenuItem> getButtonMap(ActionListener listener)
	{
		Map<MenuItemSet, JMenuItem> menuItemMap = new HashMap<MenuItemSet, JMenuItem>();
		for(MenuItemSet bs: values()) {
			menuItemMap.put(bs, bs.getMenuItem(listener));
		}
		return menuItemMap;
	}
}