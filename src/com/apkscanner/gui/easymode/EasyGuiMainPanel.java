package com.apkscanner.gui.easymode;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.easymode.contents.EasyBordPanel;
import com.apkscanner.gui.easymode.contents.EasyContentsPanel;
import com.apkscanner.gui.easymode.contents.EasyPermissionPanel;
import com.apkscanner.gui.easymode.util.EasyFileDrop;
import com.apkscanner.gui.util.FileDrop;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

class EasyGuiMainPanel extends JPanel {
	private static Color maincolor = new Color(249,249,249);
	
	private EasyLightApkScanner apklightscanner;
	
	private EasyBordPanel bordPanel;
	private EasyContentsPanel contentsPanel;
	private EasyPermissionPanel permissionPanel;
	private JFrame mainframe;
	public EasyGuiMainPanel(JFrame mainframe, EasyLightApkScanner apkscanner) {
		this.apklightscanner = apkscanner;
		this.mainframe = mainframe;
		if(apklightscanner != null) {			
			apklightscanner.setStatusListener(new ApkLightScannerListener());
		}
		
		setLayout(new BorderLayout());
		setBackground(maincolor);
		bordPanel = new EasyBordPanel(mainframe);
		contentsPanel = new EasyContentsPanel();
		permissionPanel = new EasyPermissionPanel();
		
		add(bordPanel, BorderLayout.PAGE_START);
		add(contentsPanel, BorderLayout.CENTER);		
		add(permissionPanel, BorderLayout.PAGE_END);
		setBorder(new LineBorder(Color.BLACK, 0));
		
		
		new EasyFileDrop(this, new FileDrop.Listener() {
			public void filesDropped(java.io.File[] files) {
				clearApkinfopanel();
				apklightscanner.clear(true);
				apklightscanner.setApk(files[0].getAbsolutePath());
			}
		});
	}
	
	private void showApkinfopanel() {
		bordPanel.setWindowTitle(apklightscanner.getApkInfo());
		contentsPanel.setContents(apklightscanner.getApkInfo());
		permissionPanel.setPermission(apklightscanner.getApkInfo());
		//mainframe.setVisible(true);
	}
	
	private void clearApkinfopanel() {
		bordPanel.clear();
		contentsPanel.clear();
		permissionPanel.clear();
	}
    class ApkLightScannerListener implements EasyLightApkScanner.StatusListener {
		@Override
		public void onStart() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSuccess() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onError(int error) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onCompleted() {
			// TODO Auto-generated method stub
			showApkinfopanel();
		}
    }
}


