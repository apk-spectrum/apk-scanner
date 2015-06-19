package com.ApkInfo.TabUI;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.ApkInfo.UI.MyImagePanel;
import com.ApkInfo.Core.CoreApkTool;
import com.ApkInfo.Core.MyApkInfo;

public class MyTabUIbasicInfo extends JComponent{
	public MyTabUIbasicInfo(MyApkInfo ApkInfo) {
    	JPanel panelparent = new JPanel();
    	JPanel panel = new JPanel(true);
        	        
        GridBagConstraints c = new GridBagConstraints();
        panel.setLayout(new GridBagLayout());
        
    	JTextArea apkinform = new JTextArea();
        JTextArea apkpermission = new JTextArea();
        
		JScrollPane jsp = new JScrollPane(apkpermission);
		JScrollBar jsb;
		
		String strTabInfo = "";
		
		jsb = jsp.getVerticalScrollBar();
        
        apkinform.setEditable(false);
        Font font = new Font("helvitica", Font.BOLD, 15);
        apkinform.setFont(font);
        
        //for test//
        DecimalFormat df = new DecimalFormat("#,##0");

        strTabInfo += "" + ApkInfo.strLabelname +" - ";
        strTabInfo += "" + ApkInfo.strPackageName +"\n";
        strTabInfo += "Ver. " + ApkInfo.strVersionName +" / ";
        strTabInfo += "" + ApkInfo.strVersionCode + "\n";

        strTabInfo += "\n" + CoreApkTool.getFileLength(ApkInfo.lApkSize) + " (" + df.format(ApkInfo.lApkSize) +" Bytes)\n";
        strTabInfo += "@SDK Ver. ";
        if(!ApkInfo.strMinSDKversion.equals("Unknown")) {
        	strTabInfo += "" + ApkInfo.strMinSDKversion +" (Min)";
        }
        if(!ApkInfo.strTargerSDKversion.equals("Unknown")) {
        	if(!ApkInfo.strMinSDKversion.equals("Unknown")) {
        		strTabInfo += ", "; 
        	}
        	strTabInfo += "" + ApkInfo.strTargerSDKversion +" (Target)";
        }
        if(ApkInfo.strMinSDKversion.equals("Unknown") && ApkInfo.strTargerSDKversion.equals("Unknown")) {
        	strTabInfo += "Unknown"; 
        }
        strTabInfo += "\n\n";
        
        strTabInfo += "[Feature]\n";
        //strTabInfo += "Signing : " + ApkInfo.CertList.size() +"\n";
        strTabInfo += "" + ApkInfo.strHidden +"";
        if(!ApkInfo.strStartup.equals("")) {
        	strTabInfo += ", " + ApkInfo.strStartup + "";
        }
        if(!ApkInfo.strProtectionLevel.equals("")) {
        	strTabInfo += ", " + ApkInfo.strProtectionLevel + "";
        }
        //strTabInfo += "\n\n";
        
                
        apkinform.setText(strTabInfo);
        
        apkinform.setBackground(panel.getBackground());
        
        //for test//
        apkpermission.setText(ApkInfo.strPermissions);
        //
          
        apkpermission.setEditable(false);
        
        //for test
        MyImagePanel imagepanel;
        if(ApkInfo.strIconPath != null){
            imagepanel = new MyImagePanel(ApkInfo.strIconPath);
        } else {
            imagepanel = new MyImagePanel("res/icon.png");
        }
        	        
        //panel.add(imagepanel);
        //panel.add(apkinform);        	        
        
        imagepanel.setPreferredSize(new Dimension(100, 100));
        
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


}
