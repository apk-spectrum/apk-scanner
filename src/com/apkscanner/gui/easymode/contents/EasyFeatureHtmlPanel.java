package com.apkscanner.gui.easymode.contents;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;

import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.easymode.core.EasyGuiAppFeatureData;
import com.apkscanner.gui.easymode.util.FlatPanel;
import com.apkscanner.gui.easymode.util.RoundPanel;
import com.apkscanner.gui.messagebox.MessageBoxPane;
import com.apkscanner.resource.Resource;

public class EasyFeatureHtmlPanel extends RoundPanel {
	static private Color sdkverPanelcolor = new Color(242, 242, 242);

	static private Color []featurefgfontcolor = {new Color(121, 121, 121), new Color(237, 126, 83), Color.RED} ;
	static private Color featurebgfontcolor = new Color(217, 217, 217);
	
	JLabel apkinform;
	EasyGuiAppFeatureData AppFeature;
	
	public EasyFeatureHtmlPanel() {
		setLayout(new BorderLayout());
		//setBackground(new Color(217, 217, 217));
		setRoundrectColor(new Color(217, 217, 217));
		
		//setshadowlen(10);
		AppFeature = new EasyGuiAppFeatureData();
		
		apkinform = new JLabel();
		//apkinform.setEditable(false);
		apkinform.setOpaque(false);
		apkinform.setFont(new Font(getFont().getName(), Font.BOLD, 15));
		//apkinform.setBackground(Color.white);
		apkinform.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		
		//apkinform.setHyperlinkClickListener(this);
		
		add(apkinform, BorderLayout.CENTER);
	}

	public void setfeature(ApkInfo apkInfo) {		
		AppFeature.setFeature(apkInfo);
		//makefeaturehtml(AppFeature);
		newmakefeaturehtml(AppFeature);
	}

	private String makeHyperLink(String href, String text, String title, String id, String style) {
		//return JHtmlEditorPane.makeHyperLink(href, text, title, id, style);
		return text;
	}

	private void makefeaturehtml(EasyGuiAppFeatureData featuredata) {
		StringBuilder feature = new StringBuilder();

		feature.append("<html><p style=\'line-height: 150%;\'>");
		
		if("internalOnly".equals(featuredata.installLocation)) {
			feature.append(makeHyperLink("@event", Resource.STR_FEATURE_ILOCATION_INTERNAL_LAB.getString(), Resource.STR_FEATURE_ILOCATION_INTERNAL_DESC.getString(), "feature-install-location-internal", null));
		} else if("auto".equals(featuredata.installLocation)) {
			feature.append(makeHyperLink("@event", Resource.STR_FEATURE_ILOCATION_AUTO_LAB.getString(), Resource.STR_FEATURE_ILOCATION_AUTO_DESC.getString(), "feature-install-location-auto", null));
		} else if("preferExternal".equals(featuredata.installLocation)) {
			feature.append(makeHyperLink("@event", Resource.STR_FEATURE_ILOCATION_EXTERNAL_LAB.getString(), Resource.STR_FEATURE_ILOCATION_EXTERNAL_DESC.getString(), "feature-install-location-external", null));
		}  
		feature.append("<br/>");
		//<style='line-height:0%'>
		if(featuredata.isHidden) {
			feature.append(makeHyperLink("@event", Resource.STR_FEATURE_HIDDEN_LAB.getString(), Resource.STR_FEATURE_HIDDEN_DESC.getString(), "feature-hidden", null));
		} else {
			feature.append(makeHyperLink("@event", Resource.STR_FEATURE_LAUNCHER_LAB.getString(), Resource.STR_FEATURE_LAUNCHER_DESC.getString(), "feature-launcher", null));
		}
		if(featuredata.isStartup) {
			feature.append(", " + makeHyperLink("@event", Resource.STR_FEATURE_STARTUP_LAB.getString(), Resource.STR_FEATURE_STARTUP_DESC.getString(), "feature-startup", null));
		}
		if(featuredata.sharedUserId != null && !featuredata.sharedUserId.startsWith("android.uid.system") ) {
			feature.append(", " + makeHyperLink("@event", Resource.STR_FEATURE_SHAREDUSERID_LAB.getString(), Resource.STR_FEATURE_SHAREDUSERID_DESC.getString(), "feature-shared-user-id", null));
		}
		if(featuredata.deviceRequirements != null && !featuredata.deviceRequirements.isEmpty()) {
			feature.append(", " + makeHyperLink("@event", Resource.STR_FEATURE_DEVICE_REQ_LAB.getString(), Resource.STR_FEATURE_DEVICE_REQ_DESC.getString(), "feature-device-requirements", null));
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
		if(((featuredata.hasSignatureLevel || featuredata.hasSignatureOrSystemLevel) && !systemSignature) || featuredata.hasSystemLevel) {
			importantFeatures.append(", <font style=\"color:#ED7E31; font-weight:bold\">");
			importantFeatures.append(makeHyperLink("@event", Resource.STR_FEATURE_REVOKE_PERM_LAB.getString(), Resource.STR_FEATURE_REVOKE_PERM_DESC.getString(), "feature-revoke-permissions", null));
			importantFeatures.append("</font>");
		}
		if(featuredata.debuggable) {
			importantFeatures.append(", <font style=\"color:#ED7E31; font-weight:bold\">");
			importantFeatures.append(makeHyperLink("@event", Resource.STR_FEATURE_DEBUGGABLE_LAB.getString(), Resource.STR_FEATURE_DEBUGGABLE_DESC.getString(), "feature-debuggable", null));
			importantFeatures.append("</font>");
		}
		if(featuredata.isInstrumentation) {
			importantFeatures.append(", <font style=\"color:#ED7E31; font-weight:bold\">");
			importantFeatures.append(makeHyperLink("@event", Resource.STR_FEATURE_INSTRUMENTATION_LAB.getString(), Resource.STR_FEATURE_INSTRUMENTATION_DESC.getString(), "feature-instrumentation", null));
			importantFeatures.append("</font>");
		}
		if(importantFeatures.length() > 0) {
			feature.append("<br/>" + importantFeatures.substring(2));
		}
		
		feature.append("</p></html>");
		
		//apkinform.setBody(feature.toString());
		apkinform.setText(feature.toString());
	}
	
	private void newmakefeaturehtml(EasyGuiAppFeatureData featuredata) {
		StringBuilder feature = new StringBuilder();

		feature.append("<html><p style=\'line-height: 150%;\'>");
		//<style='line-height:0%'>
		if(featuredata.isHidden) {
			feature.append(makeHyperLink("@event", Resource.STR_FEATURE_HIDDEN_LAB.getString(), Resource.STR_FEATURE_HIDDEN_DESC.getString(), "feature-hidden", null));
		} else {
			feature.append(makeHyperLink("@event", Resource.STR_FEATURE_LAUNCHER_LAB.getString(), Resource.STR_FEATURE_LAUNCHER_DESC.getString(), "feature-launcher", null));
		}

		if(featuredata.sharedUserId != null && !featuredata.sharedUserId.startsWith("android.uid.system") ) {
			feature.append(", " + makeHyperLink("@event", Resource.STR_FEATURE_SHAREDUSERID_LAB.getString(), Resource.STR_FEATURE_SHAREDUSERID_DESC.getString(), "feature-shared-user-id", null));
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
		
		feature.append("</p></html>");
		
		//apkinform.setBody(feature.toString());
		apkinform.setText(feature.toString());
	}
	
	
	private void showDialog(String content, String title, Dimension size, Icon icon)
	{
		MessageBoxPane.showTextAreaDialog(null, content, title, MessageBoxPane.INFORMATION_MESSAGE, icon, size);
	}
	
}
