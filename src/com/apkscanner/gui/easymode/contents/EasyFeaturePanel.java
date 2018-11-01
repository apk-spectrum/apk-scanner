package com.apkscanner.gui.easymode.contents;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.data.apkinfo.ApkInfoHelper;
import com.apkscanner.gui.easymode.util.EasyFlatLabel;
import com.apkscanner.gui.easymode.util.FlatPanel;
import com.apkscanner.gui.util.JHtmlEditorPane;
import com.apkscanner.resource.Resource;

public class EasyFeaturePanel extends FlatPanel {
	static private Color sdkverPanelcolor = new Color(242, 242, 242);

	static private Color []featurefgfontcolor = {new Color(121, 121, 121), new Color(237, 126, 83), Color.RED} ;
	static private Color featurebgfontcolor = new Color(217, 217, 217);
	
	private String installLocation = null;

	private boolean isHidden = false;
	private boolean isStartup = false;
	private boolean debuggable = false;
	private boolean isInstrumentation = false;
	private String sharedUserId = "";
	private String deviceRequirements = "";

	private boolean isSamsungSign = false;
	private boolean isPlatformSign = false;

	private String signaturePermissions = "";
	private String deprecatedPermissions = "";

	private boolean hasSignatureLevel = false;
	private boolean hasSystemLevel = false;
	private boolean hasSignatureOrSystemLevel = false;

	private ArrayList<FeatureObject> Featurearray = new ArrayList<FeatureObject>();

	public EasyFeaturePanel() {
		//setLayout(new FlowLayout(FlowLayout.LEFT, 7, 7));
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		// featurepanel.setBorder(BorderFactory.createEmptyBorder(15,5,5,5));
		setBackground(sdkverPanelcolor);
		setshadowlen(3);

		/////////////// feature sample
		// String[] arraystr = {"start", "hidden", "boot", "sdcard", "samsung"
		// };
		// for(String str : arraystr) {
		// EasyFlatLabel temp = new EasyFlatLabel(str, featurefontcolor,
		// ininerinfotcolor);
		// //temp.setBorder(BorderFactory.createEmptyBorder(400,10,100,5));
		// temp.setPreferredSize(new Dimension(60, 30));
		// temp.setshadowlen(3);
		// temp.setTextFont(new Font(getFont().getName(), Font.PLAIN, 15));
		// temp.setHorizontalAlignment(JTextField.CENTER);
		// temp.Addlistener();
		// add(temp);
		// }
	}

	public void setfeature(ApkInfo apkInfo) {
		installLocation = apkInfo.manifest.installLocation;

		isHidden = ApkInfoHelper.isHidden(apkInfo);
		isStartup = ApkInfoHelper.isStartup(apkInfo);
		isInstrumentation = ApkInfoHelper.isInstrumentation(apkInfo);
		debuggable = ApkInfoHelper.isDebuggable(apkInfo);

		isSamsungSign = (apkInfo.featureFlags & ApkInfo.APP_FEATURE_SAMSUNG_SIGN) != 0 ? true : false;
		isPlatformSign = (apkInfo.featureFlags & ApkInfo.APP_FEATURE_PLATFORM_SIGN) != 0 ? true : false;

		getfeature(apkInfo);

		refreshpanel();

	}

	private void refreshpanel() {
		Collections.sort(Featurearray);
		for (FeatureObject obj : Featurearray) {
			add(obj.panel);
		}
		validate();
	}

	public void clear() {
		removeAll();
		Featurearray.clear();
		add(Box.createVerticalStrut(10));
		validate();
	}

	private class FeatureObject implements Comparable<FeatureObject> {
		public static final int STYLE_NOMAL = 0;
		public static final int STYLE_IMPORTANT = 1;
		public static final int STYLE_VERY_IMPORTANT = 2;

		public EasyFlatLabel panel;
		public String title = "";
		public String description = "";
		public String id = "";
		public int style;
		// String style;

		public FeatureObject(String title, String desc, String id, int style) {
			// TODO Auto-generated constructor stub
			this.title = title;
			this.description = desc;
			this.id = id;

			panel = new EasyFlatLabel(title, featurebgfontcolor, featurefgfontcolor[style]);
			// temp.setBorder(BorderFactory.createEmptyBorder(400,10,100,5));
			panel.setPreferredSize(new Dimension(title.length() * 7, 25));
			panel.setshadowlen(3);
			panel.setTextFont(new Font(getFont().getName(), Font.PLAIN, 15));
			panel.setHorizontalAlignment(JTextField.CENTER);
			panel.Addlistener();
		}

		@Override
		public int compareTo(FeatureObject o) {
			// TODO Auto-generated method stub
			return 0;
		}
	}

	private String makeHyperLink(String href, String text, String title, String id, String style) {
		return JHtmlEditorPane.makeHyperLink(href, text, title, id, style);
	}

	private void getfeature(ApkInfo apkinfo) {
		if ("internalOnly".equals(installLocation)) {
			Featurearray.add(new FeatureObject(Resource.STR_FEATURE_ILOCATION_INTERNAL_LAB.getString(),
					Resource.STR_FEATURE_ILOCATION_INTERNAL_DESC.getString(), "feature-install-location-internal",
					FeatureObject.STYLE_NOMAL));
		} else if ("auto".equals(installLocation)) {
			Featurearray.add(new FeatureObject(Resource.STR_FEATURE_ILOCATION_AUTO_LAB.getString(),
					Resource.STR_FEATURE_ILOCATION_AUTO_DESC.getString(), "feature-install-location-auto",
					FeatureObject.STYLE_NOMAL));
		} else if ("preferExternal".equals(installLocation)) {
			Featurearray.add(new FeatureObject(Resource.STR_FEATURE_ILOCATION_EXTERNAL_LAB.getString(),
					Resource.STR_FEATURE_ILOCATION_EXTERNAL_DESC.getString(), "feature-install-location-external",
					FeatureObject.STYLE_NOMAL));
		}

		if (isHidden) {
			Featurearray.add(new FeatureObject(Resource.STR_FEATURE_HIDDEN_LAB.getString(),
					Resource.STR_FEATURE_HIDDEN_DESC.getString(), "feature-hidden", FeatureObject.STYLE_NOMAL));
		} else {
			Featurearray.add(new FeatureObject(Resource.STR_FEATURE_LAUNCHER_LAB.getString(),
					Resource.STR_FEATURE_LAUNCHER_DESC.getString(), "feature-launcher", FeatureObject.STYLE_NOMAL));
		}
		if (isStartup) {
			Featurearray.add(new FeatureObject(Resource.STR_FEATURE_STARTUP_LAB.getString(),
					Resource.STR_FEATURE_STARTUP_DESC.getString(), "feature-startup", FeatureObject.STYLE_NOMAL));
		}
		if (!signaturePermissions.isEmpty()) {
			Featurearray.add(new FeatureObject(Resource.STR_FEATURE_SIGNATURE_LAB.getString(),
					Resource.STR_FEATURE_SIGNATURE_DESC.getString(), "feature-protection-level",
					FeatureObject.STYLE_NOMAL));
		}
		if (sharedUserId != null && !sharedUserId.startsWith("android.uid.system")) {
			Featurearray.add(new FeatureObject(Resource.STR_FEATURE_SHAREDUSERID_LAB.getString(),
					Resource.STR_FEATURE_SHAREDUSERID_DESC.getString(), "feature-shared-user-id",
					FeatureObject.STYLE_NOMAL));
		}
		if (deviceRequirements != null && !deviceRequirements.isEmpty()) {
			Featurearray.add(new FeatureObject(Resource.STR_FEATURE_DEVICE_REQ_LAB.getString(),
					Resource.STR_FEATURE_DEVICE_REQ_DESC.getString(), "feature-device-requirements",
					FeatureObject.STYLE_NOMAL));
		}

		boolean systemSignature = false;

		if (sharedUserId != null && sharedUserId.startsWith("android.uid.system")) {
			int state;
			if (isSamsungSign || isPlatformSign) {
				state = FeatureObject.STYLE_IMPORTANT;
			} else {
				state = FeatureObject.STYLE_VERY_IMPORTANT;
			}
			Featurearray.add(new FeatureObject(Resource.STR_FEATURE_SYSTEM_UID_LAB.getString(),
					Resource.STR_FEATURE_SYSTEM_UID_DESC.getString(), "feature-system-user-id", state));
		}

		if (isPlatformSign) {
			Featurearray.add(new FeatureObject(Resource.STR_FEATURE_PLATFORM_SIGN_LAB.getString(),
					Resource.STR_FEATURE_PLATFORM_SIGN_DESC.getString(), "feature-platform-sign",
					FeatureObject.STYLE_IMPORTANT));
			systemSignature = true;
		}

		if (isSamsungSign) {
			Featurearray.add(new FeatureObject(Resource.STR_FEATURE_SAMSUNG_SIGN_LAB.getString(),
					Resource.STR_FEATURE_SAMSUNG_SIGN_DESC.getString(), "feature-samsung-sign",
					FeatureObject.STYLE_IMPORTANT));
			systemSignature = true;
		}
		if (((hasSignatureLevel || hasSignatureOrSystemLevel) && !systemSignature) || hasSystemLevel) {
			Featurearray.add(new FeatureObject(Resource.STR_FEATURE_REVOKE_PERM_LAB.getString(),
					Resource.STR_FEATURE_REVOKE_PERM_DESC.getString(), "feature-revoke-permissions",
					FeatureObject.STYLE_IMPORTANT));
			if (deprecatedPermissions != null && !deprecatedPermissions.isEmpty()) {
				Featurearray.add(new FeatureObject(Resource.STR_FEATURE_DEPRECATED_PREM_LAB.getString(),
						Resource.STR_FEATURE_DEPRECATED_PREM_DESC.getString(), "feature-deprecated-perm",
						FeatureObject.STYLE_IMPORTANT));
			}
			if (debuggable) {
				Featurearray.add(new FeatureObject(Resource.STR_FEATURE_DEBUGGABLE_LAB.getString(),
						Resource.STR_FEATURE_DEBUGGABLE_DESC.getString(), "feature-debuggable",
						FeatureObject.STYLE_IMPORTANT));
			}
			if (isInstrumentation) {
				Featurearray.add(new FeatureObject(Resource.STR_FEATURE_INSTRUMENTATION_LAB.getString(),
						Resource.STR_FEATURE_INSTRUMENTATION_DESC.getString(), "feature-instrumentation",
						FeatureObject.STYLE_IMPORTANT));
			}
		}
	}
}
