package com.ApkInfo.TabUI;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;

import com.ApkInfo.Resource.Resource;
import com.ApkInfo.UIUtil.JHtmlEditorPane;
import com.ApkInfo.Core.ApkManager.ApkInfo;

public class MyTabUIbasicInfo extends JComponent{
	private static final long serialVersionUID = 6431995641984509482L;

	private JHtmlEditorPane apkinform;

	public MyTabUIbasicInfo() {
    	apkinform = new JHtmlEditorPane();
        apkinform.setEditable(false);

        //Font font = new Font("helvitica", Font.BOLD, 15);
		JLabel label = new JLabel();
	    Font font = label.getFont();

	    // create some css from the label's font
	    StringBuilder style = new StringBuilder("#basic-info {");
	    style.append("font-family:" + font.getFamily() + ";");
	    style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
	    style.append("font-size:" + font.getSize() + "pt;}");
	    style.append("#basic-info a {text-decoration:none; color:black;}");
	    style.append("#perm-group a {text-decoration:none; color:#"+Integer.toHexString(label.getBackground().getRGB() & 0xFFFFFF)+";}");
	    
        apkinform.setStyle(style.toString());
        apkinform.setBackground(Color.white);

        this.setLayout(new GridBagLayout());
        this.add(apkinform);
	}

	public void setData(ApkInfo apkInfo)
	{
        System.out.println(Resource.IMG_PERM_GROUP_PHONE_CALLS.getPath());
        System.out.println("\"file:/"+apkInfo.IconPath +"\"");
		StringBuilder strTabInfo = new StringBuilder("");
		strTabInfo.append("<table width=10000><tr><td width=170 height=250>");
		strTabInfo.append("<image src=\"file:/"+apkInfo.IconPath.replaceAll("^/", "") +"\" width=150 height=150 />");
		strTabInfo.append("</td><td height=250><div id=\"basic-info\">");
        strTabInfo.append("<font style=\"font-size:20px; color:#548235; font-weight:bold\"><a href=\"\" title=\"App name\">" + apkInfo.Labelname +"</a></font><br/>");
        strTabInfo.append("<font style=\"font-size:15px; color:#4472C4\">[" + apkInfo.PackageName +"]</font><br/>");
        strTabInfo.append("<font style=\"font-size:15px; color:#ED7E31\">Ver. " + apkInfo.VersionName +" / ");
        strTabInfo.append("" + apkInfo.VersionCode + "</font><br/>");

        strTabInfo.append("<br/><br/>");
        strTabInfo.append("<font style=\"font-size:12px\">");
        strTabInfo.append(apkInfo.ApkSize + "<br/>");
        strTabInfo.append("@SDK Ver. ");
        if(!apkInfo.MinSDKversion.isEmpty()) {
        	strTabInfo.append("" + apkInfo.MinSDKversion +" (Min)");
        }
        if(!apkInfo.TargerSDKversion.isEmpty()) {
        	if(!apkInfo.MinSDKversion.isEmpty()) {
        		strTabInfo.append(", "); 
        	}
        	strTabInfo.append("" + apkInfo.TargerSDKversion +" (Target)");
        }
        if(apkInfo.MinSDKversion.isEmpty() && apkInfo.TargerSDKversion.isEmpty()) {
        	strTabInfo.append("Unknown"); 
        }
        strTabInfo.append("</font>");
        strTabInfo.append("<br/><br/>");

        strTabInfo.append("<font style=\"font-size:12px\">");
        strTabInfo.append("[Feature]<br/>");
        //strTabInfo.append("Signing : " + ApkInfo.CertList.size() +"<BR/>";
        strTabInfo.append("" + apkInfo.Hidden +"");
        if(!apkInfo.Startup.isEmpty()) {
        	strTabInfo.append(", " + apkInfo.Startup + "");
        }
        if(!apkInfo.ProtectionLevel.isEmpty()) {
        	strTabInfo.append(", " + apkInfo.ProtectionLevel + "");
        }
        
        if(!apkInfo.SharedUserId.isEmpty()) {
        	strTabInfo.append(", SHARED_USER_ID");
        }
        //strTabInfo.append("<BR/><BR/>");
        strTabInfo.append("</font><br/>");
        strTabInfo.append("</div></td></tr><tr><td colspan=2>");
        strTabInfo.append("<div id=\"perm-group\"><hr/>");
        strTabInfo.append("[Permissions]<br/>");
        strTabInfo.append("<a href=\"\" title=\"call00jl\n11111111\"><image src=\""+Resource.IMG_PERM_GROUP_PHONE_CALLS.getPath()+"\"/></a>");
        strTabInfo.append("<a href=\"\" title=\"call01\nfjkdls\"><image src=\""+Resource.IMG_PERM_GROUP_PHONE_CALLS.getPath()+"\"/></a>");
        strTabInfo.append("<a href=\"\" title=\"call02\"><image src=\""+Resource.IMG_PERM_GROUP_PHONE_CALLS.getPath()+"\"/></a>");
        strTabInfo.append("<a href=\"\" title=\"call03\"><image src=\""+Resource.IMG_PERM_GROUP_PHONE_CALLS.getPath()+"\"/></a>");
        strTabInfo.append("<a href=\"\" title=\"call04\"><image src=\""+Resource.IMG_PERM_GROUP_PHONE_CALLS.getPath()+"\"/></a>");
        strTabInfo.append("<a href=\"\" title=\"call05\"><image src=\""+Resource.IMG_PERM_GROUP_PHONE_CALLS.getPath()+"\"/></a>");
        strTabInfo.append("<a href=\"\" title=\"call06\"><image src=\""+Resource.IMG_PERM_GROUP_PHONE_CALLS.getPath()+"\"/></a>");
        strTabInfo.append("<a href=\"\" title=\"call07\"><image src=\""+Resource.IMG_PERM_GROUP_PHONE_CALLS.getPath()+"\"/></a>");
        strTabInfo.append("<a href=\"\" title=\"call08\"><image src=\""+Resource.IMG_PERM_GROUP_PHONE_CALLS.getPath()+"\"/></a>");
        strTabInfo.append("<a href=\"\" title=\"call09\"><image src=\""+Resource.IMG_PERM_GROUP_PHONE_CALLS.getPath()+"\"/></a>");
        strTabInfo.append("<a href=\"\" title=\"call10\"><image src=\""+Resource.IMG_PERM_GROUP_PHONE_CALLS.getPath()+"\"/></a>");
        strTabInfo.append("<a href=\"\" title=\"call11\"><image src=\""+Resource.IMG_PERM_GROUP_PHONE_CALLS.getPath()+"\"/></a>");
        strTabInfo.append("<a href=\"\" title=\"call12\"><image src=\""+Resource.IMG_PERM_GROUP_PHONE_CALLS.getPath()+"\"/></a>");
        strTabInfo.append("<a href=\"\" title=\"call13\"><image src=\""+Resource.IMG_PERM_GROUP_PHONE_CALLS.getPath()+"\"/></a>");
        strTabInfo.append("<a href=\"\" title=\"call14\"><image src=\""+Resource.IMG_PERM_GROUP_PHONE_CALLS.getPath()+"\"/></a>");
        strTabInfo.append("<br/>");
        strTabInfo.append("<a href=\"\" title=\"call15\"><image src=\""+Resource.IMG_PERM_GROUP_PHONE_CALLS.getPath()+"\"/></a>");
        strTabInfo.append("<a href=\"\" title=\"call16\"><image src=\""+Resource.IMG_PERM_GROUP_PHONE_CALLS.getPath()+"\"/></a>");
        strTabInfo.append("<a href=\"\" title=\"call17\"><image src=\""+Resource.IMG_PERM_GROUP_PHONE_CALLS.getPath()+"\"/></a>");
        strTabInfo.append("<a href=\"\" title=\"call18\"><image src=\""+Resource.IMG_PERM_GROUP_PHONE_CALLS.getPath()+"\"/></a>");
        strTabInfo.append("<a href=\"\" title=\"call19\"><image src=\""+Resource.IMG_PERM_GROUP_PHONE_CALLS.getPath()+"\"/></a>");
        strTabInfo.append("<a href=\"\" title=\"call20\"><image src=\""+Resource.IMG_PERM_GROUP_PHONE_CALLS.getPath()+"\"/></a>");
        strTabInfo.append("<a href=\"\" title=\"call21\"><image src=\""+Resource.IMG_PERM_GROUP_PHONE_CALLS.getPath()+"\"/></a>");
        strTabInfo.append("<a href=\"\" title=\"call22\"><image src=\""+Resource.IMG_PERM_GROUP_PHONE_CALLS.getPath()+"\"/></a>");
        strTabInfo.append("<a href=\"\" title=\"call23\"><image src=\""+Resource.IMG_PERM_GROUP_PHONE_CALLS.getPath()+"\"/></a>");
        strTabInfo.append("<a href=\"\" title=\"call24\"><image src=\""+Resource.IMG_PERM_GROUP_PHONE_CALLS.getPath()+"\"/></a>");
        strTabInfo.append("<a href=\"\" title=\"call25\"><image src=\""+Resource.IMG_PERM_GROUP_PHONE_CALLS.getPath()+"\"/></a>");
        strTabInfo.append("<a href=\"\" title=\"call26\"><image src=\""+Resource.IMG_PERM_GROUP_PHONE_CALLS.getPath()+"\"/></a>");
        strTabInfo.append("<a href=\"\" title=\"call27\"><image src=\""+Resource.IMG_PERM_GROUP_PHONE_CALLS.getPath()+"\"/></a>");
        strTabInfo.append("<a href=\"\" title=\"call28\"><image src=\""+Resource.IMG_PERM_GROUP_PHONE_CALLS.getPath()+"\"/></a>");
        strTabInfo.append("<a href=\"\" title=\"call29\"><image src=\""+Resource.IMG_PERM_GROUP_PHONE_CALLS.getPath()+"\"/></a>");
        strTabInfo.append("</div></td></tr><tr><td colspan=2 height=10000></td></tr></table>");

        apkinform.setBody(strTabInfo.toString());
	}
}
