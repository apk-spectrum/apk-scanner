package com.ApkInfo.UI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.JButton;

import com.ApkInfo.UIUtil.PlasticTabbedPaneUI;
import com.ApkInfo.Core.MyApkInfo;
import com.Apkinfo.TabUI.MyTabUIbasicInfo;


public class MyTabUI extends JPanel{
	    public MyTabUI() {
	        super(new GridLayout(1, 1));
	         
	        JTabbedPane tabbedPane = new JTabbedPane();
	        tabbedPane.setUI(new PlasticTabbedPaneUI()); 
	        
	        JComponent panel1 = new MyTabUIbasicInfo(MainUI.mApkInfo);
	        
	        tabbedPane.addTab("APK Info", null, panel1,
	                "APK Info");
	        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
	         
	        JComponent panel2 = makeTextPanel("Panel #2");
	        tabbedPane.addTab("Widget", null, panel2,
	                "Widget");
	        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
	         
	        JComponent panel3 = makeTextPanel("Panel #3");
	        tabbedPane.addTab("Lib", null, panel3,
	                "Lib");
	        tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);
	         
	        JComponent panel4 = makeTextPanel(
	                "Panel #4 (has a preferred size of 410 x 50).");
	        
	        tabbedPane.addTab("Resource", null, panel4,
	                "Resource");
	        tabbedPane.setMnemonicAt(3, KeyEvent.VK_4);

	        JComponent panel5 = makeTextPanel(
	                "Panel #4 (has a preferred size of 410 x 50).");
	        
	        tabbedPane.addTab("Activity", null, panel5,
	                "Activity");
	        tabbedPane.setMnemonicAt(4, KeyEvent.VK_5);

	        JComponent panel6 = makeTextPanel(
	                "Panel #4 (has a preferred size of 410 x 50).");
	        panel6.setPreferredSize(new Dimension(700, 500));
	        tabbedPane.addTab("Device", null, panel6,
	                "Device");
	        tabbedPane.setMnemonicAt(5, KeyEvent.VK_6);
	        
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
	}