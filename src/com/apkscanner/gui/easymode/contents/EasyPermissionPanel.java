package com.apkscanner.gui.easymode.contents;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.apkscanner.gui.easymode.util.EasyButton;
import com.apkscanner.gui.easymode.util.FlatPanel;
import com.apkscanner.gui.easymode.util.ImageUtils;
import com.apkscanner.util.Log;


public class EasyPermissionPanel extends FlatPanel implements ActionListener{
	
	static private Color bordercolor = new Color(242, 242, 242);
	static private Color dangerouscolor = new Color(181,107,105); 
	static private Color permissionbackgroundcolor = new Color(217,217,217);
	
	static private int HEIGHT = 50;
	static private int SHADOWSIZE = 3;
	static private int PERMISSIONICONSIZE = 43;
	
	public EasyPermissionPanel() {
		// TODO Auto-generated constructor stub
		setBackground(bordercolor);
		//permissionpanel = getContentPanel(); 
		//add(permissionpanel, BorderLayout.CENTER);
		setLayout(new FlowLayout(FlowLayout.LEFT, 2, 1));
		setshadowlen(SHADOWSIZE);
		
		FlatPanel permissionicon = new FlatPanel();

		EasyButton btn = new EasyButton(ImageUtils.setcolorImage(new ImageIcon(System.getProperty("user.dir") + "/res/icons/perm_group_storage.png"), dangerouscolor));
		permissionicon.setPreferredSize(new Dimension(PERMISSIONICONSIZE, PERMISSIONICONSIZE));
		permissionicon.setshadowlen(SHADOWSIZE);
		permissionicon.setBackground(permissionbackgroundcolor);
		permissionicon.add(btn);
		btn.addActionListener(this);
		add(permissionicon);		
		
		permissionicon = new FlatPanel();
		permissionicon.setPreferredSize(new Dimension(PERMISSIONICONSIZE,PERMISSIONICONSIZE));
		permissionicon.setshadowlen(SHADOWSIZE);
		permissionicon.setBackground(permissionbackgroundcolor);
		permissionicon.add(new EasyButton(new ImageIcon(System.getProperty("user.dir") + "/res/icons/perm_group_contacts.png")));
		add(permissionicon);
		
		permissionicon = new FlatPanel();
		permissionicon.setPreferredSize(new Dimension(PERMISSIONICONSIZE, PERMISSIONICONSIZE));
		permissionicon.setshadowlen(SHADOWSIZE);
		permissionicon.setBackground(permissionbackgroundcolor);
		permissionicon.add(new EasyButton(new ImageIcon(System.getProperty("user.dir") + "/res/icons/perm_group_sms.png")));
		add(permissionicon);
		
		permissionicon = new FlatPanel();
		permissionicon.setPreferredSize(new Dimension(PERMISSIONICONSIZE, PERMISSIONICONSIZE));
		permissionicon.setshadowlen(SHADOWSIZE);
		permissionicon.setBackground(permissionbackgroundcolor);
		permissionicon.add(new EasyButton(new ImageIcon(System.getProperty("user.dir") + "/res/icons/perm_group_personal_info.png")));
		add(permissionicon);
		//permissionpanel.setBorder(new BevelBorder(BevelBorder.RAISED));
		//permissionpanel.setBorder(new FlatBorder(BevelBorder.RAISED));
		
		//setBackground(Color.RED);
		setPreferredSize(new Dimension(0, HEIGHT));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		Log.d("click permission");
	}
}
