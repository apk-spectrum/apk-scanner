package com.apkscanner.gui.easymode.contents;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import com.apkscanner.core.permissionmanager.PermissionGroupInfoExt;
import com.apkscanner.core.permissionmanager.PermissionManager;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.data.apkinfo.ApkInfoHelper;
import com.apkscanner.gui.easymode.core.ToolEntryManager;
import com.apkscanner.gui.easymode.util.EasyButton;
import com.apkscanner.gui.easymode.util.EasyRoundButton;
import com.apkscanner.gui.easymode.util.FlatPanel;
import com.apkscanner.gui.easymode.util.ImageUtils;
import com.apkscanner.gui.easymode.util.RoundPanel;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

public class EasyPermissioniconPanel extends RoundPanel implements ActionListener{
	private static final long serialVersionUID = -7090063544416919223L;

	int HEIGHT = 35;
	int WIDTH = 100;
	int SHADOW_SIZE = 2;
	int ICON_GAP = 4;
	
	static private Color dangerouscolor = new Color(201, 31, 31);
	static private Color permissionbackgroundcolor = new Color(70, 70, 70);
	
	JPanel toolbartemppanel;
	JScrollPane scrollPane;	
	EasyRoundButton btnshowpermissiondlg;
	
	int permissionbuttoncount = 0;
	private PermissionManager permissionManager;
	
	public EasyPermissioniconPanel(int height, int width) {
		
		HEIGHT = height - SHADOW_SIZE * 2;
		WIDTH = width;
		init();
		setPreferredSize(new Dimension(0, height));
	}
		
	private void init() {
		//setBackground(bordercolor);
		
		setRoundrectColor(new Color(217, 217, 217));
		setshadowlen(SHADOW_SIZE);
		
		setPreferredSize(new Dimension(0, HEIGHT + SHADOW_SIZE * 2));
		setLayout(new BorderLayout());
		//setshadowlen(SHADOW_SIZE);
		
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
		toolbartemppanel.setLayout((new FlowLayout(FlowLayout.LEFT, ICON_GAP, ICON_GAP)));
		toolbartemppanel.setOpaque(false);
		//toolbartemppanel.setBackground(permissionbackgroundcolor);
		//toolbartemppanel.setRoundrectColor(permissionbackgroundcolor);
		toolbartemppanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		btnshowpermissiondlg = new EasyRoundButton(ImageUtils.setcolorImage(Resource.IMG_EASY_WINDOW_PERMISSION_ICON.getImageIcon(20, 35), permissionbackgroundcolor));
		//btnshowpermissiondlg = new EasyRoundButton(Resource.IMG_EASY_WINDOW_PERMISSION_ICON.getImageIcon(20, 35));
		
		btnshowpermissiondlg.setPreferredSize(new Dimension(25, 25));
		btnshowpermissiondlg.setBackground(new Color(217, 217, 217));
		btnshowpermissiondlg.addActionListener(this);
		
		add(btnshowpermissiondlg, BorderLayout.EAST);
		
		permissionManager = new PermissionManager();
	}
	
	public void setPermission(ApkInfo apkInfo) {
		permissionbuttoncount = 0;
		if (apkInfo.manifest.usesPermission == null || apkInfo.manifest.usesPermission.length < 1)
			return;

		Log.d(apkInfo.manifest.usesPermission.length + "");
		boolean isPlatformSign = ApkInfoHelper.isTestPlatformSign(apkInfo)
				|| ApkInfoHelper.isSamsungSign(apkInfo);
		permissionManager.clearPermissions();
		permissionManager.setPlatformSigned(isPlatformSign);
		permissionManager.setTreatSignAsRevoked((boolean) Resource.PROP_PERM_TREAT_SIGN_AS_REVOKED.getData());
		permissionManager.addUsesPermission(apkInfo.manifest.usesPermission);
		permissionManager.addUsesPermission(apkInfo.manifest.usesPermissionSdk23);
		permissionManager.addDeclarePemission(apkInfo.manifest.permission);
		if(!permissionManager.isEmpty()) {
			Integer selectSdkVer = apkInfo.manifest.usesSdk.targetSdkVersion;
			permissionManager.setSdkVersion(selectSdkVer != null ? selectSdkVer : 28);
		}

		for (PermissionGroupInfoExt g : permissionManager.getPermissionGroups()) {
			//JPanel permissionicon = new JPanel();
			//for(int i=0; i< 2; i++){
			try {
				ImageIcon imageIcon = new ImageIcon(new URL(g.getIconPath()));
				
				//imageIcon.setImage(ImageUtils.getScaledImage(imageIcon,BUTTON_IMG_SIZE,BUTTON_IMG_SIZE));
				
				//if (g.hasDangerous())
				//	ImageUtils.setcolorImage(imageIcon, dangerouscolor);
				ImageUtils.setcolorImage(imageIcon, Color.WHITE);
				
				EasyRoundButton btn = new EasyRoundButton(imageIcon);
				
				btn.setPreferredSize(new Dimension(HEIGHT-SHADOW_SIZE*2, HEIGHT- SHADOW_SIZE*2));
				//permissionicon.setshadowlen(SHADOW_SIZE);
				//permissionicon.setBackground(permissionbackgroundcolor);
				//permissionicon.add(btn);
				//btn.setshadowlen(0);
				
				btn.addActionListener(this);
				btn.setToolTipText(g.name);
				
				if (g.hasDangerous()) {
					btn.setBackground(dangerouscolor);
				} else {
					btn.setBackground(permissionbackgroundcolor);
				}
				toolbartemppanel.add(btn);				
				permissionbuttoncount++;
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		//}
	
		//WIDTH = scrollPane.getViewport().getWidth();		
		
		int line = (int)((HEIGHT + 1)* permissionbuttoncount / WIDTH);
		toolbartemppanel.setPreferredSize(new Dimension(0, HEIGHT * (line+1) + ((line !=0)? SHADOW_SIZE : 0)));
		updateUI();
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		Log.d("tool click" + e);
		
		if(e.getSource().equals(btnshowpermissiondlg)) {
			ToolEntryManager.excutePermissionDlg();
		} else {
			ToolEntryManager.showPermDetailDesc(permissionManager.getPermissionGroup(((EasyRoundButton)e.getSource()).getToolTipText()));
		}
	}
	
	public void clear() {
		toolbartemppanel.removeAll();
	}
}
