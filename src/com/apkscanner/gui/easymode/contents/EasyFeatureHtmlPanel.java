package com.apkscanner.gui.easymode.contents;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.data.apkinfo.ApkInfoHelper;
import com.apkscanner.data.apkinfo.ComponentInfo;
import com.apkscanner.gui.easymode.core.EasyGuiAppFeatureData;
import com.apkscanner.gui.easymode.core.ToolEntryManager;
import com.apkscanner.gui.easymode.dlg.EasyToolbarCertDlg;
import com.apkscanner.gui.easymode.util.EasyRoundButton;
import com.apkscanner.gui.easymode.util.EasyRoundLabel;
import com.apkscanner.gui.easymode.util.FlatPanel;
import com.apkscanner.gui.easymode.util.RoundPanel;
import com.apkscanner.gui.messagebox.MessageBoxPane;
import com.apkscanner.gui.messagebox.MessageBoxPool;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.FileUtil;
import com.apkscanner.util.Log;
import com.apkscanner.util.XmlPath;
import com.apkscanner.util.FileUtil.FSStyle;

public class EasyFeatureHtmlPanel extends RoundPanel {
	static private Color sdkverPanelcolor = new Color(242, 242, 242);

	static private Color []featurefgfontcolor = {new Color(121, 121, 121), new Color(237, 126, 83), Color.RED} ;
	static private Color featurebgfontcolor = new Color(217, 217, 217);
	
	EasyGuiAppFeatureData AppFeature;
	XmlPath sdkXmlPath;
	JPanel mainpanel;
	JScrollPane scrollPane;
	ApkInfo mapkInfo;
	public EasyFeatureHtmlPanel() {
		setLayout(new BorderLayout());
		
		mainpanel = new JPanel();
		mainpanel.setOpaque(false);
		mainpanel.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 3));
		
		scrollPane = new JScrollPane(mainpanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		//scrollPane.setPreferredSize(new Dimension(getWidth(), getHeight()));
		scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(15, 0));
		//scrollPane.getVerticalScrollBar().setUnitIncrement(HEIGHT+1);
		
		scrollPane.setBorder(null);
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);
		
		scrollPane.addComponentListener(new ComponentListener() {
			@Override
			public void componentShown(ComponentEvent e) {	}
			@Override
			public void componentResized(ComponentEvent e) {
				refreshUI();
			}
			@Override
			public void componentMoved(ComponentEvent e) {}			
			@Override
			public void componentHidden(ComponentEvent e) {}
		});
		
		
		setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
		//setBackground(new Color(217, 217, 217));
		setRoundrectColor(new Color(217, 217, 217));
		//setshadowlen(10);
		AppFeature = new EasyGuiAppFeatureData();
		setSdkXml(Resource.STR_SDK_INFO_FILE_PATH.getString());
		
		add(scrollPane, BorderLayout.CENTER);
		
		JPanel tempfeature = new JPanel(new FlowLayout());
		tempfeature.setOpaque(false);
		
	}
	private void setdefaultfeature(JComponent com) {
		com.setFont(new Font(getFont().getName(), Font.PLAIN, 13));
		com.setBorder(new EmptyBorder(5, 5, 5, 5));		
	}
	
	private JComponent makeFeatpanel(String foldstr, String spreadstr, Color foreground) {
		EasyRoundButton btn = new EasyRoundButton(foldstr, spreadstr);
		btn.setForeground(foreground);
		setdefaultfeature(btn);
		btn.addComponentListener(new ComponentListener() {
			@Override
			public void componentShown(ComponentEvent e) {}			
			@Override
			public void componentResized(ComponentEvent e) {				
				refreshUI();
			}			
			@Override
			public void componentMoved(ComponentEvent e) {}			
			@Override
			public void componentHidden(ComponentEvent e) {}
		});
		return btn;
	}
	
	private JComponent makeFeatpanel(String str, Color foreground) {
		return makeFeatpanel(str,str,foreground);
	}
	
	private JComponent makeFeatpanel(String str, ActionListener listner, Color foreground) {
		
		JComponent btn = makeFeatpanel(str, "", foreground);
		((JButton)btn).addActionListener(listner);
		return btn;
		
	}
	
	public void refreshUI() {
		mainpanel.updateUI();
		if(mainpanel.getComponentCount() > 0) {
			Component com = mainpanel.getComponent(mainpanel.getComponentCount()-1);					
			mainpanel.setPreferredSize(new Dimension(scrollPane.getViewport().getWidth(), com.getY() + com.getHeight()));
		} else {
			mainpanel.setPreferredSize(new Dimension(scrollPane.getViewport().getWidth(), scrollPane.getViewport().getHeight()));
		}
	}
	
	public void setfeature(ApkInfo apkInfo) {		
		mapkInfo = apkInfo;
		AppFeature.setFeature(apkInfo);
		//makefeaturehtml(AppFeature);
		mainpanel.removeAll();
		newmakefeaturehtml(AppFeature, apkInfo);
		updateUI();
		refreshUI();		
	}

	public void setSdkXml(String xmlPath) {
		if(xmlPath == null) {
			return;
		}
		try(InputStream xml = Resource.class.getResourceAsStream(xmlPath)) {
			if(xml != null) sdkXmlPath = new XmlPath(xml);
		} catch(IOException e) { }
		if(sdkXmlPath == null) {
			Log.w("Can not create XmlPath, xmlPath : " + xmlPath);
			return;
		}
	}
	private String makesdkString(int sdkversion) {
		String str = "";
		XmlPath sdkInfo = sdkXmlPath.getNode("/resources/sdk-info[@apiLevel='" + sdkversion + "']");
		//21-5.0/Lollipop
		str = sdkversion + "-" + sdkInfo.getAttribute("platformVersion") + " / " + sdkInfo.getAttribute("codeName");
		
		return str;
	}
	
	class ShowsignDlg implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			//ToolEntryManager.excuteSinerDlg(null);
			
			ToolEntryManager.excuteEntry(ToolEntryManager.TOOL_SHOW_SIGN_DLG);
		}		
	}
	
	private void newmakefeaturehtml(EasyGuiAppFeatureData featuredata, ApkInfo apkInfo) {
		//<style='line-height:0%'>
		
		//@SDK Ver. 21 (Min), 26 (Target)
		ShowsignDlg showsignListener = new ShowsignDlg();
		
		if(apkInfo.manifest.usesSdk.minSdkVersion!=null) {
			int minsdk = apkInfo.manifest.usesSdk.minSdkVersion;			
			//arraysdkObject.add(new sdkDrawObject(makeTextPanel("min", minsdk), minsdk));
			mainpanel.add(makeFeatpanel("(Min)" + minsdk,"(Min)" + makesdkString(minsdk), Color.BLACK));
		}
		
		if(apkInfo.manifest.usesSdk.maxSdkVersion!=null) {
			int maxsdk = apkInfo.manifest.usesSdk.maxSdkVersion;
			mainpanel.add(makeFeatpanel("(Max)" + maxsdk,"(Max)" + makesdkString(maxsdk), Color.BLACK));				
		}
		
		if(apkInfo.manifest.usesSdk.targetSdkVersion!=null) {
			int targetsdk = apkInfo.manifest.usesSdk.targetSdkVersion;
			mainpanel.add(makeFeatpanel("(Target)" + targetsdk,"(Target)" + makesdkString(targetsdk), Color.BLACK));
		}

		if(featuredata.sharedUserId != null && !featuredata.sharedUserId.startsWith("android.uid.system") ) {
			mainpanel.add(makeFeatpanel(Resource.STR_FEATURE_SHAREDUSERID_LAB.getString(), featuredata.sharedUserId, new Color(0xAAAA00)));
		}
		
		boolean systemSignature = false;
		if(featuredata.sharedUserId != null && featuredata.sharedUserId.startsWith("android.uid.system")) {
			if(featuredata.isSamsungSign || featuredata.isPlatformSign) {
				mainpanel.add(makeFeatpanel(Resource.STR_FEATURE_SYSTEM_UID_LAB.getString(), featuredata.sharedUserId, new Color(0xED7E31)));
			} else {
				mainpanel.add(makeFeatpanel(Resource.STR_FEATURE_SYSTEM_UID_LAB.getString(), featuredata.sharedUserId, new Color(0xFF0000)));
			}
		}

		if(featuredata.isnoSign) {
			mainpanel.add(makeFeatpanel(Resource.STR_FEATURE_SIGNATURE_UNSIGNED.getString(), new Color(0xFF0000)));
		} else {
			if(featuredata.isPlatformSign) {
				mainpanel.add(makeFeatpanel(Resource.STR_FEATURE_PLATFORM_SIGN_LAB.getString(), showsignListener, new Color(0xED7E31)));			
			} 
			if(featuredata.isSamsungSign) {
				mainpanel.add(makeFeatpanel(Resource.STR_FEATURE_SAMSUNG_SIGN_LAB.getString(), showsignListener, new Color(0xED7E31)));
				systemSignature = true;
			}
			if(!featuredata.isPlatformSign && !featuredata.isSamsungSign) {
				mainpanel.add(makeFeatpanel(Resource.STR_FEATURE_SIGNATURE_SIGNED.getString(), showsignListener, new Color(0x0055BB)));
			}
		}
		
		
		if(featuredata.isHidden) {
			mainpanel.add(makeFeatpanel(Resource.STR_FEATURE_HIDDEN_LAB.getString(), new Color(0xED7E31)));			
		} else {
			String str="";
			ComponentInfo[] apkActivities = ApkInfoHelper.getLauncherActivityList(apkInfo);
			if (apkActivities != null && apkActivities.length > 0) {
				
				for (ComponentInfo comp : apkActivities) {
					String strfeature = "";					
					if(comp.enabled != null && !comp.enabled) {
						strfeature += "disabled";
					}
					if(comp.exported != null && !comp.exported) {
						if(strfeature.length() > 0) strfeature +=","; 
						strfeature += "not exported";
					}
					if(comp.permission != null && !comp.permission.isEmpty()) {
						if(strfeature.length() > 0) strfeature +=",";
						strfeature += "not empty";
					}
					strfeature = ((strfeature.length() > 0)?"(" + strfeature + ")" :"");
					
					mainpanel.add(makeFeatpanel(
							Resource.STR_FEATURE_LAUNCHER_LAB.getString() + strfeature,
							comp.name + strfeature, new Color(0x0055BB)));
				}
			}
		}
		
		if(apkInfo.fileSize!=null) {
			String spread = FileUtil.getFileSize(apkInfo.fileSize, FSStyle.FULL);
			String fold = FileUtil.getFileSize(apkInfo.fileSize, FSStyle.MB);
			mainpanel.add(makeFeatpanel(fold, spread, Color.BLACK));
		}		

		
	}
	
	private void showDialog(String content, String title, Dimension size, Icon icon)
	{
		MessageBoxPane.showTextAreaDialog(null, content, title, MessageBoxPane.INFORMATION_MESSAGE, icon, size);
	}
	
}
