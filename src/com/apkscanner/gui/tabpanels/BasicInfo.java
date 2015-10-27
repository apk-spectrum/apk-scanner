package com.apkscanner.gui.tabpanels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import com.apkscanner.core.PermissionGroupManager.PermissionGroup;
import com.apkscanner.core.PermissionGroupManager.PermissionInfo;
import com.apkscanner.data.ApkInfo;
import com.apkscanner.gui.TabbedPanel.TabDataObject;
import com.apkscanner.gui.util.ImageScaler;
import com.apkscanner.gui.util.JHtmlEditorPane;
import com.apkscanner.gui.util.JHtmlEditorPane.HyperlinkClickListener;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;
import com.apkscanner.util.MyXPath;

public class BasicInfo extends JComponent implements HyperlinkClickListener, TabDataObject
{
	private static final long serialVersionUID = 6431995641984509482L;

	private JHtmlEditorPane apkinform = null;
	private String mutiLabels;
	
	private boolean wasSetData = false;
	private long remainTime = 0;
	
	private String[] Labelname = null;
	private String PackageName = null;
	private String VersionName = null;
	private String VersionCode = null;
	private String MinSDKversion = null;
	private String TargerSDKversion = null;
	private String MaxSDKversion = null;
	private boolean isHidden = false;
	private String IconPath = null;
	private String Permissions = null;
	private String Startup = null;
	private String ProtectionLevel = null;
	private boolean debuggable = false;
	private String SharedUserId = null;
	private String ApkSize = null;
	private String SignatureCN = null;
	private String CertSummary = null;
	
	private ArrayList<String> PermissionList = null;
	private HashMap<String, PermissionGroup> PermGroupMap = null;
	private JLabel TimerLabel = null;
	
	public BasicInfo(boolean opening)
	{
		if(!opening) {
			initialize();
			showAbout();
		}
	}
	
	@Override
	public void initialize()
	{
		apkinform = new JHtmlEditorPane();
		apkinform.setEditable(false);
		apkinform.setOpaque(true);

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
		style.append(".danger-perm {text-decoration:none; color:red;}");
		style.append("#about {");
		style.append("font-family:" + font.getFamily() + ";");
		style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
		style.append("font-size:" + font.getSize() + "pt;}");
		style.append("#about a {text-decoration:none;}");
		
		apkinform.setStyle(style.toString());
		apkinform.setBackground(Color.white);
		apkinform.setHyperlinkClickListener(this);
		
		//this.setLayout(new GridBagLayout());
		TimerLabel = new JLabel("");
		TimerLabel.setOpaque(true);
		TimerLabel.setBackground(Color.WHITE);
		TimerLabel.setBorder(new EmptyBorder(0,0,50,0));
		
		TimerLabel.setHorizontalAlignment(JLabel.CENTER);
		
		//this.add(apkinform);
	}
	
	private void showAbout()
	{
		StringBuilder strTabInfo = new StringBuilder("");
		strTabInfo.append("<table>");
		strTabInfo.append("  <tr>");
		strTabInfo.append("    <td width=170>");
		strTabInfo.append("      <image src=\"" + Resource.IMG_APP_ICON.getPath() + "\" width=150 height=150 />");
		strTabInfo.append("    </td>");
		strTabInfo.append("    <td>");
		strTabInfo.append("<div id=\"about\">");
		strTabInfo.append("  <H1>" + Resource.STR_APP_NAME.getString() + " " + Resource.STR_APP_VERSION.getString() + "</H1>");
		strTabInfo.append("  Using following tools,<br/>");
		strTabInfo.append("  Apktool <br/>");
		//strTabInfo.append("  Apktool " + ApkManager.getApkToolVersion() + "<br/>");
		strTabInfo.append("  - <a href=\"http://ibotpeaches.github.io/Apktool/\" title=\"Apktool Project Site\">http://ibotpeaches.github.io/Apktool/</a><br/>");
		strTabInfo.append("  Android Debug Bridge<br/>");
		strTabInfo.append("  Android Asset Packaging Tool<br/>");
		//strTabInfo.append("  " + AdbWrapper.getVersion() + "<br/>");
		strTabInfo.append("  - <a href=\"http://developer.android.com/tools/help/adb.html\" title=\"Android Developer Site\">http://developer.android.com/tools/help/adb.html</a><br/>");
		strTabInfo.append("  <br/><hr/>");
		strTabInfo.append("  Programmed by <a href=\"mailto:" + Resource.STR_APP_MAKER_EMAIL.getString() + "\" title=\"" + Resource.STR_APP_MAKER_EMAIL.getString() + "\">" + Resource.STR_APP_MAKER.getString() + "</a>, 2015.<br/>");
		strTabInfo.append("</div>");
		strTabInfo.append("    </td>");
		strTabInfo.append("  </tr>");
		strTabInfo.append("  <tr>");
		strTabInfo.append("    <td colspan=2>");
		strTabInfo.append("      <hr/>");
		strTabInfo.append("    </td>");
		strTabInfo.append("  </tr>");
		strTabInfo.append("  <tr>");
		strTabInfo.append("    <td colspan=2 height=10000></td>");
		strTabInfo.append("  </tr>");
		strTabInfo.append("</table>");
		strTabInfo.append("<div height=10000 width=10000></div>");
		
		apkinform.setBody(strTabInfo.toString());
	}
	
	private void removeData()
	{
		Labelname = null;
		PackageName = null;
		VersionName = null;
		VersionCode = null;
		MinSDKversion = null;
		TargerSDKversion = null;
		isHidden = false;
		IconPath = null;
		Permissions = null;
		Startup = null;
		ProtectionLevel = null;
		debuggable = false;
		SharedUserId = null;
		ApkSize = null;

		PermissionList = null;
		PermGroupMap = null;

		wasSetData = false;
	}
	
	private void showProcessing()
	{	
//		StringBuilder strTabInfo = new StringBuilder("");
//		strTabInfo.append("<table>");
//		strTabInfo.append("  <tr>");
//		strTabInfo.append("    <td width=600>");
//		//strTabInfo.append("      <center><image src=\"" + Resource.IMG_APK_LOADING.getPath() + "\"/></center></br>");
//		//strTabInfo.append("      <center><image src=\"" + Resource.IMG_LOADING.getPath() + "\"/></center></br>");
//		if(remainTime > -1) {			
//			strTabInfo.append("      <center>Remain time : "+remainTime+" sec</center>");
//		} else {
//			strTabInfo.append("      <center></center>");
//		}
//		strTabInfo.append("    </td>");
//		strTabInfo.append("  </tr>");
//		strTabInfo.append("</table>");
//		strTabInfo.append("<div height=10000 width=10000></div>");
//		apkinform.setBody(strTabInfo.toString());

		if(remainTime > -1) {
			TimerLabel.setText("Remain time : "+remainTime+" sec...");
		} else {
			TimerLabel.setText("");
		}
		
	}
	
	public void showProcessing(long time)
	{
		this.remainTime = (int)Math.round((double)time / 1000);

		//showProcessing();
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				Log.i("RemainTimeTimer run() " + remainTime);
				if(!wasSetData) {
					showProcessing();
				}
				if(wasSetData || --remainTime <= 0) cancel();
			}
			
		}, 0, 1000);
		
		this.removeAll();
		this.setLayout(new BorderLayout());
		
		JLabel logo = new JLabel(Resource.IMG_APK_LOGO.getImageIcon(400, 250));
		logo.setOpaque(true);
		logo.setBackground(Color.white);
		
		JLabel gif = new JLabel(Resource.IMG_WAIT_BAR.getImageIcon());
		gif.setOpaque(true);
		gif.setBackground(Color.WHITE);
		gif.setPreferredSize(new Dimension(Resource.IMG_WAIT_BAR.getImageIcon().getIconWidth(),Resource.IMG_WAIT_BAR.getImageIcon().getIconHeight()));
		
		this.add(logo,BorderLayout.NORTH);
		
		this.add(gif,BorderLayout.CENTER);
		this.add(TimerLabel,BorderLayout.SOUTH);
		
		//this.add(TimerLabel,BorderLayout.CENTER);
	}
	
	class RemainTimeTimer extends TimerTask
	{
		@Override
		public synchronized void run()
		{			
			Log.i("RemainTimeTimer run() " + remainTime);
			if(!wasSetData) {
				showProcessing();
			}
			if(wasSetData || --remainTime <= 0) {				
				cancel();				
			}
		}
	}
	

	public synchronized void setData()
	{
		if(!wasSetData) return;
		
		this.removeAll();
		String sdkVersion = "";
		if(!MinSDKversion.isEmpty()) {
			sdkVersion += makeHyperLink("@event", MinSDKversion +" (Min)", "Min SDK version", "min-sdk", null);
		}
		if(!TargerSDKversion.isEmpty()) {
			if(!sdkVersion.isEmpty()) {
				sdkVersion += ", ";
			}
			sdkVersion += makeHyperLink("@event", TargerSDKversion + " (Target)", "Targer SDK version", "target-sdk", null);
		}
		if(!MaxSDKversion.isEmpty()) {
			if(!sdkVersion.isEmpty()) {
				sdkVersion += ", ";
			}
			sdkVersion += makeHyperLink("@event", MaxSDKversion + " (Max)", "Max SDK version", "max-sdk", null);
		}
		if(sdkVersion.isEmpty()) {
			sdkVersion += "Unspecified";
		}
		
		StringBuilder feature = new StringBuilder();
		if(isHidden) {
			feature.append(makeHyperLink("@event", Resource.STR_FEATURE_HIDDEN_LAB.getString(), Resource.STR_FEATURE_HIDDEN_DESC.getString(), "feature-hidden", null));
		} else {
			feature.append(makeHyperLink("@event", Resource.STR_FEATURE_LAUNCHER_LAB.getString(), Resource.STR_FEATURE_LAUNCHER_DESC.getString(), "feature-launcher", null));
		}
		if(!Startup.isEmpty()) {
			feature.append(", " + makeHyperLink("@event", Resource.STR_FEATURE_STARTUP_LAB.getString(), Resource.STR_FEATURE_STARTUP_DESC.getString(), "feature-startup", null));
		}
		if(!ProtectionLevel.isEmpty()) {
			feature.append(", " + makeHyperLink("@event", Resource.STR_FEATURE_SIGNATURE_LAB.getString(), Resource.STR_FEATURE_SIGNATURE_DESC.getString(), "feature-protection-level", null));
		}
		if(!SharedUserId.isEmpty() && !SharedUserId.startsWith("android.uid.system") ) {
			feature.append(", " + makeHyperLink("@event", Resource.STR_FEATURE_SHAREDUSERID_LAB.getString(), Resource.STR_FEATURE_SHAREDUSERID_DESC.getString(), "feature-shared-user-id", null));
		}

		StringBuilder importantFeatures = new StringBuilder();
		if(SharedUserId.startsWith("android.uid.system")) {
			importantFeatures.append(", <font style=\"color:#ED7E31; font-weight:bold\">");
			importantFeatures.append(makeHyperLink("@event", Resource.STR_FEATURE_SYSTEM_UID_LAB.getString(), Resource.STR_FEATURE_SYSTEM_UID_DESC.getString(), "feature-system-user-id", null));
			importantFeatures.append("</font>");
		}
		if(SignatureCN != null && SignatureCN.indexOf("'Android'") > -1) {
			importantFeatures.append(", <font style=\"color:#ED7E31; font-weight:bold\">");
			importantFeatures.append(makeHyperLink("@event", Resource.STR_FEATURE_PLATFORM_SIGN_LAB.getString(), Resource.STR_FEATURE_PLATFORM_SIGN_DESC.getString(), "feature-platform-sign", null));
			importantFeatures.append("</font>");
		}
		if(SignatureCN != null && SignatureCN.indexOf("'Samsung Cert'") > -1) {
			importantFeatures.append(", <font style=\"color:#ED7E31; font-weight:bold\">");
			importantFeatures.append(makeHyperLink("@event", Resource.STR_FEATURE_SAMSUNG_SIGN_LAB.getString(), Resource.STR_FEATURE_SAMSUNG_SIGN_DESC.getString(), "feature-samsung-sign", null));
			importantFeatures.append("</font>");
		}
		if(debuggable) {
			importantFeatures.append(", <font style=\"color:#ED7E31; font-weight:bold\">");
			importantFeatures.append(makeHyperLink("@event", Resource.STR_FEATURE_DEBUGGABLE_LAB.getString(), Resource.STR_FEATURE_DEBUGGABLE_DESC.getString(), "feature-debuggable", null));
			importantFeatures.append("</font>");
		}
		if(importantFeatures.length() > 0) {
			feature.append("<br/>" + importantFeatures.substring(2));
		}
		
		String permGorupImg = makePermGroup();
		
		int infoHeight = 280;
		if(PermGroupMap.keySet().size() > 15) infoHeight = 220;
		else if(PermGroupMap.keySet().size() > 0) infoHeight = 260;
		
		mutiLabels = "";
		for(String s: Labelname) {
			mutiLabels += s + "\n";
		}

		StringBuilder strTabInfo = new StringBuilder("");
		strTabInfo.append("<table>");
		strTabInfo.append("  <tr>");
		strTabInfo.append("    <td width=170 height=" + infoHeight + ">");
		strTabInfo.append("      <image src=\"" + IconPath + "\" width=150 height=150 />");
		strTabInfo.append("    </td>");
		strTabInfo.append("    <td height=" + infoHeight + ">");
		strTabInfo.append("      <div id=\"basic-info\">");
		strTabInfo.append("        <font style=\"font-size:20px; color:#548235; font-weight:bold\">");
		if(Labelname.length > 1) {
		strTabInfo.append("          " + makeHyperLink("@event", Labelname[0], mutiLabels, "other-lang", null));
		strTabInfo.append("        </font>");
		} else {
		strTabInfo.append("          " + Labelname[0]);
		strTabInfo.append("</font><br/>");
		}
		if(Labelname.length > 1) {
		strTabInfo.append("        <font style=\"font-size:10px;\">");
		strTabInfo.append("          " + makeHyperLink("@event", "["+Labelname.length+"]", mutiLabels, "other-lang", null));
		strTabInfo.append("</font><br/>");
		}
		strTabInfo.append("        <font style=\"font-size:15px; color:#4472C4\">");
		strTabInfo.append("          [" + PackageName +"]");
		strTabInfo.append("</font><br/>");
		strTabInfo.append("        <font style=\"font-size:15px; color:#ED7E31\">");
		strTabInfo.append("          " + makeHyperLink("@event", "Ver. " + VersionName +" / " + VersionCode, "VersionName : " + VersionName + "\n" + "VersionCode : " + VersionCode, "app-version", null));
		strTabInfo.append("        </font><br/>");
		strTabInfo.append("        <br/>");
		strTabInfo.append("        <font style=\"font-size:12px\">");
		strTabInfo.append("          @SDK Ver. " + sdkVersion + "<br/>");
		strTabInfo.append("          " + ApkSize);
		strTabInfo.append("        </font>");
		strTabInfo.append("        <br/><br/>");
		strTabInfo.append("        <font style=\"font-size:12px\">");
		strTabInfo.append("          [" + Resource.STR_FEATURE_LAB.getString() + "]<br/>");
		strTabInfo.append("          " + feature);
		strTabInfo.append("        </font><br/>");
		strTabInfo.append("      </div>");
		strTabInfo.append("    </td>");
		strTabInfo.append("  </tr>");
		strTabInfo.append("</table>");
		strTabInfo.append("<div id=\"perm-group\" style=\"width:480px; padding-top:5px; border-top:1px; border-left:0px; border-right:0px; border-bottom:0px; border-style:solid;\">");
		strTabInfo.append("  <font style=\"font-size:12px;color:black;\">");
		if(PermissionList.size() > 0) {
		strTabInfo.append("    [" + Resource.STR_BASIC_PERMISSIONS.getString() + "] - ");
		strTabInfo.append("    " + makeHyperLink("@event","<u>" + Resource.STR_BASIC_PERMLAB_DISPLAY.getString() + "</u>",Resource.STR_BASIC_PERMDESC_DISPLAY.getString(),"display-list", null));
		} else {
		strTabInfo.append("    " + Resource.STR_LABEL_NO_PERMISSION.getString());
		}
		strTabInfo.append("  </font><br/>");
		strTabInfo.append("  <font style=\"font-size:5px\"><br/></font>");
		strTabInfo.append("  " + permGorupImg);
		strTabInfo.append("</div>");
		strTabInfo.append("<div height=10000 width=10000></div>");
	
		apkinform.setBody(strTabInfo.toString());
		//this.setLayout(new GridLayout());
		this.add(apkinform);
	}

	public synchronized void setData(long estimatedTime)
	{
		if(apkinform == null)
			initialize();
		removeData();

		if(estimatedTime > -1)
			showProcessing(estimatedTime);
		return;
	}

	@Override
	public synchronized void setData(ApkInfo apkInfo)
	{
		if(apkinform == null)
			initialize();
		
		if(apkInfo == null) {
			removeData();
			showAbout();
			return;
		}
		wasSetData = true;
		
		Labelname = apkInfo.Labelname;
		PackageName = apkInfo.PackageName;
		VersionName = apkInfo.VersionName;
		VersionCode = apkInfo.VersionCode;
		MinSDKversion = apkInfo.MinSDKversion;
		TargerSDKversion = apkInfo.TargerSDKversion;
		MaxSDKversion = apkInfo.MaxSDKversion;
		isHidden = apkInfo.isHidden;
		IconPath = apkInfo.IconPath;
		Permissions = apkInfo.Permissions;
		Startup = apkInfo.Startup;
		ProtectionLevel = apkInfo.ProtectionLevel;
		debuggable = apkInfo.debuggable;
		SharedUserId = apkInfo.SharedUserId;
		ApkSize = apkInfo.ApkSize;

		PermissionList = apkInfo.PermissionList;
		PermGroupMap = apkInfo.PermGroupMap;

		SignatureCN = apkInfo.CertCN;
		CertSummary = apkInfo.CertSummary;
	
		setData();
	}
	
	private String makePermGroup()
	{
		StringBuilder permGroup = new StringBuilder("");
		
		Set<String> keys = PermGroupMap.keySet();
		int cnt = 0;
		for(String key: keys) {
			PermissionGroup g = PermGroupMap.get(key);
			permGroup.append(makeHyperLink("@event", makeImage(g.icon), g.permSummary, g.permGroup, g.hasDangerous?"color:red;":null));
			if(++cnt % 15 == 0) permGroup.append("<br/>");
		}
		
		return permGroup.toString();
	}
	
	private String makeHyperLink(String href, String text, String title, String id, String style)
	{
		String attr = "";
		if(title != null) {
			attr += String.format(" title=\"%s\"", title);
		}
		if(id != null) {
			attr += String.format(" id=\"%s\"", id);
		}
		if(style != null) {
			attr += String.format(" style=\"%s\"", style);
		}
		
		return String.format("<a href=\"%s\"%s>%s</a>", href, attr, text);
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
					|| Labelname.length == 1) return;
			try {
				ImageIcon icon = null;
				if(IconPath != null && (IconPath.startsWith("jar:") || IconPath.startsWith("file:"))) {
					icon = new ImageIcon(ImageScaler.getScaledImage(new ImageIcon(new URL(IconPath)),32,32));
				}
				showDialog(mutiLabels, Resource.STR_LABEL_APP_NAME_LIST.getString(), new Dimension(300, 200), icon);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		} else if("app-version".equals(id)) {
			String ver = "VersionName : " + VersionName + "\n" + "VersionCode : " + VersionCode;
			showDialog(ver, "App version info", new Dimension(300, 50), null);
		} else if("display-list".equals(id)) {
			showPermList();
		} else if(id.endsWith("-sdk")){
			showSdkVersionInfo(id);
		} else if(id.startsWith("feature-")) {
			showFeatureInfo(id);
		} else {
			showPermDetailDesc(id);
		}
	}
	
	private void showDialog(String content, String title, Dimension size, Icon icon)
	{
		JTextArea taskOutput = new JTextArea();
		taskOutput.setText(content);
		taskOutput.setEditable(false);
		taskOutput.setCaretPosition(0);
		
		JScrollPane scrollPane = new JScrollPane(taskOutput);
		scrollPane.setPreferredSize(size);

		JOptionPane.showOptionDialog(null, scrollPane, title, JOptionPane.INFORMATION_MESSAGE, JOptionPane.INFORMATION_MESSAGE, icon,
				new String[] {Resource.STR_BTN_OK.getString()}, Resource.STR_BTN_OK.getString());
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
		showDialog(Permissions, Resource.STR_BASIC_PERM_LIST_TITLE.getString(), new Dimension(500, 200), null);
	}
	
	public void showPermDetailDesc(String group)
	{
		PermissionGroup g = PermGroupMap.get(group);
		
		if(g == null) return;

		StringBuilder body = new StringBuilder("");
		//body.append("<div id=\"perm-detail-desc\">");
		body.append("■ ");
		if(g.label != null) {
			body.append(g.label + " - ");
		}
		body.append("[" + group + "]\n");
		if(g.desc != null) {
			body.append(" : " + g.desc + "\n");
		}
		body.append("------------------------------------------------------------------------------------------------------------\n\n");
		
		for(PermissionInfo info: g.permList) {
			body.append("▶ ");
			if(info.isDangerous) {
				body.append("[DANGEROUS] ");	
			}
			if(info.label != null) {
				body.append(info.label + " ");
			}
			body.append("[" + info.permission + "]\n");
			if(info.desc != null) {
				body.append(" : " + info.desc + "\n");
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
			sdkVer = MinSDKversion;
		} else if("target-sdk".equals(id)) {
			sdkVer = TargerSDKversion;
		} else if("max-sdk".equals(id)) {
			sdkVer = MaxSDKversion;
		}
		
		StringBuilder info = new StringBuilder();
		InputStream xml = Resource.class.getResourceAsStream(Resource.STR_SDK_INFO_FILE_PATH.getString());
		MyXPath xpath = new MyXPath(xml);
		xpath.getNode("/resources/sdk-info[@apiLevel='" + sdkVer + "']");

		Dimension size = null;
		ImageIcon logoIcon = null;

		if(xpath.getNode() != null) {
			info.append(xpath.getAttributes("platformVersion"));
			info.append(" - " + xpath.getAttributes("codeName"));
			info.append("\n\nAPI Level " + sdkVer);
			info.append("\nBuild.VERSION_CODES." + xpath.getAttributes("versionCode"));

			size = new Dimension(350, 100);

			logoIcon = new ImageIcon(Resource.class.getResource(xpath.getAttributes("icon")));
		} else {
			info.append("API Level " + sdkVer);
			info.append("\nSorry, It's unknown verion.\nYou can look at the sdk info by the Android developer site\n");
			info.append("http://developer.android.com/guide/topics/manifest/uses-sdk-element.html#ApiLevels");

			size = new Dimension(500, 100);
			logoIcon = new ImageIcon(Resource.class.getResource("/icons/logo/base.png"));
		}
		
		showDialog(info.toString(), "SDK " + sdkVer, size, logoIcon);
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
			feature = "※ " + Resource.STR_FEATURE_SIGNATURE_DESC.getString() + "\n";
			for(String s: Permissions.split("\n")) {
				if(s.endsWith("- <SIGNATURE>")) {
					feature += "\n" + s;
				}
			}
			size = new Dimension(500, 100);
		} else if("feature-shared-user-id".equals(id)) {
			feature = "sharedUserId=" + SharedUserId + "\n※ ";
			feature += Resource.STR_FEATURE_SHAREDUSERID_DESC.getString();
		} else if("feature-system-user-id".equals(id)) {
			feature = "sharedUserId=" + SharedUserId + "\n※ ";
			feature += Resource.STR_FEATURE_SYSTEM_UID_DESC.getString();
		} else if("feature-platform-sign".equals(id)) {
			feature = "※ " + Resource.STR_FEATURE_PLATFORM_SIGN_DESC.getString();
			feature += "\n\n" + CertSummary;
			size = new Dimension(500, 150);
		} else if("feature-samsung-sign".equals(id)) {
			feature = "※ " + Resource.STR_FEATURE_SAMSUNG_SIGN_DESC.getString();
			feature += "\n\n" + CertSummary;
			size = new Dimension(500, 150);
		} else if("feature-debuggable".equals(id)) {
			feature = Resource.STR_FEATURE_DEBUGGABLE_DESC.getString();
		}
		
		showDialog(feature, "Feature info", size, null);
	}

	@Override
	public void reloadResource() {
		setData();
	}
}
