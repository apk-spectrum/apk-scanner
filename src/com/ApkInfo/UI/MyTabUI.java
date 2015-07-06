package com.ApkInfo.UI;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.ApkInfo.UIUtil.PlasticTabbedPaneUI;
import com.ApkInfo.Core.ApkManager.ApkInfo;
import com.ApkInfo.TabUI.MyTabUIActivity;
import com.ApkInfo.TabUI.MyTabUILib;
import com.ApkInfo.TabUI.MyTabUIResource;
import com.ApkInfo.TabUI.MyTabUISign;
import com.ApkInfo.TabUI.MyTabUIWidget;
import com.ApkInfo.TabUI.MyTabUIbasicInfo;


public class MyTabUI extends JPanel{
	private static final long serialVersionUID = -5500517956616692675L;

	Container[] Panels;
	JTabbedPane tabbedPane;
	
    public MyTabUI() {
        super(new GridLayout(1, 1));

        Panels = new Container[6];
        Panels[0] = new MyTabUIbasicInfo();
        Panels[1] = new MyTabUIWidget();
        Panels[2] = new MyTabUILib();
        Panels[3] = new MyTabUIResource();
        Panels[4] = new MyTabUIActivity();
        Panels[5] = new MyTabUISign();

        tabbedPane = new JTabbedPane();
        tabbedPane.setUI(new PlasticTabbedPaneUI());
        
        tabbedPane.addTab("APK Info", null, Panels[0], "APK Info");
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
        
        tabbedPane.addTab("Widget", null, Panels[1], "Widget");
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
        
        tabbedPane.addTab("Lib", null, Panels[2], "Lib");
        tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);

        tabbedPane.addTab("Image", null, Panels[3], "Image");
        tabbedPane.setMnemonicAt(3, KeyEvent.VK_4);
        
        tabbedPane.addTab("Activity", null, Panels[4], "Activity");
        tabbedPane.setMnemonicAt(4, KeyEvent.VK_5);

        tabbedPane.addTab("CERT", null, Panels[5], "CERT");
        tabbedPane.setMnemonicAt(5, KeyEvent.VK_6);
        
//	        JComponent panel6 = makeTextPanel("Panel #4 (has a preferred size of 410 x 50).");
//	        panel6.setPreferredSize(new Dimension(700, 500));
//	        tabbedPane.addTab("Device", null, panel6, "Device");
//	        tabbedPane.setMnemonicAt(6, KeyEvent.VK_7);
        
        //Add the tabbed pane to this panel.
        add(tabbedPane);
        
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
	
	public void setData(ApkInfo apkInfo)
	{
        tabbedPane.setTitleAt(3,"Image(...)");
		tabbedPane.setEnabledAt(3, false);
        
		((MyTabUIbasicInfo) Panels[0]).setData(apkInfo);

		if(apkInfo.WidgetList.size() > 0) {
			((MyTabUIWidget) Panels[1]).setData(apkInfo.WidgetList);
	        tabbedPane.setTitleAt(1,"Widget(" + apkInfo.WidgetList.size() + ")");
	        tabbedPane.setEnabledAt(1, true);
		} else {
	        tabbedPane.setTitleAt(1,"Widget(0)");
			tabbedPane.setEnabledAt(1, false);
		}

		if(apkInfo.LibList.size() > 0) {
	        ((MyTabUILib) Panels[2]).setData(apkInfo.LibList);
	        tabbedPane.setTitleAt(2,"Lib(" + apkInfo.LibList.size()  + ")");
			tabbedPane.setEnabledAt(2, true);
		} else {
	        tabbedPane.setTitleAt(2,"Lib(0)");
			tabbedPane.setEnabledAt(2, false);
		}

		if(apkInfo.ActivityList.size() > 0) {
	        ((MyTabUIActivity) Panels[4]).setData(apkInfo.ActivityList);
	        tabbedPane.setTitleAt(4,"Activity(" + apkInfo.ActivityList.size()  + ")");
			tabbedPane.setEnabledAt(4, true);
		} else {
	        tabbedPane.setTitleAt(4,"Activity(0)");
			tabbedPane.setEnabledAt(4, false);
		}

		if(apkInfo.CertList.size() > 0) {
	        ((MyTabUISign) Panels[5]).setData(apkInfo.CertList);
	        tabbedPane.setTitleAt(5,"CERT(" + apkInfo.CertList.size()  + ")");
			tabbedPane.setEnabledAt(5, true);
		} else {
	        tabbedPane.setTitleAt(5,"CERT(0)");
			tabbedPane.setEnabledAt(5, false);
		}

        MainUI.ProgressBarDlg.addProgress(25,"check resource(*.png)...\n");
		if(apkInfo.ImageList.size() > 0) {
			((MyTabUIResource) Panels[3]).setData(apkInfo.WorkTempPath, apkInfo.ImageList);
	        tabbedPane.setTitleAt(3,"Image(" + apkInfo.ImageList.size()  + ")");
			tabbedPane.setEnabledAt(3, true);
		} else {
	        tabbedPane.setTitleAt(3,"Image(0)");
			tabbedPane.setEnabledAt(3, false);
		}
		
		tabbedPane.setSelectedIndex(0);
	}
}