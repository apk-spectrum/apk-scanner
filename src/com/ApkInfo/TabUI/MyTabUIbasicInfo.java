package com.ApkInfo.TabUI;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.ApkInfo.Resource.Resource;
import com.ApkInfo.UI.MyImagePanel;
import com.ApkInfo.UIUtil.JHtmlEditorPane;
import com.ApkInfo.Core.ApkManager.ApkInfo;

public class MyTabUIbasicInfo extends JComponent{
	private static final long serialVersionUID = 6431995641984509482L;

	private JHtmlEditorPane apkinform;
	private JTextArea apkpermission;
	private MyImagePanel imagepanel;

	public MyTabUIbasicInfo() {
    	//JPanel panelparent = new JPanel();
    	JPanel panel = new JPanel(true);
        	        
        GridBagConstraints c = new GridBagConstraints();
        panel.setLayout(new GridBagLayout());
        
    	apkinform = new JHtmlEditorPane();
        apkpermission = new JTextArea();
        
		JScrollPane jsp = new JScrollPane(apkpermission);

		//JScrollBar jsb;
		//jsb = jsp.getVerticalScrollBar();
        
        apkinform.setEditable(false);
        //Font font = new Font("helvitica", Font.BOLD, 15);
        
	    Font font = panel.getFont();

	    // create some css from the label's font
	    StringBuilder style = new StringBuilder("#basic-info {");
	    style.append("font-family:" + font.getFamily() + ";");
	    style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
	    style.append("font-size:" + font.getSize() + "pt;}");
	    style.append("#basic-info a {text-decoration:none; color:black;}");
	    style.append("#perm-group a {text-decoration:none; color:#"+Integer.toHexString(panel.getBackground().getRGB() & 0xFFFFFF)+";}");
	    //System.out.println(">>>>>>>>>>>>>>" + Integer.toHexString(panel.getForeground().getRGB() & 0xFFFFFF));
	    
        apkinform.setStyle(style.toString());
        apkinform.setBackground(panel.getBackground());

        apkpermission.setEditable(false);
        
        imagepanel = new MyImagePanel(Resource.IMG_APP_ICON.getPath());
        //panel.add(imagepanel);
        //panel.add(apkinform);        	        
        imagepanel.setMinimumSize(new Dimension(150, 150));
        imagepanel.setPreferredSize(new Dimension(150, 150));
        
        c.weightx = 0.1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        
        panel.setBorder(BorderFactory.createEmptyBorder(0 , 30 , 0 , 0));
        panel.add(imagepanel, c);
        c.weightx = 0.5;
        c.gridx = 1;
        panel.add(apkinform, c);
        
        this.setLayout(new GridBagLayout());
        
        c.weightx = 1;
        c.weighty = 0.7;
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        
        this.add(panel,c);
        //panelparent.add(apkpermission);
        c.weightx = 1;
        c.weighty = 0.3;
        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        
        this.add(jsp,c);
        
        //this.setLayout(new GridLayout(2, 1));
        
	}

	public void setData(ApkInfo apkInfo)
	{
        if(apkInfo.IconPath != null) {
            imagepanel.setData(apkInfo.IconPath);
        }
        
		StringBuilder strTabInfo = new StringBuilder("<div id=\"basic-info\">");
        strTabInfo.append("<font style=\"font-size:20px;font-weight:bold\"><a href=\"\" title=\"App name\">" + apkInfo.Labelname +"</a></font>");
        strTabInfo.append("<font style=\"font-size:12px\"> - Ver. " + apkInfo.VersionName +" / ");
        strTabInfo.append("" + apkInfo.VersionCode + "</font><br/>");
        strTabInfo.append("<font style=\"font-size:15px\">[" + apkInfo.PackageName +"]</font>");

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
        strTabInfo.append("<br/><br/><hr/>");

        strTabInfo.append("<font style=\"font-size:12px\">");
        strTabInfo.append("[Feature] ");
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
        strTabInfo.append("</font>");
        strTabInfo.append("</div>");
        strTabInfo.append("<div id=\"perm-group\" style=\"width:100px\">");
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

        apkinform.setBody(strTabInfo.toString());
        
        String etcInfo = "■■■■■■■■■■■■■■■■■  Cert  ■■■■■■■■■■■■■■■■■■■■\n"
				+ apkInfo.CertSummary
				+ "\n■■■■■■■■■■■■■■■■ Permissions ■■■■■■■■■■■■■■■■■■"
				+ "\n" + apkInfo.Permissions;
        if(!apkInfo.SharedUserId.isEmpty()) {
        	etcInfo = "SharedUserId : " + apkInfo.SharedUserId + "\n\n" + etcInfo;
        }
        apkpermission.setText(etcInfo);
        //apkpermission.setText(apkinform.getText());
	}
}
