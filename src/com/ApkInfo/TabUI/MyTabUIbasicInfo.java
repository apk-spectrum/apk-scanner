package com.ApkInfo.TabUI;

import java.awt.Font;
import java.awt.GridLayout;
import java.text.DecimalFormat;

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

        strTabInfo += "Label : " + ApkInfo.strLabelname +"\n";
        strTabInfo += "Package : " + ApkInfo.strPackageName +"\n";
        strTabInfo += "VersionName : " +ApkInfo.strVersionName +"\n";
        strTabInfo += "VersionCode : " +ApkInfo.strVersionCode +"\n";
        strTabInfo += "Size : " + CoreApkTool.getFileLength(ApkInfo.lApkSize) + " (" + ApkInfo.lApkSize +" byte)\n";
                
        apkinform.setText(strTabInfo+"Package : com.iloen.melon\nPackage : com.iloen.melon\nPackage : com.iloen.melon\nPackage : com.iloen.melon\nPackage : com.iloen.melon");
        
        apkinform.setBackground(panel.getBackground());
        
        //for test//
        for(int i=0; i<100; i++)
        apkpermission.setText(apkpermission.getText()+"\n" + "android.permission.WRITE_EXTERNAL_STORAGE");
        //
          
        apkpermission.setEditable(false);
        
        //for test
        MyImagePanel imagepanel;
        if(ApkInfo.strIconPath != null){
            imagepanel = new MyImagePanel(ApkInfo.strIconPath);
        } else {
            imagepanel = new MyImagePanel("res/icon.png");
        }
        	        
        panel.add(imagepanel);
        panel.add(apkinform);        	        
        panel.setLayout(new GridLayout(1, 2));
        
        this.add(panel);
        //panelparent.add(apkpermission);
        this.add(jsp);
        
        this.setLayout(new GridLayout(2, 1));
        
	}


}
