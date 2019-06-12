package com.apkscanner.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.MatteBorder;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import com.apkscanner.gui.component.ExtensionButton;
import com.apkscanner.gui.component.ImageScaler;
import com.apkscanner.plugin.IExternalTool;
import com.apkscanner.plugin.IPackageSearcher;
import com.apkscanner.plugin.IPlugIn;
import com.apkscanner.plugin.PlugInManager;
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

	private static final String CMD_SELECT_DEFAULT_MENU = "CMD_SELECT_DEFAULT_MENU";
	public static final String CMD_VISIBLE_TO_BASEIC = "CMD_VISIBLE_TO_BASEIC";
	public static final String CMD_VISIBLE_TO_BASEIC_CHANGED = "CMD_VISIBLE_TO_BASEIC_CHANGED";

	private int flag = 0;
	private boolean hasTargetApk = false;
	private boolean hasDevice = false;

	private HashMap<ButtonSet, JButton> buttonMap;
	private HashMap<MenuItemSet, JMenuItem> menuItemMap;
	private JPopupMenu openPopupMenu;
	private JPopupMenu installPopupMenu;
	private JPopupMenu pluginPopupMenu;
	private JComponent pluginToolBar;
	private JPopupMenu decordePopupMenu;
	private JPopupMenu searchPopupMenu;
	private JPopupMenu explorerPopupMenu;
	private JPopupMenu launchPopupMenu;

	public enum MenuItemSet
	{
		NEW_WINDOW		(Resource.STR_MENU_NEW.getString(), null, null, null, '\0', true),
		NEW_EMPTY		(Resource.STR_MENU_NEW_WINDOW.getString(), null, Resource.IMG_TOOLBAR_MANIFEST.getImageIcon(16,16), KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK, false), 'N'),
		NEW_APK			(Resource.STR_MENU_NEW_APK_FILE.getString(), null, Resource.IMG_TOOLBAR_OPEN.getImageIcon(16,16), KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, false), 'O'),
		NEW_PACKAGE		(Resource.STR_MENU_NEW_PACKAGE.getString(), null, Resource.IMG_TOOLBAR_PACKAGETREE.getImageIcon(16,16), KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, false), 'P'),
		OPEN_APK		(Resource.STR_MENU_APK_FILE.getString(), null, Resource.IMG_TOOLBAR_OPEN.getImageIcon(16,16), KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK, false), 'O'),
		OPEN_PACKAGE	(Resource.STR_MENU_PACKAGE.getString(), null, Resource.IMG_TOOLBAR_PACKAGETREE.getImageIcon(16,16), KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK, false), 'P'),
		INSTALL_APK		(Resource.STR_MENU_INSTALL.getString(), null, Resource.IMG_TOOLBAR_INSTALL.getImageIcon(16,16), KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK, false), 'I'),
		UNINSTALL_APK	(Resource.STR_MENU_UNINSTALL.getString(), null, Resource.IMG_TOOLBAR_UNINSTALL.getImageIcon(16,16), KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK, false), 'U'),
		CLEAR_DATA		(Resource.STR_MENU_CLEAR_DATA.getString(), null, Resource.IMG_TOOLBAR_CLEAR.getImageIcon(16,16), null, '\0'),
		INSTALLED_CHECK	(Resource.STR_MENU_CHECK_INSTALLED.getString(), null, Resource.IMG_TOOLBAR_PACKAGETREE.getImageIcon(16,16), KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK, false), 'T'),
		DECODER_JD_GUI	(Resource.STR_MENU_DECODER_JD_GUI.getString(), null, null, null, '\0', Resource.STR_DECORDER_JD_GUI),
		DECODER_JADX_GUI(Resource.STR_MENU_DECODER_JADX_GUI.getString(), null, null, null, '\0', Resource.STR_DECORDER_JADX_GUI),
		DECODER_BYTECODE(Resource.STR_MENU_DECODER_BYTECODE.getString(), null, null, null, '\0', Resource.STR_DECORDER_BYTECOD),
		SEARCH_RESOURCE	(Resource.STR_MENU_SEARCH_RESOURCE.getString(), null, null, null, '\0', Resource.STR_DEFAULT_SEARCHER),
		EXPLORER_ARCHIVE(Resource.STR_MENU_EXPLORER_ARCHIVE.getString(), null, null, null, '\0', Resource.STR_EXPLORER_ARCHIVE),
		EXPLORER_FOLDER	(Resource.STR_MENU_EXPLORER_FOLDER.getString(), null, null, null, '\0', Resource.STR_EXPLORER_FOLDER),
		LAUNCH_LAUNCHER	(Resource.STR_MENU_LAUNCH_LAUNCHER.getString(), null, null, null, '\0', Resource.STR_LAUNCH_LAUNCHER),
		LAUNCH_SELECT	(Resource.STR_MENU_LAUNCH_SELECT.getString(), null, null, null, '\0', Resource.STR_LAUNCH_SELECT),
		SELECT_DEFAULT	(Resource.STR_MENU_SELECT_DEFAULT.getString(), null, null, null, '\0');

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
				menuItem = new JMenuItem(text);
				menuItem.setAccelerator(keyStroke);
			} else {
				menuItem = new JMenu(text);
			}
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					boolean isSaveDefault = false;
					for(Component c: ((JMenuItem)e.getSource()).getParent().getComponents()) {
						if(c instanceof NoCloseCheckBoxMenuItem) {
							if(CMD_SELECT_DEFAULT_MENU.equals(((JCheckBoxMenuItem)c).getActionCommand())) {
								isSaveDefault = ((JCheckBoxMenuItem)c).isSelected();
							}
						}
					}
					if(isSaveDefault) {
						String value = e.getActionCommand().replaceAll(".*:", "");
						if(DECODER_JD_GUI.matchActionEvent(e)
							|| DECODER_JADX_GUI.matchActionEvent(e)
							|| DECODER_BYTECODE.matchActionEvent(e)) {
							Resource.PROP_DEFAULT_DECORDER.setData(value);
						} else if(LAUNCH_LAUNCHER.matchActionEvent(e)
							|| LAUNCH_SELECT.matchActionEvent(e)) {
							Resource.PROP_DEFAULT_LAUNCH_MODE.setData(value);
						} else if(EXPLORER_ARCHIVE.matchActionEvent(e)
							|| EXPLORER_FOLDER.matchActionEvent(e)) {
							Resource.PROP_DEFAULT_EXPLORER.setData(value);
						} else if(SEARCH_RESOURCE.matchActionEvent(e)) {
							Resource.PROP_DEFAULT_SEARCHER.setData(value);
						}
					}
					listener.actionPerformed(e);
				}
			});
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
		EXPLORER		(Type.SUB_TOOLBAR, Resource.STR_BTN_EXPLORER.getString(), Resource.STR_BTN_EXPLORER_LAB.getString(), Resource.IMG_TOOLBAR_EXPLORER.getImageIcon(ButtonSet.SubIconSize, ButtonSet.SubIconSize), true),
		OPEN_CODE		(Type.SUB_TOOLBAR, Resource.STR_BTN_OPENCODE.getString(), Resource.STR_BTN_OPENCODE_LAB.getString(), Resource.IMG_TOOLBAR_OPENCODE.getImageIcon(ButtonSet.SubIconSize, ButtonSet.SubIconSize), true),
		SEARCH			(Type.SUB_TOOLBAR, Resource.STR_BTN_SEARCH.getString(), Resource.STR_BTN_SEARCH_LAB.getString(), Resource.IMG_TOOLBAR_SEARCH.getImageIcon(ButtonSet.SubIconSize, ButtonSet.SubIconSize), true),
		PLUGIN_EXTEND	(Type.SUB_TOOLBAR, Resource.STR_BTN_MORE.getString(), Resource.STR_BTN_MORE_LAB.getString(), Resource.IMG_TOOLBAR_OPEN_ARROW.getImageIcon(16,16)),
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
		SUB_LAUNCH			(Type.SUB_TOOLBAR, Resource.STR_BTN_LAUNCH.getString(), Resource.STR_BTN_LAUNCH_LAB.getString(), Resource.IMG_TOOLBAR_LAUNCH.getImageIcon(ButtonSet.SubIconSize, ButtonSet.SubIconSize), true),
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
		private boolean extension = false;

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

		private JButton getButton(ActionListener listener)
		{
			if(type == Type.NONE) {
				return null;
			}

			ExtensionButton button = new ExtensionButton(text, icon);
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

		static private HashMap<ButtonSet, JButton> getButtonMap(ActionListener listener)
		{
			HashMap<ButtonSet, JButton> buttonMap = new HashMap<ButtonSet, JButton>();
			for(ButtonSet bs: values()) {
				buttonMap.put(bs, bs.getButton(listener));
			}
			return buttonMap;
		}
	}

	public class NoCloseCheckBoxMenuItem extends JCheckBoxMenuItem {
		private static final long serialVersionUID = 4982874784585596549L;

		public NoCloseCheckBoxMenuItem() {
        	super();
        }

        @Override
        protected void processMouseEvent(MouseEvent evt) {
            if (evt.getID() == MouseEvent.MOUSE_RELEASED && contains(evt.getPoint())) {
                doClick();
                setArmed(true);
            } else {
                super.processMouseEvent(evt);
            }
        }
    }

	public class SearcherCheckBoxMenuItem extends JCheckBoxMenuItem {
		private static final long serialVersionUID = -6097881007848535633L;

		private IPackageSearcher plugin;
		private boolean selectMode = false;
		private Icon icon;

		public SearcherCheckBoxMenuItem(IPackageSearcher plugin) {
        	super();
        	this.plugin = plugin;
        }

		public boolean isSelectMode() {
			return selectMode;
		}

		public void setSelecteMode(boolean selectMode) {
			this.selectMode = selectMode;
			super.setIcon(selectMode ? null : icon);
			super.setSelected(selectMode ? plugin.isVisibleToBasic() : false);
		}

		@Override
		public void setIcon(Icon icon) {
			this.icon = icon;
			super.setIcon(icon);
		}

        @Override
		public void addActionListener(final ActionListener listener) {
			super.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if(isSelectMode()) {
						plugin.setVisibleToBasic(isSelected());
						PlugInManager.saveProperty();
						listener.actionPerformed(new ActionEvent(e.getSource(), e.getID(), CMD_VISIBLE_TO_BASEIC_CHANGED, e.getWhen(), e.getModifiers()));
					} else {
						listener.actionPerformed(e);
					}
				}
			});
		}

		@Override
        protected void processMouseEvent(MouseEvent evt) {
            if (selectMode && evt.getID() == MouseEvent.MOUSE_RELEASED && contains(evt.getPoint())) {
                doClick();
                setArmed(true);
            } else {
                super.processMouseEvent(evt);
            }
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
		pluginPopupMenu = new JPopupMenu();

		decordePopupMenu = new JPopupMenu();
		searchPopupMenu = new JPopupMenu();
		explorerPopupMenu = new JPopupMenu();
		launchPopupMenu = new JPopupMenu();

		Log.i("ToolBar.initUI() MenuItemSet init");
		menuItemMap = MenuItemSet.getButtonMap(listener);

		JMenuItem SubMenu = openPopupMenu.add((JMenu)menuItemMap.get(MenuItemSet.NEW_WINDOW));
		SubMenu.add(menuItemMap.get(MenuItemSet.NEW_EMPTY));
		SubMenu.add(menuItemMap.get(MenuItemSet.NEW_APK));
		SubMenu.add(menuItemMap.get(MenuItemSet.NEW_PACKAGE));
		openPopupMenu.add(menuItemMap.get(MenuItemSet.OPEN_APK));
		openPopupMenu.add(menuItemMap.get(MenuItemSet.OPEN_PACKAGE));

		explorerPopupMenu.add(menuItemMap.get(MenuItemSet.EXPLORER_ARCHIVE));
		explorerPopupMenu.add(menuItemMap.get(MenuItemSet.EXPLORER_FOLDER));
		explorerPopupMenu.addSeparator();
		explorerPopupMenu.add(makeSelectDefaultMenuItem());

		installPopupMenu.add(menuItemMap.get(MenuItemSet.UNINSTALL_APK));
		installPopupMenu.add(menuItemMap.get(MenuItemSet.CLEAR_DATA));
		installPopupMenu.add(menuItemMap.get(MenuItemSet.INSTALLED_CHECK));

		launchPopupMenu.add(menuItemMap.get(MenuItemSet.LAUNCH_LAUNCHER));
		launchPopupMenu.add(menuItemMap.get(MenuItemSet.LAUNCH_SELECT));
		launchPopupMenu.addSeparator();
		launchPopupMenu.add(makeSelectDefaultMenuItem());

		decordePopupMenu.add(menuItemMap.get(MenuItemSet.DECODER_JD_GUI));
		decordePopupMenu.add(menuItemMap.get(MenuItemSet.DECODER_JADX_GUI));
		decordePopupMenu.add(menuItemMap.get(MenuItemSet.DECODER_BYTECODE));
		decordePopupMenu.addSeparator();
		decordePopupMenu.add(makeSelectDefaultMenuItem());

		searchPopupMenu.add(menuItemMap.get(MenuItemSet.SEARCH_RESOURCE));

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

		setExtensionMenu(buttonMap.get(ButtonSet.OPEN_CODE), decordePopupMenu, Resource.PROP_DEFAULT_DECORDER);
		setExtensionMenu(buttonMap.get(ButtonSet.SEARCH), searchPopupMenu, Resource.PROP_DEFAULT_SEARCHER);
		setExtensionMenu(buttonMap.get(ButtonSet.EXPLORER), explorerPopupMenu, Resource.PROP_DEFAULT_EXPLORER);
		setExtensionMenu(buttonMap.get(ButtonSet.SUB_LAUNCH), launchPopupMenu, Resource.PROP_DEFAULT_LAUNCH_MODE);

		boolean alwaysExtended = (boolean)Resource.PROP_ALWAYS_TOOLBAR_EXTENDED.getData();
		for(ButtonSet bs: ButtonSet.values()) {
			if(bs.extension) {
				((ExtensionButton)buttonMap.get(bs)).setArrowVisible(alwaysExtended);
			}
		}

		KeyboardFocusManager ky=KeyboardFocusManager.getCurrentKeyboardFocusManager();
		ky.addKeyEventDispatcher(new KeyEventDispatcher() {
			private boolean isShiftPressed = false;
			@Override
			public boolean dispatchKeyEvent(KeyEvent e) {
				if (e.getID() == KeyEvent.KEY_PRESSED && !isShiftPressed) {
					if(e.getModifiersEx() == KeyEvent.SHIFT_DOWN_MASK) {
						isShiftPressed = true;
						if(!(boolean)Resource.PROP_ALWAYS_TOOLBAR_EXTENDED.getData()) setArrowVisible(true);
						setButtonText(ButtonSet.OPEN, Resource.STR_MENU_NEW.getString(), Resource.STR_BTN_OPEN_LAB.getString());
						setButtonText(ButtonSet.OPEN_PACKAGE, Resource.STR_MENU_NEW.getString(), Resource.STR_BTN_OPEN_PACKAGE_LAB.getString());
						setButtonText(ButtonSet.MANIFEST, Resource.STR_BTN_MANIFEST_SAVE_AS.getString(), Resource.STR_BTN_MANIFEST_LAB.getString());
						setButtonText(ButtonSet.LAUNCH, Resource.STR_BTN_LAUNCH_SELECT.getString(), Resource.STR_BTN_LAUNCH_LAB.getString());

						invokeMouseEvent(e, MouseEvent.MOUSE_ENTERED);
					}
				} else if (e.getID() == KeyEvent.KEY_RELEASED && isShiftPressed) {
					if(e.getModifiersEx() != KeyEvent.SHIFT_DOWN_MASK) {
						isShiftPressed = false;
						if(!(boolean)Resource.PROP_ALWAYS_TOOLBAR_EXTENDED.getData()) setArrowVisible(false);
						setButtonText(ButtonSet.OPEN, Resource.STR_BTN_OPEN.getString(), Resource.STR_BTN_OPEN_LAB.getString());
						setButtonText(ButtonSet.OPEN_PACKAGE, Resource.STR_BTN_OPEN_PACKAGE.getString(), Resource.STR_BTN_OPEN_PACKAGE_LAB.getString());
						setButtonText(ButtonSet.MANIFEST, Resource.STR_BTN_MANIFEST.getString(), Resource.STR_BTN_MANIFEST_LAB.getString());
						setButtonText(ButtonSet.LAUNCH, Resource.STR_BTN_LAUNCH.getString(), Resource.STR_BTN_LAUNCH_LAB.getString());

						invokeMouseEvent(e, MouseEvent.MOUSE_EXITED);
					}
				}
				return false;
			}

			private void setArrowVisible(boolean visibale) {
				for(ButtonSet bs: ButtonSet.values()) {
					if(bs.extension) {
						((ExtensionButton)buttonMap.get(bs)).setArrowVisible(visibale);
					}
				}
			}

			private void invokeMouseEvent(KeyEvent e, int mouseEvent) {
				Point p = ToolBar.this.getMousePosition();
				if(p != null) {
					Component c = ToolBar.this.getComponentAt(p);
					if(c != null) {
						if(c instanceof ExtensionButton) {
							((ExtensionButton)c).dispatchEvent(new MouseEvent(c, mouseEvent, e.getWhen() + 10, 0, p.x, p.y, 0, false));
						} else if(c instanceof JToolBar) {
							p = ((JToolBar)c).getMousePosition();
							c = ((JToolBar)c).getComponentAt(p);
							if(c instanceof ExtensionButton) {
								((ExtensionButton)c).dispatchEvent(new MouseEvent(c, mouseEvent, e.getWhen() + 10, e.getModifiersEx(), p.x, p.y, 0, false));
								if(mouseEvent == MouseEvent.MOUSE_EXITED) {
									((ExtensionButton)c).dispatchEvent(new MouseEvent(c, MouseEvent.MOUSE_ENTERED, e.getWhen() + 20, e.getModifiersEx(), p.x, p.y, 0, false));
								}
							}
						}
					}
				}
			}
		});

		Log.i("ToolBar.initUI() end");
	}

	private void setMouseEvent(Container menu, MouseListener[] listeners) {
		if(listeners == null) {
			listeners = menu.getMouseListeners();
			if(listeners == null || listeners.length == 0) return;
		}
		Component[] children = menu instanceof JMenu ?
				((JMenu)menu).getMenuComponents() : menu.getComponents();
		if(children == null) return;
		for(Component c: children) {
			for(MouseListener listen: listeners)
				c.addMouseListener(listen);
			if(c instanceof Container) {
				setMouseEvent((Container)c, listeners);
			}
		}
	}

	public void onLoadPlugin(final ActionListener listener) {
		IExternalTool[] tools = PlugInManager.getDecorderTool();
		if(tools.length > 0) {
			decordePopupMenu.removeAll();
			decordePopupMenu.add(menuItemMap.get(MenuItemSet.DECODER_JD_GUI));
			decordePopupMenu.add(menuItemMap.get(MenuItemSet.DECODER_JADX_GUI));
			decordePopupMenu.add(menuItemMap.get(MenuItemSet.DECODER_BYTECODE));
			decordePopupMenu.addSeparator();
			for(IExternalTool tool: tools) {
				decordePopupMenu.add(makePlugInMenuItem(tool, listener, Resource.PROP_DEFAULT_DECORDER));
			}
			decordePopupMenu.addSeparator();
			decordePopupMenu.add(makeSelectDefaultMenuItem());
			setMouseEvent(decordePopupMenu, null);
		}

		IPackageSearcher[] searchers = PlugInManager.getPackageSearchers();
		if(searchers.length > 0) {
			searchPopupMenu.removeAll();
			searchPopupMenu.add(menuItemMap.get(MenuItemSet.SEARCH_RESOURCE));
			searchPopupMenu.addSeparator();

			searchers = PlugInManager.getPackageSearchers(IPackageSearcher.SEARCHER_TYPE_PACKAGE_NAME);
			JMenu searchersMenu = makeSearcherSelectMenu(Resource.STR_LABEL_BY_PACKAGE_NAME.getString(), searchers, listener);
			if(searchersMenu != null) {
				searchPopupMenu.add(searchersMenu);
			}

			searchers = PlugInManager.getPackageSearchers(IPackageSearcher.SEARCHER_TYPE_APP_NAME);
			searchersMenu = makeSearcherSelectMenu(Resource.STR_LABEL_BY_APP_LABEL.getString(), searchers, listener);
			if(searchersMenu != null) {
				searchPopupMenu.add(searchersMenu);
			}

			searchPopupMenu.addSeparator();
			JCheckBoxMenuItem v2bMenuItem = new JCheckBoxMenuItem();
			v2bMenuItem.setActionCommand(CMD_VISIBLE_TO_BASEIC);
			v2bMenuItem.setText(Resource.STR_MENU_VISIBLE_TO_BASIC.getString());
			v2bMenuItem.addActionListener(listener);
			v2bMenuItem.setSelected((boolean)Resource.PROP_VISIBLE_TO_BASIC.getData());
			searchPopupMenu.add(v2bMenuItem);

			setMouseEvent(searchPopupMenu, null);
		}

		pluginToolBar = makePluginToolBar(listener);
		if(pluginToolBar != null) {
			setReplacementLayout();
		}
	}

	public void setBadgeCount(int count) {
		((ExtensionButton)buttonMap.get(ButtonSet.ABOUT)).setBadge(count);;
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

	private void setExtensionMenu(final JButton button, final JPopupMenu popupMenu, final Resource defaultPorp) {
		Icon icon = null;
		switch(defaultPorp) {
		case PROP_DEFAULT_DECORDER:
			icon = Resource.IMG_TOOLBAR_OPENCODE.getImageIcon(ButtonSet.SubIconSize, ButtonSet.SubIconSize);
			break;
		case PROP_DEFAULT_SEARCHER:
			icon = Resource.IMG_TOOLBAR_SEARCH.getImageIcon(ButtonSet.SubIconSize, ButtonSet.SubIconSize);
			break;
		case PROP_DEFAULT_EXPLORER:
			icon = Resource.IMG_TOOLBAR_EXPLORER.getImageIcon(ButtonSet.SubIconSize, ButtonSet.SubIconSize);
			break;
		case PROP_DEFAULT_LAUNCH_MODE:
			icon = Resource.IMG_TOOLBAR_LAUNCH.getImageIcon(ButtonSet.SubIconSize, ButtonSet.SubIconSize);
			break;
		default:
			break;
		};
		final Icon defIcon = icon;

		popupMenu.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
				String value = ":" + (String)defaultPorp.getData();
				for(Component c: popupMenu.getComponents()) {
					if(c instanceof NoCloseCheckBoxMenuItem) {
						if(CMD_SELECT_DEFAULT_MENU.equals(((JCheckBoxMenuItem)c).getActionCommand())) {
							((JCheckBoxMenuItem)c).setSelected(false);
						}
					} else if(c instanceof JMenuItem) {
						if(((JMenuItem) c).getActionCommand().endsWith(value)) {
							((JMenuItem) c).setIcon(defIcon);
						} else {
							((JMenuItem) c).setIcon(null);
						}
					}
				}
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) { }

			@Override
			public void popupMenuCanceled(PopupMenuEvent arg0) { }
		});

		MouseAdapter ma = new MouseAdapter() {
			Timer timer = new Timer();
			TimerTask task = null;

			@Override
            public void mouseEntered(MouseEvent me) {
				//Log.d("mouseEntered 0x" + Integer.toHexString(me.getSource().hashCode()));
            	boolean enable = false;
            	if(button instanceof ExtensionButton) {
            		enable = ((ExtensionButton)button).getArrowVisible();
            	}
            	if(enable && !me.isShiftDown() && !(boolean)Resource.PROP_ALWAYS_TOOLBAR_EXTENDED.getData()) {
					for(ButtonSet bs: ButtonSet.values()) {
						if(bs.extension) {
							((ExtensionButton)buttonMap.get(bs)).setArrowVisible(false);
						}
					}
					return;
            	}
            	if(!enable || !button.isEnabled()) return;
            	int delayMs = (boolean)Resource.PROP_ALWAYS_TOOLBAR_EXTENDED.getData() ? 1000 : 100;
				synchronized(timer) {
					if(task != null) {
						task.cancel();
						task = null;
					}
					timer.purge();
					if(!popupMenu.isShowing()) {
						task = new TimerTask() {
				            @Override
				            public void run() {
								popupMenu.show(button, button.getWidth(), 0);
				            }
				        };
						timer.schedule(task, delayMs);
					}
				}
            }

			@Override
			public void mouseClicked(MouseEvent arg0) {
				//Log.d("mouseClicked");
				mouseExited(arg0);
			}

			@Override
			public void mouseExited(MouseEvent me) {
				//Log.d("mouseExited 0x" + Integer.toHexString(me.getSource().hashCode()));
				if(popupMenu.getMousePosition() != null) return;
				synchronized(timer) {
					if(task != null) {
						task.cancel();
						task = null;
					}
					timer.purge();
					if(popupMenu.isShowing()) {
						task = new TimerTask() {
				            @Override
				            public void run() {
				            	popupMenu.setVisible(false);
				            }
				        };
						timer.schedule(task, 500);
					}
				}
			}
		};
		button.addMouseListener(ma);
		popupMenu.addMouseListener(ma);
		for(Component c: popupMenu.getComponents()) {
			c.addMouseListener(ma);
		}
	}

	private JCheckBoxMenuItem makeSelectDefaultMenuItem() {
		JCheckBoxMenuItem selDefItem = new NoCloseCheckBoxMenuItem();
		selDefItem.setActionCommand(CMD_SELECT_DEFAULT_MENU);
		selDefItem.setText(Resource.STR_MENU_SELECT_DEFAULT.getString());
		return selDefItem;
	}

	private JToolBar makeSubToolBar() {
		JToolBar subbar = new JToolBar();
		subbar.setPreferredSize(new Dimension(76,60));
		subbar.setOpaque(false);
		subbar.setFloatable(false);
		subbar.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		subbar.setBorderPainted(false);
		return subbar;
	}

	private JComponent makePluginToolBar(ActionListener listener) {
		IExternalTool[] tools = PlugInManager.getExternalTool();
		if(tools == null || tools.length <= 0) return null;

		if(tools.length == 1) {
			JButton button = new JButton(tools[0].getLabel(), null);
			button.setToolTipText(tools[0].getDescription());
			button.setBorderPainted(false);
			button.setOpaque(false);
			button.setFocusable(false);
			button.setVerticalTextPosition(JLabel.BOTTOM);
			button.setHorizontalTextPosition(JLabel.CENTER);
			button.setPreferredSize(new Dimension(68,65));
			button.setActionCommand("PLUGIN:" + tools[0].getActionCommand());
			button.addActionListener(listener);
			button.setEnabled(hasTargetApk);
			URL iconUrl = tools[0].getIconURL();
			if(iconUrl != null) {
				button.setIcon(ImageScaler.getScaledImageIcon(new ImageIcon(iconUrl),40,40));
			}
			return button;
		}
		JToolBar subbar = makeSubToolBar();
		subbar.setPreferredSize(new Dimension(90,60));
		if(tools.length <= 3) {
			for(IExternalTool tool: tools) {
				subbar.add(makePlugInButtons(tool, listener));
			}
		} else {
			subbar.add(makePlugInButtons(tools[0], listener));
			subbar.add(makePlugInButtons(tools[1], listener));
			subbar.add(buttonMap.get(ButtonSet.PLUGIN_EXTEND));
			pluginPopupMenu.removeAll();
			for(int i=2; i<tools.length; i++) {
				pluginPopupMenu.add(makePlugInMenuItem(tools[i], listener, null));
			}
			buttonMap.get(ButtonSet.PLUGIN_EXTEND).addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					JButton btn = buttonMap.get(ButtonSet.PLUGIN_EXTEND);
					pluginPopupMenu.show(btn, 0, btn.getHeight());
				}
			});
		}
		return subbar;
	}

	private JButton makePlugInButtons(final IPlugIn plugin, final ActionListener listener) {
		JButton button = new JButton(plugin.getLabel(), null);
		button.setToolTipText(plugin.getDescription());
		button.setBorderPainted(false);
		button.setOpaque(false);
		button.setFocusable(false);
		button.setPreferredSize(new Dimension(89,20));
		button.setActionCommand("PLUGIN:" + plugin.getActionCommand());
		button.addActionListener(listener);
		button.setEnabled(hasTargetApk);
		if(plugin instanceof IExternalTool && !((IExternalTool)plugin).isDecorderTool()) {
			URL iconUrl = plugin.getIconURL();
			if(iconUrl != null) {
				button.setIcon(ImageScaler.getScaledImageIcon(new ImageIcon(iconUrl),16,16));
			}
		}
		return button;
	}

	private JMenuItem makePlugInMenuItem(final IPlugIn plugin, final ActionListener listener, final Resource defaultPorp)
	{
		JMenuItem menuItem = null;
		if(plugin instanceof IPackageSearcher) {
			menuItem = new SearcherCheckBoxMenuItem((IPackageSearcher)plugin);
			URL iconUrl = plugin.getIconURL();
			if(iconUrl != null) {
				menuItem.setIcon(ImageScaler.getScaledImageIcon(new ImageIcon(iconUrl),16,16));
			}
			menuItem.addActionListener(listener);
		} else {
			menuItem = new JMenuItem();
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					boolean isSaveDefault = false;
					for(Component c: ((JMenuItem)e.getSource()).getParent().getComponents()) {
						if(c instanceof NoCloseCheckBoxMenuItem) {
							if(CMD_SELECT_DEFAULT_MENU.equals(((JCheckBoxMenuItem)c).getActionCommand())) {
								isSaveDefault = ((JCheckBoxMenuItem)c).isSelected();
							}
						}
					}
					if(isSaveDefault && defaultPorp != null) {
						String value = e.getActionCommand().replaceAll(".*:", "");
						switch(defaultPorp) {
						case PROP_DEFAULT_DECORDER:
							Resource.PROP_DEFAULT_DECORDER.setData(value);
							break;
						case PROP_DEFAULT_SEARCHER:
							Resource.PROP_DEFAULT_SEARCHER.setData(value);
							break;
						case PROP_DEFAULT_EXPLORER:
							Resource.PROP_DEFAULT_EXPLORER.setData(value);
							break;
						case PROP_DEFAULT_LAUNCH_MODE:
							Resource.PROP_DEFAULT_LAUNCH_MODE.setData(value);
							break;
						default:
							break;
						};
					}
					listener.actionPerformed(e);
				}
			});
		}
		menuItem.setText(plugin.getLabel());
		//menuItem.setIcon(icon);
		menuItem.setToolTipText(plugin.getDescription());
		menuItem.setActionCommand("PLUGIN:" + plugin.getActionCommand());

		return menuItem;
	}

	private JMenu makeSearcherSelectMenu(final String label, final IPackageSearcher[] searchers, final ActionListener listener) {
		final JMenu menu = new JMenu(label);

		final JCheckBoxMenuItem selVisible = new NoCloseCheckBoxMenuItem();
		//selVisible.setActionCommand(CMD_SELECT_DEFAULT_MENU);
		selVisible.setText(Resource.STR_MENU_VISIBLE_TO_BASIC_EACH.getString());
		selVisible.setEnabled((boolean)Resource.PROP_VISIBLE_TO_BASIC.getData());
		selVisible.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				for(Component c: menu.getMenuComponents()) {
					if(c.equals(selVisible)) continue;
					if(c instanceof SearcherCheckBoxMenuItem) {
						SearcherCheckBoxMenuItem ckbox = (SearcherCheckBoxMenuItem)c;
						ckbox.setSelecteMode(selVisible.isSelected());
					}
				}
			}
		});

		for(IPackageSearcher searcher: searchers) {
			menu.add(makePlugInMenuItem(searcher, listener, null));
		}

		menu.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(MenuEvent arg0) {
				for(Component c: menu.getMenuComponents()) {
					if(c instanceof NoCloseCheckBoxMenuItem) {
						((JCheckBoxMenuItem)c).setSelected(false);
						((JCheckBoxMenuItem)c).setEnabled((boolean)Resource.PROP_VISIBLE_TO_BASIC.getData());
					} else if(c instanceof SearcherCheckBoxMenuItem) {
						((SearcherCheckBoxMenuItem)c).setSelecteMode(false);
					}
				}
			}

			@Override
			public void menuDeselected(MenuEvent arg0) {
				//Log.v("menuDeselected");
			}

			@Override
			public void menuCanceled(MenuEvent arg0) { }
		});

		menu.addSeparator();
		menu.add(selVisible);

		return menu;
	}

	public void setReplacementLayout() {
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

		JToolBar subbar = makeSubToolBar();
		subbar.add(buttonMap.get(ButtonSet.OPEN_CODE));
		subbar.add(buttonMap.get(ButtonSet.SEARCH));
		subbar.add(buttonMap.get(ButtonSet.EXPLORER));
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

		if(pluginToolBar != null) {
			add(getNewSeparator(JSeparator.VERTICAL, sepSize));
			add(pluginToolBar);
		}

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
		setButtonText(ButtonSet.PLUGIN_EXTEND, Resource.STR_BTN_MORE.getString(), Resource.STR_BTN_MORE_LAB.getString());
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
		setMenuItemText(MenuItemSet.CLEAR_DATA, Resource.STR_MENU_CLEAR_DATA.getString(), null);
		setMenuItemText(MenuItemSet.INSTALLED_CHECK, Resource.STR_MENU_CHECK_INSTALLED.getString(), null);
		setMenuItemText(MenuItemSet.DECODER_JD_GUI, Resource.STR_MENU_DECODER_JD_GUI.getString(), null);
		setMenuItemText(MenuItemSet.DECODER_JADX_GUI, Resource.STR_MENU_DECODER_JADX_GUI.getString(), null);
		setMenuItemText(MenuItemSet.DECODER_BYTECODE, Resource.STR_MENU_DECODER_BYTECODE.getString(), null);
		setMenuItemText(MenuItemSet.SEARCH_RESOURCE, Resource.STR_MENU_SEARCH_RESOURCE.getString(), null);
		setMenuItemText(MenuItemSet.EXPLORER_ARCHIVE, Resource.STR_MENU_EXPLORER_ARCHIVE.getString(), null);
		setMenuItemText(MenuItemSet.EXPLORER_FOLDER, Resource.STR_MENU_EXPLORER_FOLDER.getString(), null);
		setMenuItemText(MenuItemSet.LAUNCH_LAUNCHER, Resource.STR_MENU_LAUNCH_LAUNCHER.getString(), null);
		setMenuItemText(MenuItemSet.LAUNCH_SELECT, Resource.STR_MENU_LAUNCH_SELECT.getString(), null);
		setMenuItemText(MenuItemSet.SELECT_DEFAULT, Resource.STR_MENU_SELECT_DEFAULT.getString(), null);
	}

	public void setEnabledAt(ButtonSet buttonId, boolean enabled)
	{
		switch(buttonId) {
		case ALL:
			for(ButtonSet bs: ButtonSet.values()) {
				buttonMap.get(bs).setEnabled(enabled);
			}
			if(pluginToolBar != null) {
				if(pluginToolBar instanceof JButton) {
					pluginToolBar.setEnabled(enabled);
				} else {
					for(Component c: pluginToolBar.getComponents()) {
						c.setEnabled(enabled);
					}
				}
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

			buttonMap.get(ButtonSet.PLUGIN_EXTEND).setEnabled(enabled);
			if(pluginToolBar != null) {
				if(pluginToolBar instanceof JButton) {
					pluginToolBar.setEnabled(enabled);
				} else {
					for(Component c: pluginToolBar.getComponents()) {
						c.setEnabled(enabled);
					}
				}
			}
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
