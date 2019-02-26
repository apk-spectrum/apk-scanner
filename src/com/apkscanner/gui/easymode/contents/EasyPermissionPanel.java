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
import javax.swing.JScrollPane;

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

public class EasyPermissionPanel extends JPanel {

	static private Color bordercolor = new Color(242, 242, 242);
	static private Color dangerouscolor = new Color(181, 107, 105);
	static private Color permissionbackgroundcolor = new Color(217, 217, 217);

	static public int HEIGHT = 40;
	static private int WIDTH = EasyContentsPanel.WIDTH;
	static private int SHADOWSIZE = 1;
	static private int PERMISSIONICONSIZE = HEIGHT - SHADOWSIZE * 2;

	private static String CARD_LAYOUT_EMPTY = "card_empty";
	private static String CARD_LAYOUT_APKINFO = "card_apkinfo";

	EasyPermissioniconPanel iconPanel;
	
	JPanel contentsCardPanel;
	//EasyGuiToolPanel toolpanel;
	FlatPanel permissiontemppanel;
	JPanel showpermissionpanel;
	JScrollPane scrollPane;
	

	int permissionbuttoncount = 0;

	public EasyPermissionPanel() {
		// TODO Auto-generated constructor stub
		Log.d("start EasyPermissionPanel ");
		setLayout(new BorderLayout());
		setBackground(bordercolor);
		setPreferredSize(new Dimension(0, HEIGHT));
		
		iconPanel = new EasyPermissioniconPanel(HEIGHT, EasyContentsPanel.WIDTH);
		
		contentsCardPanel = new JPanel(new CardLayout());
		contentsCardPanel.add(iconPanel, CARD_LAYOUT_APKINFO);

		// setEmptypanel();

		add(contentsCardPanel);
		Log.d("End EasyPermissionPanel ");
	}

	public void setEmptypanel() {
		Log.d("permission toolpanel=) emptypanel ");

//		if (toolpanel == null) {
//			toolpanel = new EasyGuiToolPanel(HEIGHT, EasyContentsPanel.WIDTH);
//			Log.d("permission new (toolpanel)");
//			contentsCardPanel.add(toolpanel, CARD_LAYOUT_EMPTY);
//		}

		((CardLayout) contentsCardPanel.getLayout()).show(contentsCardPanel, CARD_LAYOUT_EMPTY);

	}

	public void setPermission(ApkInfo apkInfo) {
		iconPanel.setPermission(apkInfo);
		((CardLayout) contentsCardPanel.getLayout()).show(contentsCardPanel, CARD_LAYOUT_APKINFO);
		// validate();
	}

	public void clear() {
		// TODO Auto-generated method stub
		iconPanel.clear();
	}
}
