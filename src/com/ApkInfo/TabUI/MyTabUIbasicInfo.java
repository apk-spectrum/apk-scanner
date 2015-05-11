package com.ApkInfo.TabUI;

import java.awt.GridLayout;
import java.text.DecimalFormat;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.ApkInfo.UI.MyImagePanel;
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
        
        //for test//

        strTabInfo += "Label : " + ApkInfo.strLabelname +"\n";
        strTabInfo += "Package : " + ApkInfo.strPackageName +"\n";
        strTabInfo += "VersionName : " +ApkInfo.strVersionName +"\n";
        strTabInfo += "VersionCode : " +ApkInfo.strVersionCode +"\n";
        strTabInfo += "Size : " + getFileLength(ApkInfo.lApkSize) + " (" + ApkInfo.lApkSize +" byte)\n";
                
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

	public static String getFileLength(long length) {
		double LengthbyUnit = (double) length;
		int Unit = 0;
		while (LengthbyUnit > 1024 && Unit < 5) { // 단위 숫자로 나누고 한번 나눌 때마다 Unit
			LengthbyUnit = LengthbyUnit / 1024;
			Unit++;
		}

		DecimalFormat df = new DecimalFormat("#,##0.00");

		 StringBuilder result = new StringBuilder(df.format(LengthbyUnit).length());

		switch (Unit) {
		case 0:
			result.append(df.format(LengthbyUnit)+" Bytes");
			break;
		case 1:
			result.append(df.format(LengthbyUnit)+" KB");
			break;
		case 2:
			result.append(df.format(LengthbyUnit)+" MB");
			break;
		case 3:
			result.append(df.format(LengthbyUnit)+" GB");
			break;
		case 4:
			result.append(df.format(LengthbyUnit)+" TB");
		}

		return result.toString();
	}

}
