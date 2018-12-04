package com.apkscanner.gui.easymode.contents;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.apkscanner.core.scanner.PermissionGroupManager;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.data.apkinfo.PermissionGroup;
import com.apkscanner.gui.easymode.util.EasyButton;
import com.apkscanner.gui.easymode.util.EasyFlatLabel;
import com.apkscanner.gui.easymode.util.FlatPanel;
import com.apkscanner.gui.easymode.util.ImageUtils;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

public class EasyPermissioniconPanel extends FlatPanel implements ActionListener{
	int HEIGHT = 35;
	int WIDTH = 100;
	int BUTTON_IMG_SIZE = 35-6;
	int SHADOW_SIZE = 3;
	
	static private Color bordercolor = new Color(242, 242, 242);
	static private Color dangerouscolor = new Color(181, 107, 105);
	static private Color permissionbackgroundcolor = new Color(217, 217, 217);
	
	JPanel toolbartemppanel;
	JScrollPane scrollPane;	
	EasyButton btnshowpermissiondlg;
	
	int permissionbuttoncount = 0;
	
	public EasyPermissioniconPanel(int height, int width) {
		
		HEIGHT = height - SHADOW_SIZE * 2;
		BUTTON_IMG_SIZE = HEIGHT - SHADOW_SIZE * 2;
		WIDTH = width;
		init();
		setPreferredSize(new Dimension(0, height));		
	}
		
	private void init() {
		setBackground(bordercolor);
		setPreferredSize(new Dimension(0, HEIGHT + SHADOW_SIZE * 2));
		setshadowlen(SHADOW_SIZE);
		
		toolbartemppanel = new JPanel();
		
		scrollPane = new JScrollPane(toolbartemppanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		//scrollPane.setPreferredSize(new Dimension(getWidth(), getHeight()));
		scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(15, 0));
		scrollPane.getVerticalScrollBar().setUnitIncrement(HEIGHT+1);
		
		scrollPane.setBorder(null);
		scrollPane.setOpaque(false);		
		scrollPane.getViewport().setOpaque(false);
		
		scrollPane.addComponentListener(new ComponentListener() {
			@Override
			public void componentShown(ComponentEvent e) {	}
			@Override
			public void componentResized(ComponentEvent e) {
				// TODO Auto-generated method stub				
				WIDTH = scrollPane.getViewport().getWidth();
				int line = (int)((HEIGHT + 1)* permissionbuttoncount / WIDTH);
				toolbartemppanel.setPreferredSize(new Dimension(0, HEIGHT * (line+1) + ((line !=0)?SHADOW_SIZE : 0)));
			}
			@Override
			public void componentMoved(ComponentEvent e) {}
			
			@Override
			public void componentHidden(ComponentEvent e) {}
		});
		
		add(scrollPane, BorderLayout.CENTER);
		toolbartemppanel.setLayout((new FlowLayout(FlowLayout.LEFT,1, 1)));
		toolbartemppanel.setOpaque(false);

		btnshowpermissiondlg = new EasyButton(Resource.IMG_EASY_WINDOW_SHOW_PERMISSION.getImageIcon(15, 15));
		btnshowpermissiondlg.setPreferredSize(new Dimension(15, 15));
		btnshowpermissiondlg.addActionListener(this);
		
		add(btnshowpermissiondlg, BorderLayout.EAST);
		
	}
	
	public void setPermission(ApkInfo apkInfo) {
		// TODO Auto-generated method stub
		permissionbuttoncount = 0;
		if (apkInfo.manifest.usesPermission == null || apkInfo.manifest.usesPermission.length < 1)
			return;

		Log.d(apkInfo.manifest.usesPermission.length + "");
		PermissionGroupManager permissionGroupManager = new PermissionGroupManager(apkInfo.manifest.usesPermission);
		Set<String> keys = permissionGroupManager.getPermGroupMap().keySet();
		int cnt = 0;
		for (String key : keys) {
			PermissionGroup g = permissionGroupManager.getPermGroupMap().get(key);
			// permGroup.append(makeHyperLink("@event", g.icon, g.permSummary,
			// g.name, g.hasDangerous?"color:red;":null));
//for(int i=0; i<11; i++) {
			FlatPanel permissionicon = new FlatPanel();
			try {
				ImageIcon imageIcon = new ImageIcon(new URL(g.icon));
				if (g.hasDangerous)
					ImageUtils.setcolorImage(imageIcon, dangerouscolor);
				EasyButton btn = new EasyButton(imageIcon);
				permissionicon.setPreferredSize(new Dimension(HEIGHT, HEIGHT));
				permissionicon.setshadowlen(SHADOW_SIZE);
				permissionicon.setBackground(permissionbackgroundcolor);
				permissionicon.add(btn);
				btn.addActionListener(this);
				btn.setToolTipText(key);
				toolbartemppanel.add(permissionicon);
				permissionbuttoncount++;
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//}
	
		WIDTH = scrollPane.getViewport().getWidth();
		int line = (int)((HEIGHT + 1)* permissionbuttoncount / WIDTH);
		toolbartemppanel.setPreferredSize(new Dimension(0, HEIGHT * (line+1) + ((line !=0)?SHADOW_SIZE : 0)));
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
		Log.d("tool click");		
	}
	
	public void clear() {
		toolbartemppanel.removeAll();
	}
}
