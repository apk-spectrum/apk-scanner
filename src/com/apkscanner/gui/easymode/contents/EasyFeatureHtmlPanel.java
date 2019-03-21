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
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.easymode.core.EasyGuiAppFeatureData;
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
	
	JLabel apkinform;
	EasyGuiAppFeatureData AppFeature;
	XmlPath sdkXmlPath;
	EasyRoundLabel launcherlabel;
	public EasyFeatureHtmlPanel() {
		setLayout(new BorderLayout());
		
		//setBackground(new Color(217, 217, 217));
		setRoundrectColor(new Color(217, 217, 217));
		//setshadowlen(10);
		AppFeature = new EasyGuiAppFeatureData();
		setSdkXml(Resource.STR_SDK_INFO_FILE_PATH.getString());
		
		apkinform = new JLabel();
		//apkinform.setEditable(false);
		apkinform.setOpaque(false);
		apkinform.setFont(new Font(getFont().getName(), Font.BOLD, 15));
		//apkinform.setBackground(Color.white);
		apkinform.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
		
		JPanel tempfeature = new JPanel(new FlowLayout());
		tempfeature.setOpaque(false);
		
		launcherlabel = new EasyRoundLabel(" ", new Color(217, 217, 217), Color.BLACK);
		//label.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		launcherlabel.setOpaque(false);
		launcherlabel.setFont(new Font(getFont().getName(), Font.BOLD, 15));
		//launcherlabel.setPreferredSize(new Dimension(200, 30));

		tempfeature.add(launcherlabel);
		//apkinform.setHyperlinkClickListener(this);
		
		add(apkinform, BorderLayout.CENTER);
		add(tempfeature, BorderLayout.SOUTH);
		
	}

	public void setfeature(ApkInfo apkInfo) {		
		AppFeature.setFeature(apkInfo);
		//makefeaturehtml(AppFeature);
		newmakefeaturehtml(AppFeature, apkInfo);
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
		StringBuilder feature = new StringBuilder();

		
		
		feature.append("<html><p style=\'line-height: 150%;\'>");
		//<style='line-height:0%'>
		
		//@SDK Ver. 21 (Min), 26 (Target)
		
		if(apkInfo.manifest.usesSdk.minSdkVersion!=null) {
			int minsdk = apkInfo.manifest.usesSdk.minSdkVersion;			
			//arraysdkObject.add(new sdkDrawObject(makeTextPanel("min", minsdk), minsdk));
			feature.append("(Min)" + makesdkString(minsdk));
			feature.append(" ");
		}
		
		if(apkInfo.manifest.usesSdk.maxSdkVersion!=null) {
			int maxsdk = apkInfo.manifest.usesSdk.maxSdkVersion;
			feature.append("(Max)" + makesdkString(maxsdk));
			feature.append(" ");
		}

		
		if(apkInfo.manifest.usesSdk.targetSdkVersion!=null) {
			int targetsdk = apkInfo.manifest.usesSdk.targetSdkVersion;
			//arraysdkObject.add(new sdkDrawObject(makeDevicePanel(Devicecolor[DEVICE_TARGET], targetsdk), targetsdk));
			//feature.append("<font style=\"color:#ED7E31; font-weight:bold\">");
			feature.append("(Target)" + makesdkString(targetsdk));
			feature.append("<br/>");
		}

		if(featuredata.sharedUserId != null && !featuredata.sharedUserId.startsWith("android.uid.system") ) {
			feature.append("<font style=\"color:#AAAA00; font-weight:bold\">");
			feature.append(makeHyperLink("@event", Resource.STR_FEATURE_SHAREDUSERID_LAB.getString(), Resource.STR_FEATURE_SHAREDUSERID_DESC.getString(), "feature-shared-user-id", null));
		}
		
		boolean systemSignature = false;
		StringBuilder importantFeatures = new StringBuilder();
		if(featuredata.sharedUserId != null && featuredata.sharedUserId.startsWith("android.uid.system")) {
			if(featuredata.isSamsungSign || featuredata.isPlatformSign) {
				importantFeatures.append(", <font style=\"color:#ED7E31; font-weight:bold\">");
			} else {
				importantFeatures.append(", <font style=\"color:#FF0000; font-weight:bold\">");
			}
			importantFeatures.append(makeHyperLink("@event", Resource.STR_FEATURE_SYSTEM_UID_LAB.getString(), Resource.STR_FEATURE_SYSTEM_UID_DESC.getString(), "feature-system-user-id", null));
			importantFeatures.append("</font>");
		}
		if(featuredata.isPlatformSign) {
			importantFeatures.append(", <font style=\"color:#ED7E31; font-weight:bold\">");
			importantFeatures.append(makeHyperLink("@event", Resource.STR_FEATURE_PLATFORM_SIGN_LAB.getString(), Resource.STR_FEATURE_PLATFORM_SIGN_DESC.getString(), "feature-platform-sign", null));
			importantFeatures.append("</font>");
			systemSignature = true;
		}
		if(featuredata.isSamsungSign) {
			importantFeatures.append(", <font style=\"color:#ED7E31; font-weight:bold\">");
			importantFeatures.append(makeHyperLink("@event", Resource.STR_FEATURE_SAMSUNG_SIGN_LAB.getString(), Resource.STR_FEATURE_SAMSUNG_SIGN_DESC.getString(), "feature-samsung-sign", null));
			importantFeatures.append("</font>");
			systemSignature = true;
		}
	
		if(importantFeatures.length() > 0) {
			feature.append("<br/>" + importantFeatures.substring(2));
		}
		
		launcherlabel.setFont(apkinform.getFont());
		if(featuredata.isHidden) {
			//feature.append("<font style=\"color:#ED7E31; font-weight:bold\">");
			//feature.append(makeHyperLink("@event", Resource.STR_FEATURE_HIDDEN_LAB.getString(), Resource.STR_FEATURE_HIDDEN_DESC.getString(), "feature-hidden", null));
			launcherlabel.setText(Resource.STR_FEATURE_HIDDEN_LAB.getString());
			launcherlabel.setclipboard(false);
			launcherlabel.setMouseHoverEffect(false);
		} else {
			//feature.append("<font style=\"color:#0055BB; font-weight:bold\">");
			//feature.append(makeHyperLink("@event", Resource.STR_FEATURE_LAUNCHER_LAB.getString(), Resource.STR_FEATURE_LAUNCHER_DESC.getString(), "feature-launcher", null));
			
			launcherlabel.setText(Resource.STR_FEATURE_LAUNCHER_LAB.getString());			
			launcherlabel.setclipboard(true);
			launcherlabel.setMouseHoverEffect(true);			
		}
		
		feature.append("</p></html>");
		
		//apkinform.setBody(feature.toString());
		apkinform.setText(feature.toString());
	}
	
	
	private void showDialog(String content, String title, Dimension size, Icon icon)
	{
		MessageBoxPane.showTextAreaDialog(null, content, title, MessageBoxPane.INFORMATION_MESSAGE, icon, size);
	}
	
}
