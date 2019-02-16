package com.apkscanner.gui.tabpanels;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.text.Element;
import javax.swing.text.html.Option;
import javax.xml.bind.DatatypeConverter;

import com.apkscanner.core.permissionmanager.PermissionGroupInfoExt;
import com.apkscanner.core.permissionmanager.PermissionManager;
import com.apkscanner.core.permissionmanager.PermissionManager.UsesPermissionTag;
import com.apkscanner.core.permissionmanager.PermissionRepository.SourceCommit;
import com.apkscanner.core.scanner.ApkScanner.Status;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.data.apkinfo.ApkInfoHelper;
import com.apkscanner.data.apkinfo.CompatibleScreensInfo;
import com.apkscanner.data.apkinfo.PermissionInfo;
import com.apkscanner.data.apkinfo.ResourceInfo;
import com.apkscanner.data.apkinfo.SupportsGlTextureInfo;
import com.apkscanner.data.apkinfo.SupportsScreensInfo;
import com.apkscanner.data.apkinfo.UsesConfigurationInfo;
import com.apkscanner.data.apkinfo.UsesFeatureInfo;
import com.apkscanner.data.apkinfo.UsesLibraryInfo;
import com.apkscanner.gui.dialog.SdkVersionInfoDlg;
import com.apkscanner.gui.messagebox.MessageBoxPane;
import com.apkscanner.gui.util.ImageScaler;
import com.apkscanner.gui.util.JHtmlEditorPane;
import com.apkscanner.gui.util.JHtmlEditorPane.HyperlinkClickListener;
import com.apkscanner.plugin.IPackageSearcher;
import com.apkscanner.plugin.IPlugIn;
import com.apkscanner.plugin.ITabbedRequest;
import com.apkscanner.plugin.PlugInManager;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.FileUtil;
import com.apkscanner.util.FileUtil.FSStyle;
import com.apkscanner.util.Log;
import com.apkscanner.util.SystemUtil;

public class BasicInfo extends AbstractTabbedPanel implements HyperlinkClickListener, IProgressListener, ListDataListener
{
	private static final long serialVersionUID = 6431995641984509482L;

	private static final String CARD_LODING_PAGE = "CARD_LODING_PROCESS";
	private static final String CARD_APK_INFORMATION = "CARD_APK_INFORMATION";

	private JHtmlEditorPane apkInfoPanel;
	private JPanel lodingPanel;
	private JLabel messageLabel;
	private CardLayout cardLayout;

	private String apkPath;
	private String iconPath;
	private String mutiLabels;
	private String certSummary;
	private String versionDesc;
	private String deviceRequirements;
	private boolean hasPlatformSign;

	private String allPermissionsList;
	private String notGrantPermmissions;
	private String deprecatedPermissions;

	private PermissionManager permissionManager;

	public BasicInfo()
	{
		setName(Resource.STR_TAB_BASIC_INFO.getString());
		setToolTipText(Resource.STR_TAB_BASIC_INFO.getString());
		setEnabled(true);

		initialize();
		showAbout();
	}

	@Override
	public void initialize()
	{
		apkInfoPanel = new JHtmlEditorPane();
		apkInfoPanel.setEditable(false);
		apkInfoPanel.setOpaque(true);

		apkInfoPanel.setBackground(Color.white);
		apkInfoPanel.setHyperlinkClickListener(this);

		// loding panel
		JLabel logo = new JLabel(Resource.IMG_APK_LOGO.getImageIcon(400, 250));
		logo.setOpaque(true);
		logo.setBackground(Color.white);

		JLabel gif = new JLabel(Resource.IMG_WAIT_BAR.getImageIcon());
		gif.setOpaque(true);
		gif.setBackground(Color.white);
		gif.setPreferredSize(new Dimension(Resource.IMG_WAIT_BAR.getImageIcon().getIconWidth(),Resource.IMG_WAIT_BAR.getImageIcon().getIconHeight()));

		messageLabel = new JLabel("");
		messageLabel.setOpaque(true);
		messageLabel.setBackground(Color.WHITE);
		messageLabel.setBorder(new EmptyBorder(0,0,50,0));
		messageLabel.setHorizontalAlignment(JLabel.CENTER);

		lodingPanel = new JPanel();
		lodingPanel.setLayout(new BorderLayout());
		lodingPanel.setOpaque(false);
		lodingPanel.setBackground(Color.white);
		lodingPanel.add(logo,BorderLayout.NORTH);
		lodingPanel.add(gif,BorderLayout.CENTER);
		lodingPanel.add(messageLabel,BorderLayout.SOUTH);

		setLayout(cardLayout = new CardLayout());
		add(CARD_LODING_PAGE, lodingPanel);
		add(CARD_APK_INFORMATION, apkInfoPanel);
		cardLayout.show(this, CARD_APK_INFORMATION);
	}

	private void showAbout()
	{
		apkInfoPanel.setText(Resource.RAW_ABUOT_HTML.getString());
		apkInfoPanel.insertElementFirst("apkscanner-icon-td", "<img src=\"" + Resource.IMG_APP_ICON.getPath() + "\" width=\"150\" height=\"150\">");
		apkInfoPanel.setInnerHTMLById("apkscanner-title", Resource.STR_APP_NAME.getString() + " " + Resource.STR_APP_VERSION.getString());
		apkInfoPanel.setOuterHTMLById("programmer-email", "<a href=\"mailto:" + Resource.STR_APP_MAKER_EMAIL.getString() + "\" title=\"" + Resource.STR_APP_MAKER_EMAIL.getString() + "\">" + Resource.STR_APP_MAKER.getString() + "</a>");
		if(!SystemUtil.hasShortCut()){
			apkInfoPanel.insertElementLast("apkscanner-icon-td", "<div id=\"create-shortcut\" class=\"div-button\">" + makeHyperLink("@event", Resource.STR_BTN_CREATE_SHORTCUT.getString(), null, "function-create-shortcut", null) + "</div>");
		}
		if(!SystemUtil.isAssociatedWithFileType(".apk")) {
			apkInfoPanel.insertElementLast("apkscanner-icon-td", "<div id=\"associate-file\" class=\"div-button\">" + makeHyperLink("@event", Resource.STR_BTN_ASSOC_FTYPE.getString(), null, "function-assoc-apk", null) + "</div>");
		}
	}

	public void onProgress(String message)
	{
		if(lodingPanel == null) initialize();
		if(!lodingPanel.isVisible()) {
			apkPath = null;
			cardLayout.show(this, CARD_LODING_PAGE);
		}
		if(message == null || message.isEmpty())
			message = "Standby for extracting.";
		messageLabel.setText(message);
	}

	@Override
	public void setData(ApkInfo apkInfo, Status status, ITabbedRequest request)
	{
		if(apkInfoPanel == null) initialize();

		if(apkInfo == null) {
			apkPath = null;
			showAbout();
			sendRequest(request, ITabbedRequest.REQUEST_SELECTED);
			return;
		}

		switch(status) {
		case BASIC_INFO_COMPLETED:
			setBasicInfo(apkInfo);
			sendRequest(request, ITabbedRequest.REQUEST_SELECTED);
			break;
		case CERT_COMPLETED:
			if(apkPath != null && !hasPlatformSign) {
				updateFeatureRelateSign(apkInfo);
				if(hasPlatformSign) setPermissionList();
			}
		default:
		}
	}

	private void setBasicInfo(ApkInfo apkInfo) {
		cardLayout.show(this, CARD_APK_INFORMATION);
		apkInfoPanel.setText(Resource.RAW_BASIC_INFO_LAYOUT_HTML.getString());

		apkPath = apkInfo.filePath;
		hasPlatformSign = ApkInfoHelper.isTestPlatformSign(apkInfo)
				|| ApkInfoHelper.isSamsungSign(apkInfo);
		if(hasPlatformSign) {
			certSummary = makeCertSummary(apkInfo);
		}

		setAppIcon(apkInfo.manifest.application.icons, apkInfo.tempWorkPath);
		setAppLabel(apkInfo.manifest.application.labels, apkInfo.manifest.packageName);
		apkInfoPanel.setOuterHTMLById("package", apkInfo.manifest.packageName);
		setVersion(apkInfo.manifest.versionName, apkInfo.manifest.versionCode);
		setSdkVersion(apkInfo.manifest.usesSdk.minSdkVersion, apkInfo.manifest.usesSdk.targetSdkVersion, apkInfo.manifest.usesSdk.maxSdkVersion);
		setFileSize(apkInfo.filePath);

		int selectSdkVer = makeSdkOptions(apkInfo.manifest.usesSdk.targetSdkVersion);
		if(permissionManager == null) {
			permissionManager = new PermissionManager();
		} else {
			permissionManager.clearPermissions();
		}
		permissionManager.addUsesPermission(apkInfo.manifest.usesPermission);
		permissionManager.addUsesPermission(apkInfo.manifest.usesPermissionSdk23);
		permissionManager.addDeclarePemission(apkInfo.manifest.permission);
		permissionManager.setSdkVersion(selectSdkVer);

		setPermissionList();
		setFeatures(apkInfo);

		setPluginSearcher();
	}

	private void setInfoAreaHeight(int groupCount) {
		int infoHeight = groupCount > 15 ? 220 : (groupCount > 0 ? 260 : 280);
		apkInfoPanel.setOuterHTMLById("basic-info-height-td", "<td id=\"basic-info-height-td\" height=\""+infoHeight+"\"></td>");
	}

	private void setPermissionList() {
		StringBuilder permissionList = new StringBuilder();

		PermissionInfo[] permissions = permissionManager.getPermissions(UsesPermissionTag.UsesPermission);
		if(permissions != null && permissions.length > 0) {
			permissionList.append("<uses-permission> [" +  permissions.length + "]\n");
			for(PermissionInfo info: permissions) {
				permissionList.append(info.name + " - " + info.protectionLevel);
				if(((info.isSignatureLevel() || info.isSignatureOrSystemLevel()) && !hasPlatformSign) || info.isSystemLevel()) {
					notGrantPermmissions += info.name + " - " + info.protectionLevel + "\n";
				}
				if(info.isDeprecated()) {
					deprecatedPermissions += info.getDeprecatedMessage() + "\n\n";
				}
				permissionList.append("\n");
			}
		}

		permissions = permissionManager.getPermissions(UsesPermissionTag.UsesPermissionSdk23);
		if(permissions != null && permissions.length > 0) {
			if(permissionList.length() > 0) permissionList.append("\n");
			permissionList.append("<uses-permission-sdk-23> [" +  permissions.length + "]\n");
			for(PermissionInfo info: permissions) {
				permissionList.append(info.name + " - " + info.protectionLevel);
				if(((info.isSignatureLevel() || info.isSignatureOrSystemLevel()) && !hasPlatformSign) || info.isSystemLevel()) {
					notGrantPermmissions += info.name + " - " + info.protectionLevel + "\n";
				}
				if(info.isDeprecated()) {
					deprecatedPermissions += info.getDeprecatedMessage() + "\n\n";
				}
				permissionList.append("\n");
			}
		}

		permissions = permissionManager.getDeclarePermissions();
		if(permissions != null && permissions.length > 0) {
			if(permissionList.length() > 0) {
				permissionList.append("\n");
			}
			permissionList.append("<permission> [" +  permissions.length + "]\n");
			for(PermissionInfo info: permissions) {
				permissionList.append(info.name + " - " + info.protectionLevel + "\n");
			}
		}
		allPermissionsList = permissionList.toString();

		if(!allPermissionsList.isEmpty()) {
			apkInfoPanel.setInnerHTMLById("perm-group-title", "[" + Resource.STR_BASIC_PERMISSIONS.getString() + "] - " +
					makeHyperLink("@event","<u>" + Resource.STR_BASIC_PERMLAB_DISPLAY.getString() + "</u>",Resource.STR_BASIC_PERMDESC_DISPLAY.getString(),"display-list", null));
		} else {
			apkInfoPanel.setInnerHTMLById("perm-group-title", Resource.STR_LABEL_NO_PERMISSION.getString());
		}

		apkInfoPanel.setOuterHTMLById("perm-groups", "<div id=\"perm-groups\">" + makePermGroup() + "</div>");
		setInfoAreaHeight(permissionManager.getPermissionGroups().length);
	}

	private String makeCertSummary(ApkInfo apkInfo) {
		StringBuilder summary = new StringBuilder();
		if(apkInfo.certificates != null) {
			for(String sign: apkInfo.certificates) {
				String[] line = sign.split("\n");
				if(line.length >= 3) {
					summary.append(line[0]).append("\n")
						.append(line[1]).append("\n")
						.append(line[2]).append("\n\n");
				} else {
					summary.append("error\n");
				}
			}
		}
		return summary.toString();
	}

	private void setAppIcon(ResourceInfo[] icons, String tempWorkPath) {
		iconPath = null;
		if(icons != null && icons.length > 0) {
			ResourceInfo[] iconList = icons;
			for(int i=iconList.length-1; i >= 0; i--) {
				if(iconList[i].name.endsWith(".xml")) continue;
				iconPath = iconList[i].name;
				if(iconPath != null) break;
			}
		}
		apkInfoPanel.setOuterHTMLById("icon", "<img src=\"" + iconPath + "\" width=\"150\" height=\"150\">");
	}

	private void setAppLabel(ResourceInfo[] labels, String packageName) {
		String appName = null;

		StringBuilder labelBuilder = new StringBuilder();
		if(labels != null && labels.length > 0) {
			appName = ApkInfoHelper.getResourceValue(labels, (String)Resource.PROP_PREFERRED_LANGUAGE.getData(""));
			if(appName != null && appName.isEmpty()) appName = null;

			for(ResourceInfo r: labels) {
				if(r.configuration == null || r.configuration.isEmpty() || "default".equals(r.configuration)) {
					labelBuilder.append(r.name != null ? r.name : packageName);
					if(appName == null && r.name != null) appName = r.name;
				} else {
					labelBuilder.append("[").append(r.configuration).append("] ").append(r.name);
				}
				labelBuilder.append("\n");
			}
		}
		if(appName == null) appName = packageName;
		mutiLabels = labelBuilder.toString();

		if(labels.length > 1) {
			apkInfoPanel.setInnerHTMLById("label", makeHyperLink("@event", appName, mutiLabels, "other-lang", null));
			apkInfoPanel.insertElementLast("label", "<font>" + makeHyperLink("@event", "&nbsp;["+labels.length+"]", mutiLabels, "other-lang", null) + "</font>");
		} else {
			apkInfoPanel.setInnerHTMLById("label", appName);
		}
	}

	private void setVersion(String versionName, Integer versionCode) {
		if(versionName == null) versionName = "";
		StringBuilder text = new StringBuilder("Ver. ")
				.append(versionName)
				.append(" / ")
				.append((versionCode != null ? versionCode : "0"));
		StringBuilder descripton = new StringBuilder("VersionName : ")
				.append(versionName)
				.append("\n")
				.append("VersionCode : ")
				.append((versionCode != null ? versionCode : "Unspecified"));
		versionDesc = descripton.toString();

		apkInfoPanel.setInnerHTMLById("version", makeHyperLink("@event", text.toString(), versionDesc, "app-version", null));
	}

	private void setSdkVersion(Integer minSdkVersion, Integer targetSdkVersion, Integer maxSdkVersion) {
		StringBuilder sdkVersion = new StringBuilder();
		if(minSdkVersion != null) {
			sdkVersion.append(", ")
				.append(makeHyperLink("@event", minSdkVersion +" (Min)", "Min SDK version", "sdk-info:" + minSdkVersion, null));
		}
		if(targetSdkVersion != null) {
			sdkVersion.append(", ")
				.append(makeHyperLink("@event", targetSdkVersion + " (Target)", "Targer SDK version", "sdk-info:" + targetSdkVersion, null));
		}
		if(maxSdkVersion != null) {
			sdkVersion.append(", ")
				.append(makeHyperLink("@event", maxSdkVersion + " (Max)", "Max SDK version", "sdk-info:" + maxSdkVersion, null));
		}
		if(sdkVersion.length() == 0) {
			sdkVersion.append(", Unspecified");
		}

		apkInfoPanel.setOuterHTMLById("sdk-version", sdkVersion.substring(2));
	}

	private int makeSdkOptions(Integer targetSdkVersion) {
		int tarSdk = targetSdkVersion != null ? targetSdkVersion : -1;
		int permSdk = -1;
		boolean matchedSdk = false;
		StringBuilder sdkOptions = new StringBuilder("<select id=\"perm-group-sdks\">");
		for(SourceCommit sdk: PermissionManager.getPermissionRepository().sources) {
			if(sdk.getCommitId() != null) {
				permSdk = sdk.getSdkVersion();
				if(!matchedSdk) matchedSdk = permSdk == tarSdk;
				sdkOptions.append(String.format("<option value=%d%s>API Level %d</option>", permSdk
						, permSdk == tarSdk ? " selected" : "", permSdk));
			}
		}
		sdkOptions.append("</select>");
		apkInfoPanel.setOuterHTMLById("perm-group-sdks", sdkOptions.toString());

		if(targetSdkVersion != null) permSdk = tarSdk;

		Object object = apkInfoPanel.getElementModelById("perm-group-sdks");
		if(object instanceof DefaultComboBoxModel) {
			DefaultComboBoxModel<?> model = (DefaultComboBoxModel<?>)object;
			if(!matchedSdk) {
				if(targetSdkVersion == null) {
					Option option = (Option)model.getElementAt(model.getSize()-1);
					model.setSelectedItem(option);
					permSdk = Integer.parseInt(option.getValue());
				} else {
					model.setSelectedItem("API Level " + permSdk);
				}
			}
			model.removeListDataListener(this);
			model.addListDataListener(this);
		}

		return permSdk;
	}

	private void setFileSize(String filePath) {
		File apkFile = new File(filePath);
		String text = FileUtil.getFileSize(apkFile.length(), FSStyle.FULL);
		String description = "MD5: " + FileUtil.getMessageDigest(apkFile, "MD5");
		apkInfoPanel.setInnerHTMLById("file-size", makeHyperLink("@event", text, description, "file-checksum", null));
	}

	private void setFeatures(ApkInfo apkInfo) {
		StringBuilder feature = new StringBuilder("[" + Resource.STR_FEATURE_LAB.getString() + "] ");
		if("internalOnly".equals(apkInfo.manifest.installLocation)) {
			feature.append(makeHyperLink("@event", Resource.STR_FEATURE_ILOCATION_INTERNAL_LAB.getString(), Resource.STR_FEATURE_ILOCATION_INTERNAL_DESC.getString(), "feature-install-location-internal", null));
		} else if("auto".equals(apkInfo.manifest.installLocation)) {
			feature.append(makeHyperLink("@event", Resource.STR_FEATURE_ILOCATION_AUTO_LAB.getString(), Resource.STR_FEATURE_ILOCATION_AUTO_DESC.getString(), "feature-install-location-auto", null));
		} else if("preferExternal".equals(apkInfo.manifest.installLocation)) {
			feature.append(makeHyperLink("@event", Resource.STR_FEATURE_ILOCATION_EXTERNAL_LAB.getString(), Resource.STR_FEATURE_ILOCATION_EXTERNAL_DESC.getString(), "feature-install-location-external", null));
		}
		feature.append("<br/>");

		if(ApkInfoHelper.isHidden(apkInfo)) {
			feature.append(makeHyperLink("@event", Resource.STR_FEATURE_HIDDEN_LAB.getString(), Resource.STR_FEATURE_HIDDEN_DESC.getString(), "feature-hidden", null));
		} else {
			feature.append(makeHyperLink("@event", Resource.STR_FEATURE_LAUNCHER_LAB.getString(), Resource.STR_FEATURE_LAUNCHER_DESC.getString(), "feature-launcher", null));
		}
		if(ApkInfoHelper.isStartup(apkInfo)) {
			feature.append(", " + makeHyperLink("@event", Resource.STR_FEATURE_STARTUP_LAB.getString(), Resource.STR_FEATURE_STARTUP_DESC.getString(), "feature-startup", null));
		}
		String sharedUserId = apkInfo.manifest.sharedUserId;
		if(sharedUserId != null && !sharedUserId.startsWith("android.uid.system") ) {
			feature.append(", " + makeHyperLink("@event", Resource.STR_FEATURE_SHAREDUSERID_LAB.getString(), Resource.STR_FEATURE_SHAREDUSERID_DESC.getString(), "feature-shared-user-id:" + sharedUserId, null));
		}
		deviceRequirements = makeDeviceRequirements(apkInfo);
		if(deviceRequirements != null && !deviceRequirements.isEmpty()) {
			feature.append(", " + makeHyperLink("@event", Resource.STR_FEATURE_DEVICE_REQ_LAB.getString(), Resource.STR_FEATURE_DEVICE_REQ_DESC.getString(), "feature-device-requirements", null));
		}

		boolean isSamsungSign = ApkInfoHelper.isSamsungSign(apkInfo);
		boolean isPlatformSign = ApkInfoHelper.isTestPlatformSign(apkInfo);
		StringBuilder particularFeatures = new StringBuilder();
		if(sharedUserId != null && sharedUserId.startsWith("android.uid.system")) {
			particularFeatures.append(", ");
			if(!isSamsungSign && !isPlatformSign) particularFeatures.append("<span id=\"system-uid\">");
			particularFeatures.append(makeHyperLink("@event", Resource.STR_FEATURE_SYSTEM_UID_LAB.getString(), Resource.STR_FEATURE_SYSTEM_UID_DESC.getString(), "feature-system-user-id", null));
			if(!isSamsungSign && !isPlatformSign) particularFeatures.append("</span>");
		}
		if(isPlatformSign) {
			particularFeatures.append(", ");
			particularFeatures.append(makeHyperLink("@event", Resource.STR_FEATURE_PLATFORM_SIGN_LAB.getString(), Resource.STR_FEATURE_PLATFORM_SIGN_DESC.getString(), "feature-platform-sign", null));
		}
		if(isSamsungSign) {
			particularFeatures.append(", ");
			particularFeatures.append(makeHyperLink("@event", Resource.STR_FEATURE_SAMSUNG_SIGN_LAB.getString(), Resource.STR_FEATURE_SAMSUNG_SIGN_DESC.getString(), "feature-samsung-sign", null));
		}
		if(notGrantPermmissions != null && !notGrantPermmissions.isEmpty()) {
			particularFeatures.append(", ");
			particularFeatures.append(makeHyperLink("@event", Resource.STR_FEATURE_REVOKE_PERM_LAB.getString(), Resource.STR_FEATURE_REVOKE_PERM_DESC.getString(), "feature-revoke-permissions", null));
		}
		if(deprecatedPermissions != null && !deprecatedPermissions.isEmpty()) {
			particularFeatures.append(", ");
			particularFeatures.append(makeHyperLink("@event", Resource.STR_FEATURE_DEPRECATED_PREM_LAB.getString(), Resource.STR_FEATURE_DEPRECATED_PREM_DESC.getString(), "feature-deprecated-perm", null));
		}
		if(ApkInfoHelper.isDebuggable(apkInfo)) {
			particularFeatures.append(", ");
			particularFeatures.append(makeHyperLink("@event", Resource.STR_FEATURE_DEBUGGABLE_LAB.getString(), Resource.STR_FEATURE_DEBUGGABLE_DESC.getString(), "feature-debuggable", null));
		}
		if(ApkInfoHelper.isInstrumentation(apkInfo)) {
			particularFeatures.append(", ");
			particularFeatures.append(makeHyperLink("@event", Resource.STR_FEATURE_INSTRUMENTATION_LAB.getString(), Resource.STR_FEATURE_INSTRUMENTATION_DESC.getString(), "feature-instrumentation", null));
		}

		apkInfoPanel.setInnerHTMLById("features", feature.toString());
		if(particularFeatures.length() > 0) {
			apkInfoPanel.insertElementAfter("features", "<h4 id=\"particular-features\"></h4>");
			apkInfoPanel.setInnerHTMLById("particular-features",  particularFeatures.substring(2));
		}
	}

	private void updateFeatureRelateSign(ApkInfo apkInfo) {
		if(apkInfo.certificates == null) {
			Log.v("certificates is empty");
			return;
		}

		boolean isSamsungSign = ApkInfoHelper.isSamsungSign(apkInfo);
		boolean isPlatformSign = ApkInfoHelper.isTestPlatformSign(apkInfo);
		hasPlatformSign = isSamsungSign || isPlatformSign;
		if(!hasPlatformSign) return;

		certSummary = makeCertSummary(apkInfo);

		boolean isOtherEmpty = false;
		Element elem = apkInfoPanel.getElementById("particular-features");
		if(elem == null) {
			apkInfoPanel.insertElementAfter("features", "<h4 id=\"particular-features\"></h4>");
			isOtherEmpty = true;
		}
		StringBuilder particularFeatures = new StringBuilder();
		elem = apkInfoPanel.getElementById("system-uid");
		if(elem != null) {
			particularFeatures.append(", ");
			particularFeatures.append(makeHyperLink("@event", Resource.STR_FEATURE_SYSTEM_UID_LAB.getString(), Resource.STR_FEATURE_SYSTEM_UID_DESC.getString(), "feature-system-user-id", null));
		}
		if(isPlatformSign) {
			particularFeatures.append(", ");
			particularFeatures.append(makeHyperLink("@event", Resource.STR_FEATURE_PLATFORM_SIGN_LAB.getString(), Resource.STR_FEATURE_PLATFORM_SIGN_DESC.getString(), "feature-platform-sign", null));
		}
		if(isSamsungSign) {
			particularFeatures.append(", ");
			particularFeatures.append(makeHyperLink("@event", Resource.STR_FEATURE_SAMSUNG_SIGN_LAB.getString(), Resource.STR_FEATURE_SAMSUNG_SIGN_DESC.getString(), "feature-samsung-sign", null));
		}
		if(particularFeatures.length() > 0) {
			if(elem != null) {
				apkInfoPanel.setOuterHTML(elem, particularFeatures.substring(2));
			} else {
				if(!isOtherEmpty) particularFeatures.append(",&nbsp;");
				apkInfoPanel.insertElementFirst("particular-features",  particularFeatures.substring(2));
			}
		}
	}

	private String makeDeviceRequirements(ApkInfo apkInfo) {
		StringBuilder deviceReqData = new StringBuilder();
		if(apkInfo.manifest.compatibleScreens != null) {
			for(CompatibleScreensInfo info: apkInfo.manifest.compatibleScreens) {
				deviceReqData.append(info.getReport());
			}
			deviceReqData.append("\n");
		}
		if(apkInfo.manifest.supportsScreens != null) {
			for(SupportsScreensInfo info: apkInfo.manifest.supportsScreens) {
				deviceReqData.append(info.getReport());
			}
			deviceReqData.append("\n");
		}
		if(apkInfo.manifest.usesFeature != null) {
			for(UsesFeatureInfo info: apkInfo.manifest.usesFeature) {
				deviceReqData.append(info.getReport());
			}
			deviceReqData.append("\n");
		}
		if(apkInfo.manifest.usesConfiguration != null) {
			for(UsesConfigurationInfo info: apkInfo.manifest.usesConfiguration) {
				deviceReqData.append(info.getReport());
			}
			deviceReqData.append("\n");
		}
		if(apkInfo.manifest.usesLibrary != null) {
			deviceReqData.append("uses library :\n");
			for(UsesLibraryInfo info: apkInfo.manifest.usesLibrary) {
				deviceReqData.append(info.getReport());
			}
			deviceReqData.append("\n");
		}
		if(apkInfo.manifest.supportsGlTexture != null) {
			for(SupportsGlTextureInfo info: apkInfo.manifest.supportsGlTexture) {
				deviceReqData.append(info.getReport());
			}
			deviceReqData.append("\n");
		}
		return deviceReqData.toString();
	}

	private void setPluginSearcher() {
		String packageSearchers = "";
		String appLabelSearchers = "";
		if((boolean)Resource.PROP_VISIBLE_TO_BASIC.getData()) {
			IPackageSearcher[] searchers = PlugInManager.getPackageSearchers();
			if(searchers.length > 0) {
				String defaultSearchIcon = Resource.IMG_TOOLBAR_SEARCH.getPath();
				for(IPackageSearcher searcher: searchers) {
					if(!searcher.isVisibleToBasic()) continue;
					URL icon = searcher.getIconURL();
					String iconPath = icon != null ? icon.toString() : defaultSearchIcon;
					String tag = makeHyperLink("@event", " <img src=\"" + iconPath + "\" width=\"16\" height=\"16\" /> ", null, "PLUGIN:"+searcher.getActionCommand(), "color:white;");
					switch(searcher.getSupportType() ) {
					case IPackageSearcher.SEARCHER_TYPE_PACKAGE_NAME:
						packageSearchers += tag;
						break;
					case IPackageSearcher.SEARCHER_TYPE_APP_NAME:
						appLabelSearchers += tag;
						break;
					};
				}
			}
		}

		apkInfoPanel.insertElementLast("label", appLabelSearchers);
		if(!packageSearchers.isEmpty()) {
			apkInfoPanel.setOuterHTMLById("package-searcher", packageSearchers);
		} else {
			apkInfoPanel.removeElementById("package-searcher");
		}
	}

	private String makePermGroup()
	{
		StringBuilder permGroup = new StringBuilder("");
		int cnt = 0;
		int sdk = permissionManager.getSdkVersion();
		boolean isDanger;
		for(PermissionGroupInfoExt g: permissionManager.getPermissionGroups()) {
			isDanger = sdk >= 23 && g.hasDangerous();
			if(cnt % 15 != 0) permGroup.append("&nbsp;");
			permGroup.append(makeHyperLink("@event", makeImage(g.getIconPath(), g.permissions.size(), isDanger), g.getSummary(), g.name, null));
			if(++cnt % 15 == 0) permGroup.append("<br>");
		}
		return permGroup.toString();
	}

	private String makeHyperLink(String href, String text, String title, String id, String style)
	{
		return JHtmlEditorPane.makeHyperLink(href, text, title, id, style);
	}

	private String makeImage(String src, int count, boolean isDanger)
	{
		BufferedImage image = null;
		try {
			image = ImageIO.read(new URL(src));
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(image == null) {
			return "<img src=\"" + src + "\">";
		}

		Map<?, ?> desktopHints = (Map<?, ?>) Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
		Graphics2D g2 = image.createGraphics();
		if (desktopHints != null) {
		    g2.setRenderingHints(desktopHints);
		} else {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		}
		Font font = UIManager.getFont("Label.font");
		if(font != null) g2.setFont(font.deriveFont(10f));

		if(isDanger) {
			g2.setColor(Color.WHITE);
			if(isDanger) g2.fillOval(0, 0, 12, 12);
			g2.setColor(Color.RED);
			g2.drawString("R", 3, 10);
		}

		g2.setColor(Color.WHITE);
		g2.fillOval(24, 24, 12, 12);
		g2.setColor(Color.BLACK);
		if(count < 10) {
			g2.drawString(Integer.toString(count), 28, 34);
		} else {
			if(font != null) g2.setFont(font.deriveFont(9.2f));
			g2.drawString(Integer.toString(count), 26, 34);
		}

		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			ImageIO.write(image, "png", os);
			String base64Image = DatatypeConverter.printBase64Binary(os.toByteArray());
			if(base64Image != null) {
				src = "data:image/png;base64, " + base64Image;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "<img src=\"" + src + "\">";
	}

	@Override
	public void hyperlinkClick(String id)
	{
		//Log.i("hyperlinkClick() " + id);
		if("other-lang".equals(id)) {
			if(mutiLabels == null || mutiLabels.isEmpty()) return;
			try {
				ImageIcon icon = null;
				if(iconPath != null && (iconPath.startsWith("jar:") || iconPath.startsWith("file:"))) {
					icon = new ImageIcon(ImageScaler.getScaledImage(new ImageIcon(new URL(iconPath)),32,32));
				}
				showDialog(mutiLabels, Resource.STR_LABEL_APP_NAME_LIST.getString(), new Dimension(300, 200), icon);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		} else if("app-version".equals(id)) {
			showDialog(versionDesc, "App version info", new Dimension(300, 50), null);
		} else if("display-list".equals(id)) {
			showPermList();
		} else if(id.startsWith("sdk-info:")){
			showSdkVersionInfo(id);
		} else if(id.startsWith("feature-")) {
			showFeatureInfo(id);
		} else if("function-create-shortcut".equals(id)) {
			SystemUtil.createShortCut();
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					if(SystemUtil.hasShortCut()){
						apkInfoPanel.removeElementById("create-shortcut");
					}
				}
			});
		} else if("function-assoc-apk".equals(id)) {
			SystemUtil.setAssociateFileType(".apk");
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					if(SystemUtil.isAssociatedWithFileType(".apk")) {
						apkInfoPanel.removeElementById("associate-file");
					}
				}
			});
		} else if("file-checksum".equals(id)) {
			File apkFile = new File(apkPath);
			StringBuilder checksum = new StringBuilder(apkPath).append("\n")
					.append(FileUtil.getFileSize(apkFile.length(), FSStyle.FULL))
					.append("\n\nMD5: ").append(FileUtil.getMessageDigest(apkFile, "MD5")).append("\n")
					.append("SHA1: ").append(FileUtil.getMessageDigest(apkFile, "SHA-1")).append("\n")
					.append("SHA256: ").append(FileUtil.getMessageDigest(apkFile, "SHA-256"));
			showDialog(checksum.toString(), "APK Checksum", new Dimension(650, 150), null);
		} else if(id != null && id.startsWith("PLUGIN:")) {
			IPlugIn plugin = PlugInManager.getPlugInByActionCommand(id.replaceAll("PLUGIN:", ""));
			if(plugin != null) {
				plugin.launch();
			}
		} else {
			showPermDetailDesc(id);
		}
	}

	@Override
	public void contentsChanged(ListDataEvent evt) {
		DefaultComboBoxModel<?> model = (DefaultComboBoxModel<?>) evt.getSource();
		Object item = model.getSelectedItem();
		if(item instanceof Option) {
			String sdkVersion = ((Option)item).getValue();
			if(sdkVersion == null || sdkVersion.isEmpty()) sdkVersion = "-1";
			Log.v("change sdkVersion " + sdkVersion);
			permissionManager.setSdkVersion(Integer.parseInt(sdkVersion));
			setPermissionList();
		}
	}
	@Override public void intervalAdded(ListDataEvent evt) { }
	@Override public void intervalRemoved(ListDataEvent evt) { }

	@Override
	public void reloadResource() {
		setName(Resource.STR_TAB_BASIC_INFO.getString());
		setToolTipText(Resource.STR_TAB_BASIC_INFO.getString());
	}

	private void showDialog(String content, String title, Dimension size, Icon icon)
	{
		MessageBoxPane.showTextAreaDialog(null, content, title, MessageBoxPane.INFORMATION_MESSAGE, icon, size);
	}

	public void showPermList()
	{
		showDialog(allPermissionsList, Resource.STR_BASIC_PERM_LIST_TITLE.getString(), new Dimension(500, 200), null);
	}

	public void showPermDetailDesc(String group)
	{
		PermissionGroupInfoExt g = permissionManager.getPermissionGroup(group);
		Log.e("g " + g + ", " + group);
		if(g == null) return;

		StringBuilder body = new StringBuilder();
		body.append("■ ");
		if(g.label != null) {
			body.append(g.getLabel() + " - ");
		}
		body.append("[" + group + "]\n");
		if(g.description != null) {
			body.append(" : " + g.getDescription() + "\n");
		}
		body.append("------------------------------------------------------------------------------------------------------------\n\n");

		for(PermissionInfo info: g.permissions) {
			body.append("▶ ");
			if(info.isDangerousLevel()) {
				body.append("[DANGEROUS] ");
			}
			if(info.labels != null) {
				body.append(info.getLabel() + " ");
			}
			body.append("[" + info.name + "]\n");
			String protection = info.protectionLevel;
			if(protection == null) protection= "normal";
			body.append(" - protectionLevel=" + info.protectionLevel + "\n");
			if(info.descriptions != null) {
				body.append(" : " + info.getDescription() + "\n");
				body.append("\n");
			}
		}

		showDialog(body.toString(), Resource.STR_BASIC_PERM_DISPLAY_TITLE.getString(), new Dimension(600, 200), new ImageIcon(g.icon.replaceAll("^file:/", "")));
	}

	public void showSdkVersionInfo(String id)
	{
		if(id == null || !id.contains(":")) return;
		SdkVersionInfoDlg sdkDlg = new SdkVersionInfoDlg(null, Resource.STR_SDK_INFO_FILE_PATH.getString(), Integer.parseInt(id.split(":")[1]));
		sdkDlg.setLocationRelativeTo(this);
		sdkDlg.setVisible(true);
	}

	public void showFeatureInfo(String id)
	{
		String feature = null;
		Dimension size = new Dimension(400, 100);

		if("feature-hidden".equals(id)) {
			feature = Resource.STR_FEATURE_HIDDEN_DESC.getString();
		} else if("feature-launcher".equals(id)) {
			feature = Resource.STR_FEATURE_LAUNCHER_DESC.getString();
		} else if("feature-startup".equals(id)) {
			feature = Resource.STR_FEATURE_STARTUP_DESC.getString();
			feature += "\nandroid.permission.RECEIVE_BOOT_COMPLETED";
		} else if("feature-shared-user-id:".startsWith(id)) {
			feature = "sharedUserId=" + id.split(":")[1] + "\n※ ";
			feature += Resource.STR_FEATURE_SHAREDUSERID_DESC.getString();
		} else if("feature-system-user-id".equals(id)) {
			feature = "sharedUserId=android.uid.system\n※ ";
			feature += Resource.STR_FEATURE_SYSTEM_UID_DESC.getString();
		} else if("feature-platform-sign".equals(id)) {
			feature = "※ " + Resource.STR_FEATURE_PLATFORM_SIGN_DESC.getString();
			feature += "\n\n" + certSummary;
			size = new Dimension(500, 150);
		} else if("feature-samsung-sign".equals(id)) {
			feature = "※ " + Resource.STR_FEATURE_SAMSUNG_SIGN_DESC.getString();
			feature += "\n\n" + certSummary;
			size = new Dimension(500, 150);
		} else if("feature-revoke-permissions".equals(id)) {
			StringBuilder revokePerms = new StringBuilder("※ " + Resource.STR_FEATURE_REVOKE_PERM_DESC.getString() + "\n\n");
			revokePerms.append(notGrantPermmissions);
			feature = revokePerms.toString();
			size = new Dimension(500, 200);
		} else if("feature-deprecated-perm".equals(id)) {
			feature = deprecatedPermissions;
			size = new Dimension(500, 200);
		} else if("feature-debuggable".equals(id)) {
			feature = Resource.STR_FEATURE_DEBUGGABLE_DESC.getString();
		} else if("feature-instrumentation".equals(id)) {
			feature = Resource.STR_FEATURE_INSTRUMENTATION_DESC.getString();
		} else if("feature-device-requirements".equals(id)) {
			feature = deviceRequirements;
			size = new Dimension(500, 250);
		} else if("feature-install-location-internal".equals(id)) {
			feature = Resource.STR_FEATURE_ILOCATION_INTERNAL_DESC.getString();
		} else if("feature-install-location-auto".equals(id)) {
			feature = Resource.STR_FEATURE_ILOCATION_AUTO_DESC.getString();
		} else if("feature-install-location-external".equals(id)) {
			feature = Resource.STR_FEATURE_ILOCATION_EXTERNAL_DESC.getString();
		}

		showDialog(feature, "Feature info", size, null);
	}
}
