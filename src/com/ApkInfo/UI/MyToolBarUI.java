package com.ApkInfo.UI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JToolBar;

import com.ApkInfo.Resource.Resource;
import com.ApkInfo.UIUtil.ToolBarButton;


public class MyToolBarUI extends JPanel implements ActionListener{
	private static final long serialVersionUID = 894134416480807167L;

	private ToolBarButton btn_open;
	private ToolBarButton btn_show_manifest;
	private ToolBarButton btn_show_explorer;
	private ToolBarButton btn_unpack;
	private ToolBarButton btn_pack;
	private ToolBarButton btn_install;
	private ToolBarButton btn_about;
	private ToolBarButton btn_setting;
    
	private JButton btn_open_arrow;
	private JButton btn_install_arrow;
	
	private JPopupMenu openPopupMenu;
	private JMenu openPopupSubMenu;
	private JPopupMenu installPopupMenu;
	
	private ActionListener listener;
	
    public enum ButtonId {
    	OPEN,
    	MANIFEST,
    	EXPLORER,
    	UNPACK,
    	PACK,
    	INSTALL,
    	ABOUT,
    	ALL,
    	NEED_TARGET_APK
    }
	
    public MyToolBarUI(ActionListener listener) {
        initUI(listener);
    }

    public final void initUI(ActionListener listener) {

        JToolBar toolbar1 = new JToolBar();
        
        if(listener == null) listener = this;
        this.listener = listener;

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        int Iconsize = 40;
        
        ImageIcon toolbar_open =  Resource.IMG_TOOLBAR_OPEN.getImageIcon(Iconsize,Iconsize);
        ImageIcon toolbar_manifest=  Resource.IMG_TOOLBAR_MANIFEST.getImageIcon(Iconsize,Iconsize);
        ImageIcon toolbar_explorer =  Resource.IMG_TOOLBAR_EXPLORER.getImageIcon(Iconsize,Iconsize);
        ImageIcon toolbar_unpack  =  Resource.IMG_TOOLBAR_UNPACK.getImageIcon(Iconsize,Iconsize);
        ImageIcon toolbar_pack  =  Resource.IMG_TOOLBAR_PACK.getImageIcon(Iconsize,Iconsize);
        ImageIcon toolbar_install  =  Resource.IMG_TOOLBAR_INSTALL.getImageIcon(Iconsize,Iconsize);
        ImageIcon toolbar_about  =  Resource.IMG_TOOLBAR_ABOUT.getImageIcon(Iconsize,Iconsize);
        ImageIcon toolbar_setting  =  Resource.IMG_TOOLBAR_SETTING.getImageIcon(Iconsize,Iconsize);
        
        //ImageIcon toobar_open_hover  =  Resource.IMG_TOOLBAR_OPEN_HOVER.getImageIcon(Iconsize,Iconsize);
        //ImageIcon toolbar_manifest_hover =  Resource.IMG_TOOLBAR_MANIFEST_HOVER.getImageIcon(Iconsize,Iconsize);
        //ImageIcon toolbar_explorer_hover =  Resource.IMG_TOOLBAR_EXPLORER_HOVER.getImageIcon(Iconsize,Iconsize);
        ImageIcon toolbar_unpack_hover  =  Resource.IMG_TOOLBAR_UNPACK_HOVER.getImageIcon(Iconsize,Iconsize);
        ImageIcon toolbar_pack_hover  =  Resource.IMG_TOOLBAR_PACK_HOVER.getImageIcon(Iconsize,Iconsize);
        //ImageIcon toolbar_install_hover  =  Resource.IMG_TOOLBAR_INSTALL_HOVER.getImageIcon(Iconsize,Iconsize);
        //ImageIcon toolbar_about_hover  =  Resource.IMG_TOOLBAR_ABOUT_HOVER.getImageIcon(Iconsize,Iconsize);
        
        ImageIcon toolbar_open_arrow  =  Resource.IMG_TOOLBAR_OPEN_ARROW.getImageIcon(10,10);
        
        /*
        btn_open = new ToolBarButton(null, toolbar_open, toobar_open_hover, listener);
        btn_show_manifest = new ToolBarButton(null, toolbar_manifest, toolbar_manifest_hover, listener);
        btn_show_explorer = new ToolBarButton(null, toolbar_explorer, toolbar_explorer_hover, listener);
        btn_unpack = new ToolBarButton(null, toolbar_unpack, toolbar_unpack_hover, listener);
        btn_pack = new ToolBarButton(null, toolbar_pack, toolbar_pack_hover, listener);
        btn_install = new ToolBarButton(null, toolbar_install, toolbar_install_hover, listener);
        btn_about = new ToolBarButton(null, toolbar_about, toolbar_about_hover, listener);
        */
        btn_open = new ToolBarButton(null, toolbar_open, toolbar_open, listener);
        btn_show_manifest = new ToolBarButton(null, toolbar_manifest, toolbar_manifest, listener);
        btn_show_explorer = new ToolBarButton(null, toolbar_explorer, toolbar_explorer, listener);
        btn_unpack = new ToolBarButton(null, toolbar_unpack, toolbar_unpack_hover, listener);
        btn_pack = new ToolBarButton(null, toolbar_pack, toolbar_pack_hover, listener);
        btn_install = new ToolBarButton(null, toolbar_install, toolbar_install, listener);
        btn_about = new ToolBarButton(null, toolbar_about, toolbar_about, listener);
        btn_setting = new ToolBarButton(null, toolbar_setting, toolbar_setting, listener);
        
        btn_open_arrow = new JButton(toolbar_open_arrow);
        btn_open_arrow.setMargin(new Insets(27,0,27,0));
        btn_open_arrow.setBorderPainted(false);
        btn_open_arrow.setOpaque(false);
        btn_open_arrow.setFocusable(false);
        
        /*
	private JPopupMenu openPopupMenu;
	private JMenu openPopupSubMenu;
	private JPopupMenu installPopupMenu;
         */
        openPopupMenu = new JPopupMenu("Menu");
        openPopupSubMenu = new JMenu(Resource.STR_MENU_NEW.getString());
        btn_open_arrow.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
            	openPopupMenu.show(btn_open_arrow, btn_open_arrow.getWidth()/2, btn_open_arrow.getHeight());
            }
        } );

        btn_install_arrow = new JButton(toolbar_open_arrow);
        btn_install_arrow.setMargin(new Insets(27,0,27,0));
        btn_install_arrow.setBorderPainted(false);
        btn_install_arrow.setOpaque(false);
        btn_install_arrow.setFocusable(false);
        
        installPopupMenu = new JPopupMenu("Menu");      
        btn_install_arrow.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
            	installPopupMenu.show(btn_install_arrow, btn_install_arrow.getWidth()/2, btn_install_arrow.getHeight());
            }
        } );
        
        reloadResource();       
              
        toolbar1.add(btn_open);
        toolbar1.add(btn_open_arrow);
        
        toolbar1.add(getNewSeparator(JSeparator.VERTICAL, 2));
                
        toolbar1.add(btn_show_manifest);
        toolbar1.add(btn_show_explorer);
        
        toolbar1.add(getNewSeparator(JSeparator.VERTICAL, 2));
        
        //toolbar1.add(btn_unpack);
        //toolbar1.add(btn_pack);
        //toolbar1.add(getNewSeparator(JSeparator.VERTICAL, 2));
        
        toolbar1.add(btn_install);
    

        toolbar1.add(btn_install_arrow);
        
        toolbar1.add(getNewSeparator(JSeparator.VERTICAL, 2));
        
        toolbar1.add(btn_setting);
        toolbar1.add(getNewSeparator(JSeparator.VERTICAL, 2));
        
        toolbar1.add(btn_about);
        
        toolbar1.addSeparator(new Dimension(138,0));

        toolbar1.setAlignmentX(0);
        toolbar1.setFloatable(false);
        toolbar1.setOpaque(false);

        panel.add(toolbar1,BorderLayout.WEST);
        add(panel, BorderLayout.NORTH);

        setLayout(new FlowLayout(FlowLayout.LEFT));

        //setTitle("Toolbars");
        //setSize(360, 250);
        //setLocationRelativeTo(null);
        //setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private JSeparator getNewSeparator(int orientation, int size)
    {
        JSeparator temp = new JSeparator(orientation);
        temp.setBackground(Color.gray);
        temp.setForeground(Color.gray);
        temp.setPreferredSize(new Dimension(size,size));
    	return temp;
    }
        
    public void setEnabledAt(ButtonId buttonId, boolean enabled)
    {
    	switch(buttonId) {
    	case ALL:
    	case OPEN:
    		btn_open.setEnabled(enabled);
    		if(buttonId != ButtonId.ALL) break;
    	case ABOUT:
    		btn_about.setEnabled(enabled);
    		if(buttonId != ButtonId.ALL) break;
    	case NEED_TARGET_APK:
    		buttonId = ButtonId.ALL;
    	case MANIFEST:
    		btn_show_manifest.setEnabled(enabled);
    		if(buttonId != ButtonId.ALL) break;
    	case EXPLORER:
    		btn_show_explorer.setEnabled(enabled);
    		if(buttonId != ButtonId.ALL) break;
    	case UNPACK:
    		btn_unpack.setEnabled(enabled);
    		if(buttonId != ButtonId.ALL) break;
    	case PACK:
    		btn_pack.setEnabled(enabled);
    		if(buttonId != ButtonId.ALL) break;
    	case INSTALL:
    		btn_install.setEnabled(enabled);
    		btn_install_arrow.setEnabled(enabled);
    		if(buttonId != ButtonId.ALL) break;
    	default:
    		break;
    	}
    }
    
    public void reloadResource()
    {
        btn_open.setText(Resource.STR_BTN_OPEN.getString());
        btn_show_manifest.setText(Resource.STR_BTN_MANIFEST.getString());
        btn_show_explorer.setText(Resource.STR_BTN_EXPLORER.getString());
        btn_unpack.setText(Resource.STR_BTN_UNPACK.getString());
        btn_pack.setText(Resource.STR_BTN_PACK.getString());
        btn_install.setText(Resource.STR_BTN_INSTALL.getString());
        btn_about.setText(Resource.STR_BTN_ABOUT.getString());
        btn_setting.setText(Resource.STR_BTN_SETTING.getString());
        
        btn_open.setToolTipText(Resource.STR_BTN_OPEN_LAB.getString());
        btn_show_manifest.setToolTipText(Resource.STR_BTN_MANIFEST_LAB.getString());
        btn_show_explorer.setToolTipText(Resource.STR_BTN_EXPLORER_LAB.getString());
        btn_unpack.setToolTipText(Resource.STR_BTN_UNPACK_LAB.getString());
        btn_pack.setToolTipText(Resource.STR_BTN_PACK_LAB.getString());
        btn_install.setToolTipText(Resource.STR_BTN_INSTALL_LAB.getString());
        btn_about.setToolTipText(Resource.STR_BTN_ABOUT_LAB.getString());
        btn_setting.setToolTipText(Resource.STR_BTN_SETTING_LAB.getString());
        
        openPopupSubMenu.setText(Resource.STR_MENU_NEW.getString());
        openPopupSubMenu.removeAll();
        openPopupSubMenu.add(Resource.STR_MENU_NEW_WINDOW.getString()).addActionListener(listener);
        openPopupSubMenu.add(Resource.STR_MENU_NEW_APK_FILE.getString()).addActionListener(listener);
        openPopupSubMenu.add(Resource.STR_MENU_NEW_PACKAGE.getString()).addActionListener(listener);

        openPopupMenu.removeAll();
        openPopupMenu.add(openPopupSubMenu);
        openPopupMenu.add(getNewSeparator(JSeparator.HORIZONTAL, 1));
        openPopupMenu.add(Resource.STR_MENU_APK_FILE.getString()).addActionListener(listener);
        openPopupMenu.add(Resource.STR_MENU_PACKAGE.getString()).addActionListener(listener);
      
        installPopupMenu.removeAll();
        installPopupMenu.add(Resource.STR_MENU_INSTALL.getString()).addActionListener(listener);
        installPopupMenu.add(Resource.STR_MENU_CHECK_INSTALLED.getString()).addActionListener(listener);
    }

	@Override
	public void actionPerformed(ActionEvent e) {
        JButton b = (JButton) e.getSource();
        
        JOptionPane.showMessageDialog(null, b.getText(), "ToolBar", JOptionPane.INFORMATION_MESSAGE);
	}
}
