package com.apkscanner.gui;

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
import com.apkscanner.gui.tabui.TabDataObject;
import com.apkscanner.gui.util.PlasticTabbedPaneUI;
import com.apkscanner.resource.Resource;


public class TabPanel extends JPanel{
	private static final long serialVersionUID = -5500517956616692675L;

	private JTabbedPane tabbedPane;
	private String[] labels;
	
    public TabPanel()
    {
        super(new GridLayout(1, 1));

        tabbedPane = new JTabbedPane();
        tabbedPane.setUI(new PlasticTabbedPaneUI());
        
        loadResource();
        
        tabbedPane.addTab(labels[0], null, new MyTabUIbasicInfo(), labels[0] + " (Alt+1)");
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

        tabbedPane.addTab(labels[1], null, new MyTabUIWidget(), labels[1] + " (Alt+2)");
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

        tabbedPane.addTab(labels[2], null, new MyTabUILib(), labels[2] + " (Alt+3)");
        tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);

        tabbedPane.addTab(labels[3], null, new MyTabUIResource(), labels[3] + " (Alt+4)");
        tabbedPane.setMnemonicAt(3, KeyEvent.VK_4);

        tabbedPane.addTab(labels[4], null, new MyTabUIActivity(), labels[4] + " (Alt+5)");
        tabbedPane.setMnemonicAt(4, KeyEvent.VK_5);

        tabbedPane.addTab(labels[5], null, new MyTabUISign(), labels[5] + " (Alt+6)");
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

    	for(int i = 0; i < 6; i++) {
    		tabbedPane.setTitleAt(i, tabbedPane.getTitleAt(i).replaceAll("^([^\\(]*)", labels[i]));
    		tabbedPane.setToolTipTextAt(i, labels[i] + " (Alt+"+ (i+1) +")");
    		((TabDataObject)(tabbedPane.getComponent(i))).reloadResource();
    	}
    	//tabbedPane.setTitleAt(0, labels[0]);
    }

	protected JComponent makeTextPanel(String text)
	{
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
        
		((TabDataObject)(tabbedPane.getComponent(0))).setData(apkInfo);

		if(apkInfo != null) {
			setPanelData(1, apkInfo.WidgetList.size(), apkInfo);
			setPanelData(2, apkInfo.LibList.size(), apkInfo);
			setPanelData(4, apkInfo.ActivityList.size(), apkInfo);
			setPanelData(5, apkInfo.CertList.size(), apkInfo);

	        //if(apkInfo != null) MainUI.ProgressBarDlg.addProgress(25,"check resource(*.png)...\n");
			setPanelData(3, apkInfo.ImageList.size(), apkInfo);
		} else {
			for(int i = 1; i < 6; i++) {
				setPanelData(i, 0, null);
			}
		}

		tabbedPane.setSelectedIndex(0);
	}

	private void setPanelData(int panelIdx, int dataSize, ApkInfo apkInfo)
	{
		if(dataSize > 0) {
			((TabDataObject)(tabbedPane.getComponent(panelIdx))).setData(apkInfo);
	        tabbedPane.setTitleAt(panelIdx, labels[panelIdx] + "(" + dataSize + ")");
	        tabbedPane.setEnabledAt(panelIdx, true);
		} else {
	        tabbedPane.setTitleAt(panelIdx, labels[panelIdx] + "(0)");
			tabbedPane.setEnabledAt(panelIdx, false);
		}
	}
}