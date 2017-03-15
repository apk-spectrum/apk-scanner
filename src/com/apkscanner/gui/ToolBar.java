package com.apkscanner.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.border.MatteBorder;

import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

public class ToolBar extends JToolBar
{
	private static final long serialVersionUID = 894134416480807167L;

	private HashMap<ButtonSet, JButton> buttonMap;
	private HashMap<MenuItemSet, JMenuItem> menuItemMap;

	public enum MenuItemSet
	{
		NEW_WINDOW		(Resource.STR_MENU_NEW.getString(), null, null, null, '\0', true),
		NEW_EMPTY		(Resource.STR_MENU_NEW_WINDOW.getString(), null, Resource.IMG_TOOLBAR_MANIFEST.getImageIcon(16,16), KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK, false), 'N'),
		NEW_APK			(Resource.STR_MENU_NEW_APK_FILE.getString(), null, Resource.IMG_TOOLBAR_OPEN.getImageIcon(16,16), KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_MASK, false), 'O'),
		NEW_PACKAGE		(Resource.STR_MENU_NEW_PACKAGE.getString(), null, Resource.IMG_TOOLBAR_PACKAGETREE.getImageIcon(16,16), KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_MASK, false), 'P'),
		OPEN_APK		(Resource.STR_MENU_APK_FILE.getString(), null, Resource.IMG_TOOLBAR_OPEN.getImageIcon(16,16), KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK, false), 'O'),
		OPEN_PACKAGE	(Resource.STR_MENU_PACKAGE.getString(), null, Resource.IMG_TOOLBAR_PACKAGETREE.getImageIcon(16,16), KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK, false), 'P'),
		INSTALL_APK		(Resource.STR_MENU_INSTALL.getString(), null, Resource.IMG_TOOLBAR_INSTALL.getImageIcon(16,16), KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK, false), 'I'),
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
    	OPEN_EXTEND		(Type.EXTEND, null, Resource.IMG_TOOLBAR_OPEN_ARROW.getImageIcon(10,10)),
    	MANIFEST		(Type.NORMAL, Resource.STR_BTN_MANIFEST.getString(), Resource.STR_BTN_MANIFEST_LAB.getString(), Resource.IMG_TOOLBAR_MANIFEST.getImageIcon(ButtonSet.IconSize, ButtonSet.IconSize)),
    	EXPLORER		(Type.NORMAL, Resource.STR_BTN_EXPLORER.getString(), Resource.STR_BTN_EXPLORER_LAB.getString(), Resource.IMG_TOOLBAR_EXPLORER.getImageIcon(ButtonSet.IconSize, ButtonSet.IconSize)),
    	INSTALL			(Type.NORMAL, Resource.STR_BTN_INSTALL.getString(), Resource.STR_BTN_INSTALL_LAB.getString(), Resource.IMG_TOOLBAR_INSTALL.getImageIcon(ButtonSet.IconSize, ButtonSet.IconSize)),
    	INSTALL_EXTEND	(Type.EXTEND, null, Resource.IMG_TOOLBAR_OPEN_ARROW.getImageIcon(10,10)),
    	SETTING			(Type.NORMAL, Resource.STR_BTN_SETTING.getString(), Resource.STR_BTN_SETTING_LAB.getString(), Resource.IMG_TOOLBAR_SETTING.getImageIcon(ButtonSet.IconSize, ButtonSet.IconSize)),
    	ABOUT			(Type.NORMAL, Resource.STR_BTN_ABOUT.getString(), Resource.STR_BTN_ABOUT_LAB.getString(), Resource.IMG_TOOLBAR_ABOUT.getImageIcon(ButtonSet.IconSize, ButtonSet.IconSize)),
    	ALL				(Type.NONE, null, null),
    	NEED_TARGET_APK	(Type.NONE, null, null),
    	OPEN_CODE		(Type.NORMAL, Resource.STR_BTN_OPENCODE.getString(), Resource.STR_BTN_OPENCODE_LAB.getString(), Resource.IMG_TOOLBAR_OPENCODE.getImageIcon(ButtonSet.IconSize, ButtonSet.IconSize)),
    	SEARCH			(Type.NORMAL, Resource.STR_BTN_SEARCH.getString(), Resource.STR_BTN_SEARCH_LAB.getString(), Resource.IMG_TOOLBAR_SEARCH.getImageIcon(ButtonSet.IconSize, ButtonSet.IconSize));

    	private enum Type {
    		NONE, NORMAL, HOVER, EXTEND
    	}

    	static private final int IconSize = 40;

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
    		JButton button = null;
    		switch(type) {
    		case NORMAL:
    		case HOVER:
    			button = new JButton(text, icon);
    			button.setToolTipText(toolTipText);
    			button.addActionListener(listener);
    			button.setVerticalTextPosition(JLabel.BOTTOM);
    			button.setHorizontalTextPosition(JLabel.CENTER);
    			button.setBorderPainted(false);
    			button.setOpaque(false);
    			button.setFocusable(false);
    			button.setPreferredSize(new Dimension(63,65));
    			if(type == Type.HOVER) {
    				button.setRolloverIcon(hoverIcon);
    			}
    			break;
    		case EXTEND:
    			button = new JButton(text, icon);
    			button.setToolTipText(toolTipText);
    			button.setMargin(new Insets(27,0,27,0));
    			button.setBorderPainted(false);
    			button.setOpaque(false);
    			button.setFocusable(false);
    			break;
    		default:
    			return null;
    		}
			button.setActionCommand(actionCommand);

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

    	final JPopupMenu openPopupMenu = new JPopupMenu();
        final JPopupMenu installPopupMenu = new JPopupMenu();

        Log.i("ToolBar.initUI() MenuItemSet init");
        menuItemMap = MenuItemSet.getButtonMap(listener);
    	JMenuItem SubMenu = openPopupMenu.add((JMenu)menuItemMap.get(MenuItemSet.NEW_WINDOW));
    	SubMenu.add(menuItemMap.get(MenuItemSet.NEW_EMPTY));
    	SubMenu.add(menuItemMap.get(MenuItemSet.NEW_APK));
    	SubMenu.add(menuItemMap.get(MenuItemSet.NEW_PACKAGE));
        openPopupMenu.add(menuItemMap.get(MenuItemSet.OPEN_APK));
        openPopupMenu.add(menuItemMap.get(MenuItemSet.OPEN_PACKAGE));

        installPopupMenu.add(menuItemMap.get(MenuItemSet.INSTALL_APK));
        installPopupMenu.add(menuItemMap.get(MenuItemSet.INSTALLED_CHECK));

        Log.i("ToolBar.initUI() ButtonSet init");
        buttonMap = ButtonSet.getButtonMap(listener);
        Dimension sepSize = new Dimension(1,63);

        Log.i("ToolBar.initUI() ButtonSet add");
        add(buttonMap.get(ButtonSet.OPEN));
        add(buttonMap.get(ButtonSet.OPEN_EXTEND));

        add(getNewSeparator(JSeparator.VERTICAL, sepSize));

        add(buttonMap.get(ButtonSet.MANIFEST));
        add(buttonMap.get(ButtonSet.EXPLORER));
        add(buttonMap.get(ButtonSet.OPEN_CODE));
        add(buttonMap.get(ButtonSet.SEARCH));

        add(getNewSeparator(JSeparator.VERTICAL, sepSize));

        add(buttonMap.get(ButtonSet.INSTALL));
        add(buttonMap.get(ButtonSet.INSTALL_EXTEND));

        add(getNewSeparator(JSeparator.VERTICAL, sepSize));

        add(buttonMap.get(ButtonSet.SETTING));
        add(getNewSeparator(JSeparator.VERTICAL, sepSize));

        add(buttonMap.get(ButtonSet.ABOUT));

        buttonMap.get(ButtonSet.OPEN_EXTEND).addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
            	JButton btn = buttonMap.get(ButtonSet.OPEN_EXTEND);
            	openPopupMenu.show(btn, btn.getWidth()/2, btn.getHeight());
            }
        });

        buttonMap.get(ButtonSet.INSTALL_EXTEND).addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
            	JButton btn = buttonMap.get(ButtonSet.INSTALL_EXTEND);
            	installPopupMenu.show(btn, btn.getWidth()/2, btn.getHeight());
            }
        });
        //Log.i("ToolBar.initUI() reloadResource s");
        //reloadResource();
        //Log.i("ToolBar.initUI() setFloatable");

        //setAlignmentX(0);
        setOpaque(true);
        setFloatable(false);
        setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
        setBorder(new MatteBorder(0,0,1,0,Color.LIGHT_GRAY));
        Log.i("ToolBar.initUI() end");
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
    	setButtonText(ButtonSet.MANIFEST, Resource.STR_BTN_MANIFEST.getString(), Resource.STR_BTN_MANIFEST_LAB.getString());
    	setButtonText(ButtonSet.EXPLORER, Resource.STR_BTN_EXPLORER.getString(), Resource.STR_BTN_EXPLORER_LAB.getString());
    	setButtonText(ButtonSet.INSTALL, Resource.STR_BTN_INSTALL.getString(), Resource.STR_BTN_INSTALL_LAB.getString());
    	setButtonText(ButtonSet.SETTING, Resource.STR_BTN_SETTING.getString(), Resource.STR_BTN_SETTING_LAB.getString());
    	setButtonText(ButtonSet.ABOUT, Resource.STR_BTN_ABOUT.getString(), Resource.STR_BTN_ABOUT_LAB.getString());
    	setButtonText(ButtonSet.OPEN_CODE, Resource.STR_BTN_OPENCODE.getString(), Resource.STR_BTN_OPENCODE_LAB.getString());
    	setButtonText(ButtonSet.SEARCH, Resource.STR_BTN_SEARCH.getString(), Resource.STR_BTN_SEARCH_LAB.getString());
    	
    	setMenuItemText(MenuItemSet.NEW_WINDOW, Resource.STR_MENU_NEW.getString(), null);
    	setMenuItemText(MenuItemSet.NEW_EMPTY, Resource.STR_MENU_NEW_WINDOW.getString(), null);
    	setMenuItemText(MenuItemSet.NEW_APK, Resource.STR_MENU_NEW_APK_FILE.getString(), null);
    	setMenuItemText(MenuItemSet.NEW_PACKAGE, Resource.STR_MENU_NEW_PACKAGE.getString(), null);
    	setMenuItemText(MenuItemSet.OPEN_APK, Resource.STR_MENU_APK_FILE.getString(), null);
    	setMenuItemText(MenuItemSet.OPEN_PACKAGE, Resource.STR_MENU_PACKAGE.getString(), null);
    	setMenuItemText(MenuItemSet.INSTALL_APK, Resource.STR_MENU_INSTALL.getString(), null);
    	setMenuItemText(MenuItemSet.INSTALLED_CHECK, Resource.STR_MENU_CHECK_INSTALLED.getString(), null);
    }

    public void setEnabledAt(ButtonSet buttonId, boolean enabled)
    {
    	switch(buttonId) {
    	case ALL:
    	case OPEN:
    		buttonMap.get(ButtonSet.OPEN).setEnabled(enabled);
    		buttonMap.get(ButtonSet.OPEN_EXTEND).setEnabled(enabled);
    		if(buttonId != ButtonSet.ALL) break;
    	case ABOUT:
    		buttonMap.get(ButtonSet.ABOUT).setEnabled(enabled);
    		if(buttonId != ButtonSet.ALL) break;
    	case NEED_TARGET_APK:
    		buttonId = ButtonSet.ALL;
    	case MANIFEST:
    		buttonMap.get(ButtonSet.MANIFEST).setEnabled(enabled);
    		if(buttonId != ButtonSet.ALL) break;
    	case EXPLORER:
    		buttonMap.get(ButtonSet.EXPLORER).setEnabled(enabled);
    		if(buttonId != ButtonSet.ALL) break;
    	case INSTALL:
    		buttonMap.get(ButtonSet.INSTALL).setEnabled(enabled);
    		buttonMap.get(ButtonSet.INSTALL_EXTEND).setEnabled(enabled);
    		if(buttonId != ButtonSet.ALL) break;
    	case OPEN_CODE:
    		if(!enabled && buttonId == ButtonSet.OPEN_CODE) {
    			setButtonText(ButtonSet.OPEN_CODE, Resource.STR_BTN_OPENING_CODE.getString(), Resource.STR_BTN_OPENING_CODE_LAB.getString());
	    		buttonMap.get(ButtonSet.OPEN_CODE).setDisabledIcon(Resource.IMG_TOOLBAR_LOADING_OPEN_JD.getImageIcon());
    		} else {
    			setButtonText(ButtonSet.OPEN_CODE, Resource.STR_BTN_OPENCODE.getString(), Resource.STR_BTN_OPENCODE_LAB.getString());
    			buttonMap.get(ButtonSet.OPEN_CODE).setDisabledIcon(null);
    		}
    		buttonMap.get(ButtonSet.OPEN_CODE).setEnabled(enabled);
    		if(buttonId != ButtonSet.ALL) break;
    	case SEARCH:
    		buttonMap.get(ButtonSet.SEARCH).setEnabled(enabled);
    		if(buttonId != ButtonSet.ALL) break;
    		break;
    	default:
    		break;
    	}
    }
}
