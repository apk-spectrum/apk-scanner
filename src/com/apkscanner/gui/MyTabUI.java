package com.apkscanner.gui;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.apkscanner.core.ApktoolManager.ApkInfo;
import com.apkscanner.gui.tabui.MyTabUIActivity;
import com.apkscanner.gui.tabui.MyTabUILib;
import com.apkscanner.gui.tabui.MyTabUIResource;
import com.apkscanner.gui.tabui.MyTabUISign;
import com.apkscanner.gui.tabui.MyTabUIWidget;
import com.apkscanner.gui.tabui.MyTabUIbasicInfo;
import com.apkscanner.gui.util.PlasticTabbedPaneUI;
import com.apkscanner.resource.Resource;


public class MyTabUI extends JPanel{
	private static final long serialVersionUID = -5500517956616692675L;

	Container[] Panels;
	JTabbedPane tabbedPane;
	
	String[] labels;
	
    public MyTabUI()
    {
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
        
        loadResource();
        
        tabbedPane.addTab(labels[0], null, Panels[0], labels[0] + " (Alt+1)");
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

        tabbedPane.addTab(labels[1], null, Panels[1], labels[1] + " (Alt+2)");
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

        tabbedPane.addTab(labels[2], null, Panels[2], labels[2] + " (Alt+3)");
        tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);

        tabbedPane.addTab(labels[3], null, Panels[3], labels[3] + " (Alt+4)");
        tabbedPane.setMnemonicAt(3, KeyEvent.VK_4);

        tabbedPane.addTab(labels[4], null, Panels[4], labels[4] + " (Alt+5)");
        tabbedPane.setMnemonicAt(4, KeyEvent.VK_5);

        tabbedPane.addTab(labels[5], null, Panels[5], labels[5] + " (Alt+6)");
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
    
    private void loadResource()
    {
    	labels = new String[] {
    		Resource.STR_TAB_BASIC_INFO.getString(),
    		Resource.STR_TAB_WIDGET.getString(),
    		Resource.STR_TAB_LIB.getString(),
    		Resource.STR_TAB_IMAGE.getString(),
    		Resource.STR_TAB_ACTIVITY.getString(),
    		Resource.STR_TAB_CERT.getString()
        };
    }
    
    public void reloadResource()
    {
    	loadResource();

    	tabbedPane.setTitleAt(0, labels[0]);
    	tabbedPane.setTitleAt(1, tabbedPane.getTitleAt(1).replaceAll("^([^\\(]*)", labels[1]));
    	tabbedPane.setTitleAt(2, tabbedPane.getTitleAt(2).replaceAll("^([^\\(]*)", labels[2]));
    	tabbedPane.setTitleAt(3, tabbedPane.getTitleAt(3).replaceAll("^([^\\(]*)", labels[3]));
    	tabbedPane.setTitleAt(4, tabbedPane.getTitleAt(4).replaceAll("^([^\\(]*)", labels[4]));
    	tabbedPane.setTitleAt(5, tabbedPane.getTitleAt(5).replaceAll("^([^\\(]*)", labels[5]));

    	tabbedPane.setToolTipTextAt(0, labels[0] + " (Alt+1)");
    	tabbedPane.setToolTipTextAt(1, labels[1] + " (Alt+2)");
    	tabbedPane.setToolTipTextAt(2, labels[2] + " (Alt+3)");
    	tabbedPane.setToolTipTextAt(3, labels[3] + " (Alt+4)");
    	tabbedPane.setToolTipTextAt(4, labels[4] + " (Alt+5)");
    	tabbedPane.setToolTipTextAt(5, labels[5] + " (Alt+6)");

		((MyTabUIbasicInfo) Panels[0]).reloadResource();
		((MyTabUIWidget) Panels[1]).reloadResource();
        ((MyTabUILib) Panels[2]).reloadResource();
		//((MyTabUIResource) Panels[3]).reloadResource();
        ((MyTabUIActivity) Panels[4]).reloadResource();
        ((MyTabUISign) Panels[5]).reloadResource();
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
		if(apkInfo != null) {
	        tabbedPane.setTitleAt(3, labels[3] + "(...)");
			tabbedPane.setEnabledAt(3, false);
		}
        
		((MyTabUIbasicInfo) Panels[0]).setData(apkInfo);

		if(apkInfo != null && apkInfo.WidgetList.size() > 0) {
			((MyTabUIWidget) Panels[1]).setData(apkInfo.WidgetList);
	        tabbedPane.setTitleAt(1, labels[1] + "(" + apkInfo.WidgetList.size() + ")");
	        tabbedPane.setEnabledAt(1, true);
		} else {
	        tabbedPane.setTitleAt(1, labels[1] + "(0)");
			tabbedPane.setEnabledAt(1, false);
		}

		if(apkInfo != null && apkInfo.LibList.size() > 0) {
	        ((MyTabUILib) Panels[2]).setData(apkInfo.LibList);
	        tabbedPane.setTitleAt(2, labels[2] + "(" + apkInfo.LibList.size()  + ")");
			tabbedPane.setEnabledAt(2, true);
		} else {
	        tabbedPane.setTitleAt(2, labels[2] + "(0)");
			tabbedPane.setEnabledAt(2, false);
		}

		if(apkInfo != null && apkInfo.ActivityList.size() > 0) {
	        ((MyTabUIActivity) Panels[4]).setData(apkInfo.ActivityList);
	        tabbedPane.setTitleAt(4, labels[4] + "(" + apkInfo.ActivityList.size()  + ")");
			tabbedPane.setEnabledAt(4, true);
		} else {
	        tabbedPane.setTitleAt(4, labels[4] + "(0)");
			tabbedPane.setEnabledAt(4, false);
		}

		if(apkInfo != null && apkInfo.CertList.size() > 0) {
	        ((MyTabUISign) Panels[5]).setData(apkInfo.CertSummary, apkInfo.CertList);
	        tabbedPane.setTitleAt(5, labels[5] + "(" + apkInfo.CertList.size()  + ")");
			tabbedPane.setEnabledAt(5, true);
		} else {
	        tabbedPane.setTitleAt(5, labels[5] + "(0)");
			tabbedPane.setEnabledAt(5, false);
		}

        //if(apkInfo != null) MainUI.ProgressBarDlg.addProgress(25,"check resource(*.png)...\n");
		if(apkInfo != null && apkInfo.ImageList.size() > 0) {
			((MyTabUIResource) Panels[3]).setData(apkInfo.WorkTempPath, apkInfo.ImageList);
	        tabbedPane.setTitleAt(3, labels[3] + "(" + apkInfo.ImageList.size()  + ")");
			tabbedPane.setEnabledAt(3, true);
		} else {
	        tabbedPane.setTitleAt(3, labels[3] + "(0)");
			tabbedPane.setEnabledAt(3, false);
		}
		
		tabbedPane.setSelectedIndex(0);
	}
}