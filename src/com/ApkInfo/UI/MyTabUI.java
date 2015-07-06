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
import com.ApkInfo.Resource.Resource;
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
        
        String label = null;
        label = Resource.STR_TAB_BASIC_INFO.getString();
        tabbedPane.addTab(label, null, Panels[0], label);
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

        label = Resource.STR_TAB_WIDGET.getString();
        tabbedPane.addTab(label, null, Panels[1], label);
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

        label = Resource.STR_TAB_LIB.getString();
        tabbedPane.addTab(label, null, Panels[2], label);
        tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);

        label = Resource.STR_TAB_IMAGE.getString();
        tabbedPane.addTab(label, null, Panels[3], label);
        tabbedPane.setMnemonicAt(3, KeyEvent.VK_4);

        tabbedPane.addTab(label, null, Panels[4], label);
        tabbedPane.setMnemonicAt(4, KeyEvent.VK_5);

        label = Resource.STR_TAB_CERT.getString();
        tabbedPane.addTab(label, null, Panels[5], label);
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
        String label = Resource.STR_TAB_IMAGE.getString();
        tabbedPane.setTitleAt(3, label + "(...)");
		tabbedPane.setEnabledAt(3, false);
        
		((MyTabUIbasicInfo) Panels[0]).setData(apkInfo);

        label = Resource.STR_TAB_WIDGET.getString();
		if(apkInfo.WidgetList.size() > 0) {
			((MyTabUIWidget) Panels[1]).setData(apkInfo.WidgetList);
	        tabbedPane.setTitleAt(1, label + "(" + apkInfo.WidgetList.size() + ")");
	        tabbedPane.setEnabledAt(1, true);
		} else {
	        tabbedPane.setTitleAt(1, label + "(0)");
			tabbedPane.setEnabledAt(1, false);
		}

        label = Resource.STR_TAB_LIB.getString();
		if(apkInfo.LibList.size() > 0) {
	        ((MyTabUILib) Panels[2]).setData(apkInfo.LibList);
	        tabbedPane.setTitleAt(2, label + "(" + apkInfo.LibList.size()  + ")");
			tabbedPane.setEnabledAt(2, true);
		} else {
	        tabbedPane.setTitleAt(2, label + "(0)");
			tabbedPane.setEnabledAt(2, false);
		}

        label = Resource.STR_TAB_ACTIVITY.getString();
		if(apkInfo.ActivityList.size() > 0) {
	        ((MyTabUIActivity) Panels[4]).setData(apkInfo.ActivityList);
	        tabbedPane.setTitleAt(4, label + "(" + apkInfo.ActivityList.size()  + ")");
			tabbedPane.setEnabledAt(4, true);
		} else {
	        tabbedPane.setTitleAt(4, label + "(0)");
			tabbedPane.setEnabledAt(4, false);
		}

        label = Resource.STR_TAB_CERT.getString();
		if(apkInfo.CertList.size() > 0) {
	        ((MyTabUISign) Panels[5]).setData(apkInfo.CertList);
	        tabbedPane.setTitleAt(5, label + "(" + apkInfo.CertList.size()  + ")");
			tabbedPane.setEnabledAt(5, true);
		} else {
	        tabbedPane.setTitleAt(5, label + "(0)");
			tabbedPane.setEnabledAt(5, false);
		}

        MainUI.ProgressBarDlg.addProgress(25,"check resource(*.png)...\n");
        label = Resource.STR_TAB_IMAGE.getString();
		if(apkInfo.ImageList.size() > 0) {
			((MyTabUIResource) Panels[3]).setData(apkInfo.WorkTempPath, apkInfo.ImageList);
	        tabbedPane.setTitleAt(3, label + "(" + apkInfo.ImageList.size()  + ")");
			tabbedPane.setEnabledAt(3, true);
		} else {
	        tabbedPane.setTitleAt(3, label + "(0)");
			tabbedPane.setEnabledAt(3, false);
		}
		
		tabbedPane.setSelectedIndex(0);
	}
}