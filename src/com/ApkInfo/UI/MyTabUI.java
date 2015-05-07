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

public class MyTabUI extends JPanel{
	    public MyTabUI() {
	        super(new GridLayout(1, 1));
	         
	        JTabbedPane tabbedPane = new JTabbedPane();
	        tabbedPane.setUI(new PlasticTabbedPaneUI()); 
	        
	        //ImageIcon icon = createImageIcon("images/middle.gif");
	        
	        JComponent panel1 = makeTab1();
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
	    
	    protected JComponent makeTab1() {
	        
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
	        
	        strTabInfo += "Package : " + MainUI.mApkInfo.strPackageName +"\n";
	        strTabInfo += "VersionName : " +MainUI.mApkInfo.strVersionName +"\n";
	        strTabInfo += "VersionCode : " +MainUI.mApkInfo.strVersionCode +"\n";
	        

	        
	        apkinform.setText(strTabInfo+"Package : com.iloen.melon\nPackage : com.iloen.melon\nPackage : com.iloen.melon\nPackage : com.iloen.melon\nPackage : com.iloen.melon");
	                
	        
	        apkinform.setBackground(panel.getBackground());
	        
	        //for test//
	        for(int i=0; i<100; i++)
	        apkpermission.setText(apkpermission.getText()+"\n" + "android.permission.WRITE_EXTERNAL_STORAGE");
	        //
	          
	        apkpermission.setEditable(false);
	        
	        //for test
	        MyImagePanel imagepanel = new MyImagePanel("res/icon.png");
	        	        
	        panel.add(imagepanel);
	        panel.add(apkinform);
	        	        
	        panel.setLayout(new GridLayout(1, 2));
	        
	        panelparent.add(panel);
	        //panelparent.add(apkpermission);
	        panelparent.add(jsp);
	        
	        panelparent.setLayout(new GridLayout(2, 1));
	        
	        return panelparent;
	    }
	     
	    /** Returns an ImageIcon, or null if the path was invalid. */
	    protected static ImageIcon createImageIcon(String path) {
	        java.net.URL imgURL = MyTabUI.class.getResource(path);
	        if (imgURL != null) {
	            return new ImageIcon(imgURL);
	        } else {
	            System.err.println("Couldn't find file: " + path);
	            return null;
	        }
	    }
	     
	    /**
	     * Create the GUI and show it.  For thread safety,
	     * this method should be invoked from
	     * the event dispatch thread.
	     */
	    private static void createAndShowGUI() {
	        //Create and set up the window.
	        JFrame frame = new JFrame("TabbedPaneDemo");
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	         
	        //Add content to the window.
	        frame.getContentPane().add(new MyTabUI(), BorderLayout.CENTER);
	        
	        //Display the window.
	        frame.pack();
	        frame.setVisible(true);
	    }
	     
//	    public static void main(String[] args) {
//	        //Schedule a job for the event dispatch thread:
//	        //creating and showing this application's GUI.
//	        SwingUtilities.invokeLater(new Runnable() {
//	            public void run() {
//	                //Turn off metal's use of bold fonts
//		        UIManager.put("swing.boldMetal", Boolean.FALSE);
//		        createAndShowGUI();
//	            }
//	        });
//	    }
	}