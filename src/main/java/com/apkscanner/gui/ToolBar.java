package com.apkscanner.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.MatteBorder;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import com.apkscanner.resource.RComp;
import com.apkscanner.resource.RConst;
import com.apkscanner.resource.RImg;
import com.apkscanner.resource.RProp;
import com.apkscanner.resource.RStr;
import com.apkspectrum.logback.Log;
import com.apkspectrum.plugin.ExternalTool;
import com.apkspectrum.plugin.PackageSearcher;
import com.apkspectrum.plugin.PlugIn;
import com.apkspectrum.plugin.PlugInManager;
import com.apkspectrum.swing.ExtensionButton;
import com.apkspectrum.swing.NoCloseCheckBoxMenuItem;
import com.apkspectrum.swing.UIAction;

public class ToolBar extends JToolBar {
    private static final long serialVersionUID = 894134416480807167L;

    public static final int FLAG_LAYOUT_NONE = 0x00; // Open file
    public static final int FLAG_LAYOUT_DEVICE_CONNECTED = 0x01;// Open package
    public static final int FLAG_LAYOUT_INSTALLED = 0x02; // Install
    public static final int FLAG_LAYOUT_INSTALLED_LOWER = 0x04; // Downgrade
    public static final int FLAG_LAYOUT_INSTALLED_UPPER = 0x08; // Update
    public static final int FLAG_LAYOUT_LAUNCHER = 0x10; // Launcher
    public static final int FLAG_LAYOUT_UNSIGNED = 0x20; // Sign
    public static final int FLAG_LAYOUT_NO_SUCH_CLASSES = 0x40;

    public static final int FLAG_LAYOUT_INSTALLED_MASK = FLAG_LAYOUT_INSTALLED
            | FLAG_LAYOUT_INSTALLED_LOWER | FLAG_LAYOUT_INSTALLED_UPPER | FLAG_LAYOUT_LAUNCHER;

    static final String CMD_SELECT_DEFAULT_MENU = "CMD_SELECT_DEFAULT_MENU";

    private int flag = 0;
    private boolean hasTargetApk = false;
    private boolean hasDevice = false;

    private Map<ButtonSet, JButton> buttonMap;
    private Map<MenuItemSet, JMenuItem> menuItemMap;
    private JPopupMenu openPopupMenu;
    private JPopupMenu installPopupMenu;
    private JPopupMenu pluginPopupMenu;
    private JComponent pluginToolBar;
    private JPopupMenu decordePopupMenu;
    private JPopupMenu searchPopupMenu;
    private JPopupMenu explorerPopupMenu;
    private JPopupMenu launchPopupMenu;

    private ActionListener listener;

    public ToolBar(ActionListener listener) {
        this.listener = listener;
        initUI(listener);
    }

    public final void initUI(ActionListener listener) {
        Log.i("ToolBar.initUI() start");
        setOpaque(true);
        setFloatable(false);
        setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
        setBorder(new MatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        openPopupMenu = new JPopupMenu();
        installPopupMenu = new JPopupMenu();
        pluginPopupMenu = new JPopupMenu();

        decordePopupMenu = new JPopupMenu();
        searchPopupMenu = new JPopupMenu();
        explorerPopupMenu = new JPopupMenu();
        launchPopupMenu = new JPopupMenu();

        Log.i("ToolBar.initUI() MenuItemSet init");
        menuItemMap = MenuItemSet.getButtonMap(listener);

        JMenuItem SubMenu = openPopupMenu.add((JMenu) menuItemMap.get(MenuItemSet.NEW_WINDOW));
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
        buttonMap.get(ButtonSet.OPEN).setPreferredSize(new Dimension(55, 65));
        buttonMap.get(ButtonSet.OPEN_PACKAGE).setPreferredSize(new Dimension(55, 65));

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

        setExtensionMenu(buttonMap.get(ButtonSet.OPEN_CODE), decordePopupMenu,
                RProp.DEFAULT_DECORDER);
        setExtensionMenu(buttonMap.get(ButtonSet.SEARCH), searchPopupMenu, RProp.DEFAULT_SEARCHER);
        setExtensionMenu(buttonMap.get(ButtonSet.EXPLORER), explorerPopupMenu,
                RProp.DEFAULT_EXPLORER);
        setExtensionMenu(buttonMap.get(ButtonSet.SUB_LAUNCH), launchPopupMenu,
                RProp.DEFAULT_LAUNCH_MODE);

        ButtonSet.setArrowVisible(buttonMap, RProp.B.ALWAYS_TOOLBAR_EXTENDED.get());

        KeyboardFocusManager ky = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        ky.addKeyEventDispatcher(new KeyEventDispatcher() {
            private boolean isShiftPressed = false;

            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getID() == KeyEvent.KEY_PRESSED && !isShiftPressed) {
                    if (e.getModifiersEx() == RConst.SHIFT_MASK) {
                        isShiftPressed = true;
                        if (!RProp.B.ALWAYS_TOOLBAR_EXTENDED.get())
                            ButtonSet.setArrowVisible(buttonMap, true);
                        setButtonText(ButtonSet.OPEN, RStr.MENU_NEW.get(), RStr.BTN_OPEN_LAB.get());
                        setButtonText(ButtonSet.OPEN_PACKAGE, RStr.MENU_NEW.get(),
                                RStr.BTN_OPEN_PACKAGE_LAB.get());
                        setButtonText(ButtonSet.MANIFEST, RStr.BTN_MANIFEST_SAVE_AS.get(),
                                RStr.BTN_MANIFEST_LAB.get());
                        setButtonText(ButtonSet.LAUNCH, RStr.BTN_LAUNCH_SELECT.get(),
                                RStr.BTN_LAUNCH_LAB.get());

                        invokeMouseEvent(e, MouseEvent.MOUSE_ENTERED);
                    }
                } else if (e.getID() == KeyEvent.KEY_RELEASED && isShiftPressed) {
                    if (e.getModifiersEx() != RConst.SHIFT_MASK) {
                        isShiftPressed = false;
                        if (!RProp.B.ALWAYS_TOOLBAR_EXTENDED.get())
                            ButtonSet.setArrowVisible(buttonMap, false);
                        setButtonText(ButtonSet.OPEN, RStr.BTN_OPEN.get(), RStr.BTN_OPEN_LAB.get());
                        setButtonText(ButtonSet.OPEN_PACKAGE, RStr.BTN_OPEN_PACKAGE.get(),
                                RStr.BTN_OPEN_PACKAGE_LAB.get());
                        setButtonText(ButtonSet.MANIFEST, RStr.BTN_MANIFEST.get(),
                                RStr.BTN_MANIFEST_LAB.get());
                        setButtonText(ButtonSet.LAUNCH, RStr.BTN_LAUNCH.get(),
                                RStr.BTN_LAUNCH_LAB.get());

                        invokeMouseEvent(e, MouseEvent.MOUSE_EXITED);
                    }
                }
                return false;
            }

            private void invokeMouseEvent(KeyEvent e, int mouseEvent) {
                Point p = MouseInfo.getPointerInfo().getLocation();
                SwingUtilities.convertPointFromScreen(p, ToolBar.this);
                if (p != null) {
                    Component c = ToolBar.this.getComponentAt(p);
                    if (c != null) {
                        if (c instanceof ExtensionButton) {
                            ((ExtensionButton) c).dispatchEvent(new MouseEvent(c, mouseEvent,
                                    e.getWhen() + 10, 0, p.x, p.y, 0, false));
                        } else if (c instanceof JToolBar) {
                            p = SwingUtilities.convertPoint(ToolBar.this, p, (JToolBar) c);
                            c = ((JToolBar) c).getComponentAt(p);
                            if (c instanceof ExtensionButton) {
                                ((ExtensionButton) c).dispatchEvent(new MouseEvent(c, mouseEvent,
                                        e.getWhen() + 10, e.getModifiersEx(), p.x, p.y, 0, false));
                                if (mouseEvent == MouseEvent.MOUSE_EXITED) {
                                    ((ExtensionButton) c).dispatchEvent(new MouseEvent(c,
                                            MouseEvent.MOUSE_ENTERED, e.getWhen() + 20,
                                            e.getModifiersEx(), p.x, p.y, 0, false));
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
        if (listeners == null) {
            listeners = menu.getMouseListeners();
            if (listeners == null || listeners.length == 0) return;
        }
        Component[] children =
                menu instanceof JMenu ? ((JMenu) menu).getMenuComponents() : menu.getComponents();
        if (children == null) return;
        for (Component c : children) {
            for (MouseListener listen : listeners)
                c.addMouseListener(listen);
            if (c instanceof Container) {
                setMouseEvent((Container) c, listeners);
            }
        }
    }

    public void onLoadPlugin() {
        ExternalTool[] tools = PlugInManager.getDecorderTool();
        if (tools.length > 0) {
            decordePopupMenu.removeAll();
            decordePopupMenu.add(menuItemMap.get(MenuItemSet.DECODER_JD_GUI));
            decordePopupMenu.add(menuItemMap.get(MenuItemSet.DECODER_JADX_GUI));
            decordePopupMenu.add(menuItemMap.get(MenuItemSet.DECODER_BYTECODE));
            decordePopupMenu.addSeparator();
            for (ExternalTool tool : tools) {
                decordePopupMenu.add(makePlugInMenuItem(tool));
            }
            decordePopupMenu.addSeparator();
            decordePopupMenu.add(makeSelectDefaultMenuItem());
            setMouseEvent(decordePopupMenu, null);
        }

        PackageSearcher[] searchers = PlugInManager.getPackageSearchers();
        if (searchers.length > 0) {
            searchPopupMenu.removeAll();
            searchPopupMenu.add(menuItemMap.get(MenuItemSet.SEARCH_RESOURCE));
            searchPopupMenu.addSeparator();

            searchers =
                    PlugInManager.getPackageSearchers(PackageSearcher.SEARCHER_TYPE_PACKAGE_NAME);
            JMenu searchersMenu = makeSearcherSelectMenu(RComp.MENU_TOOLBAR_SEARCH_BY_PACKAGE,
                    searchers, listener);
            if (searchersMenu != null) {
                searchPopupMenu.add(searchersMenu);
            }

            searchers = PlugInManager.getPackageSearchers(PackageSearcher.SEARCHER_TYPE_APP_NAME);
            searchersMenu =
                    makeSearcherSelectMenu(RComp.MENU_TOOLBAR_SEARCH_BY_NAME, searchers, listener);
            if (searchersMenu != null) {
                searchPopupMenu.add(searchersMenu);
            }

            searchPopupMenu.addSeparator();
            final JCheckBoxMenuItem v2bMenuItem = new JCheckBoxMenuItem();
            RComp.MENU_TOOLBAR_TO_BASIC_INFO.set(v2bMenuItem);
            v2bMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    RProp.B.VISIBLE_TO_BASIC.set(v2bMenuItem.isSelected());
                }
            });
            v2bMenuItem.setSelected(RProp.B.VISIBLE_TO_BASIC.get());
            searchPopupMenu.add(v2bMenuItem);

            setMouseEvent(searchPopupMenu, null);
        }

        pluginToolBar = makePluginToolBar();
        if (pluginToolBar != null) {
            setReplacementLayout();
        }
    }

    public void setBadgeCount(int count) {
        ((ExtensionButton) buttonMap.get(ButtonSet.ABOUT)).setBadge(count);;
    }

    public void setFlag(int flag) {
        int preFlag = this.flag;
        if (flag != FLAG_LAYOUT_DEVICE_CONNECTED) {
            this.flag &= ~FLAG_LAYOUT_INSTALLED_MASK;
        }
        this.flag |= flag;
        Log.v("setFlag() preFlag " + Integer.toHexString(preFlag) + ", newFlag "
                + Integer.toHexString(this.flag));
        if (preFlag != this.flag) {
            setReplacementLayout();
        }
    }

    public void unsetFlag(int flag) {
        int preFlag = this.flag;
        if (flag == FLAG_LAYOUT_DEVICE_CONNECTED || (FLAG_LAYOUT_INSTALLED_MASK & flag) != 0) {
            this.flag &= ~FLAG_LAYOUT_INSTALLED_MASK;
        }
        this.flag &= ~flag;
        if (preFlag != this.flag) {
            setReplacementLayout();
        }
    }

    public void clearFlag() {
        if (flag != 0) {
            flag = 0;
            setReplacementLayout();
        }
    }

    public boolean isSetFlag(int flag) {
        return ((this.flag & flag) == flag);
    }

    private void setExtensionMenu(final JButton button, final JPopupMenu popupMenu,
            final RProp defaultPorp) {
        Icon icon = null;
        switch (defaultPorp) {
            case DEFAULT_DECORDER:
                icon = RImg.TOOLBAR_OPENCODE.getImageIcon(ButtonSet.SUBICON_SIZE,
                        ButtonSet.SUBICON_SIZE);
                break;
            case DEFAULT_SEARCHER:
                icon = RImg.TOOLBAR_SEARCH.getImageIcon(ButtonSet.SUBICON_SIZE,
                        ButtonSet.SUBICON_SIZE);
                break;
            case DEFAULT_EXPLORER:
                icon = RImg.TOOLBAR_EXPLORER.getImageIcon(ButtonSet.SUBICON_SIZE,
                        ButtonSet.SUBICON_SIZE);
                break;
            case DEFAULT_LAUNCH_MODE:
                icon = RImg.TOOLBAR_LAUNCH.getImageIcon(ButtonSet.SUBICON_SIZE,
                        ButtonSet.SUBICON_SIZE);
                break;
            default:
                break;
        };
        final Icon defIcon = icon;

        popupMenu.putClientProperty(RConst.MENU_OWNER_KEY, button);
        popupMenu.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
                String value = ":" + defaultPorp.get();
                for (Component c : popupMenu.getComponents()) {
                    if (c instanceof NoCloseCheckBoxMenuItem) {
                        if (CMD_SELECT_DEFAULT_MENU
                                .equals(((JCheckBoxMenuItem) c).getActionCommand())) {
                            ((JCheckBoxMenuItem) c).setSelected(false);
                        }
                    } else if (c instanceof JMenuItem) {
                        if (((JMenuItem) c).getActionCommand().endsWith(value)) {
                            ((JMenuItem) c).setIcon(defIcon);
                        } else {
                            ((JMenuItem) c).setIcon(null);
                        }
                    }
                }
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {}

            @Override
            public void popupMenuCanceled(PopupMenuEvent arg0) {}
        });

        MouseAdapter ma = new MouseAdapter() {
            Timer timer = new Timer();
            TimerTask task = null;

            @Override
            public void mouseEntered(MouseEvent me) {
                // Log.d("mouseEntered 0x" + Integer.toHexString(me.getSource().hashCode()));
                boolean enable = false;
                if (button instanceof ExtensionButton) {
                    enable = ((ExtensionButton) button).getArrowVisible();
                }
                if (enable && !me.isShiftDown() && !RProp.B.ALWAYS_TOOLBAR_EXTENDED.get()) {
                    ButtonSet.setArrowVisible(buttonMap, false);
                    return;
                }
                if (!enable || !button.isEnabled()) return;
                int delayMs = RProp.B.ALWAYS_TOOLBAR_EXTENDED.get() ? 1000 : 100;
                synchronized (timer) {
                    if (task != null) {
                        task.cancel();
                        task = null;
                    }
                    timer.purge();
                    if (!popupMenu.isShowing()) {
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
                // Log.d("mouseClicked");
                mouseExited(arg0);
            }

            @Override
            public void mouseExited(MouseEvent me) {
                // Log.d("mouseExited 0x" + Integer.toHexString(me.getSource().hashCode()));
                if (popupMenu.getMousePosition() != null) return;
                synchronized (timer) {
                    if (task != null) {
                        task.cancel();
                        task = null;
                    }
                    timer.purge();
                    if (popupMenu.isShowing()) {
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
        for (Component c : popupMenu.getComponents()) {
            c.addMouseListener(ma);
        }
    }

    private JCheckBoxMenuItem makeSelectDefaultMenuItem() {
        JCheckBoxMenuItem selDefItem = new NoCloseCheckBoxMenuItem();
        selDefItem.setActionCommand(CMD_SELECT_DEFAULT_MENU);
        RComp.MENU_TOOLBAR_SELECT_DEFAULT.set(selDefItem);
        return selDefItem;
    }

    private JToolBar makeSubToolBar() {
        JToolBar subbar = new JToolBar();
        subbar.setPreferredSize(new Dimension(76, 60));
        subbar.setOpaque(false);
        subbar.setFloatable(false);
        subbar.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        if (!"Windows".equals(UIManager.getLookAndFeel().getName())) {
            subbar.setBorderPainted(false);
        }
        return subbar;
    }

    private JComponent makePluginToolBar() {
        ExternalTool[] tools = PlugInManager.getExternalTool();
        if (tools == null || tools.length <= 0) return null;

        if (tools.length == 1) {
            JButton button = new JButton(PlugInManager.getPlugInAction(tools[0]));
            if (!"Windows".equals(UIManager.getLookAndFeel().getName())) {
                button.setBorderPainted(false);
            }
            button.setOpaque(false);
            button.setFocusable(false);
            button.setVerticalTextPosition(JLabel.BOTTOM);
            button.setHorizontalTextPosition(JLabel.CENTER);
            button.setPreferredSize(new Dimension(68, 65));
            button.setEnabled(hasTargetApk);
            button.setIcon(tools[0].getIcon(40, 40));
            return button;
        }
        JToolBar subbar = makeSubToolBar();
        subbar.setPreferredSize(new Dimension(90, 60));
        if (tools.length <= 3) {
            for (ExternalTool tool : tools) {
                subbar.add(makePlugInButton(tool));
            }
        } else {
            subbar.add(makePlugInButton(tools[0]));
            subbar.add(makePlugInButton(tools[1]));
            subbar.add(buttonMap.get(ButtonSet.PLUGIN_EXTEND));
            pluginPopupMenu.removeAll();
            for (int i = 2; i < tools.length; i++) {
                pluginPopupMenu.add(makePlugInMenuItem(tools[i]));
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

    private JButton makePlugInButton(PlugIn plugin) {
        JButton button = new JButton(PlugInManager.getPlugInAction(plugin));
        if (!"Windows".equals(UIManager.getLookAndFeel().getName())) {
            button.setBorderPainted(false);
        }
        button.setOpaque(false);
        button.setFocusable(false);
        button.setPreferredSize(new Dimension(89, 20));
        button.setEnabled(hasTargetApk);
        if (plugin instanceof ExternalTool && ((ExternalTool) plugin).isDecorderTool()) {
            button.setIcon(null);
        }
        return button;
    }

    private JMenuItem makePlugInMenuItem(final PlugIn plugin) {
        JMenuItem menuItem = null;
        if (plugin instanceof PackageSearcher) {
            menuItem = new SearcherCheckBoxMenuItem((PackageSearcher) plugin);
        } else {
            menuItem = new JMenuItem(PlugInManager.getPlugInAction(plugin));
            menuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    boolean isSaveDefault = false;
                    for (Component c : ((JMenuItem) e.getSource()).getParent().getComponents()) {
                        if (c instanceof NoCloseCheckBoxMenuItem) {
                            if (CMD_SELECT_DEFAULT_MENU
                                    .equals(((AbstractButton) c).getActionCommand())) {
                                isSaveDefault = ((AbstractButton) c).isSelected();
                            }
                        }
                    }
                    if (isSaveDefault) {
                        String value = e.getActionCommand().replaceAll(".*:", "");
                        switch (RProp.DEFAULT_DECORDER) {
                            case DEFAULT_DECORDER:
                                RProp.S.DEFAULT_DECORDER.set(value);
                                break;
                            case DEFAULT_SEARCHER:
                                RProp.S.DEFAULT_SEARCHER.set(value);
                                break;
                            case DEFAULT_EXPLORER:
                                RProp.S.DEFAULT_EXPLORER.set(value);
                                break;
                            case DEFAULT_LAUNCH_MODE:
                                RProp.S.DEFAULT_LAUNCH_MODE.set(value);
                                break;
                            default:
                                break;
                        };
                    }
                }
            });
        }

        return menuItem;
    }

    private JMenu makeSearcherSelectMenu(final RComp res, final PackageSearcher[] searchers,
            final ActionListener listener) {
        final JMenu menu = new JMenu();
        res.set(menu);

        final JCheckBoxMenuItem selVisible = new NoCloseCheckBoxMenuItem();
        // selVisible.setActionCommand(CMD_SELECT_DEFAULT_MENU);
        RComp.MENU_TOOLBAR_TO_BASIC_INFO.set(selVisible);
        selVisible.setEnabled(RProp.B.VISIBLE_TO_BASIC.get());
        selVisible.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                for (Component c : menu.getMenuComponents()) {
                    if (c.equals(selVisible)) continue;
                    if (c instanceof SearcherCheckBoxMenuItem) {
                        SearcherCheckBoxMenuItem ckbox = (SearcherCheckBoxMenuItem) c;
                        ckbox.setSelecteMode(selVisible.isSelected());
                    }
                }
            }
        });

        for (PackageSearcher searcher : searchers) {
            menu.add(makePlugInMenuItem(searcher));
        }

        menu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent arg0) {
                for (Component c : menu.getMenuComponents()) {
                    if (c instanceof NoCloseCheckBoxMenuItem) {
                        ((JCheckBoxMenuItem) c).setSelected(false);
                        ((JCheckBoxMenuItem) c).setEnabled(RProp.B.VISIBLE_TO_BASIC.get());
                    } else if (c instanceof SearcherCheckBoxMenuItem) {
                        ((SearcherCheckBoxMenuItem) c).setSelecteMode(false);
                    }
                }
            }

            @Override
            public void menuDeselected(MenuEvent arg0) {
                // Log.v("menuDeselected");
            }

            @Override
            public void menuCanceled(MenuEvent arg0) {}
        });

        menu.addSeparator();
        menu.add(selVisible);

        return menu;
    }

    public void setReplacementLayout() {
        removeAll();

        Dimension sepSize = new Dimension(1, 63);

        Log.i("ToolBar.setReplacementLayout() flag " + flag);
        if (!isSetFlag(FLAG_LAYOUT_DEVICE_CONNECTED)) {
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
        if (isSetFlag(FLAG_LAYOUT_UNSIGNED)) {
            add(buttonMap.get(ButtonSet.SIGN));
            subbar.add(buttonMap.get(ButtonSet.SUB_LAUNCH));
            subbar.add(buttonMap.get(ButtonSet.SUB_INSTALL));
        } else if (isSetFlag(FLAG_LAYOUT_LAUNCHER)) {
            add(buttonMap.get(ButtonSet.LAUNCH));
            subbar.add(buttonMap.get(ButtonSet.SUB_INSTALL));
            subbar.add(buttonMap.get(ButtonSet.SUB_SIGN));
        } else if (isSetFlag(FLAG_LAYOUT_INSTALLED_LOWER)) {
            add(buttonMap.get(ButtonSet.INSTALL_UPDATE));
            subbar.add(buttonMap.get(ButtonSet.SUB_LAUNCH));
            subbar.add(buttonMap.get(ButtonSet.SUB_SIGN));
        } else if (isSetFlag(FLAG_LAYOUT_INSTALLED_UPPER)) {
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

        if (pluginToolBar != null) {
            add(getNewSeparator(JSeparator.VERTICAL, sepSize));
            add(pluginToolBar);
        }

        add(getNewSeparator(JSeparator.VERTICAL, sepSize));

        add(buttonMap.get(ButtonSet.SETTING));
        add(getNewSeparator(JSeparator.VERTICAL, sepSize));

        add(buttonMap.get(ButtonSet.ABOUT));
    }

    private JSeparator getNewSeparator(int orientation, Dimension size) {
        JSeparator separator = new JSeparator(orientation);
        // separator.setBackground(Color.gray);
        // separator.setForeground(Color.gray);
        separator.setPreferredSize(size);
        return separator;
    }

    private void setButtonText(ButtonSet buttonSet, String text, String tipText) {
        buttonMap.get(buttonSet).setText(text);
        buttonMap.get(buttonSet).setToolTipText(tipText);
    }

    public void setEnabledAt(ButtonSet buttonId, boolean enabled) {
        switch (buttonId) {
            case ALL:
                for (ButtonSet bs : ButtonSet.values()) {
                    buttonMap.get(bs).setEnabled(enabled);
                }
                if (pluginToolBar != null) {
                    if (pluginToolBar instanceof JButton) {
                        pluginToolBar.setEnabled(enabled);
                    } else {
                        for (Component c : pluginToolBar.getComponents()) {
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
            case NEED_TARGET_APK:
                hasTargetApk = enabled;
                buttonMap.get(ButtonSet.OPEN_CODE).setEnabled(enabled);
                buttonMap.get(ButtonSet.SEARCH).setEnabled(enabled);
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
                if (pluginToolBar != null) {
                    if (pluginToolBar instanceof JButton) {
                        pluginToolBar.setEnabled(enabled);
                    } else {
                        for (Component c : pluginToolBar.getComponents()) {
                            c.setEnabled(enabled);
                        }
                    }
                }
            case NEED_TARGET_APEX:
                buttonMap.get(ButtonSet.MANIFEST).setEnabled(enabled);
                buttonMap.get(ButtonSet.EXPLORER).setEnabled(enabled);
                buttonMap.get(ButtonSet.INSTALL).setEnabled(enabled);

            case NEED_DEVICE:
                if (buttonId == ButtonSet.NEED_DEVICE) hasDevice = enabled;
                enabled = hasDevice && hasTargetApk;
                break;
            default:
                buttonMap.get(buttonId).setEnabled(enabled);
                break;
        }
    }

    class SearcherCheckBoxMenuItem extends JCheckBoxMenuItem {
        private static final long serialVersionUID = -6097881007848535633L;

        private PackageSearcher plugin;
        private boolean selectMode = false;
        private Icon icon;

        public SearcherCheckBoxMenuItem(PackageSearcher plugin) {
            super(PlugInManager.getPlugInAction(plugin));
            Action action = getAction();
            action.putValue(UIAction.ICON_SIZE_KEY, new Dimension(16, 16));
            setIcon(icon = (Icon) action.getValue(Action.LARGE_ICON_KEY));
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
        public void addActionListener(final ActionListener listener) {
            super.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (isSelectMode()) {
                        plugin.setVisibleToBasic(isSelected());
                        PlugInManager.saveProperty();
                    } else {
                        listener.actionPerformed(e);
                    }
                }
            });
        }

        @Override
        protected void processMouseEvent(MouseEvent evt) {
            if (selectMode && evt.getID() == MouseEvent.MOUSE_RELEASED
                    && contains(evt.getPoint())) {
                doClick();
                setArmed(true);
            } else {
                super.processMouseEvent(evt);
            }
        }
    }
}
