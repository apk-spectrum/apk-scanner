package com.apkscanner.gui.easymode.contents;

import java.awt.BorderLayout;
import java.awt.CardLayout;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.apkscanner.core.scanner.PermissionGroupManager;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.data.apkinfo.PermissionGroup;
import com.apkscanner.data.apkinfo.PermissionGroupInfo;
import com.apkscanner.data.apkinfo.PermissionInfo;
import com.apkscanner.data.apkinfo.UsesPermissionInfo;
import com.apkscanner.gui.easymode.util.EasyButton;
import com.apkscanner.gui.easymode.util.FlatPanel;
import com.apkscanner.gui.easymode.util.ImageUtils;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;


public class EasyPermissionPanel extends JPanel implements ActionListener{
	
	static private Color bordercolor = new Color(242, 242, 242);
	static private Color dangerouscolor = new Color(181,107,105); 
	static private Color permissionbackgroundcolor = new Color(217,217,217);
	
	static public int HEIGHT = 50;
	static private int SHADOWSIZE = 3;
	static private int PERMISSIONICONSIZE = 43;
	
	private static String CARD_LAYOUT_EMPTY = "card_empty";
	private static String CARD_LAYOUT_APKINFO = "card_apkinfo";
	
	JPanel contentsCardPanel;
	FlatPanel permissionpanel;
	EasyGuiToolPanel toolpanel;
	
	public EasyPermissionPanel() {
		// TODO Auto-generated constructor stub
		
		//permissionpanel = getContentPanel(); 
		//add(permissionpanel, BorderLayout.CENTER);
		setLayout(new BorderLayout());
		
		permissionpanel = new FlatPanel();
		permissionpanel.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 1));		
		permissionpanel.setPreferredSize(new Dimension(0, HEIGHT));		
		permissionpanel.setshadowlen(SHADOWSIZE);
		permissionpanel.setBackground(bordercolor);
		
		contentsCardPanel = new JPanel(new CardLayout());
		contentsCardPanel.add(permissionpanel , CARD_LAYOUT_APKINFO);
		
		
		//setEmptypanel();
		

		
		add(contentsCardPanel);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		Log.d("click permission");
	}

	public void setEmptypanel() {
		Log.d("permission toolpanel=)" + toolpanel);
		
		if(toolpanel == null) {
			toolpanel = new EasyGuiToolPanel(HEIGHT);
			Log.d("permission new (toolpanel=)" + toolpanel);
			contentsCardPanel.add(toolpanel, CARD_LAYOUT_EMPTY);
		}
				
		((CardLayout)contentsCardPanel.getLayout()).show(contentsCardPanel,CARD_LAYOUT_EMPTY);
		
	}
	
	public void setPermission(ApkInfo apkInfo) {
		// TODO Auto-generated method stub		
		if(apkInfo.manifest.usesPermission == null || apkInfo.manifest.usesPermission.length < 1) return;
		
		Log.d(apkInfo.manifest.usesPermission.length+ "");
		PermissionGroupManager permissionGroupManager = new PermissionGroupManager(apkInfo.manifest.usesPermission);
		Set<String> keys = permissionGroupManager.getPermGroupMap().keySet();
		int cnt = 0;
		for(String key: keys) {			
			PermissionGroup g = permissionGroupManager.getPermGroupMap().get(key);
			//permGroup.append(makeHyperLink("@event", g.icon, g.permSummary, g.name, g.hasDangerous?"color:red;":null));			
			FlatPanel permissionicon = new FlatPanel();			
			try {
				ImageIcon imageIcon = new ImageIcon(new URL(g.icon));				
				if(g.hasDangerous)ImageUtils.setcolorImage(imageIcon, dangerouscolor);				
				EasyButton btn = new EasyButton(imageIcon);
				permissionicon.setPreferredSize(new Dimension(PERMISSIONICONSIZE, PERMISSIONICONSIZE));
				permissionicon.setshadowlen(SHADOWSIZE);
				permissionicon.setBackground(permissionbackgroundcolor);
				permissionicon.add(btn);
				btn.addActionListener(this);
				permissionpanel.add(permissionicon);				
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		((CardLayout)contentsCardPanel.getLayout()).show(contentsCardPanel,CARD_LAYOUT_APKINFO);
		validate();		
	}

	public void clear() {
		// TODO Auto-generated method stub
		permissionpanel.removeAll();
	}
}
