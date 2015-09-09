package com.apkscanner.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JToolBar;

import com.apkscanner.resource.Resource;


public class ToolBar extends JToolBar
{
	private static final long serialVersionUID = 894134416480807167L;

	private JPopupMenu openPopupMenu;
	private JMenu openPopupSubMenu;
	private JPopupMenu installPopupMenu;
	
	private ActionListener listener;
	
	private HashMap<ButtonSet, JButton> buttonMap = new HashMap<ButtonSet, JButton>(); 
	
    public enum ButtonSet
    {
    	OPEN			(Type.HOVER, null, Resource.IMG_TOOLBAR_OPEN.getImageIcon(ButtonSet.IconSize, ButtonSet.IconSize)),
    	OPEN_EXTEND		(Type.EXTEND, null, Resource.IMG_TOOLBAR_OPEN_ARROW.getImageIcon(10,10)),
    	MANIFEST		(Type.HOVER, null, Resource.IMG_TOOLBAR_MANIFEST.getImageIcon(ButtonSet.IconSize, ButtonSet.IconSize)),
    	EXPLORER		(Type.HOVER, null, Resource.IMG_TOOLBAR_EXPLORER.getImageIcon(ButtonSet.IconSize, ButtonSet.IconSize)),
    	INSTALL			(Type.HOVER, null, Resource.IMG_TOOLBAR_INSTALL.getImageIcon(ButtonSet.IconSize, ButtonSet.IconSize)),
    	INSTALL_EXTEND	(Type.EXTEND, null, Resource.IMG_TOOLBAR_OPEN_ARROW.getImageIcon(10,10)),
    	SETTING			(Type.HOVER, null, Resource.IMG_TOOLBAR_SETTING.getImageIcon(ButtonSet.IconSize, ButtonSet.IconSize)),
    	ABOUT			(Type.HOVER, null, Resource.IMG_TOOLBAR_ABOUT.getImageIcon(ButtonSet.IconSize, ButtonSet.IconSize)),
    	ALL				(Type.NONE, null, null),
    	NEED_TARGET_APK	(Type.NONE, null, null);

    	private enum Type {
    		NONE, NORMAL, HOVER, EXTEND
    	}
    	
    	static private final int IconSize = 40;

    	private Type type = null;
    	private String text = null;
    	private ImageIcon icon = null;
    	private ImageIcon hoverIcon = null;
    	
    	ButtonSet(Type type, String text, ImageIcon icon)
    	{
    		this(type, text, icon, icon);
    	}
    	
    	ButtonSet(Type type, String text, ImageIcon icon, ImageIcon hoverIcon)
    	{
    		this.type = type;
    		this.text = text;
    		this.icon = icon;
    		this.hoverIcon = hoverIcon;
    	}
    	
    	public boolean matchActionEvent(ActionEvent e)
    	{
    		return this.toString().equals(e.getActionCommand());
    	}

    	private JButton getButton(ActionListener listener)
    	{
    		JButton button = null;
    		switch(type) {
    		case NORMAL:
    			button = new JButton(text, icon);
    			break;
    		case HOVER:
    			button = new ToolBarButton(text, icon, hoverIcon, null);
    			button.setVerticalTextPosition(JLabel.BOTTOM);
    			button.setHorizontalTextPosition(JLabel.CENTER);
    			button.setBorderPainted(false);
    			button.setOpaque(false);
    			button.setFocusable(false);
    			button.setPreferredSize(new Dimension(63,65));
    			break;
    		case EXTEND:
    			button = new JButton(text, icon);
    			button.setMargin(new Insets(27,0,27,0));
    			button.setBorderPainted(false);
    			button.setOpaque(false);
    			button.setFocusable(false);
    			break;
    		default:
    			return null;
    		}
			button.setActionCommand(this.toString());
			button.addActionListener(listener);
    		
    		return button;
    	}
    }
	
    static private class ToolBarButton extends JButton
    {
    	private static final long serialVersionUID = -6788392217820751244L;
    	public ImageIcon  mHoverIcon, mIcon;

    	public ToolBarButton(String text, ImageIcon icon, ImageIcon hoverIcon, ActionListener listener)
    	{
    		super(text, icon);
    		this.mHoverIcon = hoverIcon;
    		this.mIcon = icon;

    		this.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent evt) {
                	setIcon(mHoverIcon);
                }
                public void mouseExited(MouseEvent evt) {
                	setIcon(mIcon);
                }
            });
    	}
    }
    
    public ToolBar(ActionListener listener)
    {
        initUI(listener);
    }

    public final void initUI(ActionListener listener)
    {
        this.listener = listener;
        
        for(ButtonSet bs: ButtonSet.values()) {
        	buttonMap.put(bs, bs.getButton(listener));
        }
        
        openPopupMenu = new JPopupMenu("Menu");
        openPopupSubMenu = new JMenu(Resource.STR_MENU_NEW.getString());
        ButtonSet.OPEN_EXTEND.getButton(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
            	JButton btn = buttonMap.get(ButtonSet.OPEN_EXTEND);
            	openPopupMenu.show(btn, btn.getWidth()/2, btn.getHeight());
            }
        });

        installPopupMenu = new JPopupMenu("Menu");      
        ButtonSet.INSTALL_EXTEND.getButton(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
            	JButton btn = buttonMap.get(ButtonSet.INSTALL_EXTEND);
            	installPopupMenu.show(btn, btn.getWidth()/2, btn.getHeight());
            }
        } );
        
        reloadResource();
        
        Dimension sepSize = new Dimension(2,65);
              
        add(buttonMap.get(ButtonSet.OPEN));
        add(buttonMap.get(ButtonSet.OPEN_EXTEND));
        
        add(getNewSeparator(JSeparator.VERTICAL, sepSize));
                
        add(buttonMap.get(ButtonSet.MANIFEST));
        add(buttonMap.get(ButtonSet.EXPLORER));
        
        add(getNewSeparator(JSeparator.VERTICAL, sepSize));

        add(buttonMap.get(ButtonSet.INSTALL));
        add(buttonMap.get(ButtonSet.INSTALL_EXTEND));
        
        add(getNewSeparator(JSeparator.VERTICAL, sepSize));

        add(buttonMap.get(ButtonSet.SETTING));
        add(getNewSeparator(JSeparator.VERTICAL, sepSize));
        
        add(buttonMap.get(ButtonSet.ABOUT));

        setAlignmentX(0);
        setFloatable(false);
        setOpaque(false);

        setLayout(new FlowLayout(FlowLayout.LEFT));
    }

    private JSeparator getNewSeparator(int orientation, Dimension size)
    {
        JSeparator temp = new JSeparator(orientation);
        temp.setBackground(Color.gray);
        temp.setForeground(Color.gray);
        temp.setPreferredSize(size);
    	return temp;
    }
    
    private void setButtonText(ButtonSet buttonSet, String text, String tipText)
    {
    	buttonMap.get(buttonSet).setText(text);
    	buttonMap.get(buttonSet).setToolTipText(tipText);
    }
       
    public void reloadResource()
    {
    	setButtonText(ButtonSet.OPEN, Resource.STR_BTN_OPEN.getString(), Resource.STR_BTN_OPEN_LAB.getString());
    	setButtonText(ButtonSet.MANIFEST, Resource.STR_BTN_MANIFEST.getString(), Resource.STR_BTN_MANIFEST_LAB.getString());
    	setButtonText(ButtonSet.EXPLORER, Resource.STR_BTN_EXPLORER.getString(), Resource.STR_BTN_EXPLORER_LAB.getString());
    	setButtonText(ButtonSet.INSTALL, Resource.STR_BTN_INSTALL.getString(), Resource.STR_BTN_INSTALL_LAB.getString());
    	setButtonText(ButtonSet.SETTING, Resource.STR_BTN_SETTING.getString(), Resource.STR_BTN_SETTING_LAB.getString());
    	setButtonText(ButtonSet.ABOUT, Resource.STR_BTN_ABOUT.getString(), Resource.STR_BTN_ABOUT_LAB.getString());
        
        openPopupSubMenu.setText(Resource.STR_MENU_NEW.getString());
        openPopupSubMenu.removeAll();
        openPopupSubMenu.add(Resource.STR_MENU_NEW_WINDOW.getString()).addActionListener(listener);
        openPopupSubMenu.add(Resource.STR_MENU_NEW_APK_FILE.getString()).addActionListener(listener);
        openPopupSubMenu.add(Resource.STR_MENU_NEW_PACKAGE.getString()).addActionListener(listener);

        openPopupMenu.removeAll();
        openPopupMenu.add(openPopupSubMenu);
        openPopupMenu.add(getNewSeparator(JSeparator.HORIZONTAL, new Dimension(1,1)));
        openPopupMenu.add(Resource.STR_MENU_APK_FILE.getString()).addActionListener(listener);
        openPopupMenu.add(Resource.STR_MENU_PACKAGE.getString()).addActionListener(listener);
      
        installPopupMenu.removeAll();
        installPopupMenu.add(Resource.STR_MENU_INSTALL.getString()).addActionListener(listener);
        installPopupMenu.add(Resource.STR_MENU_CHECK_INSTALLED.getString()).addActionListener(listener);
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
    	default:
    		break;
    	}
    }
}
