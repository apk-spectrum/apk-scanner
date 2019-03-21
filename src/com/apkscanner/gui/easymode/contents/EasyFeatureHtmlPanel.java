package com.apkscanner.gui.easymode.contents;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.easymode.core.EasyGuiAppFeatureData;
import com.apkscanner.gui.easymode.util.EasyRoundButton;
import com.apkscanner.gui.easymode.util.EasyRoundLabel;
import com.apkscanner.gui.easymode.util.FlatPanel;
import com.apkscanner.gui.easymode.util.RoundPanel;
import com.apkscanner.gui.messagebox.MessageBoxPane;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;
import com.apkscanner.util.XmlPath;

public class EasyFeatureHtmlPanel extends RoundPanel {
	static private Color sdkverPanelcolor = new Color(242, 242, 242);

	static private Color []featurefgfontcolor = {new Color(121, 121, 121), new Color(237, 126, 83), Color.RED} ;
	static private Color featurebgfontcolor = new Color(217, 217, 217);
	
	EasyGuiAppFeatureData AppFeature;
	XmlPath sdkXmlPath;
	
	public EasyFeatureHtmlPanel() {
		//setLayout(new BorderLayout());
		setLayout(new FlowLayout(FlowLayout.LEFT, 3, 3));
		setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
		//setBackground(new Color(217, 217, 217));
		setRoundrectColor(new Color(217, 217, 217));
		//setshadowlen(10);
		AppFeature = new EasyGuiAppFeatureData();
		setSdkXml(Resource.STR_SDK_INFO_FILE_PATH.getString());
		
		JPanel tempfeature = new JPanel(new FlowLayout());
		tempfeature.setOpaque(false);
		
	}
	private void setdefaultfeature(JComponent com) {
		com.setFont(new Font(getFont().getName(), Font.BOLD, 13));
		com.setBorder(new EmptyBorder(5, 5, 5, 5));
	}
	
	private JComponent makeFeatpanel(String str, Color background, Color foreground) {
		EasyRoundButton btn = new EasyRoundButton(str);
		btn.setBackground(background);
		btn.setForeground(foreground);
		setdefaultfeature(btn);
		return btn;
	}
	private JComponent makeFeatpanel(String str, Color foreground) {
		EasyRoundButton btn = new EasyRoundButton(str);
		btn.setForeground(foreground);
		setdefaultfeature(btn);
		return btn;
	}	
	private JComponent makeFeatpanel(String str) {
		EasyRoundButton btn = new EasyRoundButton(str); 
		setdefaultfeature(btn);
		return btn;
	}

	public void setfeature(ApkInfo apkInfo) {		
		AppFeature.setFeature(apkInfo);
		//makefeaturehtml(AppFeature);
		removeAll();		
		newmakefeaturehtml(AppFeature, apkInfo);
		updateUI();
	}

	private String makeHyperLink(String href, String text, String title, String id, String style) {
		//return JHtmlEditorPane.makeHyperLink(href, text, title, id, style);
		return text;
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
		str = sdkversion + "-" + sdkInfo.getAttribute("platformVersion") + "/" + sdkInfo.getAttribute("codeName");
		
		return str;
	}
	
	private void newmakefeaturehtml(EasyGuiAppFeatureData featuredata, ApkInfo apkInfo) {
		//<style='line-height:0%'>
		
		//@SDK Ver. 21 (Min), 26 (Target)
		
		if(apkInfo.manifest.usesSdk.minSdkVersion!=null) {
			int minsdk = apkInfo.manifest.usesSdk.minSdkVersion;			
			//arraysdkObject.add(new sdkDrawObject(makeTextPanel("min", minsdk), minsdk));
			add(makeFeatpanel("(Min)" + makesdkString(minsdk)));
		}
		
		if(apkInfo.manifest.usesSdk.maxSdkVersion!=null) {
			int maxsdk = apkInfo.manifest.usesSdk.maxSdkVersion;
			add(makeFeatpanel("(Max)" + makesdkString(maxsdk)));	
		}

		
		if(apkInfo.manifest.usesSdk.targetSdkVersion!=null) {
			int targetsdk = apkInfo.manifest.usesSdk.targetSdkVersion;
			//arraysdkObject.add(new sdkDrawObject(makeDevicePanel(Devicecolor[DEVICE_TARGET], targetsdk), targetsdk));
			//feature.append("<font style=\"color:#ED7E31; font-weight:bold\">");
			add(makeFeatpanel("(Target)" + makesdkString(targetsdk)));
			
		}

		if(featuredata.sharedUserId != null && !featuredata.sharedUserId.startsWith("android.uid.system") ) {
			add(makeFeatpanel(Resource.STR_FEATURE_SHAREDUSERID_LAB.getString(), new Color(0xAAAA00)));
		}
		
		boolean systemSignature = false;
		if(featuredata.sharedUserId != null && featuredata.sharedUserId.startsWith("android.uid.system")) {
			if(featuredata.isSamsungSign || featuredata.isPlatformSign) {
				add(makeFeatpanel(Resource.STR_FEATURE_SYSTEM_UID_LAB.getString(), new Color(0xED7E31)));
			} else {
				add(makeFeatpanel(Resource.STR_FEATURE_SYSTEM_UID_LAB.getString(), new Color(0xFF0000)));
			}
		}
		if(featuredata.isPlatformSign) {
			add(makeFeatpanel(Resource.STR_FEATURE_PLATFORM_SIGN_LAB.getString(), new Color(0xED7E31)));			
		}
		if(featuredata.isSamsungSign) {			
			add(makeFeatpanel(Resource.STR_FEATURE_SAMSUNG_SIGN_LAB.getString(), new Color(0xED7E31)));
			systemSignature = true;
		}
		
		if(featuredata.isHidden) {
			add(makeFeatpanel(Resource.STR_FEATURE_HIDDEN_LAB.getString(), new Color(0xED7E31)));			
		} else {
			add(makeFeatpanel(Resource.STR_FEATURE_LAUNCHER_LAB.getString(), new Color(0x0055BB)));
		}
		
	}
	
	
	private void showDialog(String content, String title, Dimension size, Icon icon)
	{
		MessageBoxPane.showTextAreaDialog(null, content, title, MessageBoxPane.INFORMATION_MESSAGE, icon, size);
	}
	
}
