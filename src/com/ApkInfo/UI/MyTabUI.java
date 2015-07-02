package com.ApkInfo.UI;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.ApkInfo.UIUtil.PlasticTabbedPaneUI;
import com.ApkInfo.Core.CoreApkTool;
import com.ApkInfo.TabUI.MyTabUIActivity;
import com.ApkInfo.TabUI.MyTabUILib;
import com.ApkInfo.TabUI.MyTabUIResource;
import com.ApkInfo.TabUI.MyTabUISign;
import com.ApkInfo.TabUI.MyTabUIWidget;
import com.ApkInfo.TabUI.MyTabUIbasicInfo;


public class MyTabUI extends JPanel{
	    public MyTabUI() {
	        super(new GridLayout(1, 1));
	         
	        JTabbedPane tabbedPane = new JTabbedPane();
	        tabbedPane.setUI(new PlasticTabbedPaneUI());
	        
	        
	        Container panel1 = new MyTabUIbasicInfo(MainUI.GetMyApkInfo());	        
	        tabbedPane.addTab("APK Info", null, panel1, "APK Info");
	        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
	         
	        
	        JPanel panel2 = new MyTabUIWidget();
	        tabbedPane.addTab("Widget", null, panel2, "Widget");
	        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
	        
	        
	        JComponent panel3 = new MyTabUILib();
	        tabbedPane.addTab("Lib", null, panel3, "Lib");
	        tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);
	         
	        JComponent panel4 = new MyTabUIResource();
	        tabbedPane.addTab("Image", null, panel4, "Image");
	        
	        tabbedPane.setMnemonicAt(3, KeyEvent.VK_4);

	        JComponent panel5 = new MyTabUIActivity();
	        
	        tabbedPane.addTab("Activity", null, panel5, "Activity");
	        tabbedPane.setMnemonicAt(4, KeyEvent.VK_5);

	        Container panelsign = new MyTabUISign();
	        
	        tabbedPane.addTab("CERT", null, panelsign, "CERT");
	        tabbedPane.setMnemonicAt(5, KeyEvent.VK_6);
	        
//	        JComponent panel6 = makeTextPanel("Panel #4 (has a preferred size of 410 x 50).");
//	        panel6.setPreferredSize(new Dimension(700, 500));
//	        tabbedPane.addTab("Device", null, panel6, "Device");
//	        tabbedPane.setMnemonicAt(6, KeyEvent.VK_7);
	        
	        //Add the tabbed pane to this panel.
	        add(tabbedPane);
	        
	        
	        //widget
	        tabbedPane.setTitleAt(1,tabbedPane.getTitleAt(1) + "(" + MainUI.GetMyApkInfo().arrWidgets.size() + ")");

		      //Lib
	        tabbedPane.setTitleAt(2,tabbedPane.getTitleAt(2) + "(" + MainUI.GetMyApkInfo().LibPathList.size()  + ")");

	        //Image
	        tabbedPane.setTitleAt(3,tabbedPane.getTitleAt(3) + "(" + MainUI.GetMyApkInfo().ImagePathList.size()  + ")");
	        
	      //activity
	        tabbedPane.setTitleAt(4,tabbedPane.getTitleAt(4) + "(" + MainUI.GetMyApkInfo().ActivityList.size()  + ")");
	      
	      //activity
	        tabbedPane.setTitleAt(5,tabbedPane.getTitleAt(5) + "(" + MainUI.GetMyApkInfo().CertList.size()  + ")");
	        
	        //The following line enables to use scrolling tabs.
	        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
	    }

		protected JComponent makeTextPanel(String text) {
	        JPanel panel = new JPanel(false);
	        JLabel filler = new JLabel(text);
	        filler.setHorizontalAlignment(JLabel.CENTER);
	        panel.setLayout(new GridLayout(1, 1));
	        panel.add(filler);
	        	        
	        return panel;
	    }
	}