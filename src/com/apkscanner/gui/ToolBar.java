package com.apkscanner.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.MatteBorder;

import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

public class ToolBar extends JToolBar
{
	private static final long serialVersionUID = 894134416480807167L;

	public static final int FLAG_LAYOUT_NONE = 0x00;				// Open file
	public static final int FLAG_LAYOUT_DEVICE_CONNECTED = 0x01;	// Open package
	public static final int FLAG_LAYOUT_INSTALLED = 0x02;			// Install 
	public static final int FLAG_LAYOUT_INSTALLED_LOWER = 0x04;		// Downgrade
	public static final int FLAG_LAYOUT_INSTALLED_UPPER = 0x08;		// Update
	public static final int FLAG_LAYOUT_LAUNCHER = 0x10;			// Launcher
	public static final int FLAG_LAYOUT_UNSIGNED = 0x20;			// Sign
	public static final int FLAG_LAYOUT_NO_SUCH_CLASSES = 0x40;

	public static final int FLAG_LAYOUT_INSTALLED_MASK = FLAG_LAYOUT_INSTALLED | FLAG_LAYOUT_INSTALLED_LOWER | FLAG_LAYOUT_INSTALLED_UPPER | FLAG_LAYOUT_LAUNCHER;

	private int flag = 0;
	private boolean hasTargetApk = false;
	private boolean hasDevice = false;

	private HashMap<ButtonSet, JButton> buttonMap;
	private HashMap<MenuItemSet, JMenuItem> menuItemMap;
	private JPopupMenu openPopupMenu;
	private JPopupMenu installPopupMenu;

	public enum MenuItemSet
	{
		NEW_WINDOW		(Resource.STR_MENU_NEW.getString(), null, null, null, '\0', true),
		NEW_EMPTY		(Resource.STR_MENU_NEW_WINDOW.getString(), null, Resource.IMG_TOOLBAR_MANIFEST.getImageIcon(16,16), KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK, false), 'N'),
		NEW_APK			(Resource.STR_MENU_NEW_APK_FILE.getString(), null, Resource.IMG_TOOLBAR_OPEN.getImageIcon(16,16), KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_MASK, false), 'O'),
		NEW_PACKAGE		(Resource.STR_MENU_NEW_PACKAGE.getString(), null, Resource.IMG_TOOLBAR_PACKAGETREE.getImageIcon(16,16), KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_MASK, false), 'P'),
		OPEN_APK		(Resource.STR_MENU_APK_FILE.getString(), null, Resource.IMG_TOOLBAR_OPEN.getImageIcon(16,16), KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK, false), 'O'),
		OPEN_PACKAGE	(Resource.STR_MENU_PACKAGE.getString(), null, Resource.IMG_TOOLBAR_PACKAGETREE.getImageIcon(16,16), KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK, false), 'P'),
		INSTALL_APK		(Resource.STR_MENU_INSTALL.getString(), null, Resource.IMG_TOOLBAR_INSTALL.getImageIcon(16,16), KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK, false), 'I'),
		UNINSTALL_APK	(Resource.STR_MENU_UNINSTALL.getString(), null, Resource.IMG_TOOLBAR_UNINSTALL.getImageIcon(16,16), KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK, false), 'U'),
		INSTALLED_CHECK	(Resource.STR_MENU_CHECK_INSTALLED.getString(), null, Resource.IMG_TOOLBAR_PACKAGETREE.getImageIcon(16,16), KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK, false), 'T');

		private String text = null;
		private String toolTipText = null;
		private ImageIcon icon = null;
		private String actionCommand = null;
		private KeyStroke keyStroke = null;
		private char mnemonic = '\0';
		private boolean extend = false;

		MenuItemSet(String text, String toolTipText, ImageIcon icon, KeyStroke keyStroke, char mnemonic) {
			this(text, toolTipText, icon, keyStroke, mnemonic, false);
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

		private JMenuItem getMenuItem(ActionListener listener)
		{
			JMenuItem menuItem = null;
			if(!extend) {
				menuItem = new JMenuItem(text);
				menuItem.setAccelerator(keyStroke);
			} else {
				menuItem = new JMenu(text);
			}
			menuItem.addActionListener(listener);
			menuItem.setActionCommand(actionCommand);
			menuItem.setIcon(icon);
			menuItem.setToolTipText(toolTipText);

			menuItem.setMnemonic(mnemonic);
			return menuItem;
		}

		static private HashMap<MenuItemSet, JMenuItem> getButtonMap(ActionListener listener)
		{
			HashMap<MenuItemSet, JMenuItem> menuItemMap = new HashMap<MenuItemSet, JMenuItem>();
			for(MenuItemSet bs: values()) {
				menuItemMap.put(bs, bs.getMenuItem(listener));
			}
			return menuItemMap;
		}
	}

	public enum ButtonSet
	{
		OPEN			(Type.NORMAL, Resource.STR_BTN_OPEN.getString(), Resource.STR_BTN_OPEN_LAB.getString(), Resource.IMG_TOOLBAR_OPEN.getImageIcon(ButtonSet.IconSize, ButtonSet.IconSize)),
		OPEN_PACKAGE	(Type.NORMAL, Resource.STR_BTN_OPEN_PACKAGE.getString(), Resource.STR_BTN_OPEN_PACKAGE_LAB.getString(), Resource.IMG_TOOLBAR_PACKAGETREE.getImageIcon(ButtonSet.IconSize,ButtonSet.IconSize)),
		OPEN_EXTEND		(Type.EXTEND, null, Resource.IMG_TOOLBAR_OPEN_ARROW.getImageIcon(16,16)),
		MANIFEST		(Type.NORMAL, Resource.STR_BTN_MANIFEST.getString(), Resource.STR_BTN_MANIFEST_LAB.getString(), Resource.IMG_TOOLBAR_MANIFEST.getImageIcon(ButtonSet.IconSize, ButtonSet.IconSize)),
		EXPLORER		(Type.NORMAL, Resource.STR_BTN_EXPLORER.getString(), Resource.STR_BTN_EXPLORER_LAB.getString(), Resource.IMG_TOOLBAR_EXPLORER.getImageIcon(ButtonSet.IconSize, ButtonSet.IconSize)),
		OPEN_CODE		(Type.SUB_TOOLBAR, Resource.STR_BTN_OPENCODE.getString(), Resource.STR_BTN_OPENCODE_LAB.getString(), Resource.IMG_TOOLBAR_OPENCODE.getImageIcon(ButtonSet.SubIconSize, ButtonSet.SubIconSize)),
		SEARCH			(Type.SUB_TOOLBAR, Resource.STR_BTN_SEARCH.getString(), Resource.STR_BTN_SEARCH_LAB.getString(), Resource.IMG_TOOLBAR_SEARCH.getImageIcon(ButtonSet.SubIconSize, ButtonSet.SubIconSize)),
		INSTALL			(Type.NORMAL, Resource.STR_BTN_INSTALL.getString(), Resource.STR_BTN_INSTALL_LAB.getString(), Resource.IMG_TOOLBAR_INSTALL.getImageIcon(ButtonSet.IconSize, ButtonSet.IconSize)),
		INSTALL_UPDATE	(Type.NORMAL, Resource.STR_BTN_INSTALL_UPDATE.getString(), Resource.STR_BTN_INSTALL_UPDATE_LAB.getString(), Resource.IMG_TOOLBAR_INSTALL.getImageIcon(ButtonSet.IconSize, ButtonSet.IconSize)),
		INSTALL_DOWNGRADE(Type.NORMAL, Resource.STR_BTN_INSTALL_DOWNGRAD.getString(), Resource.STR_BTN_INSTALL_DOWNGRAD_LAB.getString(), Resource.IMG_TOOLBAR_INSTALL.getImageIcon(ButtonSet.IconSize, ButtonSet.IconSize)),
		LAUNCH			(Type.NORMAL, Resource.STR_BTN_LAUNCH.getString(), Resource.STR_BTN_LAUNCH_LAB.getString(), Resource.IMG_TOOLBAR_LAUNCH.getImageIcon(ButtonSet.IconSize, ButtonSet.IconSize)),
		SIGN			(Type.NORMAL, Resource.STR_BTN_SIGN.getString(), Resource.STR_BTN_SIGN_LAB.getString(), Resource.IMG_TOOLBAR_SIGNNING.getImageIcon(ButtonSet.IconSize, ButtonSet.IconSize)),
		INSTALL_EXTEND	(Type.SUB_TOOLBAR, Resource.STR_BTN_MORE.getString(), Resource.STR_BTN_MORE_LAB.getString(), Resource.IMG_TOOLBAR_OPEN_ARROW.getImageIcon(16,16)),
		SETTING			(Type.NORMAL, Resource.STR_BTN_SETTING.getString(), Resource.STR_BTN_SETTING_LAB.getString(), Resource.IMG_TOOLBAR_SETTING.getImageIcon(ButtonSet.IconSize, ButtonSet.IconSize)),
		ABOUT			(Type.NORMAL, Resource.STR_BTN_ABOUT.getString(), Resource.STR_BTN_ABOUT_LAB.getString(), Resource.IMG_TOOLBAR_ABOUT.getImageIcon(ButtonSet.IconSize, ButtonSet.IconSize)),

		SUB_INSTALL			(Type.SUB_TOOLBAR, Resource.STR_BTN_INSTALL.getString(), Resource.STR_BTN_INSTALL_LAB.getString(), Resource.IMG_TOOLBAR_INSTALL.getImageIcon(ButtonSet.SubIconSize, ButtonSet.SubIconSize)),
		SUB_INSTALL_UPDATE	(Type.SUB_TOOLBAR, Resource.STR_BTN_INSTALL_UPDATE.getString(), Resource.STR_BTN_INSTALL_UPDATE_LAB.getString(), Resource.IMG_TOOLBAR_INSTALL.getImageIcon(ButtonSet.SubIconSize, ButtonSet.SubIconSize)),
		SUB_INSTALL_DOWNGRADE(Type.SUB_TOOLBAR, Resource.STR_BTN_INSTALL_DOWNGRAD.getString(), Resource.STR_BTN_INSTALL_DOWNGRAD_LAB.getString(), Resource.IMG_TOOLBAR_INSTALL.getImageIcon(ButtonSet.SubIconSize, ButtonSet.SubIconSize)),
		SUB_LAUNCH			(Type.SUB_TOOLBAR, Resource.STR_BTN_LAUNCH.getString(), Resource.STR_BTN_LAUNCH_LAB.getString(), Resource.IMG_TOOLBAR_LAUNCH.getImageIcon(ButtonSet.SubIconSize, ButtonSet.SubIconSize)),
		SUB_SIGN			(Type.SUB_TOOLBAR, Resource.STR_BTN_SIGN.getString(), Resource.STR_BTN_SIGN_LAB.getString(), Resource.IMG_TOOLBAR_SIGNNING.getImageIcon(ButtonSet.SubIconSize, ButtonSet.SubIconSize)),

		NEED_TARGET_APK	(Type.NONE, null, null),
		NEED_DEVICE		(Type.NONE, null, null),
		ALL				(Type.NONE, null, null);

		private enum Type {
			NONE, NORMAL, HOVER, EXTEND, SUB_TOOLBAR
		}

		static private final int IconSize = 40;
		static private final int SubIconSize = 16;

		private Type type = null;
		private String text = null;
		private String toolTipText = null;
		private ImageIcon icon = null;
		private ImageIcon hoverIcon = null;
		private String actionCommand = null;

		ButtonSet(Type type, String text, ImageIcon icon)
		{
			this(type, text, null, icon, icon);
		}

		ButtonSet(Type type, String text, String toolTipText, ImageIcon icon)
		{
			this(type, text, toolTipText, icon, icon);
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

		public boolean matchActionEvent(ActionEvent e)
		{
			return actionCommand.equals(e.getActionCommand());
		}

		private JButton getButton(ActionListener listener)
		{
			if(type == Type.NONE) {
				return null;
			}

			JButton button = new JButton(text, icon);
			button.setToolTipText(toolTipText);
			button.addActionListener(listener);
			button.setBorderPainted(false);
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
				button.setPreferredSize(new Dimension(68,20));
				button.setHorizontalAlignment(SwingConstants.LEFT);
				button.setHorizontalTextPosition(AbstractButton.RIGHT);
				button.setVerticalTextPosition(AbstractButton.CENTER);
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

		static private HashMap<ButtonSet, JButton> getButtonMap(ActionListener listener)
		{
			HashMap<ButtonSet, JButton> buttonMap = new HashMap<ButtonSet, JButton>();
			for(ButtonSet bs: values()) {
				buttonMap.put(bs, bs.getButton(listener));
			}
			return buttonMap;
		}
	}

	public ToolBar(ActionListener listener)
	{
		initUI(listener);
	}

	public final void initUI(ActionListener listener)
	{
		Log.i("ToolBar.initUI() start");
		setOpaque(true);
		setFloatable(false);
		setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
		setBorder(new MatteBorder(0,0,1,0,Color.LIGHT_GRAY));

		openPopupMenu = new JPopupMenu();
		installPopupMenu = new JPopupMenu();

		Log.i("ToolBar.initUI() MenuItemSet init");
		menuItemMap = MenuItemSet.getButtonMap(listener);

		JMenuItem SubMenu = openPopupMenu.add((JMenu)menuItemMap.get(MenuItemSet.NEW_WINDOW));
		SubMenu.add(menuItemMap.get(MenuItemSet.NEW_EMPTY));
		SubMenu.add(menuItemMap.get(MenuItemSet.NEW_APK));
		SubMenu.add(menuItemMap.get(MenuItemSet.NEW_PACKAGE));
		openPopupMenu.add(menuItemMap.get(MenuItemSet.OPEN_APK));
		openPopupMenu.add(menuItemMap.get(MenuItemSet.OPEN_PACKAGE));

		installPopupMenu.add(menuItemMap.get(MenuItemSet.UNINSTALL_APK));
		installPopupMenu.add(menuItemMap.get(MenuItemSet.INSTALLED_CHECK));

		Log.i("ToolBar.initUI() ButtonSet init");
		buttonMap = ButtonSet.getButtonMap(listener);
		buttonMap.get(ButtonSet.OPEN).setPreferredSize(new Dimension(55,65));
		buttonMap.get(ButtonSet.OPEN_PACKAGE).setPreferredSize(new Dimension(55,65));

		buttonMap.get(ButtonSet.OPEN_EXTEND).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				JButton btn = buttonMap.get(ButtonSet.OPEN_EXTEND);
				openPopupMenu.show(btn, 0, btn.getHeight());
			}
		});

		buttonMap.get(ButtonSet.INSTALL_EXTEND).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				JButton btn = buttonMap.get(ButtonSet.INSTALL_EXTEND);
				installPopupMenu.show(btn, 0, btn.getHeight());
			}
		});

		setReplacementLayout();

		Log.i("ToolBar.initUI() end");
	}

	public void setFlag(int flag) {
		int preFlag = this.flag;
		if(flag != FLAG_LAYOUT_DEVICE_CONNECTED) {
			this.flag &= ~FLAG_LAYOUT_INSTALLED_MASK;
		}
		this.flag |= flag;
		Log.v("setFlag() preFlag " + Integer.toHexString(preFlag) + ", newFlag " + Integer.toHexString(this.flag));
		if(preFlag != this.flag) {
			setReplacementLayout();
		}
	}

	public void unsetFlag(int flag) {
		int preFlag = this.flag;
		if(flag == FLAG_LAYOUT_DEVICE_CONNECTED
				|| (FLAG_LAYOUT_INSTALLED_MASK & flag) != 0) {
			this.flag &= ~FLAG_LAYOUT_INSTALLED_MASK;
		}
		this.flag &= ~flag;
		if(preFlag != this.flag) {
			setReplacementLayout();
		}
	}

	public void clearFlag() {
		if(flag != 0) {
			flag = 0;
			setReplacementLayout();
		}
	}

	public boolean isSetFlag(int flag) {
		return ((this.flag & flag) == flag);
	}

	private JToolBar makeSubToolBar() {
		JToolBar subbar = new JToolBar();
		subbar.setPreferredSize(new Dimension(69,60));
		subbar.setOpaque(false);
		subbar.setFloatable(false);
		subbar.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		subbar.setBorderPainted(false);
		return subbar;
	}

	private void setReplacementLayout() {
		removeAll();

		Dimension sepSize = new Dimension(1,63);

		Log.i("ToolBar.setReplacementLayout() flag " + flag);
		if(!isSetFlag(FLAG_LAYOUT_DEVICE_CONNECTED)) {
			add(buttonMap.get(ButtonSet.OPEN));
		} else {
			add(buttonMap.get(ButtonSet.OPEN_PACKAGE));
		}
		add(buttonMap.get(ButtonSet.OPEN_EXTEND));

		add(getNewSeparator(JSeparator.VERTICAL, sepSize));

		add(buttonMap.get(ButtonSet.MANIFEST));
		add(buttonMap.get(ButtonSet.EXPLORER));

		JToolBar subbar = makeSubToolBar();
		subbar.add(buttonMap.get(ButtonSet.OPEN_CODE));
		subbar.add(buttonMap.get(ButtonSet.SEARCH));
		add(subbar);

		add(getNewSeparator(JSeparator.VERTICAL, sepSize));

		subbar = makeSubToolBar();
		if(isSetFlag(FLAG_LAYOUT_UNSIGNED)) {
			add(buttonMap.get(ButtonSet.SIGN));
			subbar.add(buttonMap.get(ButtonSet.SUB_LAUNCH));
			subbar.add(buttonMap.get(ButtonSet.SUB_INSTALL));
		} else if(isSetFlag(FLAG_LAYOUT_LAUNCHER)) {
			add(buttonMap.get(ButtonSet.LAUNCH));
			subbar.add(buttonMap.get(ButtonSet.SUB_INSTALL));
			subbar.add(buttonMap.get(ButtonSet.SUB_SIGN));
		} else if(isSetFlag(FLAG_LAYOUT_INSTALLED_LOWER)) {
			add(buttonMap.get(ButtonSet.INSTALL_UPDATE));
			subbar.add(buttonMap.get(ButtonSet.SUB_LAUNCH));
			subbar.add(buttonMap.get(ButtonSet.SUB_SIGN));
		} else if(isSetFlag(FLAG_LAYOUT_INSTALLED_UPPER)) {
			add(buttonMap.get(ButtonSet.INSTALL_DOWNGRADE));
			subbar.add(buttonMap.get(ButtonSet.SUB_LAUNCH));
			subbar.add(buttonMap.get(ButtonSet.SUB_SIGN));
		} else {
			add(buttonMap.get(ButtonSet.INSTALL));
			subbar.add(buttonMap.get(ButtonSet.SUB_LAUNCH));
			subbar.add(buttonMap.get(ButtonSet.SUB_SIGN));
		}
		subbar.add(buttonMap.get(ButtonSet.INSTALL_EXTEND));
		add(subbar);

		add(getNewSeparator(JSeparator.VERTICAL, sepSize));

		add(buttonMap.get(ButtonSet.SETTING));
		add(getNewSeparator(JSeparator.VERTICAL, sepSize));

		add(buttonMap.get(ButtonSet.ABOUT));
	}

	private JSeparator getNewSeparator(int orientation, Dimension size)
	{
		JSeparator separator = new JSeparator(orientation);
		//separator.setBackground(Color.gray);
		//separator.setForeground(Color.gray);
		separator.setPreferredSize(size);
		return separator;
	}

	private void setButtonText(ButtonSet buttonSet, String text, String tipText)
	{
		buttonMap.get(buttonSet).setText(text);
		buttonMap.get(buttonSet).setToolTipText(tipText);
	}

	@SuppressWarnings("unused")
	private void setButtonIcon(ButtonSet buttonSet, ImageIcon img)
	{
		buttonMap.get(buttonSet).setIcon(img);
	}

	private void setMenuItemText(MenuItemSet menuItemSet, String text, String tipText)
	{
		menuItemMap.get(menuItemSet).setText(text);
		menuItemMap.get(menuItemSet).setToolTipText(tipText);
	}

	public void reloadResource()
	{
		setButtonText(ButtonSet.OPEN, Resource.STR_BTN_OPEN.getString(), Resource.STR_BTN_OPEN_LAB.getString());
		setButtonText(ButtonSet.OPEN_PACKAGE, Resource.STR_BTN_OPEN_PACKAGE.getString(), Resource.STR_BTN_OPEN_PACKAGE_LAB.getString());
		setButtonText(ButtonSet.MANIFEST, Resource.STR_BTN_MANIFEST.getString(), Resource.STR_BTN_MANIFEST_LAB.getString());
		setButtonText(ButtonSet.EXPLORER, Resource.STR_BTN_EXPLORER.getString(), Resource.STR_BTN_EXPLORER_LAB.getString());
		setButtonText(ButtonSet.OPEN_CODE, Resource.STR_BTN_OPENCODE.getString(), Resource.STR_BTN_OPENCODE_LAB.getString());
		setButtonText(ButtonSet.SEARCH, Resource.STR_BTN_SEARCH.getString(), Resource.STR_BTN_SEARCH_LAB.getString());
		setButtonText(ButtonSet.INSTALL, Resource.STR_BTN_INSTALL.getString(), Resource.STR_BTN_INSTALL_LAB.getString());
		setButtonText(ButtonSet.INSTALL_UPDATE, Resource.STR_BTN_INSTALL_UPDATE.getString(), Resource.STR_BTN_INSTALL_UPDATE_LAB.getString());
		setButtonText(ButtonSet.INSTALL_DOWNGRADE, Resource.STR_BTN_INSTALL_DOWNGRAD.getString(), Resource.STR_BTN_INSTALL_DOWNGRAD_LAB.getString());
		setButtonText(ButtonSet.LAUNCH, Resource.STR_BTN_LAUNCH.getString(), Resource.STR_BTN_LAUNCH_LAB.getString());
		setButtonText(ButtonSet.SIGN, Resource.STR_BTN_SIGN.getString(), Resource.STR_BTN_SIGN_LAB.getString());
		setButtonText(ButtonSet.INSTALL_EXTEND, Resource.STR_BTN_MORE.getString(), Resource.STR_BTN_MORE_LAB.getString());
		setButtonText(ButtonSet.SETTING, Resource.STR_BTN_SETTING.getString(), Resource.STR_BTN_SETTING_LAB.getString());
		setButtonText(ButtonSet.ABOUT, Resource.STR_BTN_ABOUT.getString(), Resource.STR_BTN_ABOUT_LAB.getString());

		setButtonText(ButtonSet.SUB_INSTALL, Resource.STR_BTN_INSTALL.getString(), Resource.STR_BTN_INSTALL_LAB.getString());
		setButtonText(ButtonSet.SUB_INSTALL_UPDATE, Resource.STR_BTN_INSTALL_UPDATE.getString(), Resource.STR_BTN_INSTALL_UPDATE_LAB.getString());
		setButtonText(ButtonSet.SUB_INSTALL_DOWNGRADE, Resource.STR_BTN_INSTALL_DOWNGRAD.getString(), Resource.STR_BTN_INSTALL_DOWNGRAD_LAB.getString());
		setButtonText(ButtonSet.SUB_LAUNCH, Resource.STR_BTN_LAUNCH.getString(), Resource.STR_BTN_LAUNCH_LAB.getString());
		setButtonText(ButtonSet.SUB_SIGN, Resource.STR_BTN_SIGN.getString(), Resource.STR_BTN_SIGN_LAB.getString());

		setMenuItemText(MenuItemSet.NEW_WINDOW, Resource.STR_MENU_NEW.getString(), null);
		setMenuItemText(MenuItemSet.NEW_EMPTY, Resource.STR_MENU_NEW_WINDOW.getString(), null);
		setMenuItemText(MenuItemSet.NEW_APK, Resource.STR_MENU_NEW_APK_FILE.getString(), null);
		setMenuItemText(MenuItemSet.NEW_PACKAGE, Resource.STR_MENU_NEW_PACKAGE.getString(), null);
		setMenuItemText(MenuItemSet.OPEN_APK, Resource.STR_MENU_APK_FILE.getString(), null);
		setMenuItemText(MenuItemSet.OPEN_PACKAGE, Resource.STR_MENU_PACKAGE.getString(), null);
		setMenuItemText(MenuItemSet.INSTALL_APK, Resource.STR_MENU_INSTALL.getString(), null);
		setMenuItemText(MenuItemSet.UNINSTALL_APK, Resource.STR_MENU_UNINSTALL.getString(), null);
		setMenuItemText(MenuItemSet.INSTALLED_CHECK, Resource.STR_MENU_CHECK_INSTALLED.getString(), null);
	}

	public void setEnabledAt(ButtonSet buttonId, boolean enabled)
	{
		switch(buttonId) {
		case ALL:
			for(ButtonSet bs: ButtonSet.values()) {
				buttonMap.get(bs).setEnabled(enabled);
			}
			break;
		case OPEN:
			buttonMap.get(ButtonSet.OPEN).setEnabled(enabled);
			buttonMap.get(ButtonSet.OPEN_PACKAGE).setEnabled(enabled);
			buttonMap.get(ButtonSet.OPEN_EXTEND).setEnabled(enabled);
			break;
		case OPEN_CODE:
			if(!enabled && buttonId == ButtonSet.OPEN_CODE) {
				setButtonText(ButtonSet.OPEN_CODE, Resource.STR_BTN_OPENING_CODE.getString(), Resource.STR_BTN_OPENING_CODE_LAB.getString());
				buttonMap.get(ButtonSet.OPEN_CODE).setDisabledIcon(Resource.IMG_TOOLBAR_LOADING_OPEN_JD.getImageIcon());
			} else {
				setButtonText(ButtonSet.OPEN_CODE, Resource.STR_BTN_OPENCODE.getString(), Resource.STR_BTN_OPENCODE_LAB.getString());
				buttonMap.get(ButtonSet.OPEN_CODE).setDisabledIcon(null);
			}
			buttonMap.get(ButtonSet.OPEN_CODE).setEnabled(enabled);
			break;
		case NEED_TARGET_APK:
			hasTargetApk = enabled;
			buttonMap.get(ButtonSet.MANIFEST).setEnabled(enabled);
			buttonMap.get(ButtonSet.EXPLORER).setEnabled(enabled);
			buttonMap.get(ButtonSet.OPEN_CODE).setEnabled(enabled);
			buttonMap.get(ButtonSet.SEARCH).setEnabled(enabled);
			buttonMap.get(ButtonSet.INSTALL).setEnabled(enabled);
			buttonMap.get(ButtonSet.INSTALL_EXTEND).setEnabled(enabled);
			buttonMap.get(ButtonSet.INSTALL_DOWNGRADE).setEnabled(enabled);
			buttonMap.get(ButtonSet.INSTALL_UPDATE).setEnabled(enabled);
			buttonMap.get(ButtonSet.SIGN).setEnabled(enabled);
			buttonMap.get(ButtonSet.SUB_INSTALL).setEnabled(enabled);
			buttonMap.get(ButtonSet.SUB_INSTALL_DOWNGRADE).setEnabled(enabled);
			buttonMap.get(ButtonSet.SUB_INSTALL_UPDATE).setEnabled(enabled);
			buttonMap.get(ButtonSet.SUB_SIGN).setEnabled(enabled);
			buttonMap.get(ButtonSet.LAUNCH).setEnabled(enabled);
			buttonMap.get(ButtonSet.SUB_LAUNCH).setEnabled(enabled);
		case NEED_DEVICE:
			if(buttonId == ButtonSet.NEED_DEVICE) hasDevice = enabled;
			enabled = hasDevice && hasTargetApk;
			break;
		default:
			buttonMap.get(buttonId).setEnabled(enabled);
			break;
		}
	}
}
