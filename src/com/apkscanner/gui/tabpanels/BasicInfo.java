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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton.ToggleButtonModel;
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
import com.apkscanner.core.permissionmanager.RevokedPermissionInfo;
import com.apkscanner.core.permissionmanager.RevokedPermissionInfo.RevokedReason;
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
import com.apkscanner.data.apkinfo.UsesSdkInfo;
import com.apkscanner.gui.dialog.SdkVersionInfoDlg;
import com.apkscanner.gui.messagebox.MessageBoxPane;
import com.apkscanner.gui.util.JHtmlEditorPane;
import com.apkscanner.gui.util.JHtmlEditorPane.HyperlinkClickEvent;
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

public class BasicInfo extends AbstractTabbedPanel implements HyperlinkClickListener, IProgressListener, ListDataListener, ItemListener
{
	private static final long serialVersionUID = 6431995641984509482L;

	private static final String CARD_LODING_PAGE = "CARD_LODING_PROCESS";
	private static final String CARD_APK_INFORMATION = "CARD_APK_INFORMATION";

	private JHtmlEditorPane apkInfoPanel;
	private JPanel lodingPanel;
	private JLabel messageLabel;
	private CardLayout cardLayout;

	private PermissionManager permissionManager = new PermissionManager();

	public BasicInfo() {
		setName(Resource.STR_TAB_BASIC_INFO.getString());
		setToolTipText(Resource.STR_TAB_BASIC_INFO.getString());
		setEnabled(true);

		initialize();
		showAbout();
	}

	@Override
	public void initialize() {
		apkInfoPanel = new JHtmlEditorPane();
		apkInfoPanel.setEditable(false);
		apkInfoPanel.setOpaque(true);
		apkInfoPanel.setBackground(Color.white);
		apkInfoPanel.addHyperlinkClickListener(this);

		JLabel logo = new JLabel(Resource.IMG_APK_LOGO.getImageIcon(400, 250));
		logo.setOpaque(true);
		logo.setBackground(Color.white);

		JLabel gif = new JLabel(Resource.IMG_WAIT_BAR.getImageIcon());
		gif.setOpaque(true);
		gif.setBackground(Color.white);
		gif.setPreferredSize(new Dimension(Resource.IMG_WAIT_BAR.getImageIcon().getIconWidth(), Resource.IMG_WAIT_BAR.getImageIcon().getIconHeight()));

		messageLabel = new JLabel();
		messageLabel.setOpaque(true);
		messageLabel.setBackground(Color.WHITE);
		messageLabel.setBorder(new EmptyBorder(0, 0, 50, 0));
		messageLabel.setHorizontalAlignment(JLabel.CENTER);

		lodingPanel = new JPanel();
		lodingPanel.setLayout(new BorderLayout());
		lodingPanel.setOpaque(false);
		lodingPanel.setBackground(Color.white);
		lodingPanel.add(logo, BorderLayout.NORTH);
		lodingPanel.add(gif, BorderLayout.CENTER);
		lodingPanel.add(messageLabel, BorderLayout.SOUTH);

		setLayout(cardLayout = new CardLayout());
		add(CARD_LODING_PAGE, lodingPanel);
		add(CARD_APK_INFORMATION, apkInfoPanel);
		cardLayout.show(this, CARD_APK_INFORMATION);
	}

	@Override
	public void reloadResource() {
		setName(Resource.STR_TAB_BASIC_INFO.getString());
		setToolTipText(Resource.STR_TAB_BASIC_INFO.getString());
	}

	private void showAbout() {
		apkInfoPanel.setText(Resource.RAW_ABUOT_HTML.getString());
		apkInfoPanel.insertElementFirst("apkscanner-icon-td", String.format("<img src=\"%s\" width=\"150\" height=\"150\">", Resource.IMG_APP_ICON.getPath()));
		apkInfoPanel.setInnerHTMLById("apkscanner-title", Resource.STR_APP_NAME.getString() + " " + Resource.STR_APP_VERSION.getString());
		apkInfoPanel.setOuterHTMLById("programmer-email", String.format("<a href=\"mailto:%s\" title=\"%s\">%s</a>",
				Resource.STR_APP_MAKER_EMAIL.getString(), Resource.STR_APP_MAKER_EMAIL.getString(), Resource.STR_APP_MAKER.getString()));
		if(!SystemUtil.hasShortCut()){
			apkInfoPanel.insertElementLast("apkscanner-icon-td", String.format("<div id=\"create-shortcut\" class=\"div-button\">%s</div>",
					makeHyperEvent("function-create-shortcut", Resource.STR_BTN_CREATE_SHORTCUT.getString(), null)));
		}
		if(!SystemUtil.isAssociatedWithFileType(".apk")) {
			apkInfoPanel.insertElementLast("apkscanner-icon-td", String.format("<div id=\"associate-file\" class=\"div-button\">%s</div>",
					makeHyperEvent("function-assoc-apk", Resource.STR_BTN_ASSOC_FTYPE.getString(), null)));
		}
	}

	public void onProgress(String message) {
		if(lodingPanel == null) initialize();
		if(!lodingPanel.isVisible()) {
			apkInfoPanel.setText("");
			cardLayout.show(this, CARD_LODING_PAGE);
		}
		if(message == null || message.isEmpty())
			message = "Standby for extracting.";
		messageLabel.setText(message);
	}

	@Override
	public void setData(ApkInfo apkInfo, Status status, ITabbedRequest request) {
		if(apkInfoPanel == null) initialize();

		if(apkInfo == null) {
			showAbout();
			sendRequest(request, ITabbedRequest.REQUEST_SELECTED);
			return;
		}

		switch(status) {
		case BASIC_INFO_COMPLETED:
			setBasicInfo(apkInfo);
			cardLayout.show(this, CARD_APK_INFORMATION);
			sendRequest(request, ITabbedRequest.REQUEST_SELECTED);
			break;
		case CERT_COMPLETED:
			if(apkInfoPanel.getElementById("basic-info") != null) {
				if(ApkInfoHelper.isTestPlatformSign(apkInfo) || ApkInfoHelper.isSamsungSign(apkInfo)) {
					permissionManager.setPlatformSigned(true);
					updateFeatureRelateSign(apkInfo);
					setPermissionList(null);
				}
			}
		default:
		}
	}

	private void setBasicInfo(ApkInfo apkInfo) {
		apkInfoPanel.setText(Resource.RAW_BASIC_INFO_LAYOUT_HTML.getString());
		setAppIcon(apkInfo.manifest.application.icons);
		setAppLabel(apkInfo.manifest.application.labels, apkInfo.manifest.packageName);
		setPackageName(apkInfo.manifest.packageName);
		setVersion(apkInfo.manifest.versionName, apkInfo.manifest.versionCode);
		setSdkVersion(apkInfo.manifest.usesSdk);
		setFileSize(apkInfo.filePath);
		setFeatures(apkInfo);
		setPermissionList(apkInfo);
		setPluginSearcher();
	}

	private void setAppIcon(ResourceInfo[] icons) {
		String iconPath = null;
		if(icons != null && icons.length > 0) {
			ResourceInfo[] iconList = icons;
			for(int i=iconList.length-1; i >= 0; i--) {
				if(iconList[i].name.endsWith(".xml")) continue;
				iconPath = iconList[i].name;
				if(iconPath != null) break;
			}
		}
		apkInfoPanel.setOuterHTMLById("icon", String.format("<img src=\"%s\" width=\"150\" height=\"150\">", iconPath));
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
		String mutiLabels = labelBuilder.toString();
		if(appName == null) appName = packageName;

		if(labels.length > 1) {
			apkInfoPanel.setInnerHTMLById("label", makeHyperEvent("other-lang", appName, mutiLabels));
			apkInfoPanel.insertElementLast("label", "<font>" + makeHyperEvent("other-lang", "&nbsp;["+labels.length+"]", mutiLabels, mutiLabels) + "</font>");
		} else {
			apkInfoPanel.setInnerHTMLById("label", appName);
		}
	}

	private void setPackageName(String packageName) {
		apkInfoPanel.setOuterHTMLById("package", packageName);
	}

	private void setVersion(String versionName, Integer versionCode) {
		if(versionName == null) versionName = "";
		StringBuilder text = new StringBuilder("Ver. ").append(versionName)
				.append(" / ").append((versionCode != null ? versionCode : "0"));
		StringBuilder descripton = new StringBuilder("VersionName : ").append(versionName).append("\n")
				.append("VersionCode : ").append((versionCode != null ? versionCode : "Unspecified"));
		String versionDesc = descripton.toString();

		apkInfoPanel.setInnerHTMLById("version", makeHyperEvent("app-version", text.toString(), versionDesc, versionDesc));
	}

	private void setSdkVersion(UsesSdkInfo usesSdk) {
		StringBuilder sdkVersion = new StringBuilder();
		if(usesSdk.minSdkVersion != null) {
			sdkVersion.append(", ")
				.append(makeHyperEvent("min-sdk-info", usesSdk.minSdkVersion +" (Min)", "Min SDK version", usesSdk.minSdkVersion));
		}
		if(usesSdk.targetSdkVersion != null) {
			sdkVersion.append(", ")
				.append(makeHyperEvent("target-sdk-info", usesSdk.targetSdkVersion + " (Target)", "Targer SDK version", usesSdk.targetSdkVersion));
		}
		if(usesSdk.maxSdkVersion != null) {
			sdkVersion.append(", ")
				.append(makeHyperEvent("max-sdk-info", usesSdk.maxSdkVersion + " (Max)", "Max SDK version", usesSdk.maxSdkVersion));
		}
		if(sdkVersion.length() == 0) {
			sdkVersion.append(", Unspecified");
		}
		apkInfoPanel.setOuterHTMLById("sdk-version", sdkVersion.substring(2));
	}

	private void setFileSize(String filePath) {
		File apkFile = new File(filePath);
		String text = FileUtil.getFileSize(apkFile.length(), FSStyle.FULL);
		String description = "MD5: " + FileUtil.getMessageDigest(apkFile, "MD5");
		apkInfoPanel.setInnerHTMLById("file-size", makeHyperEvent("file-checksum", text, description, filePath));
	}

	private void setFeatures(ApkInfo apkInfo) {
		StringBuilder feature = new StringBuilder("[" + Resource.STR_FEATURE_LAB.getString() + "] ");
		if("internalOnly".equals(apkInfo.manifest.installLocation)) {
			feature.append(makeHyperEvent("feature-install-location-internal", Resource.STR_FEATURE_ILOCATION_INTERNAL_LAB.getString(), Resource.STR_FEATURE_ILOCATION_INTERNAL_DESC.getString()));
		} else if("auto".equals(apkInfo.manifest.installLocation)) {
			feature.append(makeHyperEvent("feature-install-location-auto", Resource.STR_FEATURE_ILOCATION_AUTO_LAB.getString(), Resource.STR_FEATURE_ILOCATION_AUTO_DESC.getString()));
		} else if("preferExternal".equals(apkInfo.manifest.installLocation)) {
			feature.append(makeHyperEvent("feature-install-location-external", Resource.STR_FEATURE_ILOCATION_EXTERNAL_LAB.getString(), Resource.STR_FEATURE_ILOCATION_EXTERNAL_DESC.getString()));
		}
		feature.append("<br/>");

		if(ApkInfoHelper.isHidden(apkInfo)) {
			feature.append(makeHyperEvent("feature-hidden", Resource.STR_FEATURE_HIDDEN_LAB.getString(), Resource.STR_FEATURE_HIDDEN_DESC.getString()));
		} else {
			feature.append(makeHyperEvent("feature-launcher", Resource.STR_FEATURE_LAUNCHER_LAB.getString(), Resource.STR_FEATURE_LAUNCHER_DESC.getString()));
		}
		if(ApkInfoHelper.isStartup(apkInfo)) {
			feature.append(", ");
			feature.append(makeHyperEvent("feature-startup", Resource.STR_FEATURE_STARTUP_LAB.getString(), Resource.STR_FEATURE_STARTUP_DESC.getString()));
		}
		String sharedUserId = apkInfo.manifest.sharedUserId;
		if(sharedUserId != null && !sharedUserId.startsWith("android.uid.system") ) {
			feature.append(", ");
			feature.append(makeHyperEvent("feature-shared-user-id", Resource.STR_FEATURE_SHAREDUSERID_LAB.getString(), Resource.STR_FEATURE_SHAREDUSERID_DESC.getString(), sharedUserId));
		}
		String deviceRequirements = makeDeviceRequirements(apkInfo);
		if(deviceRequirements != null && !deviceRequirements.isEmpty()) {
			feature.append(", ");
			feature.append(makeHyperEvent("feature-device-requirements", Resource.STR_FEATURE_DEVICE_REQ_LAB.getString(), Resource.STR_FEATURE_DEVICE_REQ_DESC.getString(), deviceRequirements));
		}

		boolean isSamsungSign = ApkInfoHelper.isSamsungSign(apkInfo);
		boolean isPlatformSign = ApkInfoHelper.isTestPlatformSign(apkInfo);
		String certSummary = makeCertSummary(apkInfo.certificates);

		StringBuilder particularFeatures = new StringBuilder();
		if(sharedUserId != null && sharedUserId.startsWith("android.uid.system")) {
			particularFeatures.append(", ");
			if(!isSamsungSign && !isPlatformSign) particularFeatures.append("<span id=\"system-uid\">");
			particularFeatures.append(makeHyperEvent("feature-system-user-id", Resource.STR_FEATURE_SYSTEM_UID_LAB.getString(), Resource.STR_FEATURE_SYSTEM_UID_DESC.getString()));
			if(!isSamsungSign && !isPlatformSign) particularFeatures.append("</span>");
		}
		if(isPlatformSign) {
			particularFeatures.append(", ");
			particularFeatures.append(makeHyperEvent("feature-platform-sign", Resource.STR_FEATURE_PLATFORM_SIGN_LAB.getString(), Resource.STR_FEATURE_PLATFORM_SIGN_DESC.getString(), certSummary));
		}
		if(isSamsungSign) {
			particularFeatures.append(", ");
			particularFeatures.append(makeHyperEvent("feature-samsung-sign", Resource.STR_FEATURE_SAMSUNG_SIGN_LAB.getString(), Resource.STR_FEATURE_SAMSUNG_SIGN_DESC.getString(), certSummary));
		}
		if(ApkInfoHelper.isDebuggable(apkInfo)) {
			particularFeatures.append(", ");
			particularFeatures.append(makeHyperEvent("feature-debuggable", Resource.STR_FEATURE_DEBUGGABLE_LAB.getString(), Resource.STR_FEATURE_DEBUGGABLE_DESC.getString()));
		}
		if(ApkInfoHelper.isInstrumentation(apkInfo)) {
			particularFeatures.append(", ");
			particularFeatures.append(makeHyperEvent("feature-instrumentation", Resource.STR_FEATURE_INSTRUMENTATION_LAB.getString(), Resource.STR_FEATURE_INSTRUMENTATION_DESC.getString()));
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
		String certSummary = makeCertSummary(apkInfo.certificates);

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
			particularFeatures.append(makeHyperEvent("feature-system-user-id", Resource.STR_FEATURE_SYSTEM_UID_LAB.getString(), Resource.STR_FEATURE_SYSTEM_UID_DESC.getString()));
		}
		if(isPlatformSign && apkInfoPanel.getElementById("feature-platform-sign") == null) {
			particularFeatures.append(", ");
			particularFeatures.append(makeHyperEvent("feature-platform-sign", Resource.STR_FEATURE_PLATFORM_SIGN_LAB.getString(), Resource.STR_FEATURE_PLATFORM_SIGN_DESC.getString(), certSummary));
		}
		if(isSamsungSign && apkInfoPanel.getElementById("feature-samsung-sign") == null) {
			particularFeatures.append(", ");
			particularFeatures.append(makeHyperEvent("feature-samsung-sign", Resource.STR_FEATURE_SAMSUNG_SIGN_LAB.getString(), Resource.STR_FEATURE_SAMSUNG_SIGN_DESC.getString(), certSummary));
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

	private void setPermissionList(ApkInfo apkInfo) {
		if(apkInfo != null) {
			boolean isPlatformSign = ApkInfoHelper.isTestPlatformSign(apkInfo)
					|| ApkInfoHelper.isSamsungSign(apkInfo);
			permissionManager.clearPermissions();
			permissionManager.setPlatformSigned(isPlatformSign);
			permissionManager.setTreatSignAsRevoked((boolean) Resource.PROP_PERM_TREAT_SIGN_AS_REVOKED.getData());
			permissionManager.addUsesPermission(apkInfo.manifest.usesPermission);
			permissionManager.addUsesPermission(apkInfo.manifest.usesPermissionSdk23);
			permissionManager.addDeclarePemission(apkInfo.manifest.permission);
			if(!permissionManager.isEmpty()) {
				int selectSdkVer = makeSdkOptions(apkInfo.manifest.usesSdk.targetSdkVersion);
				permissionManager.setSdkVersion(selectSdkVer);
			}
		}

		int groupCount = 0;
		if(permissionManager.isEmpty()) {
			apkInfoPanel.setInnerHTMLById("perm-group-title", Resource.STR_LABEL_NO_PERMISSION.getString());
		} else {
			StringBuilder titlebar = new StringBuilder();
			titlebar.append("[").append(Resource.STR_BASIC_PERMISSIONS.getString()).append("] - ");
			titlebar.append(makeHyperEvent("display-list", String.format("<u>%s</u>", Resource.STR_BASIC_PERMLAB_DISPLAY.getString()), Resource.STR_BASIC_PERMDESC_DISPLAY.getString()));
			if(apkInfoPanel.getElementById("perm-settings") == null) {
				titlebar.append(makeHyperEvent("show-perm-setting", String.format("<img src=\"%s\">", Resource.IMG_PERM_MARKER_SETTING.getPath()), null));
			} else {
				titlebar.append(makeHyperEvent("close-perm-setting", String.format("<img src=\"%s\">", Resource.IMG_PERM_MARKER_CLOSE.getPath()), null));
			}
			apkInfoPanel.removeElementById("show-perm-setting");
			apkInfoPanel.removeElementById("close-perm-setting");
			apkInfoPanel.setInnerHTMLById("perm-group-title", titlebar.toString());
			apkInfoPanel.setOuterHTMLById("perm-groups", String.format("<div id=\"perm-groups\">%s</div>", makePermGroup()));
			groupCount = permissionManager.getPermissionGroups().length;
		}
		setInfoAreaHeight(groupCount);
	}

	private void setInfoAreaHeight(int groupCount) {
		int infoHeight = groupCount > 15 ? 220 : (groupCount > 0 ? 260 : 280);
		if(apkInfoPanel.getElementById("perm-settings") != null) infoHeight -= 20;
		apkInfoPanel.setOuterHTMLById("basic-info-height-td", String.format("<td id=\"basic-info-height-td\" height=\"%d\"></td>", infoHeight));
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
					String tag = makeHyperEvent("PLUGIN:"+searcher.hashCode(), String.format("<img src=\"%s\" width=\"16\" height=\"16\">", iconPath), null, searcher.getActionCommand());
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

	private String makeCertSummary(String[] certificates) {
		if(certificates == null || certificates.length == 0) return null;
		StringBuilder summary = new StringBuilder();
		for(String sign: certificates) {
			String[] line = sign.split("\n");
			if(line.length >= 3) {
				summary.append(line[0]).append("\n")
					.append(line[1]).append("\n")
					.append(line[2]).append("\n\n");
			} else {
				summary.append("error\n");
			}
		}
		return summary.toString();
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

	private String makePermGroup() {
		StringBuilder permGroup = new StringBuilder("");
		int cnt = 0;
		int sdk = permissionManager.getSdkVersion();
		boolean isDanger;
		for(PermissionGroupInfoExt g: permissionManager.getPermissionGroups()) {
			isDanger = sdk >= 23 && g.hasDangerous();
			if(cnt % 15 != 0) permGroup.append("&nbsp;");
			permGroup.append(makeHyperEvent("PermGroup:"+g.hashCode(), makeGroupImage(g.getIconPath(), g.permissions.size(), isDanger), g.getSummary(), g.name));
			if(++cnt % 15 == 0) permGroup.append("<font style=\"font-size:1px\"><br><br></font>");
		}
		return permGroup.toString();
	}

	private String makeGroupImage(String src, int count, boolean isDanger) {
		BufferedImage image = null;
		try {
			image = ImageIO.read(new URL(src));
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(image == null) {
			return String.format("<img src=\"%s\">", src);
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

		if(isDanger && (Boolean) Resource.PROP_PERM_MARK_RUNTIME.getData()) {
			g2.setColor(Color.WHITE);
			if(isDanger) g2.fillOval(0, 0, 12, 12);
			g2.setColor(Color.RED);
			g2.drawString("R", 3, 10);
		}

		if((Boolean) Resource.PROP_PERM_MARK_COUNT.getData()) {
			g2.setColor(Color.WHITE);
			g2.fillOval(24, 24, 12, 12);
			g2.setColor(Color.BLACK);
			if(count < 10) {
				g2.drawString(Integer.toString(count), 28, 34);
			} else {
				if(font != null) g2.setFont(font.deriveFont(9.2f));
				g2.drawString(Integer.toString(count), 26, 34);
			}
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

		return String.format("<img src=\"%s\">", src);
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
				sdkOptions.append(String.format("<option value=%d%s>API Level %d</option>",
						permSdk, permSdk == tarSdk ? " selected" : "", permSdk));
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

	@Override
	public void contentsChanged(ListDataEvent evt) {
		DefaultComboBoxModel<?> model = (DefaultComboBoxModel<?>) evt.getSource();
		Object item = model.getSelectedItem();
		if(item instanceof Option) {
			String sdkVersion = ((Option)item).getValue();
			if(sdkVersion == null || sdkVersion.isEmpty()) sdkVersion = "-1";
			Log.v("change sdkVersion " + sdkVersion);
			permissionManager.setSdkVersion(Integer.parseInt(sdkVersion));
			setPermissionList(null);
		}
	}
	@Override public void intervalAdded(ListDataEvent evt) { }
	@Override public void intervalRemoved(ListDataEvent evt) { }

	@Override
	public void itemStateChanged(ItemEvent evt) {
		Object source = evt.getSource();
		if(source instanceof ToggleButtonModel) {
			ToggleButtonModel checkbox = (ToggleButtonModel) source;
			String elemId = checkbox.getActionCommand();
			boolean selected = evt.getStateChange() == ItemEvent.SELECTED;
			Resource.setPropData(elemId, selected);
			if("treat-sign-as-revoked".equals(elemId)) {
				permissionManager.setTreatSignAsRevoked(selected);
			}
			setPermissionList(null);
		}		
	}

	private String makeHyperEvent(String id, String text, String title) {
		return makeHyperEvent(id, text, title, null);
	}

	private String makeHyperEvent(String id, String text, String title, Object userData) {
		String style = null;
		if(id != null && (id.startsWith("PLUGIN:") || id.contains("-perm-setting"))) style = "color:white";
		return apkInfoPanel.makeHyperLink("@event", text, title, id, style, userData);
	}

	@Override
	public void hyperlinkClick(HyperlinkClickEvent evt) {
		String id = evt.getId();
		switch(id) {
		case "other-lang":
			String mutiLabels = (String) evt.getUserData();
			if(mutiLabels == null || mutiLabels.isEmpty()) return;
			showDialog(mutiLabels, Resource.STR_LABEL_APP_NAME_LIST.getString(), new Dimension(300, 200), null);
			break;
		case "app-version":
			String versionDesc = (String) evt.getUserData();
			showDialog(versionDesc, "App version info", new Dimension(300, 50), null);
			break;
		case "display-list":
			showPermList();
			break;
		case "min-sdk-info": case "target-sdk-info": case "max-sdk-info":
			SdkVersionInfoDlg sdkDlg = new SdkVersionInfoDlg(null, Resource.STR_SDK_INFO_FILE_PATH.getString(), (Integer)evt.getUserData());
			sdkDlg.setLocationRelativeTo(this);
			sdkDlg.setVisible(true);
			break;
		case "function-create-shortcut":
			SystemUtil.createShortCut();
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					if(SystemUtil.hasShortCut()){
						apkInfoPanel.removeElementById("create-shortcut");
					}
				}
			});
			break;
		case "function-assoc-apk":
			SystemUtil.setAssociateFileType(".apk");
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					if(SystemUtil.isAssociatedWithFileType(".apk")) {
						apkInfoPanel.removeElementById("associate-file");
					}
				}
			});
			break;
		case "file-checksum":
			String apkPath = (String) evt.getUserData();
			File apkFile = new File(apkPath);
			StringBuilder checksum = new StringBuilder(apkPath).append("\n")
					.append(FileUtil.getFileSize(apkFile.length(), FSStyle.FULL))
					.append("\n\nMD5: ").append(FileUtil.getMessageDigest(apkFile, "MD5")).append("\n")
					.append("SHA1: ").append(FileUtil.getMessageDigest(apkFile, "SHA-1")).append("\n")
					.append("SHA256: ").append(FileUtil.getMessageDigest(apkFile, "SHA-256"));
			showDialog(checksum.toString(), "APK Checksum", new Dimension(650, 150), null);
			break;
		case "show-perm-setting":
			apkInfoPanel.removeElementById("show-perm-setting");
			apkInfoPanel.insertElementBefore("perm-groups", "<div id=\"perm-settings\"><div>");
			StringBuilder settings = new StringBuilder();
			settings.append(" <input id=\"mark-runtime\" type=\"checkbox\">" + Resource.STR_LABEL_MARK_A_RUNTIME.getString());
			settings.append(" <input id=\"mark-count\" type=\"checkbox\">" + Resource.STR_LABEL_MARK_A_COUNT.getString());
			settings.append(" <input id=\"treat-sign-as-revoked\" type=\"checkbox\">" + Resource.STR_LABEL_TREAT_SIGN_AS_REVOKED.getString());
			apkInfoPanel.setInnerHTMLById("perm-settings", settings.toString());
			apkInfoPanel.insertElementLast("perm-group-title", makeHyperEvent("close-perm-setting", String.format("<img src=\"%s\">", Resource.IMG_PERM_MARKER_CLOSE.getPath()), null));
			for(String elemId: new String[] {"mark-runtime", "mark-count", "treat-sign-as-revoked"}) {
				Object object = apkInfoPanel.getElementModelById(elemId);
				if(object instanceof ToggleButtonModel) {
					ToggleButtonModel checkbox = (ToggleButtonModel) object;
					if("treat-sign-as-revoked".equals(elemId) && permissionManager.isPlatformSigned()) {
						checkbox.setSelected(false);
						checkbox.setEnabled(false);;
					} else {
						checkbox.setSelected((boolean) Resource.getPropData(elemId, true));
					}
					checkbox.setActionCommand(elemId);
					checkbox.addItemListener(this);
				}
			}
			setInfoAreaHeight(permissionManager.getPermissionGroups().length);
			break;
		case "close-perm-setting":
			apkInfoPanel.removeElementById("close-perm-setting");
			apkInfoPanel.removeElementById("perm-settings");
			apkInfoPanel.insertElementLast("perm-group-title", makeHyperEvent("show-perm-setting", String.format("<img src=\"%s\">", Resource.IMG_PERM_MARKER_SETTING.getPath()), null));
			setInfoAreaHeight(permissionManager.getPermissionGroups().length);
			break;
		default:
			if(id.startsWith("feature-")) {
				showFeatureInfo(id, evt.getUserData());
			} else if(id.startsWith("PermGroup:")) {
				showPermDetailDesc(evt);
			} else if(id.startsWith("PLUGIN:")) {
				IPlugIn plugin = PlugInManager.getPlugInByActionCommand((String)evt.getUserData());
				if(plugin != null) {
					plugin.launch();
				}
			} else {
				Log.w("Unknown id " + id);
			}
		}
	}

	private void showFeatureInfo(String id, Object userData) {
		String feature = null;
		Dimension size = new Dimension(400, 100);

		switch(id) {
		case "feature-hidden":
			feature = Resource.STR_FEATURE_HIDDEN_DESC.getString();
			break;
		case "feature-launcher":
			feature = Resource.STR_FEATURE_LAUNCHER_DESC.getString();
			break;
		case "feature-startup":
			feature = Resource.STR_FEATURE_STARTUP_DESC.getString();
			feature += "\nandroid.permission.RECEIVE_BOOT_COMPLETED";
			break;
		case "feature-shared-user-id":
			feature = "sharedUserId=" + userData + "\n※ ";
			feature += Resource.STR_FEATURE_SHAREDUSERID_DESC.getString();
			break;
		case "feature-system-user-id":
			feature = "sharedUserId=android.uid.system\n※ ";
			feature += Resource.STR_FEATURE_SYSTEM_UID_DESC.getString();
			break;
		case "feature-platform-sign":
			feature = "※ " + Resource.STR_FEATURE_PLATFORM_SIGN_DESC.getString();
			feature += "\n\n" + userData;
			size = new Dimension(500, 150);
			break;
		case "feature-samsung-sign":
			feature = "※ " + Resource.STR_FEATURE_SAMSUNG_SIGN_DESC.getString();
			feature += "\n\n" + userData;
			size = new Dimension(500, 150);
			break;
		case "feature-debuggable":
			feature = Resource.STR_FEATURE_DEBUGGABLE_DESC.getString();
			break;
		case "feature-instrumentation":
			feature = Resource.STR_FEATURE_INSTRUMENTATION_DESC.getString();
			break;
		case "feature-device-requirements":
			feature = (String) userData;
			size = new Dimension(500, 250);
			break;
		case "feature-install-location-internal":
			feature = Resource.STR_FEATURE_ILOCATION_INTERNAL_DESC.getString();
			break;
		case "feature-install-location-auto":
			feature = Resource.STR_FEATURE_ILOCATION_AUTO_DESC.getString();
			break;
		case "feature-install-location-external":
			feature = Resource.STR_FEATURE_ILOCATION_EXTERNAL_DESC.getString();
			break;
		}
		showDialog(feature, "Feature info", size, null);
	}

	private void showPermList() {
		StringBuilder permissionList = new StringBuilder();

		PermissionInfo[] permissions = permissionManager.getPermissions(UsesPermissionTag.UsesPermission);
		if(permissions != null && permissions.length > 0) {
			permissionList.append("<uses-permission> [").append(permissions.length).append("]\n");
			for(PermissionInfo info: permissions) {
				permissionList.append(info.name).append(" - ").append(info.protectionLevel).append("\n");
			}
		}

		permissions = permissionManager.getPermissions(UsesPermissionTag.UsesPermissionSdk23);
		if(permissions != null && permissions.length > 0) {
			if(permissionList.length() > 0) permissionList.append("\n");
			permissionList.append("<uses-permission-sdk-23> [").append(permissions.length).append("]\n");
			for(PermissionInfo info: permissions) {
				permissionList.append(info.name).append(" - ").append(info.protectionLevel);
				permissionList.append("\n");
			}
		}

		permissions = permissionManager.getDeclarePermissions();
		if(permissions != null && permissions.length > 0) {
			if(permissionList.length() > 0) {
				permissionList.append("\n");
			}
			permissionList.append("<permission> [").append(permissions.length).append("]\n");
			for(PermissionInfo info: permissions) {
				permissionList.append(info.name).append(" - ").append(info.protectionLevel).append("\n");
			}
		}

		permissions = permissionManager.getRevokedPermissions();
		if(permissions != null && permissions.length > 0) {
			if(permissionList.length() > 0) {
				permissionList.append("\n");
			}
			permissionList.append("<revoked> [").append(permissions.length).append("]\n");
			for(RevokedPermissionInfo info: (RevokedPermissionInfo[])permissions) {
				permissionList.append(info.name).append(" - ").append(info.getReasonText()).append("\n");
			}
		}

		showDialog(permissionList.toString(), Resource.STR_BASIC_PERM_LIST_TITLE.getString(), new Dimension(500, 200), null);
	}

	private void showPermDetailDesc(HyperlinkClickEvent evt) {
		String group = (String) evt.getUserData();
		PermissionGroupInfoExt g = permissionManager.getPermissionGroup(group);
		Log.e("g " + g + ", " + group);
		if(g == null) return;

		StringBuilder body = new StringBuilder();
		body.append("■ ");
		if(g.label != null) {
			body.append(g.getLabel()).append(" - ");
		}
		body.append("[").append(group).append("]\n");
		if(g.description != null) {
			body.append(" : ").append(g.getDescription()).append("\n");
		}
		body.append("------------------------------------------------------------------------------------------------------------\n\n");

		for(PermissionInfo info: g.permissions) {
			body.append("▶ ");
			if(info.isDangerousLevel()) {
				body.append("[DANGEROUS] ");
			}
			if(info.labels != null) {
				body.append(info.getLabel()).append(" ");
			}
			body.append("[").append(info.name).append("]\n");
			String protection = info.protectionLevel;
			if(protection == null) protection= "normal";
			if(info instanceof RevokedPermissionInfo) {
				RevokedPermissionInfo revokeInfo = (RevokedPermissionInfo) info;
				body.append(" - reason : ").append(revokeInfo.getReasonText()).append("\n");
				if(RevokedReason.NEED_PLATFORM_SIGNATURE.equals(revokeInfo.reason)) {
					body.append(" - protectionLevel=").append(info.protectionLevel).append("\n");
				}
			} else {
				body.append(" - protectionLevel=").append(info.protectionLevel).append("\n");
			}
			if(info.descriptions != null) {
				body.append(" : ").append(info.getDescription()).append("\n\n");
			}
		}

		showDialog(body.toString(), Resource.STR_BASIC_PERM_DISPLAY_TITLE.getString(), new Dimension(600, 200), new ImageIcon(g.icon.replaceAll("^file:/", "")));
	}

	private void showDialog(String content, String title, Dimension size, Icon icon) {
		MessageBoxPane.showTextAreaDialog(null, content, title, MessageBoxPane.INFORMATION_MESSAGE, icon, size);
	}
}
