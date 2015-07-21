package com.ApkInfo.TabUI;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import com.ApkInfo.Resource.Resource;
import com.ApkInfo.UIUtil.JHtmlEditorPane;
import com.ApkInfo.UIUtil.JHtmlEditorPane.HyperlinkClickListener;
import com.ApkInfo.Core.ApkManager.ApkInfo;
import com.ApkInfo.Core.PermissionGroupManager.PermissionGroup;
import com.ApkInfo.Core.PermissionGroupManager;

public class MyTabUIbasicInfo extends JComponent implements HyperlinkClickListener
{
	private static final long serialVersionUID = 6431995641984509482L;

	private JHtmlEditorPane apkinform;
	private ApkInfo apkInfo;

	public MyTabUIbasicInfo() {
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
	    
        apkinform.setStyle(style.toString());
        apkinform.setBackground(Color.white);
        apkinform.setHyperlinkClickListener(this);

        this.setLayout(new GridBagLayout());
        this.add(apkinform);
	}

	public void setData(ApkInfo apkInfo)
	{
		this.apkInfo = apkInfo;
		
		String sdkVersion = "@SDK Ver.";
        if(!apkInfo.MinSDKversion.isEmpty()) {
        	sdkVersion += apkInfo.MinSDKversion +" (Min)";
        }
        if(!apkInfo.TargerSDKversion.isEmpty()) {
        	if(!apkInfo.MinSDKversion.isEmpty()) {
        		sdkVersion += ", "; 
        	}
        	sdkVersion += apkInfo.TargerSDKversion + " (Target)";
        }
        if(apkInfo.MinSDKversion.isEmpty() && apkInfo.TargerSDKversion.isEmpty()) {
        	sdkVersion += "Unknown"; 
        }
        
        String feature = makeHyperLink("", apkInfo.Hidden, apkInfo.Hidden, null);
        if(!apkInfo.Startup.isEmpty()) {
        	feature += ", " + makeHyperLink("", apkInfo.Startup, apkInfo.Startup, null);
        }
        if(!apkInfo.ProtectionLevel.isEmpty()) {
        	feature += ", " + makeHyperLink("", apkInfo.ProtectionLevel, apkInfo.ProtectionLevel, null);
        }
        if(!apkInfo.SharedUserId.isEmpty()) {
        	feature += ", " + makeHyperLink("", "SHARED_USER_ID", "SHARED_USER_ID", null);
        }

        StringBuilder strTabInfo = new StringBuilder("");
		strTabInfo.append("<table width=10000>");
		strTabInfo.append("  <tr>");
		strTabInfo.append("    <td width=170 height=230>");
		strTabInfo.append("      <image src=\"file:/"+apkInfo.IconPath.replaceAll("^/", "") +"\" width=150 height=150 />");
		strTabInfo.append("    </td>");
		strTabInfo.append("    <td height=230>");
		strTabInfo.append("      <div id=\"basic-info\">");
        strTabInfo.append("        <font style=\"font-size:20px; color:#548235; font-weight:bold\">");
        strTabInfo.append("          " + makeHyperLink("", apkInfo.Labelname, "App name", null));
        strTabInfo.append("        </font><br/>");
        strTabInfo.append("        <font style=\"font-size:15px; color:#4472C4\">");
        strTabInfo.append("          [" + makeHyperLink("", apkInfo.PackageName, "Package name", null) +"]");
        strTabInfo.append("        </font><br/>");
        strTabInfo.append("        <font style=\"font-size:15px; color:#ED7E31\">");
        strTabInfo.append("          " + makeHyperLink("", "Ver. " + apkInfo.VersionName +" / " + apkInfo.VersionCode, "VersionName : " + apkInfo.VersionName + "\n" + "VersionCode : " + apkInfo.VersionCode, null));
        strTabInfo.append("        </font><br/>");
        strTabInfo.append("        <br/><br/>");
        strTabInfo.append("        <font style=\"font-size:12px\">");
        strTabInfo.append("          " + makeHyperLink("", apkInfo.ApkSize, "APK size", null) + "<br/>");
        strTabInfo.append("          " + makeHyperLink("", sdkVersion, "Target SDK version", null));
        strTabInfo.append("        </font>");
        strTabInfo.append("        <br/><br/>");
        strTabInfo.append("        <font style=\"font-size:12px\">");
        strTabInfo.append("          [Feature]<br/>");
        strTabInfo.append("          " + feature);
        strTabInfo.append("        </font><br/>");
        strTabInfo.append("      </div>");
        strTabInfo.append("    </td>");
        strTabInfo.append("  </tr>");
        strTabInfo.append("  <tr>");
        strTabInfo.append("    <td colspan=2>");
        strTabInfo.append("      <div id=\"perm-group\">");
        strTabInfo.append("        <hr/>");
        strTabInfo.append("        <font style=\"font-size:12px;color:black;\">[Permissions] - ");
        strTabInfo.append("          " + makeHyperLink("@event","<u>Display the entire list</u>","Display the entire list","display-list"));
        strTabInfo.append("        </font><br/>");
        strTabInfo.append("        <font style=\"font-size:5px\"><br/></font>");
        strTabInfo.append("        " + makePermGroup());
        strTabInfo.append("      </div>");
        strTabInfo.append("    </td>");
        strTabInfo.append("  </tr>");
        strTabInfo.append("  <tr>");
        strTabInfo.append("    <td colspan=2 height=10000></td>");
        strTabInfo.append("  </tr>");
        strTabInfo.append("</table>");
        
        apkinform.setBody(strTabInfo.toString());
	}
	
	private String makePermGroup()
	{
		StringBuilder permGroup = new StringBuilder("");
		
		for(String pgk: apkInfo.PermissionList) {
			System.out.println(pgk);
		}
		PermissionGroupManager permGroupManager = new PermissionGroupManager(apkInfo.PermissionList.toArray(new String[0]));
		HashMap<String, PermissionGroup> map = permGroupManager.getPermGroupMap();
		Set<String> keys = map.keySet();
		for(String key: keys) {
			System.out.println("key - " + key);
			PermissionGroup g = map.get(key);
			permGroup.append(makeHyperLink("@event", makeImage(g.icon), g.permGroup, g.permGroup));			
		}
		
		return permGroup.toString();
	}
	
	private String makeHyperLink(String href, String text, String title, String id)
	{
		String attr = "";
		if(title != null) {
			attr += String.format(" title=\"%s\"", title);
		}
		if(id != null) {
			attr += String.format(" id=\"%s\"", id);
		}
		
		return String.format("<a href=\"%s\"%s>%s</a>", href, attr, text);
	}
	
	private String makeImage(String src)
	{
		return "<image src=\"" + src + "\"/>";
	}
	
	@Override
	public void hyperlinkClick(String id) {
		System.out.println("click : "+id);
		if(id.equals("display-list")) {
			JOptionPane.showMessageDialog(null, apkInfo.Permissions, "Permissions list", JOptionPane.INFORMATION_MESSAGE);
		}
	}
}
