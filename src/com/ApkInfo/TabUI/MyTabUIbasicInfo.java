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
import com.ApkInfo.Core.ApkManager.ApkInfo;

public class MyTabUIbasicInfo extends JComponent{
	private static final long serialVersionUID = 6431995641984509482L;

	private JTextArea apkinform;
	private JTextArea apkpermission;
	private MyImagePanel imagepanel;

	public MyTabUIbasicInfo() {
    	//JPanel panelparent = new JPanel();
    	JPanel panel = new JPanel(true);
        	        
        GridBagConstraints c = new GridBagConstraints();
        panel.setLayout(new GridBagLayout());
        
    	apkinform = new JTextArea();
        apkpermission = new JTextArea();
        
		JScrollPane jsp = new JScrollPane(apkpermission);

		//JScrollBar jsb;
		//jsb = jsp.getVerticalScrollBar();
        
        apkinform.setEditable(false);
        Font font = new Font("helvitica", Font.BOLD, 15);
        apkinform.setFont(font);
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
        
        this.add(panel);
        //panelparent.add(apkpermission);
        this.add(jsp);
        
        this.setLayout(new GridLayout(2, 1));
        
	}

	public void setData(ApkInfo apkInfo)
	{
        if(apkInfo.IconPath != null) {
            imagepanel.setData(apkInfo.IconPath);
        }
        
		String strTabInfo = "";

        strTabInfo += "" + apkInfo.Labelname +" - ";
        strTabInfo += "" + apkInfo.PackageName +"\n";
        strTabInfo += "Ver. " + apkInfo.VersionName +" / ";
        strTabInfo += "" + apkInfo.VersionCode + "\n";

        strTabInfo += "\n" + apkInfo.ApkSize + "\n";
        strTabInfo += "@SDK Ver. ";
        if(!apkInfo.MinSDKversion.isEmpty()) {
        	strTabInfo += "" + apkInfo.MinSDKversion +" (Min)";
        }
        if(!apkInfo.TargerSDKversion.isEmpty()) {
        	if(!apkInfo.MinSDKversion.isEmpty()) {
        		strTabInfo += ", "; 
        	}
        	strTabInfo += "" + apkInfo.TargerSDKversion +" (Target)";
        }
        if(apkInfo.MinSDKversion.isEmpty() && apkInfo.TargerSDKversion.isEmpty()) {
        	strTabInfo += "Unknown"; 
        }
        strTabInfo += "\n\n";
        
        strTabInfo += "[Feature]\n";
        //strTabInfo += "Signing : " + ApkInfo.CertList.size() +"\n";
        strTabInfo += "" + apkInfo.Hidden +"";
        if(!apkInfo.Startup.isEmpty()) {
        	strTabInfo += ", " + apkInfo.Startup + "";
        }
        if(!apkInfo.ProtectionLevel.isEmpty()) {
        	strTabInfo += ", " + apkInfo.ProtectionLevel + "";
        }
        
        if(!apkInfo.SharedUserId.isEmpty()) {
        	strTabInfo += ", SHARED_USER_ID";
        }
        //strTabInfo += "\n\n";

        apkinform.setText(strTabInfo);
        
        String etcInfo = "■■■■■■■■■■■■■■■■■  Cert  ■■■■■■■■■■■■■■■■■■■■\n"
				+ apkInfo.CertSummary
				+ "\n■■■■■■■■■■■■■■■■ Permissions ■■■■■■■■■■■■■■■■■■"
				+ "\n" + apkInfo.Permissions;
        if(!apkInfo.SharedUserId.isEmpty()) {
        	etcInfo = "SharedUserId : " + apkInfo.SharedUserId + "\n\n" + etcInfo;
        }
        apkpermission.setText(etcInfo);
	}
}
