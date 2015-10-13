package com.apkscanner.gui.tabpanels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.apkscanner.core.PermissionGroupManager.PermissionGroup;
import com.apkscanner.core.PermissionGroupManager.PermissionInfo;
import com.apkscanner.data.ApkInfo;
import com.apkscanner.gui.TabbedPanel.TabDataObject;
import com.apkscanner.gui.util.ImageScaler;
import com.apkscanner.gui.util.JHtmlEditorPane;
import com.apkscanner.gui.util.JHtmlEditorPane.HyperlinkClickListener;
import com.apkscanner.resource.Resource;

public class BasicInfo extends JComponent implements HyperlinkClickListener, TabDataObject
{
	private static final long serialVersionUID = 6431995641984509482L;

	private JHtmlEditorPane apkinform = null;
	private String mutiLabels;
	
	private boolean wasSetData = false;
	
	private String ApkFilePath = null;
	private String[] Labelname = null;
	private String PackageName = null;
	private String VersionName = null;
	private String VersionCode = null;
	private String MinSDKversion = null;
	private String TargerSDKversion = null;
	private boolean isHidden = false;
	private String IconPath = null;
	private String Permissions = null;
	private String Startup = null;
	private String ProtectionLevel = null;
	private boolean debuggable = false;
	private String SharedUserId = null;
	private String ApkSize = null;
	
	private ArrayList<String> PermissionList = null;
	private HashMap<String, PermissionGroup> PermGroupMap = null;

	public BasicInfo() {

	}
	
	@Override
	public void initialize()
	{
		apkinform = new JHtmlEditorPane();
		apkinform.setEditable(false);

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

		this.setLayout(new GridBagLayout());
		this.add(apkinform);
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
	
	public void setData()
	{
		if(!wasSetData) return;
		
		String sdkVersion = "@SDK Ver.";
		if(!MinSDKversion.isEmpty()) {
			sdkVersion += MinSDKversion +" (Min)";
		}
		if(!TargerSDKversion.isEmpty()) {
			if(!MinSDKversion.isEmpty()) {
				sdkVersion += ", ";
			}
			sdkVersion += TargerSDKversion + " (Target)";
		}
		if(MinSDKversion.isEmpty() && TargerSDKversion.isEmpty()) {
			sdkVersion += "Unknown";
		}
		
		String feature;
		if(isHidden) {
			feature = makeHyperLink("", Resource.STR_FEATURE_HIDDEN_LAB.getString(), Resource.STR_FEATURE_HIDDEN_DESC.getString(), null, null);
		} else {
			feature = makeHyperLink("", Resource.STR_FEATURE_LAUNCHER_LAB.getString(), Resource.STR_FEATURE_LAUNCHER_DESC.getString(), null, null);
		}
		
		if(!Startup.isEmpty()) {
			feature += ", " + makeHyperLink("", Resource.STR_FEATURE_STARTUP_LAB.getString(), Resource.STR_FEATURE_STARTUP_DESC.getString(), null, null);
		}
		if(!ProtectionLevel.isEmpty()) {
			feature += ", " + makeHyperLink("", Resource.STR_FEATURE_SIGNATURE_LAB.getString(), Resource.STR_FEATURE_SIGNATURE_DESC.getString(), null, null);
		}
		if(!SharedUserId.isEmpty()) {
			feature += ", " + makeHyperLink("", Resource.STR_FEATURE_SHAREDUSERID_LAB.getString(), Resource.STR_FEATURE_SHAREDUSERID_DESC.getString(), null, null);
		}
		if(debuggable) {
			feature += ", " + makeHyperLink("", Resource.STR_FEATURE_DEBUGGABLE_LAB.getString(), Resource.STR_FEATURE_DEBUGGABLE_DESC.getString(), null, null);
		}
		
		String permGorupImg = makePermGroup();
		
		int infoHeight = 300;
		if(PermGroupMap.keySet().size() > 15) infoHeight = 230;
		else if(PermGroupMap.keySet().size() > 0) infoHeight = 270;
		
		mutiLabels = "";
		for(String s: Labelname) {
			mutiLabels += s + "\n";
		}

		StringBuilder strTabInfo = new StringBuilder("");
		strTabInfo.append("<table>");
		strTabInfo.append("  <tr>");
		strTabInfo.append("    <td width=170 height=" + infoHeight + ">");
		strTabInfo.append("      <image src=\"jar:file:" + ApkFilePath.replaceAll("#", "%23") + "!/" + IconPath + "\" width=150 height=150 />");
		strTabInfo.append("    </td>");
		strTabInfo.append("    <td height=" + infoHeight + ">");
		strTabInfo.append("      <div id=\"basic-info\">");
		strTabInfo.append("        <font style=\"font-size:20px; color:#548235; font-weight:bold\">");
		strTabInfo.append("          " + makeHyperLink("@event", Labelname[0], mutiLabels, "other-lang", null));
		strTabInfo.append("        </font>");
		if(Labelname.length > 1) {
			strTabInfo.append("<font style=\"font-size:10px;\">");
			strTabInfo.append(" " + makeHyperLink("@event", "["+Labelname.length+"]", mutiLabels, "other-lang", null));
			strTabInfo.append("</font>");
		}
		strTabInfo.append("        <br/>");
		strTabInfo.append("        <font style=\"font-size:15px; color:#4472C4\">");
		strTabInfo.append("          [" + makeHyperLink("", PackageName, "Package name", null, null) +"]");
		strTabInfo.append("        </font><br/>");
		strTabInfo.append("        <font style=\"font-size:15px; color:#ED7E31\">");
		strTabInfo.append("          " + makeHyperLink("", "Ver. " + VersionName +" / " + VersionCode, "VersionName : " + VersionName + "\n" + "VersionCode : " + VersionCode, null, null));
		strTabInfo.append("        </font><br/>");
		strTabInfo.append("        <br/><br/>");
		strTabInfo.append("        <font style=\"font-size:12px\">");
		strTabInfo.append("          " + makeHyperLink("", ApkSize, "APK size", null, null) + "<br/>");
		strTabInfo.append("          " + makeHyperLink("", sdkVersion, "Target SDK version", null, null));
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
	}

	@Override
	public void setData(ApkInfo apkInfo)
	{
		if(apkinform == null)
			initialize();
		
		if(apkInfo == null) {
			removeData();
			wasSetData = false;
			return;
		}
		wasSetData = true;
		
		ApkFilePath = apkInfo.ApkPath;
		Labelname = apkInfo.Labelname;
		PackageName = apkInfo.PackageName;
		VersionName = apkInfo.VersionName;
		VersionCode = apkInfo.VersionCode;
		MinSDKversion = apkInfo.MinSDKversion;
		TargerSDKversion = apkInfo.TargerSDKversion;
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
	public void hyperlinkClick(String id) {

		if(id.equals("other-lang")) {
			if(mutiLabels == null || mutiLabels.isEmpty()
					|| Labelname.length == 1) return;
			showDialog(mutiLabels, Resource.STR_LABEL_APP_NAME_LIST.getString(), new Dimension(300, 200)
					, new ImageIcon(ImageScaler.getScaledImage(new ImageIcon(IconPath),32,32)));
		} else if(id.equals("display-list")) {
			showPermList();
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

	@Override
	public void reloadResource() {
		setData();
	}
}
