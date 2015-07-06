package com.ApkInfo.UI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

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
        
        ImageIcon toobar_open_hover  =  Resource.IMG_TOOLBAR_OPEN_HOVER.getImageIcon(Iconsize,Iconsize);
        ImageIcon toolbar_manifest_hover =  Resource.IMG_TOOLBAR_MANIFEST_HOVER.getImageIcon(Iconsize,Iconsize);
        ImageIcon toolbar_explorer_hover =  Resource.IMG_TOOLBAR_EXPLORER_HOVER.getImageIcon(Iconsize,Iconsize);
        ImageIcon toolbar_unpack_hover  =  Resource.IMG_TOOLBAR_UNPACK_HOVER.getImageIcon(Iconsize,Iconsize);
        ImageIcon toolbar_pack_hover  =  Resource.IMG_TOOLBAR_PACK_HOVER.getImageIcon(Iconsize,Iconsize);
        ImageIcon toolbar_install_hover  =  Resource.IMG_TOOLBAR_INSTALL_HOVER.getImageIcon(Iconsize,Iconsize);
        ImageIcon toolbar_about_hover  =  Resource.IMG_TOOLBAR_ABOUT_HOVER.getImageIcon(Iconsize,Iconsize);
        
        btn_open = new ToolBarButton(Resource.STR_BTN_OPEN.getString(), toolbar_open, toobar_open_hover, listener);
        btn_show_manifest = new ToolBarButton(Resource.STR_BTN_MANIFEST.getString(), toolbar_manifest, toolbar_manifest_hover, listener);
        btn_show_explorer = new ToolBarButton(Resource.STR_BTN_EXPLORER.getString(), toolbar_explorer, toolbar_explorer_hover, listener);
        btn_unpack = new ToolBarButton(Resource.STR_BTN_UNPACK.getString(), toolbar_unpack, toolbar_unpack_hover, listener);
        btn_pack = new ToolBarButton(Resource.STR_BTN_PACK.getString(), toolbar_pack, toolbar_pack_hover, listener);
        btn_install = new ToolBarButton(Resource.STR_BTN_INSTALL.getString(), toolbar_install, toolbar_install_hover, listener);
        btn_about = new ToolBarButton(Resource.STR_BTN_ABOUT.getString(), toolbar_about, toolbar_about_hover, listener);

              
        toolbar1.add(btn_open);
        toolbar1.add(getNewSeparator());
                
        toolbar1.add(btn_show_manifest);
        toolbar1.add(btn_show_explorer);
        
        toolbar1.add(getNewSeparator());
        
        toolbar1.add(btn_unpack);
        toolbar1.add(btn_pack);
        
        toolbar1.add(getNewSeparator());
        
        toolbar1.add(btn_install);
    
        toolbar1.add(getNewSeparator());
        
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
    
    private JSeparator getNewSeparator()
    {
        JSeparator temp = new JSeparator(JSeparator.VERTICAL);
        temp.setBackground(Color.gray);
        temp.setForeground(Color.gray);
        temp.setPreferredSize(new Dimension(1,0));
    	return temp;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	MyToolBarUI ex = new MyToolBarUI(null);
                ex.setVisible(true);
            }
        });
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
    		if(buttonId != ButtonId.ALL) break;
    	default:
    		break;
    	}
    }

	@Override
	public void actionPerformed(ActionEvent e) {
        JButton b = (JButton) e.getSource();
        
        JOptionPane.showMessageDialog(null, b.getText(), "ToolBar", JOptionPane.INFORMATION_MESSAGE);
	}
}
