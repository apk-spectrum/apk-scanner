package com.apkscanner.gui.tabpanels;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.text.html.Option;

import com.apkscanner.core.permissionmanager.PermissionGroupInfoExt;
import com.apkscanner.core.permissionmanager.PermissionManager;
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

	private JHtmlEditorPane apkInfoPanel = null;
	private JPanel lodingPanel = null;
	private CardLayout cardLayout = new CardLayout();

	private String mutiLabels;

	private boolean wasSetData = false;

	private String[] labels = new String[] {""};
	private String appName = "";
	private String packageName = "";
	private String versionName = "";
	private String versionCode = "";
	private String iconPath = "";

	private Integer minSdkVersion = null;
	private Integer targerSdkVersion = null;
	private Integer maxSdkVersion = null;

	private String installLocation = null;

	private boolean isHidden = false;
	private boolean isStartup = false;
	private boolean debuggable = false;
	private boolean isInstrumentation = false;
	private String sharedUserId = "";

	private Long ApkSize = 0L;
	private String apkPath = "";
	private String checkSumMd5 = "";

	private boolean isSamsungSign = false;
	private boolean isPlatformSign = false;
	private String CertSummary = "";

	private String allPermissionsList = "";
	private String signaturePermissions = "";
	private String notGrantPermmissions = "";
	private String deprecatedPermissions = "";

	private PermissionManager permissionManager;

	private boolean hasSignatureLevel = false;
	private boolean hasSystemLevel = false;
	private boolean hasSignatureOrSystemLevel = false;

	private String deviceRequirements = "";

	private JLabel TimerLabel = null;

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
		apkInfoPanel.addStyleRule(makeStyleRule());

		// loding panel
		JLabel logo = new JLabel(Resource.IMG_APK_LOGO.getImageIcon(400, 250));
		logo.setOpaque(true);
		logo.setBackground(Color.white);

		JLabel gif = new JLabel(Resource.IMG_WAIT_BAR.getImageIcon());
		gif.setOpaque(true);
		gif.setBackground(Color.white);
		gif.setPreferredSize(new Dimension(Resource.IMG_WAIT_BAR.getImageIcon().getIconWidth(),Resource.IMG_WAIT_BAR.getImageIcon().getIconHeight()));

		TimerLabel = new JLabel("");
		TimerLabel.setOpaque(true);
		TimerLabel.setBackground(Color.WHITE);
		TimerLabel.setBorder(new EmptyBorder(0,0,50,0));
		TimerLabel.setHorizontalAlignment(JLabel.CENTER);

		lodingPanel = new JPanel();
		lodingPanel.setLayout(new BorderLayout());
		lodingPanel.setOpaque(false);
		lodingPanel.setBackground(Color.white);
		lodingPanel.add(logo,BorderLayout.NORTH);
		lodingPanel.add(gif,BorderLayout.CENTER);
		lodingPanel.add(TimerLabel,BorderLayout.SOUTH);

		this.setLayout(cardLayout);
		this.add(CARD_LODING_PAGE, lodingPanel);
		this.add(CARD_APK_INFORMATION, apkInfoPanel);

		cardLayout.show(this, CARD_APK_INFORMATION);
	}

	private String makeStyleRule()
	{
		//Font font = new Font("helvitica", Font.BOLD, 15);
		JLabel label = new JLabel();
		Font font = label.getFont();

		// create some css from the label's font
		StringBuilder style = new StringBuilder("#basic-info, #perm-group {");
		style.append("font-family:" + font.getFamily() + ";");
		style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
		style.append("font-size:" + font.getSize() + "pt;}");
		style.append("#basic-info a {text-decoration:none; color:black;}");
		style.append("#perm-group a {text-decoration:none; color:#"+Integer.toHexString(label.getBackground().getRGB() & 0xFFFFFF)+";}");
		style.append("#about {");
		style.append("font-family:" + font.getFamily() + ";");
		style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
		style.append("font-size:" + font.getSize() + "pt;}");
		style.append("#about a {text-decoration:none;}");
		style.append("#create-shortcut, #associate-file { background-color: #e7e7e7; border: none; color: white; margin:1px; padding: 5px; text-align: center; text-decoration: none; display: inline-block;");
		style.append("font-family:" + font.getFamily() + ";");
		style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
		style.append("font-size:" + font.getSize() + "pt;}");
		style.append("#create-shortcut a, #associate-file a {text-decoration:none; color:black;}");
		style.append("H1 {margin-top: 0px; margin-bottom: 0px;}");
		style.append("H3 {margin-top: 5px; margin-bottom: 0px;}");

		return style.toString();
	}

	private void showAbout()
	{
		apkInfoPanel.setBody(Resource.RAW_ABUOT_HTML.getString());
		apkInfoPanel.insertElementFirst("apkscanner-icon-td", "<image src=\"" + Resource.IMG_APP_ICON.getPath() + "\" width=\"150\" height=\"150\">");
		apkInfoPanel.setInnerHTMLById("apkscanner-title", Resource.STR_APP_NAME.getString() + " " + Resource.STR_APP_VERSION.getString());
		apkInfoPanel.setOuterHTMLById("programmer-email", "<a href=\"mailto:" + Resource.STR_APP_MAKER_EMAIL.getString() + "\" title=\"" + Resource.STR_APP_MAKER_EMAIL.getString() + "\">" + Resource.STR_APP_MAKER.getString() + "</a>");
		if(!SystemUtil.hasShortCut()){
			apkInfoPanel.insertElementLast("apkscanner-icon-td", "<div id=\"create-shortcut\">" + makeHyperLink("@event", Resource.STR_BTN_CREATE_SHORTCUT.getString(), null, "function-create-shortcut", null) + "</div>");
		}
		if(!SystemUtil.isAssociatedWithFileType(".apk")) {
			apkInfoPanel.insertElementLast("apkscanner-icon-td", "<div id=\"associate-file\">" + makeHyperLink("@event", Resource.STR_BTN_ASSOC_FTYPE.getString(), null, "function-assoc-apk", null) + "</div>");
		}
	}

	private void removeData()
	{
		labels = new String[] {""};
		appName = "";
		packageName = "";
		versionName = "";
		versionCode = "";
		iconPath = "";

		minSdkVersion = null;
		targerSdkVersion = null;
		maxSdkVersion = null;

		installLocation = null;

		isHidden = false;

		isStartup = false;
		debuggable = false;
		isInstrumentation = false;
		sharedUserId = "";
		ApkSize = 0L;
		checkSumMd5 = "";

		isSamsungSign = false;
		isPlatformSign = false;

		allPermissionsList = "";
		signaturePermissions = "";
		notGrantPermmissions = "";
		deprecatedPermissions = "";

		deviceRequirements = "";

		wasSetData = false;
	}

	private void setData()
	{
		if(!wasSetData) return;

		cardLayout.show(this, CARD_APK_INFORMATION);

		String sdkVersion = "";
		if(minSdkVersion != null) {
			sdkVersion += makeHyperLink("@event", minSdkVersion +" (Min)", "Min SDK version", "min-sdk", null);
		}
		if(targerSdkVersion != null) {
			if(!sdkVersion.isEmpty()) {
				sdkVersion += ", ";
			}
			sdkVersion += makeHyperLink("@event", targerSdkVersion + " (Target)", "Targer SDK version", "target-sdk", null);
		}
		if(maxSdkVersion != null) {
			if(!sdkVersion.isEmpty()) {
				sdkVersion += ", ";
			}
			sdkVersion += makeHyperLink("@event", maxSdkVersion + " (Max)", "Max SDK version", "max-sdk", null);
		}
		if(sdkVersion.isEmpty()) {
			sdkVersion += "Unspecified";
		}

		StringBuilder feature = new StringBuilder();

		if("internalOnly".equals(installLocation)) {
			feature.append(makeHyperLink("@event", Resource.STR_FEATURE_ILOCATION_INTERNAL_LAB.getString(), Resource.STR_FEATURE_ILOCATION_INTERNAL_DESC.getString(), "feature-install-location-internal", null));
		} else if("auto".equals(installLocation)) {
			feature.append(makeHyperLink("@event", Resource.STR_FEATURE_ILOCATION_AUTO_LAB.getString(), Resource.STR_FEATURE_ILOCATION_AUTO_DESC.getString(), "feature-install-location-auto", null));
		} else if("preferExternal".equals(installLocation)) {
			feature.append(makeHyperLink("@event", Resource.STR_FEATURE_ILOCATION_EXTERNAL_LAB.getString(), Resource.STR_FEATURE_ILOCATION_EXTERNAL_DESC.getString(), "feature-install-location-external", null));
		}
		feature.append("<br/>");

		if(isHidden) {
			feature.append(makeHyperLink("@event", Resource.STR_FEATURE_HIDDEN_LAB.getString(), Resource.STR_FEATURE_HIDDEN_DESC.getString(), "feature-hidden", null));
		} else {
			feature.append(makeHyperLink("@event", Resource.STR_FEATURE_LAUNCHER_LAB.getString(), Resource.STR_FEATURE_LAUNCHER_DESC.getString(), "feature-launcher", null));
		}
		if(isStartup) {
			feature.append(", " + makeHyperLink("@event", Resource.STR_FEATURE_STARTUP_LAB.getString(), Resource.STR_FEATURE_STARTUP_DESC.getString(), "feature-startup", null));
		}
		if(!signaturePermissions.isEmpty()) {
			feature.append(", " + makeHyperLink("@event", Resource.STR_FEATURE_SIGNATURE_LAB.getString(), Resource.STR_FEATURE_SIGNATURE_DESC.getString(), "feature-protection-level", null));
		}
		if(sharedUserId != null && !sharedUserId.startsWith("android.uid.system") ) {
			feature.append(", " + makeHyperLink("@event", Resource.STR_FEATURE_SHAREDUSERID_LAB.getString(), Resource.STR_FEATURE_SHAREDUSERID_DESC.getString(), "feature-shared-user-id", null));
		}
		if(deviceRequirements != null && !deviceRequirements.isEmpty()) {
			feature.append(", " + makeHyperLink("@event", Resource.STR_FEATURE_DEVICE_REQ_LAB.getString(), Resource.STR_FEATURE_DEVICE_REQ_DESC.getString(), "feature-device-requirements", null));
		}

		boolean systemSignature = false;
		StringBuilder importantFeatures = new StringBuilder();
		if(sharedUserId != null && sharedUserId.startsWith("android.uid.system")) {
			if(isSamsungSign || isPlatformSign) {
				importantFeatures.append(", <font style=\"color:#ED7E31; font-weight:bold\">");
			} else {
				importantFeatures.append(", <font style=\"color:#FF0000; font-weight:bold\">");
			}
			importantFeatures.append(makeHyperLink("@event", Resource.STR_FEATURE_SYSTEM_UID_LAB.getString(), Resource.STR_FEATURE_SYSTEM_UID_DESC.getString(), "feature-system-user-id", null));
			importantFeatures.append("</font>");
		}
		if(isPlatformSign) {
			importantFeatures.append(", <font style=\"color:#ED7E31; font-weight:bold\">");
			importantFeatures.append(makeHyperLink("@event", Resource.STR_FEATURE_PLATFORM_SIGN_LAB.getString(), Resource.STR_FEATURE_PLATFORM_SIGN_DESC.getString(), "feature-platform-sign", null));
			importantFeatures.append("</font>");
			systemSignature = true;
		}
		if(isSamsungSign) {
			importantFeatures.append(", <font style=\"color:#ED7E31; font-weight:bold\">");
			importantFeatures.append(makeHyperLink("@event", Resource.STR_FEATURE_SAMSUNG_SIGN_LAB.getString(), Resource.STR_FEATURE_SAMSUNG_SIGN_DESC.getString(), "feature-samsung-sign", null));
			importantFeatures.append("</font>");
			systemSignature = true;
		}
		if(((hasSignatureLevel || hasSignatureOrSystemLevel) && !systemSignature) || hasSystemLevel) {
			importantFeatures.append(", <font style=\"color:#ED7E31; font-weight:bold\">");
			importantFeatures.append(makeHyperLink("@event", Resource.STR_FEATURE_REVOKE_PERM_LAB.getString(), Resource.STR_FEATURE_REVOKE_PERM_DESC.getString(), "feature-revoke-permissions", null));
			importantFeatures.append("</font>");
		}
		if(deprecatedPermissions != null && !deprecatedPermissions.isEmpty()) {
			importantFeatures.append(", <font style=\"color:#ED7E31; font-weight:bold\">");
			importantFeatures.append(makeHyperLink("@event", Resource.STR_FEATURE_DEPRECATED_PREM_LAB.getString(), Resource.STR_FEATURE_DEPRECATED_PREM_DESC.getString(), "feature-deprecated-perm", null));
			importantFeatures.append("</font>");
		}
		if(debuggable) {
			importantFeatures.append(", <font style=\"color:#ED7E31; font-weight:bold\">");
			importantFeatures.append(makeHyperLink("@event", Resource.STR_FEATURE_DEBUGGABLE_LAB.getString(), Resource.STR_FEATURE_DEBUGGABLE_DESC.getString(), "feature-debuggable", null));
			importantFeatures.append("</font>");
		}
		if(isInstrumentation) {
			importantFeatures.append(", <font style=\"color:#ED7E31; font-weight:bold\">");
			importantFeatures.append(makeHyperLink("@event", Resource.STR_FEATURE_INSTRUMENTATION_LAB.getString(), Resource.STR_FEATURE_INSTRUMENTATION_DESC.getString(), "feature-instrumentation", null));
			importantFeatures.append("</font>");
		}
		if(importantFeatures.length() > 0) {
			feature.append("<br/>" + importantFeatures.substring(2));
		}

		int groupCount = permissionManager.getPermissionGroups().length;
		int infoHeight = 280;
		if(groupCount > 15) infoHeight = 220;
		else if(groupCount > 0) infoHeight = 260;

		mutiLabels = "";
		for(String s: labels) {
			mutiLabels += s + "\n";
		}

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
					String tag = makeHyperLink("@event", " <image src=\"" + iconPath + "\" width=16 height=16 /> ", null, "PLUGIN:"+searcher.getActionCommand(), "color:white;");
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

		StringBuilder strTabInfo = new StringBuilder("");
		strTabInfo.append("<table>");
		strTabInfo.append("  <tr>");
		strTabInfo.append("    <td width=170 height=" + infoHeight + ">");
		strTabInfo.append("      <image src=\"" + iconPath + "\" width=150 height=150 />");
		strTabInfo.append("    </td>");
		strTabInfo.append("    <td height=" + infoHeight + ">");
		strTabInfo.append("      <div id=\"basic-info\">");
		strTabInfo.append("        <font style=\"font-size:20px; color:#548235; font-weight:bold\">");
		if(labels.length > 1) {
			strTabInfo.append("          " + makeHyperLink("@event", appName, mutiLabels, "other-lang", null));
			strTabInfo.append("        </font>");
		} else {
			strTabInfo.append("          " + appName);
			strTabInfo.append("</font> " + appLabelSearchers + "<br/>");
		}
		if(labels.length > 1) {
			strTabInfo.append("        <font style=\"font-size:10px;\">");
			strTabInfo.append("          " + makeHyperLink("@event", "["+labels.length+"]", mutiLabels, "other-lang", null));
			strTabInfo.append("</font>" + appLabelSearchers + "<br/>");
		}
		strTabInfo.append("        <font style=\"font-size:15px; color:#4472C4\">");
		strTabInfo.append("          [" + packageName +"]" + packageSearchers);
		strTabInfo.append("</font><br/>");
		strTabInfo.append("        <font style=\"font-size:15px; color:#ED7E31\">");
		strTabInfo.append("          " + makeHyperLink("@event", "Ver. " + versionName +" / " + (!versionCode.isEmpty() ? versionCode : "0"), "VersionName : " + versionName + "\n" + "VersionCode : " + (!versionCode.isEmpty() ? versionCode : "Unspecified"), "app-version", null));
		strTabInfo.append("        </font><br/>");
		strTabInfo.append("        <br/>");
		strTabInfo.append("        <font style=\"font-size:12px\">");
		strTabInfo.append("          @SDK Ver. " + sdkVersion + "<br/>");
		strTabInfo.append("          " + makeHyperLink("@event", FileUtil.getFileSize(ApkSize, FSStyle.FULL), "MD5: " + checkSumMd5, "file-checksum", null));
		strTabInfo.append("        </font>");
		strTabInfo.append("        <br/><br/>");
		strTabInfo.append("        <font style=\"font-size:12px\">");
		strTabInfo.append("          [" + Resource.STR_FEATURE_LAB.getString() + "] ");
		strTabInfo.append("          " + feature);
		strTabInfo.append("        </font><br/>");
		strTabInfo.append("      </div>");
		strTabInfo.append("    </td>");
		strTabInfo.append("  </tr>");
		strTabInfo.append("</table>");
		strTabInfo.append("<div id=\"perm-group\" style=\"text-align:left; width:480px; padding-top:5px; border-top:1px; border-left:0px; border-right:0px; border-bottom:0px; border-style:solid;\">");
		strTabInfo.append("<table width=\"100%\" style=\"border:0px;padding:0px;margin:0px;\"><tr style=\\\"border:0px;padding:0px;margin:0px;\\\" ><td style=\\\"border:0px;padding:0px;margin:0px;\\\">");
		strTabInfo.append("  <font style=\"font-size:12px;color:black;\">");
		if(allPermissionsList != null && !allPermissionsList.isEmpty()) {
			strTabInfo.append("    [" + Resource.STR_BASIC_PERMISSIONS.getString() + "] - ");
			strTabInfo.append("    " + makeHyperLink("@event","<u>" + Resource.STR_BASIC_PERMLAB_DISPLAY.getString() + "</u>",Resource.STR_BASIC_PERMDESC_DISPLAY.getString(),"display-list", null));
		} else {
			strTabInfo.append("    " + Resource.STR_LABEL_NO_PERMISSION.getString());
		}
		strTabInfo.append("</font></td><td width=\"150px\" style=\"text-align:right;\"><select id=\"sdk-version\"><option value=\"27\">API Level 27</option><option value=\"28\">API Level 28</option></select></td></tr></table>");
		strTabInfo.append("<div id=\"perm-groups\">" + makePermGroup() + "</div>");
		strTabInfo.append("</div>");
		strTabInfo.append("<div height=10000 width=10000 id='testid'></div>");

		apkInfoPanel.setBody(strTabInfo.toString());

		Object object = apkInfoPanel.getElementModelById("sdk-version");
		if(object instanceof DefaultComboBoxModel) {
			DefaultComboBoxModel<?> model = (DefaultComboBoxModel<?>)object;
			model.removeListDataListener(this);
			model.addListDataListener(this);
		}
	}

	public void onProgress(String message)
	{
		//Log.i("setProgress() percent " + percent);

		if(lodingPanel == null)
			initialize();

		if(wasSetData || !lodingPanel.isVisible()) {
			removeData();
			cardLayout.show(this, CARD_LODING_PAGE);
		}

		if(message != null && !message.isEmpty())
			TimerLabel.setText(message);
		else
			TimerLabel.setText("Standby for extracting.");
		return;
	}

	@Override
	public void setData(ApkInfo apkInfo, Status status, ITabbedRequest request)
	{
		if(apkInfoPanel == null)
			initialize();

		if(apkInfo == null) {
			removeData();
			showAbout();
			sendRequest(request, ITabbedRequest.REQUEST_SELECTED);
			return;
		}

		if(!Status.BASIC_INFO_COMPLETED.equals(status) && !Status.CERT_COMPLETED.equals(status)) {
			if(CertSummary.isEmpty() && apkInfo.certificates != null) {
				Log.i("cert completed. status: " + status);
			} else {
				return;
			}
		}
		wasSetData = true;

		if(apkInfo.manifest.application.labels != null && apkInfo.manifest.application.labels.length > 0) {
			this.appName = ApkInfoHelper.getResourceValue(apkInfo.manifest.application.labels, (String)Resource.PROP_PREFERRED_LANGUAGE.getData(""));

			ArrayList<String> labels = new ArrayList<String>();
			for(ResourceInfo r: apkInfo.manifest.application.labels) {
				if(r.configuration == null || r.configuration.isEmpty() || "default".equals(r.configuration)) {
					if(r.name != null) {
						labels.add(r.name);
					} else {
						labels.add(apkInfo.manifest.packageName);
					}
				} else {
					labels.add("[" + r.configuration + "] " + r.name);
				}
			}

			this.labels = labels.toArray(new String[0]);
		} else {
			this.labels = new String[] { apkInfo.manifest.packageName }; // apkInfo.Labelname;
		}

		if(this.appName == null || this.appName.isEmpty()) {
			this.appName = this.labels.length > 0 ? this.labels[0] : apkInfo.manifest.packageName;
		}

		if(apkInfo.manifest.packageName != null) packageName = apkInfo.manifest.packageName;
		if(apkInfo.manifest.versionName != null) versionName = apkInfo.manifest.versionName;
		if(apkInfo.manifest.versionCode != null) versionCode = apkInfo.manifest.versionCode.toString();
		iconPath = null;
		if(apkInfo.manifest.application.icons != null && apkInfo.manifest.application.icons.length > 0) {
			ResourceInfo[] iconList = apkInfo.manifest.application.icons;
			for(int i=iconList.length-1; i >= 0; i--) {
				if(iconList[i].name.endsWith(".xml")) continue;
				iconPath = iconList[i].name;
				if(iconPath.toLowerCase().endsWith(".webp")) {
					iconPath = covertWebp2Png(iconPath, apkInfo.tempWorkPath);
				}
				if(iconPath != null) break;
			}
		}
		minSdkVersion = apkInfo.manifest.usesSdk.minSdkVersion;
		targerSdkVersion = apkInfo.manifest.usesSdk.targetSdkVersion;
		maxSdkVersion = apkInfo.manifest.usesSdk.maxSdkVersion;
		sharedUserId = apkInfo.manifest.sharedUserId;
		installLocation = apkInfo.manifest.installLocation;

		isHidden = ApkInfoHelper.isHidden(apkInfo);
		isStartup = ApkInfoHelper.isStartup(apkInfo);
		isInstrumentation = ApkInfoHelper.isInstrumentation(apkInfo);
		debuggable = ApkInfoHelper.isDebuggable(apkInfo);

		isSamsungSign = (apkInfo.featureFlags & ApkInfo.APP_FEATURE_SAMSUNG_SIGN) != 0 ? true : false;
		isPlatformSign = (apkInfo.featureFlags & ApkInfo.APP_FEATURE_PLATFORM_SIGN) != 0 ? true : false;

		CertSummary = ""; // apkInfo.CertSummary;
		if(apkInfo.certificates != null) {
			for(String sign: apkInfo.certificates) {
				String[] line = sign.split("\n");
				if(line.length >= 3) {
					CertSummary += line[0] + "\n" + line[1] + "\n" + line[2] + "\n\n";
				} else {
					CertSummary += "error\n";
				}
			}
		}

		ApkSize = apkInfo.fileSize;
		apkPath = apkInfo.filePath;
		checkSumMd5 = FileUtil.getMessageDigest(new File(apkPath), "MD5");

		hasSignatureLevel = false; // apkInfo.hasSignatureLevel;
		hasSignatureOrSystemLevel = false; // apkInfo.hasSignatureOrSystemLevel;
		hasSystemLevel = false; // apkInfo.hasSystemLevel;
		notGrantPermmissions = "";

		StringBuilder permissionList = new StringBuilder();

		if(apkInfo.manifest.usesPermission != null && apkInfo.manifest.usesPermission.length > 0) {
			permissionManager = new PermissionManager();
			permissionManager.setSdkVersion(27);
			permissionManager.addUsesPermission(apkInfo.manifest.usesPermission);

			permissionList.append("<uses-permission> [" +  apkInfo.manifest.usesPermission.length + "]\n");
			for(PermissionInfo info: permissionManager.getPermissions()) {
				permissionList.append(info.name + " - " + info.protectionLevel);
				if(info.isSignatureLevel()) hasSignatureLevel = true;
				if(info.isSignatureOrSystemLevel()) hasSignatureOrSystemLevel = true;
				if(info.isSystemLevel()) hasSystemLevel = true;
				if(((info.isSignatureLevel() || info.isSignatureOrSystemLevel()) && !(isSamsungSign || isPlatformSign)) || info.isSystemLevel()) {
					notGrantPermmissions += info.name + " - " + info.protectionLevel + "\n";
				}
				//if(info.maxSdkVersion != null) {
					//permissionList.append(", maxSdkVersion : " + info.maxSdkVersion);
				//}
				if(info.isDeprecated()) {
					deprecatedPermissions += info.getDeprecatedMessage() + "\n\n";
				}
				permissionList.append("\n");
			}
		}
		if(apkInfo.manifest.usesPermissionSdk23 != null && apkInfo.manifest.usesPermissionSdk23.length > 0) {
			permissionManager.addUsesPermission(apkInfo.manifest.usesPermissionSdk23);
			if(permissionList.length() > 0) {
				permissionList.append("\n");
			}
			permissionList.append("<uses-permission-sdk-23> [" +  apkInfo.manifest.usesPermissionSdk23.length + "]\n");
			for(PermissionInfo info: permissionManager.getPermissions()) {
				permissionList.append(info.name + " - " + info.protectionLevel);
				if(info.isSignatureLevel()) hasSignatureLevel = true;
				if(info.isSignatureOrSystemLevel()) hasSignatureOrSystemLevel = true;
				if(info.isSystemLevel()) hasSystemLevel = true;
				if(((info.isSignatureLevel() || info.isSignatureOrSystemLevel()) && !(isSamsungSign || isPlatformSign)) || info.isSystemLevel()) {
					notGrantPermmissions += info.name + " - " + info.protectionLevel + "\n";
				}
				//if(info.maxSdkVersion != null) {
					//permissionList.append(", maxSdkVersion : " + info.maxSdkVersion);
				//}
				if(info.isDeprecated()) {
					deprecatedPermissions += info.getDeprecatedMessage() + "\n\n";
				}
				permissionList.append("\n");
			}
		}

		signaturePermissions = "";
		if(apkInfo.manifest.permission != null && apkInfo.manifest.permission.length > 0) {
			if(permissionList.length() > 0) {
				permissionList.append("\n");
			}
			permissionList.append("<permission> [" +  apkInfo.manifest.permission.length + "]\n");
			for(PermissionInfo info: apkInfo.manifest.permission) {
				permissionList.append(info.name + " - " + info.protectionLevel + "\n");
				if(!"normal".equals(info.protectionLevel)) {
					signaturePermissions += info.name + " - " + info.protectionLevel + "\n";
				}
			}
		}
		allPermissionsList = permissionList.toString();

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

		deviceRequirements = deviceReqData.toString();

		setData();

		sendRequest(request, ITabbedRequest.REQUEST_SELECTED);
	}

	private String covertWebp2Png(final String imagePath, final String tempPath) {
		String[] path = imagePath.split("!");
		String convetPath = imagePath;
		try {
			String apkPath = path[0].replaceAll("^(jar:)?file:", "");
			ZipFile zipFile = new ZipFile(apkPath);
			ZipEntry entry = zipFile.getEntry(path[1].replaceAll("^/", ""));
			if(entry != null) {
				  //String tempPath = FileUtil.makeTempPath(apkPath.substring(apkPath.lastIndexOf(File.separator)));
				  FileUtil.makeFolder(tempPath);
				  String tempImg = tempPath + File.separator + path[1].replaceAll(".*/", "") + ".png";
	              File out = new File(tempImg);
	              InputStream is = zipFile.getInputStream(entry);
	              BufferedImage image = ImageIO.read(is);
	              ImageIO.write(image, "png", out);
	              if(out.exists()) {
	            	  convetPath = "file:"+out.getAbsolutePath();
	              }
			}
			zipFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return convetPath;
	}

	private String makePermGroup()
	{
		StringBuilder permGroup = new StringBuilder("");
		int cnt = 0;
		for(PermissionGroupInfoExt g: permissionManager.getPermissionGroups()) {
			permGroup.append(makeHyperLink("@event", makeImage(g.getIconPath()), g.getSummary(), g.name, g.hasDangerous?"color:red;":null));
			if(++cnt % 15 == 0) permGroup.append("<br/>");
		}
		return permGroup.toString();
	}

	private String makeHyperLink(String href, String text, String title, String id, String style)
	{
		return JHtmlEditorPane.makeHyperLink(href, text, title, id, style);
	}

	private String makeImage(String src)
	{
		return "<image src=\"" + src + "\"/>";
	}

	@Override
	public void hyperlinkClick(String id)
	{
		//Log.i("hyperlinkClick() " + id);
		if("other-lang".equals(id)) {
			if(mutiLabels == null || mutiLabels.isEmpty()
					|| labels.length == 1) return;
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
			String ver = "versionName : " + versionName + "\n" + "versionCode : " + (!versionCode.isEmpty() ? versionCode : "Unspecified");
			showDialog(ver, "App version info", new Dimension(300, 50), null);
		} else if("display-list".equals(id)) {
			showPermList();
		} else if(id.endsWith("-sdk")){
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
			String checksum = apkPath + "\n" + FileUtil.getFileSize(ApkSize, FSStyle.FULL) + "\n\n";
			checksum += "MD5: " + checkSumMd5 + "\n";
			checksum += "SHA1: " + FileUtil.getMessageDigest(new File(apkPath), "SHA-1") + "\n";
			checksum += "SHA256: " + FileUtil.getMessageDigest(new File(apkPath), "SHA-256");
			showDialog(checksum, "APK Checksum", new Dimension(650, 150), null);
		} else if(id != null && id.startsWith("PLUGIN:")) {
			IPlugIn plugin = PlugInManager.getPlugInByActionCommand(id.replaceAll("PLUGIN:", ""));
			if(plugin != null) {
				plugin.launch();
			}
		} else {
			showPermDetailDesc(id);
		}
	}

	@Override public void intervalAdded(ListDataEvent arg0) { }
	@Override public void intervalRemoved(ListDataEvent arg0) { }
	@Override
	public void contentsChanged(ListDataEvent arg0) {
		DefaultComboBoxModel<?> model = (DefaultComboBoxModel<?>) arg0.getSource();
		Object item = model.getSelectedItem();
		if(item instanceof Option) {
			String sdkVersion = ((Option)item).getValue();
			if(sdkVersion == null || sdkVersion.isEmpty()) sdkVersion = "-1";
			Log.e("sdkVersion " + sdkVersion);
			permissionManager.setSdkVersion(Integer.parseInt(sdkVersion));
		}
		apkInfoPanel.setOuterHTMLById("perm-groups", "<div id=\"perm-groups\">" + makePermGroup() + "</div>");
	}

	@Override
	public void reloadResource() {
		setName(Resource.STR_TAB_BASIC_INFO.getString());
		setToolTipText(Resource.STR_TAB_BASIC_INFO.getString());
		setData();
	}

	private void showDialog(String content, String title, Dimension size, Icon icon)
	{
		MessageBoxPane.showTextAreaDialog(null, content, title, MessageBoxPane.INFORMATION_MESSAGE, icon, size);
	}

	public void showPermList()
	{
		/*
		JLabel label = new JLabel();
		Font font = label.getFont();

		StringBuilder body = new StringBuilder("");
		body.append("<div id=\"perm-list\">");
		body.append(Permissions.replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
		body.append("</div>");

		// create some css from the label's font
		StringBuilder style = new StringBuilder("#perm-list {");
		style.append("font-family:" + font.getFamily() + ";");
		style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
		style.append("font-size:" + font.getSize() + "pt;}");
		style.append("#about a {text-decoration:none;}");

		// html content
		JHtmlEditorPane descPane = new JHtmlEditorPane("", "", body.toString().replaceAll("\n", "<br/>"));
		descPane.setStyle(style.toString());

		descPane.setEditable(false);
		descPane.setBackground(label.getBackground());
		 */
		showDialog(allPermissionsList, Resource.STR_BASIC_PERM_LIST_TITLE.getString(), new Dimension(500, 200), null);
	}

	public void showPermDetailDesc(String group)
	{
		PermissionGroupInfoExt g = permissionManager.getPermissionGroup(group);
		Log.e("g " + g + ", " + group);
		if(g == null) return;

		StringBuilder body = new StringBuilder();
		//body.append("<div id=\"perm-detail-desc\">");
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
		//body.append("</div>");

		/*
		JLabel label = new JLabel();
		Font font = label.getFont();

		// create some css from the label's font
		StringBuilder style = new StringBuilder("#perm-detail-desc {");
		style.append("font-family:" + font.getFamily() + ";");
		style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
		style.append("font-size:" + font.getSize() + "pt;}");
		style.append("#about a {text-decoration:none;}");

		// html content
		JHtmlEditorPane descPane = new JHtmlEditorPane("", "", body.toString().replaceAll("\n", "<br/>"));
		descPane.setStyle(style.toString());

		descPane.setEditable(false);
		descPane.setBackground(label.getBackground());
		 */
		showDialog(body.toString(), Resource.STR_BASIC_PERM_DISPLAY_TITLE.getString(), new Dimension(600, 200), new ImageIcon(g.icon.replaceAll("^file:/", "")));
	}

	public void showSdkVersionInfo(String id)
	{
		String sdkVer = null;
		if("min-sdk".equals(id)) {
			sdkVer = minSdkVersion.toString();
		} else if("target-sdk".equals(id)) {
			sdkVer = targerSdkVersion.toString();
		} else if("max-sdk".equals(id)) {
			sdkVer = maxSdkVersion.toString();
		}

		SdkVersionInfoDlg sdkDlg = new SdkVersionInfoDlg(null, Resource.STR_SDK_INFO_FILE_PATH.getString(), Integer.parseInt(sdkVer));
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
		} else if("feature-protection-level".equals(id)) {
			feature = "※ " + Resource.STR_FEATURE_SIGNATURE_DESC.getString() + "\n\n" + signaturePermissions;
			size = new Dimension(500, 200);
		} else if("feature-shared-user-id".equals(id)) {
			feature = "sharedUserId=" + sharedUserId + "\n※ ";
			feature += Resource.STR_FEATURE_SHAREDUSERID_DESC.getString();
		} else if("feature-system-user-id".equals(id)) {
			feature = "sharedUserId=" + sharedUserId + "\n※ ";
			feature += Resource.STR_FEATURE_SYSTEM_UID_DESC.getString();
		} else if("feature-platform-sign".equals(id)) {
			feature = "※ " + Resource.STR_FEATURE_PLATFORM_SIGN_DESC.getString();
			feature += "\n\n" + CertSummary;
			size = new Dimension(500, 150);
		} else if("feature-samsung-sign".equals(id)) {
			feature = "※ " + Resource.STR_FEATURE_SAMSUNG_SIGN_DESC.getString();
			feature += "\n\n" + CertSummary;
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
