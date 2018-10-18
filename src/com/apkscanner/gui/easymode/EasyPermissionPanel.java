package com.apkscanner.gui.easymode;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.apkscanner.gui.easymode.test.FlatPanel;


public class EasyPermissionPanel extends FlatPanel{
	
	static private Color bordercolor = new Color(242, 242, 242);
	
	
	public EasyPermissionPanel() {
		// TODO Auto-generated constructor stub
		setBackground(bordercolor);
		//permissionpanel = getContentPanel(); 
		//add(permissionpanel, BorderLayout.CENTER);
		setLayout(new FlowLayout(FlowLayout.LEFT, 2, 1));
		
		FlatPanel permissionicon = new FlatPanel();
		permissionicon.setPreferredSize(new Dimension(40, 40));
		permissionicon.setshadowlen(3);
		permissionicon.add(new JLabel(new ImageIcon(System.getProperty("user.dir") + "/res/icons/perm_group_storage.png")));
		add(permissionicon);

		permissionicon = new FlatPanel();
		permissionicon.setPreferredSize(new Dimension(40, 40));
		permissionicon.setshadowlen(3);
		permissionicon.add(new JLabel(new ImageIcon(System.getProperty("user.dir") + "/res/icons/perm_group_contacts.png")));
		add(permissionicon);
		
		permissionicon = new FlatPanel();
		permissionicon.setPreferredSize(new Dimension(40, 40));
		permissionicon.setshadowlen(3);
		permissionicon.add(new JLabel(new ImageIcon(System.getProperty("user.dir") + "/res/icons/perm_group_sms.png")));
		add(permissionicon);
		
		permissionicon = new FlatPanel();
		permissionicon.setPreferredSize(new Dimension(40, 40));
		permissionicon.setshadowlen(3);
		permissionicon.add(new JLabel(new ImageIcon(System.getProperty("user.dir") + "/res/icons/perm_group_personal_info.png")));
		add(permissionicon);
		//permissionpanel.setBorder(new BevelBorder(BevelBorder.RAISED));
		//permissionpanel.setBorder(new FlatBorder(BevelBorder.RAISED));
		
		//setBackground(Color.RED);
		setPreferredSize(new Dimension(0, 50));
	}

}
